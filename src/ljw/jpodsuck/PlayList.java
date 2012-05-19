package ljw.jpodsuck;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.*;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagOptionSingleton;
import org.jaudiotagger.*;
import org.thirdparty.alphanumeric.AlphanumComparator;


public class PlayList {
	private Path folder;
	private Path playListFile;
	private Map<String, PlayListEntry> mp3s = new TreeMap<String, PlayListEntry>(new AlphanumComparator());
	PlayList(Path folder) {
		this.folder = folder;
		if (folder.getNameCount() < 1)
			throw new RuntimeException("Folder name error");
		this.playListFile = Paths.get(folder.toString(), folder.getName(folder.getNameCount() - 1).toString() + ".m3u");
	}

	public void create() {
		//Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
		if (Files.exists(playListFile, LinkOption.NOFOLLOW_LINKS)) {
			readPlayList();
		}
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, "*.mp3")) {
			for (Path entry : stream) {
				System.out.println(entry);
				if (!mp3s.containsKey(entry.toString())) {
					AudioFile f = AudioFileIO.read(entry.toFile());
					Tag tag = f.getTag();
					String title;
					if (tag != null)
						title = tag.getFirst(FieldKey.TITLE);
					
					else
						title = entry.getFileName().toString();
					PlayListEntry playListEntry = new PlayListEntry(title, f.getAudioHeader().getTrackLength());
					mp3s.put(entry.toString(), playListEntry);
					System.out.println("mp3: " + playListEntry.title + ", " + playListEntry.length);
				}
			}
			writePlayList();
		} catch (DirectoryIteratorException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void readPlayList() {
		try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(playListFile.toFile()), "UTF-8"))) {
			String ln = in.readLine();
			if (!ln.startsWith("#EXTM3U")) {
				throw new RuntimeException("header not found in: " + playListFile.toString());
			}
			Scanner scanner = new Scanner(in);
			String pattern = "^#EXTINF:(\\d+),(\\S+)$";
			while (scanner.hasNext(pattern)) {
				MatchResult match = scanner.match();
				PlayListEntry plentry = new PlayListEntry(match.group(2), Integer.parseInt(match.group(1)));
				String file = in.readLine();
				mp3s.put(file, plentry);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	void writePlayList() {
		try (Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(playListFile.toFile()), "UTF-8"))) {
			out.write("#EXTM3U\n");
			for (Map.Entry<String, PlayListEntry> entry : mp3s.entrySet()) {
				out.write("#EXTINF:" + entry.getValue().length + "," + entry.getValue().title + "\n");
				out.write(entry.getKey() + "\n");
				System.out.println(entry.getKey() + ": " + entry.getValue().title);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class PlayListEntry {
	String title;
	int length = 0;
	PlayListEntry(String title, int length) {
		this.title = title;
		this.length = length;
	}
}