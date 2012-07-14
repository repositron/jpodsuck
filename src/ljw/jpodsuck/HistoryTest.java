package ljw.jpodsuck;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import junit.framework.Assert;

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
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void test() {
		try {
			History h = new History(Paths.get("dlfiles").toAbsolutePath(), "a1");
			Assert.assertFalse(h.needToDownload(Paths.get("dlfiles/a1/file1.mp3").toAbsolutePath(), 10));
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
