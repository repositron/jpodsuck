import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
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
	                FileHistory fh = parseLine(line, pattern);
                    if (fh != null)
                        filesHistory.put(fh.fileAbsolutePath, fh);
				}	
			}	
		}
	}

    private FileHistory parseLine(String line, Pattern pattern) throws MalformedURLException {
        Matcher m = pattern.matcher(line);
        if (m.matches() && m.groupCount() == 6) {
            FileHistory fH = new FileHistory();
            fH.fileAbsolutePath = m.group(1);
            fH.url = new URL(m.group(2));
            //fH.niceFilename = m.group(3);
            fH.rssSize = Integer.parseInt(m.group(3));
            fH.fileSize = Integer.parseInt(m.group(4));
            fH.success = Boolean.parseBoolean(m.group(5));
            fH.attempts = Integer.parseInt(m.group(6));
            return fH;
        }
        else {
            logger.error("couldn't parse history file. Ln: = " + line);
            return null;
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
						h.fileAbsolutePath, h.url.toString(), h.rssSize, h.fileSize, h.success, h.attempts);
				writer.println();
			}
		} catch (IOException e) {
			logger.error("writeHistory", e);
		}
	}

	private FileHistory checkIfNeedToDownload(FileHistory fileHistory, long latestRssLength) throws IOException, CloneNotSupportedException {
        FileHistory newFileHistory = (FileHistory) fileHistory.clone();
        fileHistory.needToDownload = false;

		Path fileAbsolutePath = Paths.get(fileHistory.fileAbsolutePath);
		if (Files.notExists(fileAbsolutePath, LinkOption.NOFOLLOW_LINKS))
            return newFileHistory;

		if (Files.size(fileAbsolutePath) == latestRssLength) // sizes of correctly downloaded files sometimes don't match the size in the rss
            return newFileHistory;

        logger.info(fileHistory.fileAbsolutePath + " Already exists but size is different origSize: " + Files.size(fileAbsolutePath) + " newSize:" + fileHistory.rssSize);
        // see if the original rss length has changed
        if (latestRssLength != fileHistory.rssSize) {
            // the rsslength has changed
            newFileHistory.rssSize = latestRssLength;
            newFileHistory.needToDownload = true;
            return newFileHistory;
        }

        logger.info(fileAbsolutePath.toString() + " doesn't exist");
        fileHistory.needToDownload = true;
		return newFileHistory;

	}
	
	FileHistory getFileHistory(String fileAbsolutePath, URL url, long rssLength) throws IOException {
		FileHistory fileHistory = filesHistory.get(fileAbsolutePath);
		if (fileHistory == null) {
			// no record found so need to download
			fileHistory = new FileHistory();
			fileHistory.fileAbsolutePath = fileAbsolutePath;
			fileHistory.url = url;
			fileHistory.rssSize = rssLength;
			fileHistory.fileSize = 0;
			fileHistory.attempts = 0;
			fileHistory.success = false;
			fileHistory.needToDownload = true; // assume we haven't downloaded it
			filesHistory.put(fileAbsolutePath, fileHistory);
		}
		else {
            try {
                fileHistory = checkIfNeedToDownload(fileHistory, rssLength);
            } catch (CloneNotSupportedException e) {
                logger.error("clone error: ", e);
            }

		}
		return fileHistory;
	}

	void close() {
		
	}
	
	class FileHistory implements Cloneable {
		String fileAbsolutePath;
		URL url;
		//String niceFilename;
		long rssSize;
		long fileSize;
		boolean success;
		int attempts;
		boolean needToDownload;

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
	}

}
