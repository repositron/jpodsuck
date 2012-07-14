package ljw.jpodsuck;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void test() {
		try {
			History h = new History(Paths.get("dlfiles").toAbsolutePath(), "a1");
			Assert.assertFalse(h.needToDownload(Paths.get("dlfiles/a1/file1.mp3").toAbsolutePath(), 18));
			Assert.assertTrue(h.needToDownload(Paths.get("dlfiles/a1/file2.mp3").toAbsolutePath(), 10));
			Assert.assertTrue(h.needToDownload(Paths.get("dlfiles/a1/file3.mp3").toAbsolutePath(), 10));
			h.recordFileWritten(Paths.get("dlfiles/a1/file2.mp3").toAbsolutePath(), new URL("http://jp.com/file2.mp3"), true, 2);
			h.recordFileWritten(Paths.get("dlfiles/a1/file3.mp3").toAbsolutePath(), new URL("http://jp.com/file3.mp3"), true, 10);
			h.writeHistory();
			try (FileWriter f3 = new FileWriter(Paths.get("dlfiles/a1/file3.mp3").toAbsolutePath().toFile())) {
				
				char tenchars[] = new char[10];
				Arrays.fill(tenchars, 'a');
				f3.write(tenchars);	
			}
			
			History h2 = new History(Paths.get("dlfiles").toAbsolutePath(), "a1");
			Assert.assertFalse(h2.needToDownload(Paths.get("dlfiles/a1/file1.mp3").toAbsolutePath(), 18));
			Assert.assertFalse(h2.needToDownload(Paths.get("dlfiles/a1/file2.mp3").toAbsolutePath(), 2));
			Assert.assertFalse(h2.needToDownload(Paths.get("dlfiles/a1/file3.mp3").toAbsolutePath(), 10));
			
		} catch (FileNotFoundException e) {
			fail("exception");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			fail("exception");
		}
	}

}
