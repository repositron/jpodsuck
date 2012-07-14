package ljw.jpodsuck;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;

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
	protected Map<String, DownloadTask> downloads = new TreeMap<String, DownloadTask>();
	static Logger logger = Logger.getLogger("ljw.jpodsuck");
	
	ChannelProcessor(DefaultHttpClient httpclient, URL urlChannel, String saveToRootFolder) {
		this.httpclient = httpclient;
		this.urlChannel = urlChannel;
		this.saveToRootFolder = saveToRootFolder;
	}
	public void process() {
		loadHistory();
		downloadRssFile();
	}
	void loadHistory() {
		
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
		    Visitor visitor = new Visitor(this.history);
		    podcasts.accept(visitor);
		    saveRssFile(folder, rss);
		   
		} catch (Exception e) {
			logger.error("downloadRssFile exception", e);
		}
	}
	public Boolean isFinished() {
		// iterate through work requests removing ones which has finished
		Iterator<Map.Entry<String, DownloadTask>> it = downloads.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, DownloadTask> entry = it.next();
			if (entry.getValue().isDone())
			{
				logger.info("finished dl " + entry.getKey().toString());
				it.remove();
			}
		}
		return downloads.isEmpty();
	}
	public void close() {
		
	}
	
	public void writePlayList() {
		PlayList pl = new PlayList(saveFolder);
		pl.create();
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

				if (history.needToDownload(savePath, item.length))
				{
					ChannelProcessor.this.downloads.put(url.toString(), Downloader.INSTANCE.download(url.toString(), savePath.toString(), history));
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
