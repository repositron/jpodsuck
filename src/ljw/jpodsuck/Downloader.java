package ljw.jpodsuck;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.FutureTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.AbstractHttpClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;

import org.apache.commons.io.IOUtils;

public enum Downloader {
	INSTANCE;
	
	AbstractHttpClient httpClient;
	ExecutorService exec = Executors.newFixedThreadPool(5);
	//BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(); 
	//DnlThreadPoolExecutor executor = new DnlThreadPoolExecutor(queue);
	
	void init(AbstractHttpClient httpClient) {
		this.httpClient = httpClient;
	}
	
	public DownloadTask download(URL url, String filename) {
		DownloadRunnable downloadRunnable = new DownloadRunnable(httpClient, url, filename);
		DownloadTask downloadTask = new DownloadTask(downloadRunnable);
		exec.execute(downloadTask);
		return downloadTask;
	}
	
	void close() {
		httpClient = null;
	}
	
}
 
class DownloadRunnable implements Callable<Integer>
{
	AbstractHttpClient httpClient;
	public URL  source;
	public String destination;
	
	DownloadRunnable(AbstractHttpClient httpClient, URL source, String destination) {
		this.httpClient = httpClient;
		this.source = source;
		this.destination = destination;
	}
	

	@Override
	public Integer call() throws Exception {
		HttpResponse response;
		try {
			HttpGet httpget = new HttpGet(source.toString());
			response = httpClient.execute(httpget);
			HttpEntity entity = response.getEntity();
	        //BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
	        File f = new File(destination);
	        IOUtils.copy(new InputStreamReader(entity.getContent()), new FileOutputStream(f));
	        
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 1;
	}
	
}