package ljw.jpodsuck;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NiceNamerTest {
	Map<String, String> lookup = new HashMap<String, String>();
	@Before
	public void setUp() throws Exception {
		
		lookup.put("Lower Intermediate", "LI");
		lookup.put("Intermediate Lesson", "Int");
		lookup.put("Beginner", "Beg");
		
	}

	@After
	public void tearDown() throws Exception {
		lookup = null;
	}

	
	@Test
	public void titleTest() {
		NiceNamer niceNamer = new NiceNamer(lookup);
		String shortTitle = niceNamer.makeTitle("Lower Intermediate S6 #7 - When Did That Happen in Japan?");
		Assert.assertEquals("LI S6 #7 - When Did That Happen in Japan?", shortTitle);
	}

	@Test
	public void noAlbumTest() {
		NiceNamer niceNamer = new NiceNamer(lookup);
		String shortTitle = niceNamer.makeTitle("Japanese Superstitions");
		Assert.assertEquals("Japanese Superstitions", shortTitle);
	}
	
	@Test
	public void noSeasonTest() {
		NiceNamer niceNamer = new NiceNamer(lookup);
		String shortTitle = niceNamer.makeTitle("Intermediate Lesson #85 - A Very Bōsōzoku New Years");
		Assert.assertEquals("Int #85 - A Very Bōsōzoku New Years", shortTitle);
	}
	
	@Test
	public void noHyphensThatArenotTitleMarkerTest() {
		NiceNamer niceNamer = new NiceNamer(lookup);
		String shortTitle = niceNamer.makeTitle("Coming-of-Age");
		Assert.assertEquals("Coming-of-Age", shortTitle);
	}
	
}
