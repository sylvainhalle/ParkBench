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

import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonList;
import ca.uqac.lif.json.JsonMap;
import ca.uqac.lif.parkbench.plot.Plot;

/**
 * A benchmark controls the execution of a set of experiments
 * @author Sylvain
 *
 */
public class Benchmark
{
	/**
	 * The set of experiments managed by the benchmark
	 */
	protected Set<Experiment> m_tests;
	
	/**
	 * A subset of experiments managed by the benchmark. This set should
	 * contain one experiment instance per distinct class in the benchmark.
	 * It is used for deserializing tests from JSON documents. 
	 */
	protected Set<Experiment> m_classInstances;

	/**
	 * A name for this benchmark
	 */
	protected String m_name;

	/**
	 * The dispatcher for the threads for running the experiments
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

	/**
	 * A map containing all plots managed by this benchmark. Its keys
	 * are "meaningless" integers, and its values are the plots
	 * corresponding to each key. 
	 */
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
	 * @param num_threads The number of threads to allocate to this benchmark
	 *  (i.e. the number of experiments that are allowed to run
	 *  simultaneously)
	 */
	public Benchmark(int num_threads)
	{
		super();
		m_tests = new HashSet<Experiment>();
		m_classInstances = new HashSet<Experiment>();
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
	 * Add an experiment to the benchmark
	 * @param t The experiment to add
	 */
	public void addExperiment(Experiment t)
	{
		m_tests.add(t);
		addExperimentToClassInstances(t);
	}
	
	/**
	 * Conditionally adds an experiment instances to the set of test
	 * classes. The experiment will be added to the set only if there is
	 * no experiment of the same class already present.
	 * @param t The experiment to add
	 * @return true if the experiment was <em>not</em> added to the set,
	 *   false otherwise
	 */
	protected boolean addExperimentToClassInstances(Experiment t)
	{
		// Loop through class instances and add only if this class does
		// not exist
		boolean found = false;
		for (Experiment t_i : m_classInstances)
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
	 * Retrieves a set of experiments based on a set of parameters
	 * @param tests The collection of experiments to pick from
	 * @param parameters The parameters
	 * @return The experiments with the corresponding parameters, if
	 *   any
	 */
	public static final Set<Experiment> getExperiments(Collection<Experiment> tests, Parameters parameters)
	{
		Set<Experiment> out = new HashSet<Experiment>();
		for (Experiment t : tests)
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
	public Set<Experiment> getExperiments()
	{
		return m_tests;
	}

	/**
	 * Retrieves a single experiment based on a set of parameters. In case
	 * many experiments match the parameters, one is picked
	 * nondeterministically.
	 * @param tests The collection of experiments to pick from
	 * @param parameters The parameters
	 * @return The experiment with the corresponding parameters,
	 *   null if not found
	 */	
	public static final Experiment getExperiment(Collection<Experiment> tests, Parameters parameters)
	{
		Set<Experiment> out = getExperiments(tests, parameters);
		if (out.isEmpty())
		{
			return null;
		}
		for (Experiment t : out)
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
		Iterator<Experiment> it = m_tests.iterator();
		while (it.hasNext())
		{
			Experiment t = it.next();
			Parameters input = t.getParameters();
			if (override || !t.prerequisitesFulilled(input))
			{
				t.fulfillPrerequisites(input);
			}
		}
	}

	/**
	 * Retrieves the set of all parameter names contained in
	 * at least one experiment
	 * @return The set of parameter names
	 */
	public Set<String> getExperimentParameterNames()
	{
		Set<String> out = new HashSet<String>();
		for (Experiment t : m_tests)
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
	 * Sequentially runs all the experiments in the benchmark
	 */
	public void runAllExperiments()
	{
		Iterator<Experiment> it = m_tests.iterator();
		while (it.hasNext())
		{
			Experiment t = it.next();
			if (t.getStatus() == Experiment.Status.NOT_DONE)
			{
				t.run();
			}
		}
	}

	/**
	 * Queues all the experiments in the benchmark
	 */
	public void queueAllExperiments()
	{
		Iterator<Experiment> it = m_tests.iterator();

		while (it.hasNext())
		{
			Experiment t = it.next();
			if (t.canRun(t.getParameters()))
			{
				m_dispatcher.putInQueue(t);
			}
		}
	}

	/**
	 * Runs an experiment in the benchmark
	 * @param exp_id The id of the experiment to run
	 * @return true if an experiment with that ID exists, false otherwise
	 */
	public boolean runExperiment(int exp_id)
	{
		Experiment t = getExperiment(exp_id);
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
	 * Places an experiment in the waiting queue to be executed
	 * @param test_id The id of the experiment to run
	 * @return true if an experiment with that ID exists, false otherwise
	 */
	public boolean queueExperiment(int test_id)
	{
		Experiment t = getExperiment(test_id);
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
	 * Stops an experiment
	 * @param exp_id The ID of the experiment to stop
	 * @return true if the experiment exits (whether or not it needed
	 *   stopping), false otherwise
	 */
	public boolean stopExperiment(int exp_id)
	{
		return m_dispatcher.cancel(exp_id);		
	}

	/**
	 * Checks if all the experiments in the benchmark are done
	 * (either finished or interrupted)
	 * @return true if all experiments are finished, false otherwise
	 */
	public boolean isFinished()
	{
		return m_dispatcher.allDone();
	}

	/**
	 * Sets the dry run status of every experiment in the benchmark.
	 * See {@link Experiment#setDryRun(boolean)}.
	 * @param b The dry run status
	 * @return An instance of this benchmark
	 */
	public Benchmark setDryRun(boolean b)
	{
		for (Experiment t : m_tests)
		{
			t.setDryRun(b);
		}
		return this;
	}

	/**
	 * Sets the number of threads to be used with this benchmark.
	 * <b>NOTE:</b> this will stop and <b>wipe out</b> the current
	 * {@link ThreadDispatcher}, leaving dangling any tasks that were running
	 * in it. It is not recommended to call this method while experiments are
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
		for (Experiment t : m_tests)
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
		Set<Experiment> new_tests = new HashSet<Experiment>();
		m_name = state.getString("name");
		JsonList test_list = (JsonList) state.get("tests");
		for (JsonElement el : test_list)
		{
			JsonMap el_test = (JsonMap) el;
			int test_id = el_test.getNumber("id").intValue();
			Experiment test_instance = findExperimentWithState(el_test);
			if (test_instance != null)
			{
				Experiment new_test = test_instance.newExperiment(test_id);
				new_test.deserializeState(el_test);
				new_tests.add(new_test);
			}
		}
		// Replaces the old set of experiments with the ones created from the JSON
		// Note that we take care of changing the existing objects, so that
		// e.g. existing plots don't lose references to these experiments
		for (Experiment t : new_tests)
		{
			Experiment old_t = getExperimentFromExperiment(t);
			if (old_t != null)
			{
				if (!merge || t.getStatus() == Experiment.Status.DONE)
				{
					// We take the content of the experiment in the file only
					// if its status is DONE, or if we overwrite everything
					old_t.mirror(t);					
				}
			}
		}
		if (!merge)
		{
			// We don't merge, so all experiments that are not in the input
			// JSON will be deleted
			Iterator<Experiment> t_it = m_tests.iterator();
			while (t_it.hasNext())
			{
				Experiment t = t_it.next();
				if (!new_tests.contains(t))
				{
					t_it.remove();
				}
			}
		}
	}

	/**
	 * Retrieves an experiment with given ID
	 * @param exp_id The experiment ID to look for
	 * @return The experiment instance, null if none exists with
	 *   such ID
	 */
	protected Experiment getExperiment(int exp_id)
	{
		Iterator<Experiment> it = m_tests.iterator();
		while (it.hasNext())
		{
			Experiment t = it.next();
			if (t.getId() == exp_id)
			{
				return t;
			}
		}
		return null;
	}

	/**
	 * Cleans an experiment
	 * @param exp_id The experiment ID to clean
	 * @return true if the experiment was found, false otherwise
	 */
	boolean cleanExperiment(int exp_id)
	{
		Experiment t = getExperiment(exp_id);
		if (t != null)
		{
			t.clean(t.getParameters());
			return true;
		}
		return false;
	}

	/**
	 * Resets an experiment
	 * @param exp_id The experiment ID to reset
	 * @return true if the experiment was found, false otherwise
	 */
	public boolean resetExperiment(int exp_id)
	{
		Experiment t = getExperiment(exp_id);
		if (t != null)
		{
			t.reset();
			return true;
		}
		return false;
	}
	
	/**
	 * Resets an experiment
	 * @param exp_id The experiment ID to reset
	 * @return true if the experiment was found, false otherwise
	 */
	public boolean resetExperimentState(int exp_id)
	{
		Experiment t = getExperiment(exp_id);
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
		Set<String> test_params = getExperimentParameterNames();
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
		for (Experiment test : m_tests)
		{
			JsonMap m = test.serializeState();
			String status = Experiment.statusToString(test.getStatus());
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
	 * In the set of current experiments, finds one with the same name as
	 * the parameter. If there are many experiments with the same name,
	 * one is picked non-deterministically.
	 * @param name The experiment's name
	 * @return An experiment with the same name, null if none found
	 */
	protected Experiment findExperimentWithName(String name)
	{
		for (Experiment t : m_tests)
		{
			if (name.compareTo(t.getName()) == 0)
			{
				return t;
			}
		}
		return null;
	}
	
	/**
	 * In the set of current experiments, finds an instance compatible
	 * with the parameter values present in the JSON
	 * @param state The JSON to analyze
	 * @return A compatible experiment instance, null if none found
	 */
	protected Experiment findExperimentWithState(JsonMap state)
	{
		for (Experiment t : m_classInstances)
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
	 * Retrieves a set of experiments based on a set of parameters
	 * @param params The parameters
	 * @return The experiments with the corresponding parameters, if
	 *   any
	 */
	public Collection<Experiment> getExperiments(Parameters params)
	{
		return getExperiments(m_tests, params);
	}

	/**
	 * Fills a map with dummy values corresponding to the benchmark's
	 * status.
	 * @return A map
	 */
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
	 * {@link Plot#addExperiment(Experiment)} or {@link Plot#addExperiments(Collection)}.
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

	/**
	 * Retrieves a plot based on its ID. Note that this method does not check
	 * whether the plot with given ID actually exists in the benchmark.
	 * @param plot_id The plot ID
	 * @return The plot
	 */
	public Plot getPlot(int plot_id)
	{
		return m_plots.get(plot_id);
	}

	/**
	 * Fills a status map with the current state of an experiment
	 * @param t The experiment
	 * @param map The map to fill
	 */
	static void putInStatusMap(Experiment t, Map<String,Integer> map)
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
	 * Gets the experiment instance in the benchmark with the same
	 * input parameters and the same name as the experiment passed as an
	 * argument
	 * @param t The experiment to look for
	 * @return An experiment instance, null if none found
	 */
	protected Experiment getExperimentFromExperiment(Experiment t)
	{
		Parameters t_p = t.getParameters();
		String t_name = t.getName();
		for (Experiment in_t : m_tests)
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
