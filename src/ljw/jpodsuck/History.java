package ljw.jpodsuck;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ljw.jpodsuck.History.FileHistory;

import org.apache.log4j.Logger;

public class History {
	Path rssFolderPath;
	Path historyPath;
	Path historyBackupPath;
	String channelTitle;
	static Logger logger = Logger.getLogger("ljw.jpodsuck");

	Map<Path, FileHistory> filesHistory = new TreeMap<Path, FileHistory>();

	History(Path folder, String channelTitle) throws FileNotFoundException, IOException {
		this.rssFolderPath = Paths.get(folder.toString(), "rssBackup");
		this.channelTitle = channelTitle;
		this.historyPath = Paths.get(folder.toString(), channelTitle, ".history");
		this.historyBackupPath = Paths.get(folder.toString(), channelTitle, ".history_bak");
		if (Files.exists(historyPath)) {
			try (BufferedReader bReader = new BufferedReader(new FileReader(this.historyPath.toFile())))		
			{
				// file path, url, size from rss, size on disk, success, attempts
				Pattern pattern = Pattern.compile("^\"(.+)\",(.+),rss_size=(\\d+),size=(\\d+),(true|false),(\\d+)$");
				String line;
				while ((line = bReader.readLine()) != null) {
					Matcher m = pattern.matcher(line);
					if (m.matches() && m.groupCount() == 6)
					{
						FileHistory fH = new FileHistory();
						fH.filePath = Paths.get(m.group(1));
						fH.url = new URL(m.group(2));
						fH.rssSize = Integer.parseInt(m.group(3));
						fH.fileSize = Integer.parseInt(m.group(4));
						fH.success = Boolean.parseBoolean(m.group(5));
						fH.attempts = Integer.parseInt(m.group(6));
						filesHistory.put(fH.filePath, fH);
					}
				}	
			}	
		}
	}
	public void writeHistory() {
		if (Files.exists(historyPath)) {
			if (Files.exists(historyBackupPath)) {
				historyBackupPath.toFile().delete();	
			}
			historyPath.toFile().renameTo(historyBackupPath.toFile());
		}
		try (PrintWriter writer = new PrintWriter(new FileWriter(historyPath.toFile()))) {
			for (Map.Entry<Path, FileHistory>  entry : filesHistory.entrySet()) {
				FileHistory h = entry.getValue();
				writer.format("\"%s\",%s,rss_size=%d,size=%d,%b,%d",
						h.filePath.toString(), h.url.toString(), h.rssSize, h.fileSize, h.success, h.attempts);
				writer.println();
			}
		} catch (IOException e) {
			logger.error("writeHistory", e);
		}
	}
	public void saveRss(String rssFile) throws IOException {
		final SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	    String rssFileName = channelTitle + "_" + isoFormat.format(new GregorianCalendar().getTime()) + ".xml";
	    if (Files.notExists(rssFolderPath, LinkOption.NOFOLLOW_LINKS))
		{
	    	Files.createDirectory(rssFolderPath);
		}
	    Path rssFilePath = Paths.get(rssFolderPath.toString(), rssFileName);
	    try (BufferedWriter writer = new BufferedWriter(new FileWriter(rssFilePath.toFile()))) {
	    	writer.write(rssFile);
	    } catch (Exception e) {
	    	throw e;
	    }
	}

	String findlastSaveRssFile() {
		String file = "";
		return file;
	}
	
	private void needToDownload(FileHistory fileHistory, long latestRssLength) throws IOException {
		fileHistory.needToDownload = false;
		if (Files.exists(fileHistory.filePath, LinkOption.NOFOLLOW_LINKS))
		{
			if (Files.size(fileHistory.filePath) != latestRssLength)
			{
				logger.info(fileHistory.filePath.toString() + " Already exists but size is different origSize: " + Files.size(fileHistory.filePath) + " newSize:" + fileHistory.rssSize);
				if (latestRssLength != fileHistory.rssSize) {
					// the rsslength has changed
					fileHistory.rssSize = latestRssLength;
					fileHistory.needToDownload = true;
				}
			}
		}
		else
		{
			logger.info(fileHistory.filePath.toString() + " doesn't exist");
			fileHistory.needToDownload = true;
			
		}
	}
	
	public FileHistory getFileHistory(Path savePath, URL url, long rssLength) throws IOException {
		FileHistory fileHistory = filesHistory.get(savePath);
		if (fileHistory == null) {
			fileHistory = new FileHistory();
			fileHistory.filePath = savePath;
			fileHistory.url = url;
			fileHistory.rssSize = rssLength;
			fileHistory.fileSize = 0;
			fileHistory.attempts = 0;
			fileHistory.success = false;
			fileHistory.needToDownload = true; // assume we haven't downloaded it
			filesHistory.put(savePath, fileHistory);
		}
		else
			needToDownload(fileHistory, rssLength);
		return fileHistory;
	}

	void close() {
		
	}
	
	class FileHistory {
		Path filePath;
		URL url;
		long rssSize;
		long fileSize;
		boolean success;
		int attempts;
		boolean needToDownload;
	}

}
