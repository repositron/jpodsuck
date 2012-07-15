package ljw.jpodsuck;
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
			// TODO Auto-generated catch block
			logger.error("cannot open config file.");
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
				Downloader.INSTANCE.init(httpclient);
	            
				ArrayList<ChannelProcessor> processors = new ArrayList<ChannelProcessor>();
				for (String s: config.urls) {
					processors.add(new ChannelProcessor(httpclient, new URL(s), config.folder));
				}

				for (ChannelProcessor p : processors) {
					p.process();
				}
				
				while (!processors.isEmpty()) {
					Iterator<ChannelProcessor> it = processors.iterator();
					while (it.hasNext()) {
						ChannelProcessor cp = it.next();
						if (cp.isFinished()) {
							cp.writeHistory();
							cp.writePlayList();
							it.remove();
						}
					}
					Thread.sleep(30);
				}
				
	            System.out.println("----------------------------------------");

	        } catch (Exception e) {
	        	logger.error("Exception ", e);
				e.printStackTrace(System.out);
			} finally {
				Downloader.INSTANCE.close();
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
