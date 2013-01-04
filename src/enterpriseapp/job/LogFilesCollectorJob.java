package enterpriseapp.job;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import enterpriseapp.EnterpriseApplication;
import enterpriseapp.Utils;

/**
 * Quartz job that moves the server log files to a new directory on a monthly basis.
 * 
 * @author Alejandro Duarte
 *
 */
public class LogFilesCollectorJob implements Job {
	
	private static Logger logger = LoggerFactory.getLogger(LogFilesCollectorJob.class);

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		Collection<File> files = FileUtils.listFiles(new File(Utils.getServerLogsDirectory()), new String[] {"txt"}, false);
		File dest = new File(Utils.getServerLogsDirectory() + "/" + Utils.getCurrentYear() + "/" + Utils.getPreviousMonth());
		System.out.println(dest.getName());
		System.out.println(dest.getAbsolutePath());
		
		for(File src : files) {
			try {
				logger.info("Moving " + src.getName());
				FileUtils.moveFileToDirectory(src, dest, true);
				
			} catch (IOException e) {
				// We can't move files been used by the server so it's safe(?) to ignore exceptions here
				logger.warn("Can't move " + src.getName());
				// logger.error("Error moving log file.", e);
			}
		}
		
		logger.info("Log files collected.");
		scheduleLogFilesCollectorJob();
	}

	/**
	 * Schedules next job execution.
	 */
	public static void scheduleLogFilesCollectorJob() {
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.MILLISECOND, 0);
			calendar.set(Calendar.MINUTE, 5);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			calendar.add(Calendar.MONTH, 1);
			
			JobKey jobKey = new JobKey(calendar.getTime().toString());
			JobDetail job = JobBuilder.newJob(LogFilesCollectorJob.class).withIdentity(jobKey).build();
			Trigger trigger = TriggerBuilder.newTrigger().startAt(calendar.getTime()).build();
			EnterpriseApplication.getScheduler().scheduleJob(job, trigger);
			
			logger.info("LogFilesCollectorJob scheduled at " + Utils.dateToString(calendar.getTime(), Utils.getAlternateDateTimeFormatPattern()));
			
		} catch (SchedulerException e) {
			logger.error("Error shcheduling LogFilesCollectorJob.", e);
		}
	}
	
}
