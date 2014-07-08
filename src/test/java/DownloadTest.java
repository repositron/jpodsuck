/**
 * Created by ljw on 27/06/14.
 */

import mockit.*;
import org.junit.*;

import org.apache.http.impl.client.DefaultHttpClient;

public final class DownloadTest {
    @Mocked ChannelProcessor.FileProcessing fileProcessing;
    @Mocked DefaultHttpClient httpClient;
    @Mocked History.FileHistory fh;

    @Test
    public void test1() {


        new Expectations() {{

        }};

        Downloader dl = new Downloader(httpClient);
        dl.download(fh, fileProcessing);
    }



}
