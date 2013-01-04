package enterpriseapp.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import enterpriseapp.hibernate.Db;

/**
 * Extend this class to create a job that performs database operations.
 * 
 * @author Alejandro Duarte
 *
 */
public abstract class TransactionalJob implements Job {
	
	public abstract void executeJob(JobExecutionContext context) throws JobExecutionException;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		Db.beginTransaction();
		executeJob(context);
		Db.commitTransaction();
	}
	
}
