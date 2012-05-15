package ljw.jpodsuck;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.BufferedReader;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.util.EntityUtils;
import org.apache.commons.codec.*;

public class Top {
	Config config;
	DefaultHttpClient httpclient;
	Top()
	{
		ObjectMapper mapper = new ObjectMapper();
		try {
			config = mapper.readValue(new File("config.json"), Config.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void update()
	{
		try {
			httpclient = new DefaultHttpClient(new ThreadSafeClientConnManager());
			try {
				URL url = new URL(config.urls.get(0));
				System.out.println(url.toString());
				System.out.println(url.getHost());
				System.out.println(httpclient.getCredentialsProvider().toString());
				httpclient.getCredentialsProvider().setCredentials(new AuthScope(url.getHost(), AuthScope.ANY_PORT, null, "basic"),
                    new UsernamePasswordCredentials(config.user, config.password));
				System.out.println(httpclient.getCredentialsProvider().toString());
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
							cp.writePlayList();
							it.remove();
						}
					}
					Thread.sleep(30);
				}
				
	            System.out.println("----------------------------------------");

	        } catch (Exception e) {
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

		Top t = new Top();
		t.update();

	}

}
