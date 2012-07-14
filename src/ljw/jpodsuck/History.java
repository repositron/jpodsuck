package ljw.jpodsuck;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
				Pattern pattern = Pattern.compile("^\"(.+)\",(.+),(\\d+),(true|false),(\\d+)$");
				String line;
				while ((line = bReader.readLine()) != null) {
					Matcher m = pattern.matcher(line);
					if (m.matches() && m.groupCount() == 5)
					{
						String a = m.group(1);
						FileHistory fH = new FileHistory();
						fH.filePath = Paths.get(m.group(1));
						fH.url = new URL(m.group(2));
						fH.size = Integer.parseInt(m.group(3));
						fH.success = Boolean.parseBoolean(m.group(4));
						fH.attempts = Integer.parseInt(m.group(5));
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
		try (BufferedWriter bWriter = new BufferedWriter(new FileWriter(historyPath.toFile()))) {
			for (Map.Entry<Path, FileHistory>  entry : filesHistory.entrySet()) {
				FileHistory h = entry.getValue();
				bWriter.write("\"" + h.filePath.toString() + "\"," + h.url.toString() + "," + h.size + "," + Boolean.toString(h.success) + "," + h.attempts);
				bWriter.newLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	public void recordFileWritten(Path filePath, URL url, boolean success, int Size) {
		FileHistory fileHistory = filesHistory.get(filePath);
		if (fileHistory == null) {
			fileHistory = new FileHistory();
			fileHistory.filePath = filePath;
			fileHistory.attempts = 1;
			filesHistory.put(filePath, fileHistory);
		}			
		fileHistory.success = success;
		if (!success)
			fileHistory.size = 0;
		else
			fileHistory.size = Size;
		fileHistory.url = url;
	}
	void write() {
		
	}
	String findlastSaveRssFile() {
		String file = "";
		return file;
	}
	void loadPrevRssFile() {
		
	}
	
	boolean needToDownload(Path file, int rssLength) throws IOException {
		boolean download = false;
		if (Files.exists(file, LinkOption.NOFOLLOW_LINKS))
		{
			if (Files.size(file) != rssLength)
			{
				logger.info(file.toString() + " Already exists but size is different origSize: " + Files.size(file) + " newSize:" + rssLength);
				FileHistory fH = filesHistory.get(file);
				if (fH != null) {
					if (rssLength != fH.size) {
						download = true;
					}
				}
				else
					download = true;
			}
		}
		else
		{
			logger.info(file.toString() + " doesn't exist");
			download = true;
			
		}
		return download;
	}
	void close() {
		
	}
	
	class FileHistory {
		Path filePath;
		URL url;
		int size;
		boolean success;
		int attempts;
		
	}
	
}
