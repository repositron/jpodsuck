package ljw.jpodsuck;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.nio.file.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.commons.io.FilenameUtils;

public class ChannelProcessor {

	private DefaultHttpClient httpclient;
	private URL urlChannel;
	private String folder;
	protected Map<URL, DownloadTask> downloads = new TreeMap<URL, DownloadTask>();
	
	ChannelProcessor(DefaultHttpClient httpclient, URL urlChannel, String folder) {
		this.httpclient = httpclient;
		this.urlChannel = urlChannel;
		this.folder = folder;
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
	        Visitor visitor = new Visitor();
	        podcasts.accept(visitor);
		} catch (Exception e) {
			
		}
	}
	public Boolean isFinished() {
		Iterator<Map.Entry<URL, DownloadTask>> it = downloads.entrySet().iterator();
		if (it.hasNext()) {
			try {
				it.next().getValue().get(1, TimeUnit.MILLISECONDS);
				it.remove();
			} catch (TimeoutException e) {
			} catch (Exception e) {
			}
		
		}
		return downloads.isEmpty();
	}
	public void close() {
		
	}
	class Visitor implements PodcastVisitor 
	{
		@Override
		public void visit(Item item) {
			try {
				URL url =  new URL(item.url);
				System.out.println(url.getPath());
				System.out.println(FilenameUtils.getName(url.getPath()));
				Path savePath = Paths.get(ChannelProcessor.this.folder, FilenameUtils.getName(url.getPath()));
				ChannelProcessor.this.downloads.put(url, Downloader.INSTANCE.download(url, savePath.toString()));
			} catch (Exception e) {
				
			}
		}
	}
}
