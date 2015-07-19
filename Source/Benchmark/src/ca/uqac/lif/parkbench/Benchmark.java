package ca.uqac.lif.parkbench;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A benchmark controls the execution of a set of tests
 * @author Sylvain
 *
 */
public class Benchmark
{
	/**
	 * The set of tests managed by the benchmark
	 */
	protected Set<Test> m_tests;
	
	/**
	 * A name for this benchmark
	 */
	protected String m_name;
	
	/**
	 * The dispatcher for the threads for running the tests
	 */
	protected ThreadDispatcher m_dispatcher;
	
	
	/**
	 * Create an empty benchmark 
	 */
	public Benchmark()
	{
		// By default, single-threaded
		this(1);
	}
	
	/**
	 * Create an empty benchmark 
	 */
	public Benchmark(int num_threads)
	{
		super();
		m_tests = new HashSet<Test>();
		m_name = "Untitled Benchmark";
		m_dispatcher = new ThreadDispatcher(num_threads);
		Thread th = new Thread(m_dispatcher);
		th.start();
	}
	
	/**
	 * Sets the benchmark's name
	 * @param name The name
	 */
	public void setName(String name)
	{
		m_name = name;
	}
	
	/**
	 * Gets the benchmark's name
	 * @return The name
	 */
	public String getName()
	{
		return m_name;
	}
	
	/**
	 * Add a test to the benchmark
	 * @param t The test to add
	 */
	public void addTest(Test t)
	{
		m_tests.add(t);
	}
	
	/**
	 * Retrieves a set of tests based on a set of parameters
	 * @param parameters The parameters
	 * @return The tests with the corresponding parameters, if
	 *   any
	 */
	public static final Set<Test> getTests(Collection<Test> tests, Parameters parameters)
	{
		Set<Test> out = new HashSet<Test>();
		for (Test t : tests)
		{
			Parameters t_params = t.getParameters();
			if (t_params.match(parameters))
			{
				out.add(t);
			}
		}
		return out;
	}
	
	/**
	 * Return the set of all tests in the benchmark
	 * @return The tests
	 */
	public Set<Test> getTests()
	{
		return m_tests;
	}
	
	/**
	 * Retrieves a single test based on a set of parameters. In case
	 * many tests match the parameters, one is picked nondeterministically.
	 * @param parameters The parameters
	 * @return The test with the corresponding parameters,
	 *   null if not found
	 */	
	public static final Test getTest(Collection<Test> tests, Parameters parameters)
	{
		Set<Test> out = getTests(tests, parameters);
		if (out.isEmpty())
		{
			return null;
		}
		for (Test t : out)
		{
			return t;
		}
		return null;
	}

	/**
	 * Sequentially generates all the prerequisites in the benchmark
	 * @param override Set to <tt>true</tt> to re-generate the prerequisites
	 *   even when they are present; otherwise use <tt>false</tt> 
	 */
	public void generateAllPrerequisites(boolean override)
	{
		Iterator<Test> it = m_tests.iterator();
		while (it.hasNext())
		{
			Test t = it.next();
			if (override || !t.prerequisitesFulilled())
			{
				t.fulfillPrerequisites();
			}
		}
	}
	
	/**
	 * Retrieves the set of all parameter names contained in
	 * at least one test
	 * @return The set of parameter names
	 */
	public Set<String> getTestParameterNames()
	{
		Set<String> out = new HashSet<String>();
		for (Test t : m_tests)
		{
			out.addAll(t.getParameters().keySet());
		}
		return out;
	}
	
	/**
	 * Counts the threads associated to the benchmark
	 * @return The number of threads
	 */
	public int threadCount()
	{
		return m_dispatcher.threadCount();
	}
	
	/**
	 * Sequentially runs all the tests in the benchmark
	 */
	public void runAllTests()
	{
		Iterator<Test> it = m_tests.iterator();
		while (it.hasNext())
		{
			Test t = it.next();
			if (t.getStatus() == Test.Status.NOT_DONE)
			{
				t.run();
			}
		}
	}
	
	/**
	 * Runs a test in the benchmark
	 * @param test_id The id of the test to run
	 * @return true if a test with that ID exists, false otherwise
	 */
	public boolean runTest(int test_id)
	{
		Iterator<Test> it = m_tests.iterator();
		while (it.hasNext())
		{
			Test t = it.next();
			if (t.getId() == test_id)
			{
				t.run();
				return true;
			}
		}
		return false;
	}
	
	public boolean queueTest(int test_id)
	{
		Iterator<Test> it = m_tests.iterator();
		while (it.hasNext())
		{
			Test t = it.next();
			if (t.getId() == test_id)
			{
				m_dispatcher.putInQueue(t);
				return true;
			}
		}
		return false;		
	}

	
	/**
	 * Sets the dry run status of every test in the benchmark.
	 * See {@link Test#setDryRun(boolean)}.
	 * @param b The dry run status
	 */
	public void setDryRun(boolean b)
	{
		for (Test t : m_tests)
		{
			t.setDryRun(b);
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder out = new StringBuilder();
		for (Test t : m_tests)
		{
			out.append(t);
		}
		return out.toString();
	}
}
