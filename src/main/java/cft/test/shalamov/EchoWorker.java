package cft.test.shalamov;

import cft.test.shalamov.job.Job;
import cft.test.shalamov.job.JobWorkerInterface;

/**
 * @author Shalamov
 */
public class EchoWorker implements JobWorkerInterface{

	/**
	 * @param job
	 */
	public void doJob(Job job) {
		System.out.println("Job " + job.getGroupId() + ":" + job.getId() + " done;");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {}
	}
	
}
