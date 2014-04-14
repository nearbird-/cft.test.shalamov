package cft.test.shalamov.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * @author Shalamov
 */
public class JobServer {
	
	private Integer autoIncrement = 1;
	private Random randomGenerator;
	
	private HashMap<Integer, LinkedList<Job>> pool;

	private HashMap<Integer, JobWorkerInterface> workers;
	
	private JobThread[] threads;
	
	private class JobThread extends Thread {

		private JobServer server;
		
		public JobThread(JobServer server) {
			this.server = server;
		}
		
		@Override
		public void run() {
			while(true) {
				try {
					Job job = server.removeJob();
					server.getWorker(job.getGroupId()).doJob(job);
				} catch (NoSuchElementException e) {
					synchronized (this) {
						try {
							wait();
						} catch (InterruptedException e1) {}
					}
				} catch (RuntimeException e1) {}
			}
		}
	}
	
	/**
	 * @param workersCount - limit of threads
	 */
	public JobServer(Integer workersCount) {
		randomGenerator = new Random();
		pool = new HashMap<Integer, LinkedList<Job>>();
		workers = new HashMap<Integer, JobWorkerInterface>();
		threads = new JobThread[workersCount];
		
		for (Integer i = 0; i < workersCount; i++) {
			threads[i] = new JobThread(this);
			threads[i].start();
		}
	}
	
	/**
	 * Register new worker for group with groupId
	 * 
	 * @param groupId
	 * @param worker
	 * @throws RuntimeException - if worker for group with groupId already exists 
	 */
	public void registerWorker(Integer groupId, JobWorkerInterface worker) throws RuntimeException {
		synchronized (workers) {
			if (workers.containsKey(groupId)) {
				throw new RuntimeException("Worker to '" + groupId + "' already exists");
			}
			
			workers.put(groupId, worker);
		}
	}
	
	/**
	 * 
	 * Get worker for groupId
	 * 
	 * @param groupId
	 * @return
	 * @throws RuntimeException - if worker for groupId not exists
	 */
	public JobWorkerInterface getWorker(Integer groupId) throws RuntimeException {
		synchronized (workers) {
			if (!workers.containsKey(groupId)) {
				throw new RuntimeException("Worker to '" + groupId + "' not exists");
			}
			
			return workers.get(groupId);
		}
	}
	
	/**
	 * Add job to pool
	 * 
	 * @param job 
	 * @throws RuntimeException - if Worker to job not exists
	 */
	public synchronized void doJob(Job job) throws RuntimeException {
		if (!workers.containsKey(job.getGroupId())) {
			throw new RuntimeException("Worker to '" + job.getGroupId() + "' not exists");
		}
		
		job.setId(autoIncrement++);
		LinkedList<Job> fifo;
		if ((fifo = pool.get(job.getGroupId())) == null) {
			fifo = new LinkedList<Job>();
			pool.put(job.getGroupId(), fifo);
		}
		fifo.add(job);
		
		notifyThread();
	}
	
	/**
	 * notify first free thread
	 */
	private void notifyThread() {
		for(JobThread thread : threads) {
			synchronized (thread) {
				if (thread.getState() == Thread.State.WAITING) {
					thread.notify();
					break;
				}
			}
		}
	}
	
	/**
	 * 
	 * Get job and remove from pool
	 * 
	 * @return Job
	 * @throws NoSuchElementException - if pool is empty
	 */
	public synchronized Job removeJob() throws NoSuchElementException {
		if (!pool.isEmpty()) {
			//select random group
			ArrayList<Integer> groups = new ArrayList<Integer>(pool.keySet());
			Integer index = randomGenerator.nextInt(groups.size());
			Integer key = groups.get(index);
			
			LinkedList<Job> fifo = pool.get(key);
			
			try {
				return fifo.remove();
			} catch (NoSuchElementException e) {
				pool.remove(key);
				return this.removeJob();
			}
		}
		
		throw new NoSuchElementException();
	}
}	
