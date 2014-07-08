import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.apache.commons.io.FilenameUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;


public class ChannelProcessor {
	private String saveToRootFolder;
	private Path saveFolder;
	private History history;
	private Boolean changes = false;
	private PlayList playList;
    private Downloader downloader;
	final String downloadsInProgressFileName = "downloadsinprogress.txt";
	private Map<String, Downloader.DownloadTask> downloads = new TreeMap<String, Downloader.DownloadTask>();
	static final NiceNamer niceNamer = new NiceNamer(createAbbreviationList());
	static Logger logger = Logger.getLogger("ljw.jpodsuck");
	
	ChannelProcessor(String saveToRootFolder, Downloader downloader) {
		this.saveToRootFolder = saveToRootFolder;
        this.downloader = downloader;
	}

	public void process(String rss) {
		processRss(rss);
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
	
	private void saveRssFile(String channelTitle, String rssData) throws Exception {
		final SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	    String rssFileName = channelTitle + "_" + isoFormat.format(new GregorianCalendar().getTime()) + ".xml";
	    Path rssFilePath = Paths.get(saveFolder.toString(), "rssBackup");//, rssFileName);
	    if (Files.notExists(rssFilePath, LinkOption.NOFOLLOW_LINKS)) {
	    	Files.createDirectory(rssFilePath);
		}
	    rssFilePath = Paths.get(rssFilePath.toString(), rssFileName);
	    try (BufferedWriter writer = new BufferedWriter(new FileWriter(rssFilePath.toFile()))) {
	    	writer.write(rssData);
	    } catch (Exception e) {
	    	throw e;
	    }    
	}
	
	private void processRss(String rss) {
		try {
            PodcastsInterface podcasts = getPodcasts(rss);
		    final String throwAwayStr = "JapanesePod101.com | My Feed - ";
		    String channelTitle = podcasts.getChannelTitle();
		    String folder;
		    if (channelTitle.startsWith(throwAwayStr)) {
		    	folder = channelTitle.substring(throwAwayStr.length());	
		    }
		    else {
		    	folder = channelTitle;
		    }
		    saveFolder = Paths.get(saveToRootFolder, folder);
		    if (Files.notExists(saveFolder, LinkOption.NOFOLLOW_LINKS)) {
		    	Files.createDirectory(saveFolder);
		    }
		    boolean isDownloadsInProgress = isDownloadsInProgress();
			this.playList = new PlayList(saveFolder, isDownloadsInProgress());
			if (isDownloadsInProgress) {
				removeDownloadsInProgress();
			}
		    this.history = new History(Paths.get(saveToRootFolder), folder);
		    Visitor visitor = new Visitor(this.history, downloader);
		    podcasts.accept(visitor); // visit all podcast items and process.
		    saveRssFile(folder, rss);
		   
		} catch (Exception e) {
			logger.error("process exception", e);
		}
	}

	public Boolean isFinished() {
		// iterate through work requests removing ones which has finished and records any downloads
		Iterator<Map.Entry<String, Downloader.DownloadTask>> it = downloads.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Downloader.DownloadTask> entry = it.next();
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
	private void setDownloadsInProgress() {
		if (!this.changes)
		{
			try {
				Paths.get(saveFolder.toString(), downloadsInProgressFileName).toFile().createNewFile();
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
			this.changes = true;
		}
	}
	private void removeDownloadsInProgress() {
		if (!this.changes) {
			logger.error("expected to be in downloads in progress state");
		}
		if (Paths.get(saveFolder.toString(), downloadsInProgressFileName).toFile().delete()) {
			logger.error("downloads in progress file can't be deleted or does not exist");
		}
	}
	private boolean isDownloadsInProgress() {
		return Paths.get(saveFolder.toString(), downloadsInProgressFileName).toFile().exists();
	}
	
	void doPostActions()
	{
		if (changes) {
			logger.info("changes detected updating history and playlists");
			history.writeHistory();
			this.playList.create();
			removeDownloadsInProgress();
		}
	}

	final static Map<String, String> createAbbreviationList() {
		Map<String, String> lookup = new HashMap<String, String>();
		lookup.put("Lower Intermediate", "LInt");
		lookup.put("Intermediate Lesson", "Int");
		lookup.put("Beginner", "Beg");
		lookup.put("Japanese Culture Class", "Culture");
		lookup.put("Upper Intermediate", "UInt");
		lookup.put("Upper Beginner", "UBeg");
		return lookup;
	}
	
	interface FileProcessingInterface
	{
		public void onSave();
	}
	
	class FileProcessing implements FileProcessingInterface {
		private Item item;
		private Path filePath;
		
		FileProcessing(Item item, Path filePath) {
			this.item = item;
			this.filePath = filePath;
		}
		
		@Override
		public void onSave() {
			try {
				if (filePath.toString().endsWith(".mp3"))
				{
					String shortTitle = ChannelProcessor.niceNamer.makeTitle(this.item.title);
					AudioFile f = AudioFileIO.read(filePath.toFile());
					Tag tag = f.getTag();
					String title;
					if (tag != null)
					{
						title = tag.getFirst(FieldKey.TITLE);
						if (title != shortTitle)
						{
							tag.setField(FieldKey.TITLE, shortTitle);
							AudioFileIO.write(f);
						}
					}
					ChannelProcessor.this.playList.addMp3(filePath.toString(), shortTitle, f.getAudioHeader().getTrackLength()); // how does Java find the correct instance of ChannelProcessor?
				}	
				logger.info(this.item.title);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	class Visitor implements PodcastVisitor 
	{
		History history;
        Downloader downloader;
		Visitor(History history, Downloader downloader) {
            this.history = history;
            this.downloader = downloader;
		}
		@Override
		public void visit(Item item) {
			try {
				URL url =  new URL(item.url); // validate url
				Path savePath = Paths.get(ChannelProcessor.this.saveFolder.toString(), FilenameUtils.getName(url.getPath()));
				History.FileHistory fh = history.getFileHistory(savePath.toString(), url, item.length); 
				if (fh.needToDownload)
				{
					setDownloadsInProgress();
					ChannelProcessor.this.downloads.put(url.toString(), downloader.download(fh, new FileProcessing(item, savePath)));
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
