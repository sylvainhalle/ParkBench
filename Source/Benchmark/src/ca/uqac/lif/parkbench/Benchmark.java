package ca.uqac.lif.parkbench;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ca.uqac.lif.cornipickle.json.JsonElement;
import ca.uqac.lif.cornipickle.json.JsonList;
import ca.uqac.lif.cornipickle.json.JsonMap;

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

	/**
	 * Sets the state of the benchmark to the contents of a JSON structure
	 * @param state The JSON structure
	 */
	public void deserializeState(JsonMap state)
	{
		Set<Test> new_tests = new HashSet<Test>();
		m_name = state.getString("name");
		JsonList test_list = (JsonList) state.get("tests");
		for (JsonElement el : test_list)
		{
			JsonMap el_test = (JsonMap) el;
			String test_name = el_test.getString("name");
			int test_id = el_test.getNumber("id").intValue();
			Test test_instance = findTestWithName(test_name);
			if (test_instance != null)
			{
				Test new_test = test_instance.newTest(test_id);
				new_test.deserializeState(el_test);
				new_tests.add(new_test);
			}
		}
		// Replaces the old set of tests with the one created from the JSON
		m_tests = new_tests;
	}
	
	/**
	 * Serializes the status of the benchmark into a JSON structure
	 * @return The JSON structure
	 */
	public JsonMap serializeState()
	{
		JsonMap out = new JsonMap();
		out.put("name", m_name);
		JsonList list = new JsonList();
		Set<String> test_params = getTestParameterNames();
		JsonList param_list = new JsonList();
		for (String param_name : test_params)
		{
			param_list.add(param_name);
		}
		out.put("param-names", param_list);
		Map<String,Integer> test_status = fillStatusMap();
		for (Test test : m_tests)
		{
			JsonMap m = test.serializeState();
			String status = Test.statusToString(test.getStatus());
			m.put("status", status);
			putInStatusMap(test, test_status);
			list.add(m);
		}
		out.put("tests", list);
		JsonMap status_map = new JsonMap();
		for (String status : test_status.keySet())
		{
			status_map.put(status, test_status.get(status));
		}
		out.put("status", status_map);
		return out;
	}
	
	/**
	 * In the set of current tests, finds a test with the same name as
	 * the parameter
	 * @param name The test's name
	 * @return A test instance with the same name, null if none found
	 */
	protected Test findTestWithName(String name)
	{
		for (Test t : m_tests)
		{
			if (name.compareTo(t.getName()) == 0)
			{
				return t;
			}
		}
		return null;
	}
	
	static Map<String,Integer> fillStatusMap()
	{
		Map<String,Integer> out = new HashMap<String,Integer>();
		out.put("status-done", 0);
		out.put("status-failed", 0);
		out.put("status-ready", 0);
		out.put("status-not-ready", 0);
		out.put("status-queued", 0);
		out.put("status-running", 0);
		return out;
	}
	
	static void putInStatusMap(Test t, Map<String,Integer> map)
	{
		switch (t.getStatus())
		{
		case DONE:
			map.put("status-done", map.get("status-done") + 1);
			break;
		case FAILED:
			map.put("status-failed", map.get("status-failed") + 1);
			break;
		case NOT_DONE:
			if (t.prerequisitesFulilled())
			{
				map.put("status-ready", map.get("status-ready") + 1);
			}
			else
			{
				map.put("status-not-ready", map.get("status-not-ready") + 1);
			}
			break;
		case RUNNING:
			map.put("status-running", map.get("status-running") + 1);
			break;
		case QUEUED:
			map.put("status-queued", map.get("status-queued") + 1);
			break;
		default:
			break;
		}
	}
}
