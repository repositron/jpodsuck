import static org.junit.Assert.*;
import java.util.regex.*;

import org.junit.Test;

public class PlayListTest {

	@Test
	public final void testReadPlayList() {
		String test1 = "#EXTINF:50,name x\n";
		Matcher m = Pattern.compile("^#EXTINF:(\\d+),(.+)$").matcher(test1);
		assertTrue(m.find());
		assertEquals(2, m.groupCount());
		assertEquals("50", m.group(1));
		assertEquals("name x", m.group(2));
	}
	
	@Test
	public final void testReadPlayList2() {
		String test1 = "#EXTINF:27,Particles #1 - Coincidence Times Two, Japanese Particles Wa, No, and Mo - Dialog\n78886";
		Matcher m = Pattern.compile("^#EXTINF:(\\d+),(.+)\n78886").matcher(test1);
		assertTrue(m.find());
		assertEquals(2, m.groupCount());
		assertEquals("27", m.group(1));
		assertEquals("Particles #1 - Coincidence Times Two, Japanese Particles Wa, No, and Mo - Dialog", m.group(2));
	}

}
