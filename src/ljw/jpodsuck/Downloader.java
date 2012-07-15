package ljw.jpodsuck;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
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
	
	public DownloadTask download(History.FileHistory fileHistory) {
		logger.info("downloading " + fileHistory.url.toString() + " file: " + fileHistory.filePath.toString());
		DownloadRunnable downloadRunnable = new DownloadRunnable(httpClient, fileHistory);
		DownloadTask downloadTask = new DownloadTask(downloadRunnable);
		exec.execute(downloadTask);
		return downloadTask;
	}
	
	void close() {
		httpClient = null;
	}
	
}
 
class DownloadRunnable implements Callable<History.FileHistory>
{
	static Logger logger = Logger.getLogger("ljw.jpodsuck");
	AbstractHttpClient httpClient;
	long rssSize;
	History.FileHistory fileHistory;
	
	
	DownloadRunnable(AbstractHttpClient httpClient, History.FileHistory fileHistory) { 
		this.httpClient = httpClient;
		this.fileHistory = fileHistory;
	}
	
	@Override
	public History.FileHistory call() throws Exception {
		HttpResponse response;
		try {
			logger.info("call(): " + fileHistory.url);
			fileHistory.attempts++;
			HttpGet httpget = new HttpGet(fileHistory.url.toURI());
			response = httpClient.execute(httpget);
			HttpEntity entity = response.getEntity();
	        FileOutputStream outputStream = new FileOutputStream(fileHistory.filePath.toFile());
	        InputStream input = entity.getContent();
	        IOUtils.copy(input, outputStream);
	        outputStream.close();
	        input.close();
	        fileHistory.success = true;
	        fileHistory.fileSize = Files.size(fileHistory.filePath);
	        logger.info("written: " + fileHistory.filePath);
	        
		} catch (Exception e) {
			fileHistory.success = false;
			fileHistory.fileSize = 0;
			logger.info("exception: ", e);
		}
		return fileHistory;
	}
	
}