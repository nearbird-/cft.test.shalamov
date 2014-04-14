package cft.test.shalamov.job;

import java.util.NoSuchElementException;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JobServerTest extends Assert {
	private JobServer server;
	
	public static Boolean jobResult = false;
	
	
	@Before
	public void before() {
		server = new JobServer(0);
		server.registerWorker(1, new JobWorkerInterface() {
			@Override
			public void doJob(Job job) {}
		});
	}
	
	@Test
	public void testAddWorker() {
		JobWorkerInterface worker = new JobWorkerInterface() {
			@Override
			public void doJob(Job job) {}
		}; 
		server.registerWorker(2, worker); 
		assertEquals(server.getWorker(2), worker);
	}
	
	/**
	 * If worker already registered
	 */
	@Test(expected=RuntimeException.class)
	public void testRegisterWorkerExists() {
		server.registerWorker(1, new JobWorkerInterface() {
			@Override
			public void doJob(Job job) {}
		});
	}
	
	/**
	 * If worker not registered
	 */
	@Test(expected=RuntimeException.class)
	public void testGetWorkerNotExists() {
		server.getWorker(2);
	}
	
	/**
	 * If worker not exists
	 */
	@Test(expected=RuntimeException.class)
	public void testDoJobWorkerNotExists() {
		Job job = new Job(2);
		server.doJob(job);
	}

	@Test
	public void testAddJob() {
		Job job = new Job(1);
		server.doJob(job);
		assertEquals(server.removeJob(), job);
	}
	
	
	@Test(expected=NoSuchElementException.class)
	public void testRemoveJobNoSuch() {
		server.removeJob();
	}
	
	@Test
	public void testRemoveCountShouldEqualsAddedCount() {
		Integer removeCount = 0,
				addedCount = new Random().nextInt(10);
		server.registerWorker(2, new JobWorkerInterface() {
			@Override
			public void doJob(Job job) {}
		});
		for(Integer i = 0; i < addedCount; i++) {
			server.doJob(new Job(i%2==0 ? 1 : 2));
		}
		
		while(true) {
			try {
				server.removeJob();
				removeCount++;
			} catch (NoSuchElementException e) {
				break;
			}
		}
		
		assertEquals(removeCount, addedCount);
	}
	
	public void testJobResult() {
		server = new JobServer(1);
		JobServerTest.jobResult = false;
		server.registerWorker(1, new JobWorkerInterface() {
			@Override
			public void doJob(Job job) {JobServerTest.jobResult = true;}
		});
		server.doJob(new Job(1));
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {}
		assertTrue(JobServerTest.jobResult);
	}
}
