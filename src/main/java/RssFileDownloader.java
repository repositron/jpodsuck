/**
 * Created by ljw on 27/06/14.
 */

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class RssFileDownloader {

    DefaultHttpClient httpclient;
    private URL urlChannel;

    public RssFileDownloader(DefaultHttpClient httpclient, URL urlChannel) {
        this.httpclient = httpclient;
        this.urlChannel = urlChannel;
    }

    String download() {
        try {
            //logger.info("channel: " + urlChannel.toString());
            HttpGet httpget = new HttpGet(urlChannel.toString());
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
            // download rss file to memory stream.
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            IOUtils.copy(reader, byteStream);
            return byteStream.toString("UTF-8");

        }  catch (Exception e) {
            //logger.error("downloadRssFile exception", e);
            return "";
        }
    }
}
