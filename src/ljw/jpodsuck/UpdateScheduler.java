package ljw.jpodsuck;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

class UpdateScheduler {
	final private Path lastUpdateFile;
	private Calendar nextTime;
	private int periodMinutes;
	private int retryMinutes;
	final private SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
	
	UpdateScheduler(Path lastUpdateFile, int periodMinutes, int retryMinutes) {
		this.lastUpdateFile = lastUpdateFile;
		this.periodMinutes = periodMinutes;
		this.retryMinutes = retryMinutes;
		this.nextTime = new GregorianCalendar(1973,0,0);
		
		//BufferedReader reader;
		try {
			if (Files.exists(lastUpdateFile, LinkOption.NOFOLLOW_LINKS)) {
				try (BufferedReader reader = new BufferedReader(new FileReader(lastUpdateFile.toFile()))) {
					String dateTimeStr = reader.readLine();
					if (dateTimeStr != null) {
						this.nextTime.setTimeInMillis(isoFormat.parse(dateTimeStr).getTime());
					}
				}
			}
		} catch (Exception e) {
		} 
		
	}
	
	public Boolean canUpdate(Calendar currentTime) {
		System.out.println("next time " + isoFormat.format(nextTime.getTime()));
		if (currentTime.after(nextTime))
			return true;
		else
			return false;
	}
	
	public void updateSuccess(Calendar currentTime) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(lastUpdateFile.toFile()))) {
			this.nextTime = (Calendar)currentTime.clone();
			this.nextTime.add(Calendar.MINUTE, periodMinutes);
			String dateTime = isoFormat.format(nextTime.getTime());
			writer.write(dateTime + "\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}