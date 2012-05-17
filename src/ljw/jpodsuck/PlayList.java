package ljw.jpodsuck;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	private Map<String, PlayListEntry> mp3s = new TreeMap<String, PlayListEntry>(new AlphanumComparator());
	PlayList(Path folder) {
		this.folder = folder;
	}

	public void create() {
		Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
		//Logger.getGlobal().
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, "*.mp3")) {
			for (Path entry : stream) {
				System.out.println(entry);
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
			for (Map.Entry<String, PlayListEntry> entry : mp3s.entrySet()) {
				System.out.println(entry.getKey() + ": " + entry.getValue().title);
			}
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
}

class PlayListEntry {
	String title;
	int length = 0;
	PlayListEntry(String title, int length) {
		this.title = title;
		this.length = length;
	}
}