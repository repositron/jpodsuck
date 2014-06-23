import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ HistoryTest.class, PlayListTest.class, RssXmlParserTest.class,
		UpdateSchedulerTest.class })
public class AllTests {

}
