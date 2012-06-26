package ljw.jpodsuck;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.AbstractHttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public enum Downloader {
	INSTANCE;
	
	AbstractHttpClient httpClient;
	ExecutorService exec = Executors.newFixedThreadPool(5);
	
	void init(AbstractHttpClient httpClient) {
		this.httpClient = httpClient;
	}
	
	public DownloadTask download(String url, String filename) {
		System.out.println("downloading " + url.toString() + " file: " + filename);
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
	public String  source;
	public String destination;
	
	DownloadRunnable(AbstractHttpClient httpClient, String url, String destination) {
		this.httpClient = httpClient;
		this.source = url;
		this.destination = destination;
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
	        System.out.println("written: " + destination);
	        
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 1;
	}
	
}