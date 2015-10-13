/*
    ParkBench, a versatile benchmark environment
    Copyright (C) 2015 Sylvain Hallé

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.parkbench;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import ca.uqac.lif.parkbench.Test.Status;

/**
 * Manages the execution of tests using multiple threads.
 * The dispatcher works as follows.
 * <ol>
 * <li>The benchmark puts tests in the dispatcher's queue using
 *   {@link #putInQueue(Test)}.</li>
 * <li>The dispatcher periodically checks whether some tests are present
 *   in the queue (the infinite loop of method {@link #run()}, which
 *   calls {@link #check()}.</li>
 * <li>If this is the case, the dispatcher loops through its array
 *   of threads to see if any of them has stopped. If so, the
 *   dispatcher creates a new {@link TestThread}, pops into it the first test
 *   from the waiting queue, starts it and puts it into the
 *   array's vacant slot.</li>
 * </ol>
 */
public class ThreadDispatcher implements Runnable
{
	/**
	 * The array of threads managed by the dispatcher
	 */
	protected final TestThread[] m_threads;
	
	/**
	 * The queue of tests waiting to be started
	 */
	protected Queue<Test> m_testQueue;
	
	/**
	 * A variable used as a semaphore to signal the dispatcher
	 * to stop
	 */
	protected boolean m_stop = false;
	
	/**
	 * The time (in ms) before the dispatcher looks again for new
	 * tests in its queue. Don't set it too low, it will clog your CPU.
	 */
	protected static int s_pollInterval = 1000;
	
	/**
	 * Creates a new thread dispatcher
	 * @param num_threads The number of threads that this dispatcher
	 *   will manage
	 */
	public ThreadDispatcher(int num_threads)
	{
		super();
		m_threads = new TestThread[num_threads];
		m_testQueue = new LinkedList<Test>();
	}
	
	/**
	 * Puts a new test in the waiting queue
	 * @param t The test to put in the queue
	 */
	synchronized public void putInQueue(Test t)
	{
		//System.out.println("Test added with ID " + t.getId());
		t.setStatus(Test.Status.QUEUED);
		m_testQueue.add(t);
	}
	
	/**
	 * Interrupts all running threads
	 */
	synchronized public void stopAll()
	{
		for (int i = 0; i < m_threads.length; i++)
		{
			TestThread th = m_threads[i];
			if (th != null)
			{
				th.interrupt();
			}
		}
	}
	
	/**
	 * Looks for terminated threads so
	 * that tests waiting in the queue can be started.
	 */
	synchronized protected void check()
	{
		for (int i = 0; i < m_threads.length; i++)
		{
			TestThread th = m_threads[i];
			if (th != null && th.isAlive())
			{
				if (th.m_test.canKill())
				{
					// This test can be interrupted
					th.interrupt();
					th.m_test.setStatus(Test.Status.TIMEOUT);
				}
				else
				{
					Test.Status st = th.m_test.getStatus();
					if (!(st == Test.Status.FAILED || st == Test.Status.DONE || 
							st == Test.Status.TIMEOUT))
					{
						continue;
					}
				}
			}
			// This thread has finished its execution, and a test is
			// waiting to be run
			Test test = m_testQueue.poll();
			if (test != null)
			{
				//System.out.println("Test ID " + test.getId() + " assigned to thread #" + i);
				TestThread th_new = new TestThread(test);
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
			// Wait some time before trying again
			try {
			    Thread.sleep(s_pollInterval);
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
		}
	}
	
	/**
	 * Tells the dispatcher to stop polling its queue. Beware: this
	 * has no effect on currently running tests. It will only prevent
	 * the dispatcher to start <em>new</em> tests.
	 */
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
	
	/**
	 * Interrupts the execution of a test. This either removes it from the
	 * queue if it was not started, or stops it if it is currently running.
	 * @param test_id The id of the test to stop
	 * @return true if a test was found and cancelled, false otherwise
	 */
	synchronized public boolean cancel(int test_id)
	{
		// First, look in the threads if the test is there
		for (int i = 0; i < m_threads.length; i++)
		{
			TestThread th = m_threads[i];
			if (th.getTestId() == test_id)
			{
				if (th.isAlive())
				{
					// Test is running: stop it
					th.interrupt();
				}
				// We found it; leave
				return true;
			}
		}
		// If we get here, then the test we look for was not in a thread.
		// Let's look for it in the waiting queue...
		Iterator<Test> t_it = m_testQueue.iterator();
		while (t_it.hasNext())
		{
			Test t = t_it.next();
			if (t.getId() == test_id)
			{
				// This is the test we look for: remove it from the
				// queue and leave
				// Since the test was not started, we put it back into the
				// NOT_DONE state (rather than FAILED)
				t.stopWithStatus(Status.NOT_DONE);
				t_it.remove();
				return true;
			}
		}
		// No success!
		return false;
	}
	
	/**
	 * Checks if the dispatcher is done. This is the case when the input
	 * queue is empty and all tests in the threads are finished or
	 * interrupted. 
	 * @return true if all done, false otherwise
	 */
	public boolean allDone()
	{
		if (!m_testQueue.isEmpty())
		{
			return false;
		}
		for (int i = 0; i < m_threads.length; i++)
		{
			Thread th = m_threads[i];
			if (th != null && th.isAlive())
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * A special case of thread used to run tests
	 */
	protected static class TestThread extends Thread
	{
		/**
		 * The test contained by that thread
		 */
		protected Test m_test;
		
		public TestThread(Test t)
		{
			super(t);
			m_test = t;
		}
		
		/**
		 * Gets the ID of the test contained in that thread
		 * @return The ID
		 */
		public int getTestId()
		{
			return m_test.getId();
		}
		
		@Override
		public void interrupt()
		{
			m_test.stopWithStatus(Test.Status.FAILED);
			super.interrupt();
		}
	}
}
