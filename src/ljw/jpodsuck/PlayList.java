package ljw.jpodsuck;

import java.io.IOException;
import java.nio.file.*;
import java.util.TreeMap;

public class PlayList {
	private Path folder;
	private TreeMap<String, PlayListEntry> mp3s = new TreeMap<String, PlayListEntry>();
	PlayList(Path folder) {
		this.folder = folder;
	}

	public void create() {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, "*.mp3")) {
			for (Path entry : stream) {
				PlayListEntry playListEntry = new PlayListEntry();
				mp3s.put(entry.toString(), playListEntry);
			}
		} catch (DirectoryIteratorException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class PlayListEntry {
	String title;
	int length = 0;
}