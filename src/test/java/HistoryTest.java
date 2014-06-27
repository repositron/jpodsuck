import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.*;


import org.apache.commons.io.IOUtils;
import org.apache.log4j.xml.DOMConfigurator;

public class HistoryTest {

	@BeforeClass 
	static public void beforeClass() throws Exception {
		DOMConfigurator.configure("log4j_utest.xml");
	}
	
	@Before
	public void setUp() throws Exception {
		Path dest = Paths.get("dlfiles/a1/.history");
		if (Files.exists(dest))
			dest.toFile().delete();
		IOUtils.copy(new FileInputStream(Paths.get("a1history.txt").toFile()), new FileOutputStream(Paths.get("dlfiles/a1/.history").toFile()));
		
		Path p = Paths.get("dlfiles/a1/file3.mp3").toAbsolutePath();
		if (Files.exists(p))
			p.toFile().delete();
		
		Path dest2 = Paths.get("dlfiles/a2/.history");
		if (Files.exists(dest2))
			dest2.toFile().delete();
	}

	@After
	public void tearDown() throws Exception {
	}

	boolean needToDownload(History h, String filename, URL url, long l) throws IOException {
		History.FileHistory fh = h.getFileHistory(filename, url, l);
		return fh.needToDownload;
	}
	
	@Test
	public final void testExistHistory() {
		try {
			History h = new History(Paths.get("dlfiles").toAbsolutePath(), "a1");
			Assert.assertFalse(needToDownload(h, "dlfiles/a1/file1.mp3", new URL("http://jp.com/file1.mp3"), 18));
			History.FileHistory fh2 = h.getFileHistory("dlfiles/a1/file2.mp3", new URL("http://jp.com/file2.mp3"), 10);
			Assert.assertTrue(fh2.needToDownload);
			History.FileHistory fh3 = h.getFileHistory("dlfiles/a1/file3.mp3", new URL("http://jp.com/file3.mp3"), 10);
			Assert.assertTrue(fh3.needToDownload);

			Path file2 = Paths.get("dlfiles/a1/file2.mp3").toAbsolutePath();
			Path file3 = Paths.get("dlfiles/a1/file3.mp3").toAbsolutePath();
			fh2.success = true;
			fh2.rssSize = 2;
			fh2.fileSize =  Files.size(file2);
			
			try (FileWriter f3 = new FileWriter(Paths.get("dlfiles/a1/file3.mp3").toAbsolutePath().toFile())) {
				
				char tenchars[] = new char[10];
				Arrays.fill(tenchars, 'a');
				f3.write(tenchars);	
			}
			fh3.success = true;
			fh3.rssSize = 10;
			fh3.fileSize =  Files.size(file3);

			h.writeHistory();
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}

    @Test
    public final void testDownloadedMp3sNotDownloadAgain() {
        testExistHistory();
        try {
            History h2 = new History(Paths.get("dlfiles").toAbsolutePath(), "a1");
            Assert.assertFalse(needToDownload(h2, "dlfiles/a1/file1.mp3", new URL("http://jp.com/file1.mp3"), 18));
            Assert.assertFalse(needToDownload(h2, "dlfiles/a1/file2.mp3", new URL("http://jp.com/file2.mp3"), 2));
            Assert.assertFalse(needToDownload(h2, "dlfiles/a1/file3.mp3", new URL("http://jp.com/file3.mp3"), 10));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

	@Test
	public final void testNoHistoryFileButMp3Exists() {
		try {
			History h = new History(Paths.get("dlfiles").toAbsolutePath(), "a2");
			
			History.FileHistory fh = h.getFileHistory("dlfiles/a2/file1.mp3", new URL("http://jp.com/file1.mp3"), 4);
			Assert.assertTrue(fh.needToDownload);

			fh.success = true;
			fh.rssSize = 5;
			fh.fileSize = Files.size(Paths.get("dlfiles/a2/file1.mp3").toAbsolutePath());
			h.writeHistory();
		} catch (IOException e) {
			e.printStackTrace();
			fail("exception");
		}
		try {
			History h = new History(Paths.get("dlfiles").toAbsolutePath(), "a2");
			History.FileHistory fh = h.getFileHistory("dlfiles/a2/file1.mp3", new URL("http://jp.com/file1.mp3"), 5);
			Assert.assertFalse(fh.needToDownload);		
			h.writeHistory();
		} catch (IOException e) {
			e.printStackTrace();
			fail("exception");
		}
	}
}
