package ljw.jpodsuck;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;


public class DownloadTask extends FutureTask<History.FileHistory> {
	DownloadTask(Callable<History.FileHistory> callable) {
		super(callable);
	}
}
