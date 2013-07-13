package ljw.jpodsuck;


import java.io.*;
import java.nio.file.*;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import org.thirdparty.alphanumeric.AlphanumComparator;


class PlayList {
	private Path playListFile;
	private  Map<String, PlayListEntry> mp3s = new TreeMap<String, PlayListEntry>(new AlphanumComparator());
	PlayList(Path folder) {
		if (folder.getNameCount() < 1)
			throw new RuntimeException("Folder name error");
		this.playListFile = Paths.get(folder.toString(), folder.getName(folder.getNameCount() - 1).toString() + ".m3u");
		if (Files.exists(this.playListFile, LinkOption.NOFOLLOW_LINKS)) {
			readPlayList();
		}
	}
	
	// can be called from multiple threads
	synchronized void addMp3(String fileName, String title, int trackLength) {
		PlayListEntry playListEntry = new PlayListEntry(title, trackLength);
		mp3s.put(fileName, playListEntry);
	}
	
	void create() {
		writePlayList();
	}
	
	private void readPlayList() {
		try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(playListFile.toFile()), "UTF-8"))) {
			String ln = in.readLine();
			if (ln == null || !ln.startsWith("#EXTM3U")) {
				throw new RuntimeException("header not found in: " + playListFile.toString());
			}
			try (Scanner scanner = new Scanner(in)) {
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
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	private void writePlayList() {
		try (Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(playListFile.toFile()), "UTF-8"))) {
			out.write("#EXTM3U\n");
			for (Map.Entry<String, PlayListEntry> entry : mp3s.entrySet()) {
				out.write("#EXTINF:" + entry.getValue().length + "," + entry.getValue().title + "\n");
				out.write(entry.getKey() + "\n");
				System.out.println(entry.getKey() + ": " + entry.getValue().title);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private class PlayListEntry {
		private String title;
		private int length = 0;
		PlayListEntry(String title, int length) {
			this.title = title;
			this.length = length;
		}
	}
}

