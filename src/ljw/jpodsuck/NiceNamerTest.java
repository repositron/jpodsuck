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
		lookup.put("Japanese Culture Class", "Culture");
		lookup.put("Upper Intermediate", "UI");
	}

	@After
	public void tearDown() throws Exception {
		lookup = null;
	}

	
	@Test
	public void titleTest() {
		NiceNamer niceNamer = new NiceNamer(lookup);
		String shortTitle = niceNamer.makeTitle("Lower Intermediate S6 #7 - When Did That Happen in Japan? - Audio");
		Assert.assertEquals("LI S6 #7 - When Did That Happen in Japan? - Audio", shortTitle);
		String shortTitle2 = niceNamer.makeTitle("Lower Intermediate S6 #7 - When Did That Happen in Japan? - Dialog");
		Assert.assertEquals("LI S6 #7 - When Did That Happen in Japan? - Dialog", shortTitle2);
		
	}

	@Test
	public void noAlbumTest() {
		NiceNamer niceNamer = new NiceNamer(lookup);
		String shortTitle = niceNamer.makeTitle("Japanese Culture Class #2 - Japanese Superstitions - Audio");
		Assert.assertEquals("Culture #2 - Japanese Superstitions - Audio", shortTitle);
	}
	
	@Test
	public void noSeasonTest() {
		NiceNamer niceNamer = new NiceNamer(lookup);
		String shortTitle = niceNamer.makeTitle("Intermediate Lesson #85 - A Very Bōsōzoku New Years - Audio");
		Assert.assertEquals("Int #85 - A Very Bōsōzoku New Years - Audio", shortTitle);
	}
	
	@Test
	public void noHyphensThatArenotTitleMarkerTest() {
		NiceNamer niceNamer = new NiceNamer(lookup);
		String shortTitle = niceNamer.makeTitle("Japanese Culture Class #1 - Coming-of-Age - Audio");
		Assert.assertEquals("Culture #1 - Coming-of-Age - Audio", shortTitle);
	}
	
}
