package ljw.jpodsuck;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;


public class DownloadTask extends FutureTask<Integer> {
	DownloadTask(Callable<Integer> callable) {
		super(callable);
	}
}
