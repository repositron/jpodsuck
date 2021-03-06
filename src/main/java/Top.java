import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import org.codehaus.jackson.map.ObjectMapper;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;


public class Top {
	static Logger logger = Logger.getLogger("ljw.jpodsuck");
	
	Config config;
	DefaultHttpClient httpclient;
	static UpdateScheduler updateScheduler = new UpdateScheduler(Paths.get(".updateschedule"), 120, 10);
	Top()
	{
		ObjectMapper mapper = new ObjectMapper();
		try {
			config = mapper.readValue(new File("config.json"), Config.class);
		} catch (Exception e) {
			logger.error("cannot open config file. " + e.getMessage());
			e.printStackTrace();
		}
	}
	public void update()
	{
		try {
			logger.info("updating");
			httpclient = new DefaultHttpClient(new ThreadSafeClientConnManager());
			try {
				URL url = new URL(config.urls.get(0));
	
				httpclient.getCredentialsProvider().setCredentials(new AuthScope(url.getHost(), AuthScope.ANY_PORT, null, "basic"),
                    new UsernamePasswordCredentials(config.user, config.password));
				Downloader downloader = new Downloader(httpclient);
	            
				ArrayList<ChannelProcessor> processors = new ArrayList<ChannelProcessor>();
				for (String s: config.urls) {
                    RssFileDownloader rssfileDownloader = new RssFileDownloader(httpclient, new URL(s));
                    ChannelProcessor ch = new ChannelProcessor(config.folder, downloader);
					processors.add(ch);
                    ch.process(rssfileDownloader.download());
				}

				while (!processors.isEmpty()) {
					Iterator<ChannelProcessor> it = processors.iterator();
					while (it.hasNext()) {
						ChannelProcessor cp = it.next();
						if (cp.isFinished()) {
							cp.doPostActions();
							it.remove();
						}
					}
					Thread.sleep(30);
				}
				
				logger.info("----------------------------------------");

	        } catch (Exception e) {
	        	logger.error("Exception ", e);
				e.printStackTrace(System.out);
			} finally {
	            httpclient.getConnectionManager().shutdown();
	        }
		}
		catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}
	
	public static void main(String[] args) {
		DOMConfigurator.configure("log4j.xml");
		logger.info("jpodsuck starting");
		Top t = new Top();
		while (true) {
			try {					
				if (updateScheduler.canUpdate(new GregorianCalendar())) {
					t.update();
					updateScheduler.updateSuccess(new GregorianCalendar());
				}
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
