package ljw.jpodsuck;

import static org.junit.Assert.*;

import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class UpdateSchedulerTest {

	private UpdateScheduler us;
	final private SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
	final private Path time2Path = Paths.get("testfiles/time2.txt");
	final private Calendar c1980 = new GregorianCalendar(1980, 01, 01, 12, 12);
	final private Path nofilePath = Paths.get("testfiles/time.txt");
	@Before
	public void setUp() throws Exception {
		time2Path.toFile().delete();
		nofilePath.toFile().delete();
		try (FileWriter writer = new FileWriter(time2Path.toFile())) {
			String dateTime = isoFormat.format(c1980.getTime()); 
			writer.write(dateTime + "\n");
		}
	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public final void testNoFile() {
		UpdateScheduler us = new UpdateScheduler(nofilePath, 20, 10);
		Calendar c = c1980;
		assertTrue(us.canUpdate(c));
		us.updateSuccess(c);
		assertTrue(Files.exists(nofilePath, LinkOption.NOFOLLOW_LINKS));
		assertFalse(us.canUpdate(c));
		System.out.println(isoFormat.format(c.getTime()));
		c.add(Calendar.MINUTE, 10);
		System.out.println(isoFormat.format(c.getTime()));
		assertFalse(us.canUpdate(c));
		c.add(Calendar.MINUTE, 10);
		assertFalse(us.canUpdate(c));
		c.add(Calendar.MINUTE, 1);
		System.out.println(isoFormat.format(c.getTime()));
		assertTrue(us.canUpdate(c));
	}
	
	@Test
	public final void testFile() {
		UpdateScheduler us = new UpdateScheduler(time2Path, 20, 10);
		assertTrue(Files.exists(time2Path, LinkOption.NOFOLLOW_LINKS));
		Calendar c = c1980;
		assertFalse(us.canUpdate(c));
		us.updateSuccess(c);
		c.add(Calendar.MINUTE, 20);
		assertFalse(us.canUpdate(c));
		c.add(Calendar.MINUTE, 1);
		assertTrue(us.canUpdate(c));
		us.updateSuccess(c);
	}
	
	@Test
	public final void testSimulateShutdownRestart() {
		UpdateScheduler us = new UpdateScheduler(time2Path, 20, 10);
		assertTrue(Files.exists(time2Path, LinkOption.NOFOLLOW_LINKS));
		Calendar c = c1980;
		c.add(Calendar.MINUTE, 1);
		System.out.println(isoFormat.format(c.getTime()));
		assertTrue(us.canUpdate(c));
		us.updateSuccess(c);
		
		us = null;
		// Simulate daemon shutdown for 10 days
		UpdateScheduler us2 = new UpdateScheduler(time2Path, 20, 10);
		assertTrue(Files.exists(time2Path, LinkOption.NOFOLLOW_LINKS));
		c.add(Calendar.HOUR, 24*10);
		assertTrue(us2.canUpdate(c));
		us2.updateSuccess(c);
		assertFalse(us2.canUpdate(c));
	}

}
