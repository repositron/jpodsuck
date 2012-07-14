package ljw.jpodsuck;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;

public enum Downloader {
	INSTANCE;
	static Logger logger = Logger.getLogger("ljw.jpodsuck");
	AbstractHttpClient httpClient;
	ExecutorService exec = Executors.newFixedThreadPool(5);
	
	void init(AbstractHttpClient httpClient) {
		this.httpClient = httpClient;
	}
	
	public DownloadTask download(String url, String filename, History history) {
		logger.info("downloading " + url.toString() + " file: " + filename);
		DownloadRunnable downloadRunnable = new DownloadRunnable(httpClient, url, filename, history);
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
	static Logger logger = Logger.getLogger("ljw.jpodsuck");
	AbstractHttpClient httpClient;
	String  source;
	public String destination;
	History history;
	
	DownloadRunnable(AbstractHttpClient httpClient, String url, String destination, History history) {
		this.httpClient = httpClient;
		this.source = url;
		this.destination = destination;
		this.history = history;
		
	}
	

	@Override
	public Integer call() throws Exception {
		HttpResponse response;
		try {
			System.out.println("call(): " + source);
			HttpGet httpget = new HttpGet(source);
			response = httpClient.execute(httpget);
			HttpEntity entity = response.getEntity();
	        File f = new File(destination);
	        FileOutputStream outputStream = new FileOutputStream(f);
	        InputStream input = entity.getContent();
	        IOUtils.copy(input, outputStream);
	        outputStream.close();
	        input.close();
	        history.recordFileWritten(Paths.get(destination), new URL(source), true, 0 /*TODO: need rss size*/);
	        logger.info("written: " + destination);
	        
		} catch (Exception e) {
			history.recordFileWritten(Paths.get(destination), new URL(source), false, 0);
			logger.info("exception: ", e);
		}
		return 1;
	}
	
}