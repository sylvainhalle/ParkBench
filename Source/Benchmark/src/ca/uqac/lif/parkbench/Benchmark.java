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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ca.uqac.lif.cornipickle.json.JsonElement;
import ca.uqac.lif.cornipickle.json.JsonList;
import ca.uqac.lif.cornipickle.json.JsonMap;
import ca.uqac.lif.parkbench.plot.Plot;

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
	 * A subset of tests managed by the benchmark. This set should
	 * contain one test instance per distinct class in the benchmark.
	 * It is used for deserializing tests from JSON documents. 
	 */
	protected Set<Test> m_classInstances;

	/**
	 * A name for this benchmark
	 */
	protected String m_name;

	/**
	 * The dispatcher for the threads for running the tests
	 */
	protected ThreadDispatcher m_dispatcher;

	/**
	 * A counter for plot IDs
	 */
	protected int s_plotCounter = 0;

	/**
	 * The thread running the dispatcher
	 */
	protected Thread m_dispatcherThread;

	protected Map<Integer,Plot> m_plots;


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
	 * @param num_threads The number of threads to allow to this benchmark
	 *  (i.e. the number of tests that are allowed to run simultaneously)
	 */
	public Benchmark(int num_threads)
	{
		super();
		m_tests = new HashSet<Test>();
		m_classInstances = new HashSet<Test>();
		m_name = "Untitled";
		m_dispatcher = new ThreadDispatcher(num_threads);
		m_dispatcherThread = new Thread(m_dispatcher);
		m_dispatcherThread.start();
		m_plots = new HashMap<Integer,Plot>();
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
		addTestToClassInstances(t);
	}
	
	/**
	 * Conditionally adds a test instances to the set of test
	 * classes. The test will be added to the set only if there is
	 * no test of the same class already present.
	 * @param t The test to add
	 * @return true if the test was <em>not</em> added to the set,
	 *   false otherwise
	 */
	protected boolean addTestToClassInstances(Test t)
	{
		// Loop through class instances and add only if this class does
		// not exist
		boolean found = false;
		for (Test t_i : m_classInstances)
		{
			if (t.isCompatible(t_i))
			{
				found = true;
				break;
			}
		}
		if (!found)
		{
			m_classInstances.add(t);
		}
		return found;
	}

	/**
	 * Retrieves a set of tests based on a set of parameters
	 * @param tests The collection of tests to pick from
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
	 * @param tests The collection of tests to pick from
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
			Parameters input = t.getParameters();
			if (override || !t.prerequisitesFulilled(input))
			{
				t.fulfillPrerequisites(input);
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
	 * Queues all the tests in the benchmark
	 */
	public void queueAllTests()
	{
		Iterator<Test> it = m_tests.iterator();

		while (it.hasNext())
		{
			Test t = it.next();
			if (t.canRun(t.getParameters()))
			{
				m_dispatcher.putInQueue(t);
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
		Test t = getTest(test_id);
		if (t != null)
		{
			if (t.canRun(t.getParameters()))
			{
				t.run();
				return true;
			}
		}
		return false;
	}

	/**
	 * Places a test in the waiting queue to be executed
	 * @param test_id The id of the test to run
	 * @return true if a test with that ID exists, false otherwise
	 */
	public boolean queueTest(int test_id)
	{
		Test t = getTest(test_id);
		if (t != null)
		{
			if (t.canRun(t.getParameters()))
			{
				m_dispatcher.putInQueue(t);
				return true;
			}
		}
		return false;
	}

	/**
	 * Stops a test
	 * @param test_id The ID of the test to stop
	 * @return true if the test exits (whether or not it needed stopping),
	 *   false otherwise
	 */
	public boolean stopTest(int test_id)
	{
		return m_dispatcher.cancel(test_id);		
	}

	/**
	 * Checks if all the tests in the benchmark are done
	 * (either finished or interrupted)
	 * @return true if all tests are finished, false otherwise
	 */
	public boolean isFinished()
	{
		return m_dispatcher.allDone();
	}

	/**
	 * Sets the dry run status of every test in the benchmark.
	 * See {@link Test#setDryRun(boolean)}.
	 * @param b The dry run status
	 * @return An instance of this benchmark
	 */
	public Benchmark setDryRun(boolean b)
	{
		for (Test t : m_tests)
		{
			t.setDryRun(b);
		}
		return this;
	}

	/**
	 * Sets the number of threads to be used with this benchmark.
	 * <b>NOTE:</b> this will stop and <b>wipe out</b> the current
	 * {@link ThreadDispatcher}, leaving dangling any tasks that were running
	 * in it. It is not recommended to call this method while tests are
	 * running.
	 * @param num_threads The number of threads this benchmark should use
	 * @return An instance of this benchmark
	 */
	public Benchmark setThreads(int num_threads)
	{
		m_dispatcherThread.interrupt();
		m_dispatcher = new ThreadDispatcher(num_threads);
		m_dispatcherThread = new Thread(m_dispatcher);
		m_dispatcherThread.start();
		return this;
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
	 * Merges the state of the benchmark to the contents of a JSON structure
	 * @param state The JSON structure
	 */
	public void deserializeState(JsonMap state)
	{
		deserializeState(state, false);
	}

	/**
	 * Sets the state of the benchmark to the contents of a JSON structure
	 * @param state The JSON structure
	 * @param merge Whether the JSON will overwrite the existing benchmark
	 *   (false) or be merged with it (true)
	 */
	public void deserializeState(JsonMap state, boolean merge)
	{
		Set<Test> new_tests = new HashSet<Test>();
		m_name = state.getString("name");
		JsonList test_list = (JsonList) state.get("tests");
		for (JsonElement el : test_list)
		{
			JsonMap el_test = (JsonMap) el;
			int test_id = el_test.getNumber("id").intValue();
			Test test_instance = findTestWithState(el_test);
			if (test_instance != null)
			{
				Test new_test = test_instance.newTest(test_id);
				new_test.deserializeState(el_test);
				new_tests.add(new_test);
			}
		}
		// Replaces the old set of tests with the ones created from the JSON
		// Note that we take care of changing the existing objects, so that
		// e.g. existing plots don't lose references to these tests
		for (Test t : new_tests)
		{
			Test old_t = getTestFromTest(t);
			if (old_t != null)
			{
				if (!merge || t.getStatus() == Test.Status.DONE)
				{
					// We take the content of the test in the file only
					// if its status is DONE, or if we overwrite everything
					old_t.mirror(t);					
				}
			}
		}
		if (!merge)
		{
			// We don't merge, so all tests that are not in the input
			// JSON will be deleted
			Iterator<Test> t_it = m_tests.iterator();
			while (t_it.hasNext())
			{
				Test t = t_it.next();
				if (!new_tests.contains(t))
				{
					t_it.remove();
				}
			}
		}
	}

	/**
	 * Retrieves a test with given ID
	 * @param test_id The test ID to look for
	 * @return The test instance, null if no test exists with
	 *   such ID
	 */
	protected Test getTest(int test_id)
	{
		Iterator<Test> it = m_tests.iterator();
		while (it.hasNext())
		{
			Test t = it.next();
			if (t.getId() == test_id)
			{
				return t;
			}
		}
		return null;
	}

	/**
	 * Cleans a test
	 * @param test_id The test ID to clean
	 * @return true if the test was found, false otherwise
	 */
	boolean cleanTest(int test_id)
	{
		Test t = getTest(test_id);
		if (t != null)
		{
			t.clean(t.getParameters());
			return true;
		}
		return false;
	}

	/**
	 * Resets a test
	 * @param test_id The test ID to reset
	 * @return true if the test was found, false otherwise
	 */
	public boolean resetTest(int test_id)
	{
		Test t = getTest(test_id);
		if (t != null)
		{
			t.reset();
			return true;
		}
		return false;
	}
	
	/**
	 * Resets a test
	 * @param test_id The test ID to reset
	 * @return true if the test was found, false otherwise
	 */
	public boolean resetTestState(int test_id)
	{
		Test t = getTest(test_id);
		if (t != null)
		{
			t.resetState();
			return true;
		}
		return false;
	}

	/**
	 * Serializes the status of the benchmark into a JSON structure
	 * @return The JSON structure
	 */
	public JsonMap serializeState()
	{
		JsonMap out = new JsonMap();
		out.put("name", m_name);
		out.put("version", Cli.s_versionString);
		JsonList list = new JsonList();
		Set<String> test_params = getTestParameterNames();
		JsonList param_list = new JsonList();
		for (String param_name : test_params)
		{
			param_list.add(param_name);
		}
		out.put("param-names", param_list);
		JsonList plot_list = new JsonList();
		for (Integer k : m_plots.keySet())
		{
			plot_list.add(k);
		}
		out.put("plots", plot_list);
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
	
	/**
	 * In the set of current tests, finds a test instance compatible
	 * with the parameter values present in the JSON
	 * @param state The JSON to analyze
	 * @return A compatible test instance, null if none found
	 */
	protected Test findTestWithState(JsonMap state)
	{
		for (Test t : m_classInstances)
		{
			if (t.isCompatible(state))
			{
				return t;
			}
		}
		return null;
	}

	/**
	 * Stops the benchmark
	 */
	public void stop()
	{
		m_dispatcher.stopAll();
		m_dispatcherThread.interrupt();
	}
	
	/**
	 * Retrieves a set of tests based on a set of parameters
	 * @param params The parameters
	 * @return The tests with the corresponding parameters, if
	 *   any
	 */
	public Collection<Test> getTests(Parameters params)
	{
		return getTests(m_tests, params);
	}

	static Map<String,Integer> fillStatusMap()
	{
		Map<String,Integer> out = new HashMap<String,Integer>();
		out.put("status-done", 0);
		out.put("status-failed", 0);
		out.put("status-prerequisites", 0);
		out.put("status-ready", 0);
		out.put("status-not-ready", 0);
		out.put("status-queued", 0);
		out.put("status-running", 0);
		out.put("status-impossible", 0);
		return out;
	}

	/**
	 * Adds a plot to the benchmark. Note that this does not associate
	 * any test to the plot; you have to do it by yourself using
	 * {@link Plot#addTest(Test)} or {@link Plot#addTests(Collection)}.
	 * @param plot The plot to add
	 * @return This benchmark
	 */
	public Benchmark addPlot(Plot plot)
	{
		int plot_id = s_plotCounter++; 
		plot.setId(plot_id);
		m_plots.put(plot_id, plot);
		return this;
	}
	
	/**
	 * Retrieves all the plots from the benchmark
	 * @return The plots
	 */
	public Collection<Plot> getAllPlots()
	{
		return m_plots.values();
	}

	public Plot getPlot(int plot_id)
	{
		return m_plots.get(plot_id);
	}

	static void putInStatusMap(Test t, Map<String,Integer> map)
	{
		if (!t.canRun(t.getParameters()))
		{
			map.put("status-impossible", map.get("status-impossible") + 1);
			return;
		}
		switch (t.getStatus())
		{
		case DONE:
			map.put("status-done", map.get("status-done") + 1);
			break;
		case FAILED:
			map.put("status-failed", map.get("status-failed") + 1);
			break;
		case NOT_DONE:
			if (t.prerequisitesFulilled(t.getParameters()))
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
		case PREREQUISITES:
			map.put("status-prerequisites", map.get("status-prerequisites") + 1);
			break;
		default:
			break;
		}
	}
	
	/**
	 * Gets the test instance in the benchmark with the same
	 * input parameters and the same name as the test passed as an
	 * argument
	 * @param t The test to look for
	 * @return A test instance, null if none found
	 */
	protected Test getTestFromTest(Test t)
	{
		Parameters t_p = t.getParameters();
		String t_name = t.getName();
		for (Test in_t : m_tests)
		{
			if (in_t.getName().compareTo(t_name) == 0 &&
					in_t.getParameters().equals(t_p))
			{
				return in_t;
			}
		}
		return null;
	}
}
