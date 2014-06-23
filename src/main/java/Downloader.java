import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.log4j.Logger;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;

class DownloadTask extends FutureTask<History.FileHistory> {
	private ChannelProcessor.FileProcessing fileProcessing;
	DownloadTask(Callable<History.FileHistory> callable, ChannelProcessor.FileProcessing fileProcessing) {
		super(callable);
		this.fileProcessing = fileProcessing;
	}
	@Override
	protected void done() {
		fileProcessing.onSave();
	}
}

public enum Downloader {
	INSTANCE;
	static Logger logger = Logger.getLogger("ljw.jpodsuck");
	AbstractHttpClient httpClient;
	ExecutorService exec = Executors.newFixedThreadPool(5);
	
	void init(AbstractHttpClient httpClient) {
		this.httpClient = httpClient;
	}
	
	public DownloadTask download(History.FileHistory fileHistory, ChannelProcessor.FileProcessing fileProcessing) {
		logger.info("downloading " + fileHistory.url.toString() + " file: " + fileHistory.fileName);
		DownloadRunnable downloadRunnable = new DownloadRunnable(httpClient, fileHistory);
		DownloadTask downloadTask = new DownloadTask(downloadRunnable, fileProcessing);
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
			String downloadFileName = fileHistory.fileName + "~download";
	        try (FileOutputStream outputStream = new FileOutputStream(downloadFileName)) {
	        	InputStream input = entity.getContent();
	        	IOUtils.copy(input, outputStream);
	        	outputStream.close();
	        	input.close();
	        	Files.move(Paths.get(downloadFileName), Paths.get(fileHistory.fileName), StandardCopyOption.REPLACE_EXISTING);
	        } catch (Exception e) {
	        	logger.info("exception: ", e);
	        	throw e;
	        }
	        fileHistory.success = true;
	        fileHistory.fileSize = Files.size(Paths.get(fileHistory.fileName));
	        logger.info("written: " + fileHistory.fileName);
	        
		} catch (Exception e) {
			fileHistory.success = false;
			fileHistory.fileSize = 0;
			logger.info("exception: ", e);
		}
		return fileHistory;
	}
	
}