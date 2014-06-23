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
import org.apache.log4j.Logger;

public class History {
	private Path historyPath;
	private Path historyBackupPath;
	private Path folderPath;
	static Logger logger = Logger.getLogger("ljw.jpodsuck");

	private Map<String, FileHistory> filesHistory = new TreeMap<String, FileHistory>();

	History(Path folder, String channelTitle) throws FileNotFoundException, IOException {
		this.folderPath = Paths.get(folder.toString(), channelTitle);
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
					if (m.matches() && m.groupCount() == 6) {
						FileHistory fH = new FileHistory();
						fH.fileName = m.group(1);
						fH.url = new URL(m.group(2));
						//fH.niceFilename = m.group(3);
						fH.rssSize = Integer.parseInt(m.group(3));
						fH.fileSize = Integer.parseInt(m.group(4));
						fH.success = Boolean.parseBoolean(m.group(5));
						fH.attempts = Integer.parseInt(m.group(6));
						filesHistory.put(fH.fileName, fH);
					}
					else {
						logger.error("couldn't parse history file. Ln: = " + line);
					}
					
				}	
			}	
		}
	}
	void writeHistory() {
		if (Files.exists(historyPath)) {
			if (Files.exists(historyBackupPath)) {
				historyBackupPath.toFile().delete();	
			}
			historyPath.toFile().renameTo(historyBackupPath.toFile());
		}
		try (PrintWriter writer = new PrintWriter(new FileWriter(historyPath.toFile()))) {
			for (Map.Entry<String, FileHistory>  entry : filesHistory.entrySet()) {
				FileHistory h = entry.getValue();
				writer.format("\"%s\",%s,rss_size=%d,size=%d,%b,%d",
						h.fileName, h.url.toString(), h.rssSize, h.fileSize, h.success, h.attempts);
				writer.println();
			}
		} catch (IOException e) {
			logger.error("writeHistory", e);
		}
	}
	
	private void checkIfNeedToDownload(FileHistory fileHistory, long latestRssLength) throws IOException {
		fileHistory.needToDownload = false;
		Path filePath = Paths.get(fileHistory.fileName);
		if (Files.exists(filePath, LinkOption.NOFOLLOW_LINKS))
		{
			if (Files.size(filePath) != latestRssLength) // sizes of correctly downloaded files sometimes don't match the size in the rss  
			{
				logger.info(fileHistory.fileName + " Already exists but size is different origSize: " + Files.size(filePath) + " newSize:" + fileHistory.rssSize);
				// see if the original rss length has changed
				if (latestRssLength != fileHistory.rssSize) {
					// the rsslength has changed
					fileHistory.rssSize = latestRssLength;
					fileHistory.needToDownload = true;
				}
			}
		}
		else
		{
			logger.info(filePath.toString() + " doesn't exist");
			fileHistory.needToDownload = true;
			
		}
	}
	
	FileHistory getFileHistory(String fileName, URL url, long rssLength) throws IOException {
		FileHistory fileHistory = filesHistory.get(fileName);
		if (fileHistory == null) {
			// no record found so need to download
			fileHistory = new FileHistory();
			fileHistory.fileName = fileName;
			fileHistory.url = url;
			fileHistory.rssSize = rssLength;
			fileHistory.fileSize = 0;
			fileHistory.attempts = 0;
			fileHistory.success = false;
			fileHistory.needToDownload = true; // assume we haven't downloaded it
			filesHistory.put(fileName, fileHistory);
		}
		else {
			checkIfNeedToDownload(fileHistory, rssLength);
		}
		return fileHistory;
	}

	void close() {
		
	}
	
	class FileHistory {
		String fileName;
		URL url;
		//String niceFilename;
		long rssSize;
		long fileSize;
		boolean success;
		int attempts;
		boolean needToDownload;
	}

}
