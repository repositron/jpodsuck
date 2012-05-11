package ljw.jpodsuck;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DownloadTask extends FutureTask<Integer> {
	DownloadTask(Callable<Integer> callable) {
		super(callable);
	}
}
