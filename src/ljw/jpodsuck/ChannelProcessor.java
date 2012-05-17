package ljw.jpodsuck;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.nio.file.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.commons.io.FilenameUtils;

public class ChannelProcessor {

	private DefaultHttpClient httpclient;
	private URL urlChannel;
	private String saveToRootFolder;
	private Path saveFolder;
	protected Map<String, DownloadTask> downloads = new TreeMap<String, DownloadTask>();
	
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
	void downloadRssFile() {
		try{
			HttpGet httpget = new HttpGet(urlChannel.toString());
			HttpResponse response = httpclient.execute(httpget);
	        HttpEntity entity = response.getEntity();
	        BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
	        PodcastsInterface podcasts = new Podcasts();
	        RssXmlParser parser = new RssXmlParser(reader, podcasts);
	        parser.parse();
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
	        Visitor visitor = new Visitor();
	        podcasts.accept(visitor);
		} catch (Exception e) {
			
		}
	}
	public Boolean isFinished() {
		// iterate through work requests removing ones which has finished
		Iterator<Map.Entry<String, DownloadTask>> it = downloads.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, DownloadTask> entry = it.next();
			if (entry.getValue().isDone())
			{
				System.out.println("finished dl " + entry.getKey().toString());
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
		@Override
		public void visit(Item item) {
			try {
				URL url =  new URL(item.url); // validate url
				Path savePath = Paths.get(ChannelProcessor.this.saveFolder.toString(), FilenameUtils.getName(url.getPath()));
				if (Files.notExists(savePath, LinkOption.NOFOLLOW_LINKS) || Files.size(savePath) != item.length) {
					ChannelProcessor.this.downloads.put(url.toString(), Downloader.INSTANCE.download(url.toString(), savePath.toString()));
					System.out.println("sz after: " + ChannelProcessor.this.downloads.size());
				}
				else {
					System.out.println(item.url + ": already downloaded.");
				}
			} catch (MalformedURLException e) {
				System.out.println("malformed url" + item.url);
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		}
	}
}
