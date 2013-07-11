package ljw.jpodsuck;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.nio.file.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

public class ChannelProcessor {

	private DefaultHttpClient httpclient;
	private URL urlChannel;
	private String saveToRootFolder;
	private Path saveFolder;
	private History history;
	private Boolean changes = false;
	private NiceNamer niceNamer = new NiceNamer(createAbbreviationList());
	protected Map<String, DownloadTask> downloads = new TreeMap<String, DownloadTask>();
	static Logger logger = Logger.getLogger("ljw.jpodsuck");
	
	ChannelProcessor(DefaultHttpClient httpclient, URL urlChannel, String saveToRootFolder) {
		this.httpclient = httpclient;
		this.urlChannel = urlChannel;
		this.saveToRootFolder = saveToRootFolder;
	}
	public void process() {
		downloadRssFile();
	}

	PodcastsInterface getPodcasts(String s) throws Exception {
		try (StringReader sr = new StringReader(s)) {
			PodcastsInterface podcasts = new Podcasts();
		    RssXmlParser parser = new RssXmlParser(sr, podcasts);
		    parser.parse();
		    return podcasts;
		}
		catch (Exception e) {
			throw e;
		}
	}
	void saveRssFile(String channelTitle, String rssData) throws Exception {
		final SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	    String rssFileName = channelTitle + "_" + isoFormat.format(new GregorianCalendar().getTime()) + ".xml";
	    Path rssFilePath = Paths.get(saveFolder.toString(), "rssBackup");//, rssFileName);
	    if (Files.notExists(rssFilePath, LinkOption.NOFOLLOW_LINKS))
		{
	    	Files.createDirectory(rssFilePath);
		}
	    rssFilePath = Paths.get(rssFilePath.toString(), rssFileName);
	    try (BufferedWriter writer = new BufferedWriter(new FileWriter(rssFilePath.toFile()))) {
	    	writer.write(rssData);
	    } catch (Exception e) {
	    	throw e;
	    }
	    
	}
	
	void downloadRssFile() {
		try {
			logger.info("channel: " + urlChannel.toString());
			HttpGet httpget = new HttpGet(urlChannel.toString());
			HttpResponse response = httpclient.execute(httpget);
		    HttpEntity entity = response.getEntity();
		    BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
		    
		    // download rss file to memory stream.
		    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		    IOUtils.copy(reader, byteStream);
		    String rss = byteStream.toString("UTF-8");
		    PodcastsInterface podcasts = getPodcasts(rss);
		    
		    final String throwAwayStr = "JapanesePod101.com | My Feed - ";
		    String channelTitle = podcasts.getChannelTitle();
		    String folder;
		    if (channelTitle.startsWith(throwAwayStr))
		    {
		    	folder = channelTitle.substring(throwAwayStr.length());	
		    }
		    else {
		    	folder =  channelTitle;
		    }
		    saveFolder = Paths.get(saveToRootFolder, folder);
		    if (Files.notExists(saveFolder, LinkOption.NOFOLLOW_LINKS)) {
		    	Files.createDirectory(saveFolder);
		    }
		    this.history = new History(Paths.get(saveToRootFolder), folder);
		    Visitor visitor = new Visitor(this.history);
		    podcasts.accept(visitor);
		    saveRssFile(folder, rss);
		   
		} catch (Exception e) {
			logger.error("downloadRssFile exception", e);
		}
	}
	
	Map<String, String> createAbbreviationList() {
		Map<String, String> lookup = new HashMap<String, String>();
		lookup.put("Lower Intermediate", "LI");
		lookup.put("Intermediate Lesson", "Int");
		lookup.put("Beginner", "Beg");
		lookup.put("Japanese Culture Class", "Culture");
		lookup.put("Upper Intermediate", "UI");
		return lookup;
	}
	
	public Boolean isFinished() {
		// iterate through work requests removing ones which has finished and records any downloads
		Iterator<Map.Entry<String, DownloadTask>> it = downloads.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, DownloadTask> entry = it.next();
			if (entry.getValue().isDone())
			{
				try {
					logger.info("finished dl " + entry.getKey().toString());
					it.remove();
				} catch (Exception e) {
					logger.error("isFinished", e);
				}
			}
		}
		return downloads.isEmpty();
	}
	public void doPostActions()
	{
		if (changes) {
			logger.info("changes detected updating history and playlists");
			history.writeHistory();
			PlayList pl = new PlayList(saveFolder);
			pl.create();
		}
	}
	interface FileProcessing
	{
		public void onSave();
	}
	class Visitor implements PodcastVisitor 
	{
		History history;
		Visitor(History history) {
			this.history = history;
		}
		@Override
		public void visit(Item item) {
			try {
				URL url =  new URL(item.url); // validate url
				Path savePath = Paths.get(ChannelProcessor.this.saveFolder.toString(), FilenameUtils.getName(url.getPath()));
				History.FileHistory fh = history.getFileHistory(savePath.toString(), url, item.length); 
				if (fh.needToDownload)
				{
					ChannelProcessor.this.changes = true;
					ChannelProcessor.this.downloads.put(url.toString(), Downloader.INSTANCE.download(fh, new FileProcessing() {
						private Item item;
						@Override
						public void onSave() {
							//ChannelProcessor.this.
							
							logger.info(this.item.title);
						}
						private FileProcessing init(Item item, Path filePath) {
							this.item = item;
							return this;
						}
					}.init(item, savePath)
					));
					logger.info("Dl " + url.toString() + " to " + savePath.toString() + " size: " + item.length);
				}
			} catch (MalformedURLException e) {
				logger.error("malformed url" + item.url, e);
			} catch (Exception e) {
				logger.error("vistor", e);
			}
		}
	}
}
