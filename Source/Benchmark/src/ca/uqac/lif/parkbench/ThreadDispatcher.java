package ca.uqac.lif.parkbench;

import java.util.LinkedList;
import java.util.Queue;

public class ThreadDispatcher implements Runnable
{
	protected Thread[] m_threads;
	
	protected Queue<Test> m_testQueue;
	
	protected boolean m_stop = false;
	
	public ThreadDispatcher(int num_threads)
	{
		super();
		m_threads = new Thread[num_threads];
		m_testQueue = new LinkedList<Test>();
	}
	
	synchronized public void putInQueue(Test t)
	{
		System.out.println("Test added with ID " + t.getId());
		t.setStatus(Test.Status.QUEUED);
		m_testQueue.add(t);
	}
	
	synchronized protected void check()
	{
		if (m_testQueue.isEmpty())
			return;
		for (int i = 0; i < m_threads.length; i++)
		{
			Thread th = m_threads[i];
			if (th != null && th.isAlive())
			{
				continue;
			}
			// This thread has finished its execution, and a test is
			// waiting to be run
			Test test = m_testQueue.poll();
			if (test != null)
			{
				System.out.println("Test ID " + test.getId() + " assigned to thread #" + i);
				Thread th_new = new Thread(test);
				th_new.start();
				m_threads[i] = th_new;
				break;
			}
		}
	}

	@Override
	public void run()
	{
		while (!m_stop)
		{
			check();
			// Wait one second before trying again
			try {
			    Thread.sleep(1000);
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
		}
	}
	
	synchronized public void stop()
	{
		m_stop = true;
	}
	
	/**
	 * Returns the number of threads managed by this dispatcher
	 * @return The number of threads
	 */
	synchronized public int threadCount()
	{
		return m_threads.length;
	}
}
