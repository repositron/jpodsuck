package ljw.jpodsuck;


import java.io.*;
import java.nio.file.*;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
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
				String fileName = entry.getFileName().toString();
				System.out.println(entry);
				if (!mp3s.containsKey(fileName)) {
					AudioFile f = AudioFileIO.read(entry.toFile());
					Tag tag = f.getTag();
					String title;
					if (tag != null)
						title = tag.getFirst(FieldKey.TITLE);
					
					else
					{
						// TODO should use title from rss
						title = fileName;
						
					}
					PlayListEntry playListEntry = new PlayListEntry(title, f.getAudioHeader().getTrackLength());
					mp3s.put(fileName, playListEntry);
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
			if (ln == null || !ln.startsWith("#EXTM3U")) {
				throw new RuntimeException("header not found in: " + playListFile.toString());
			}
			Scanner scanner = new Scanner(in);
			while (scanner.hasNextLine()) {
				String entryLn = scanner.nextLine();
				final String  extinfStr = "#EXTINF:";
				if (entryLn.startsWith(extinfStr)) {
					int commarIndex = entryLn.indexOf(",", extinfStr.length());
					if (commarIndex != -1) {
						int time = Integer.parseInt(entryLn.substring(extinfStr.length(), commarIndex));
						String title = entryLn.substring(commarIndex + 1).trim();
						PlayListEntry plentry = new PlayListEntry(title, time);
						if (scanner.hasNextLine())
						{	String filePath = scanner.nextLine().trim();
							mp3s.put(filePath, plentry);
						}
					}
					else
						break;
				}
				else
					break;
			}
		} catch (Exception e) {
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