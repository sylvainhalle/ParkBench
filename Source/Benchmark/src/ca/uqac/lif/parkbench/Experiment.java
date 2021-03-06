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

import java.net.InetAddress;
import java.net.UnknownHostException;

import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonMap;
import ca.uqac.lif.json.JsonNumber;
import ca.uqac.lif.json.JsonString;

/**
 * An experiment is a set of named parameters
 * @author Sylvain Hallé
 */
public abstract class Experiment implements Runnable
{
	/**
	 * The status of the experiment. The meaning of each value is:
	 * <ul>
	 * <li><tt>NOT_DONE</tt>: the test has not started yet</li>
	 * <li><tt>QUEUED</tt>: the test is in the waiting queue</li>
	 * <li><tt>PREREQUISITES</tt>: the test has prerequisites that are in
	 *   the process of being generated</li>
	 * <li><tt>RUNNING</tt>: the test is currently running</li>
	 * <li><tt>DONE</tt>: the execution of the test has finished without error</li>
	 * <li><tt>FAILED</tt>: the execution of the test has failed, or was
	 *   manually cancelled</li>
	 * <li><tt>TIMEOUT</tt>: the execution of the test has exceeded its
	 *   maximal duration and was interrupted</li>
	 * </ul>
	 */
	public static enum Status {DONE, FAILED, RUNNING, NOT_DONE,
		QUEUED, PREREQUISITES, TIMEOUT};
	
	/**
	 * Determines if the experiment is to be executed for real, or
	 * just printed for debugging purposes.
	 */
	protected boolean m_dryRun;
	
	/**
	 * A map from parameter names to their values, which can be
	 * any Java object
	 */
	private Parameters m_parameters;
	
	/**
	 * A counter for tests
	 */
	private static int s_idCounter = 0;
	
	/**
	 * A map from parameter names to values, to store the results
	 * of the execution of the experiment
	 */
	private Parameters m_results;
	
	/**
	 * Checks whether the test is done
	 */
	private Status m_status;
	
	/**
	 * Name for this family of tests
	 */
	private final String m_name;
	
	/**
	 * A message explaining the failure of that test, if any
	 */
	private String m_failureMessage;
	
	/**
	 * A number of seconds after which the benchmark is allowed to
	 * interrupt this test. A value of 0 indicates the test should not
	 * be interrupted.
	 */
	private long m_killAfter;
	
	/**
	 * Unique ID for this test. This number is meaningless and is
	 * used only to interact with the GUI
	 */
	private int m_id;
	
	/**
	 * The test's latest start time. This value only has a meaning
	 * if the test's status is not NOT_DONE or QUEUED.
	 */
	private long m_startTime;
	
	/**
	 * The test's latest stop time. This value only has a meaning
	 * if the test's status is DONE or FAILED.
	 */
	private long m_stopTime;
	
	/**
	 * The host where this particular test was run
	 */
	private String m_host;
	
	/**
	 * Creates a new empty experiment
	 * @param name The name of the experiment
	 */
	public Experiment(String name)
	{
		super();
		m_name = name;
		m_id = s_idCounter++;
		m_parameters = new Parameters();
		m_results = new Parameters();
		m_status = Status.NOT_DONE;
		m_dryRun = false;
		m_startTime = 0;
		m_stopTime = 0;
		m_failureMessage = "";
		m_host = "";
		m_killAfter = 0;
	}
	
	Experiment(String name, int test_id)
	{
		super();
		m_name = name;
		m_id = test_id;
		s_idCounter = Math.max(s_idCounter, test_id + 1);
		m_parameters = new Parameters();
		m_results = new Parameters();
		m_status = Status.NOT_DONE;
		m_dryRun = false;
		m_startTime = 0;
		m_stopTime = 0;
		m_failureMessage = "";
		m_host = "";
		m_killAfter = 0;
	}
	
	/**
	 * Runs the experiment
	 * @param params The input parameters for this experiment. The
	 *  experiment <em>reads</em> data from this object.
	 * @param results The results of the experiment. The
	 *  experiment <em>writes</em> data to this object.
	 */
	public abstract void runExperiment(final Parameters params, Parameters results);
	
	/**
	 * Gets the test's name
	 * @return The name
	 */
	public String getName()
	{
		return m_name;
	}
	
	/**
	 * Sets the number of <em>seconds</em> after which the benchmark is allowed to
	 * interrupt this experimetn. A value of 0 indicates the experiment should not
	 * be interrupted.
	 * @param sec The number of seconds
	 * @return The test instance
	 */
	public final Experiment setKillTime(long sec)
	{
		m_killAfter = sec;
		return this;
	}
	
	/**
	 * Determines if an experiment can be interrupted by the benchmark
	 * @return true if can be interrupted, false otherwise
	 */
	public final boolean canKill()
	{
		if (m_status != Experiment.Status.RUNNING)
		{
			// Can't kill a test that is not running
			return false;
		}
		long cur_time = System.currentTimeMillis() / 1000;
		if (m_killAfter > 0 && cur_time - m_startTime > m_killAfter)
		{
			// Exp has run for long enough: can kill
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the failure message. This method returns a meaningful result
	 * only of the experiment has actually run and failed.
	 * @return The message
	 */
	public String getFailureMessage()
	{
		return m_failureMessage;
	}
	
	/**
	 * Sets a message explaining the failure of that experiment, if
	 * applicable
	 * @param message The message
	 * @return This test
	 */
	public Experiment setFailureMessage(String message)
	{
		m_failureMessage = message;
		return this;
	}
	
	/**
	 * Gets the host name that ran the experiment
	 * @return The host name, empty if the experiment was not
	 *   run yet
	 */
	public String getHost()
	{
		return m_host;
	}
	
	/**
	 * Gets the start time of the experiment
	 * <p>
	 * <b>NOTE:</b> {@link #getStartTime()} and {@link #getStopTime()}
	 * should not be used as precise measurements of a test's duration;
	 * they are only meant as "good enough" indications of how long an
	 * experiment has been running for displaying in the control panel.
	 * If you wish to measure a fine-grained running time for your
	 * experiment, you should measure it by yourself within the experiment's
	 * code.
	 * @return The start time, 0 if test not started
	 */
	public long getStartTime()
	{
		return m_startTime;
	}
	
	/**
	 * Gets the end time of the test
	 * <p>
	 * <b>NOTE:</b> {@link #getStartTime()} and {@link #getStopTime()}
	 * should not be used as precise measurements of a test's duration;
	 * they are only meant as "good enough" indications of how long an
	 * experiment has been running for displaying in the control panel.
	 * If you wish to measure a fine-grained running time for your
	 * experiment, you should measure it by yourself within the experiment's
	 * code.
	 * @return The end time, 0 if test not finished
	 */
	public long getStopTime()
	{
		return m_stopTime;
	}
	
	/**
	 * Determines if the experiment can run, given the set of parameters
	 * it is provided. <b>NOTE:</b> this must not be confused with
	 * whether an experiment has prerequisites that need to be fulfilled.
	 * The present method must return false when it is <em>impossible</em>
	 * to run the experiment with such input values.
	 * @param input The experiment,s input parameters
	 * @return True if the experiment can potentially run, false otherwise
	 */
	public boolean canRun(Parameters input)
	{
		return true;
	}

	/**
	 * Determines if the experiment is to be executed for real, or
	 * just printed for debugging purposes.
	 * @param b Set to true for a "dry run", false otherwise
	 */
	public final void setDryRun(boolean b)
	{
		m_dryRun = b;
	}
	
	/**
	 * Gets the dry run status of this experiment
	 * @see #setDryRun(boolean)   
	 * @return true for a "dry run", false otherwise
	 */
	public final boolean getDryRun()
	{
		return m_dryRun;
	}
	
	/**
	 * Sets the experiment's ID. The ID should not contain any meaningful
	 * information about the experiment itself; it is only used by the
	 * benchmark to uniquely identify every experiment instance.
	 * @param test_id The ID
	 */
	private void setId(int test_id)
	{
		m_id = test_id;
	}
	
	/**
	 * Sets the value of some parameter
	 * @param name The parameter name
	 * @param value The parameter value
	 * @return A reference to the experiment itself. This return value can be
	 *   ignored; it is there so that one can chain calls to setParameter in
	 *   the same line (e.g.: <tt>e.setParameter(...).setParameter(...)</tt>
	 */
	public final Experiment setParameter(String name, Object value)
	{
		m_parameters.put(name, value);
		return this;
	}
	
	/**
	 * Gets the value of some parameter
	 * @param name The parameter name
	 * @return The parameter value, or null if that parameter
	 *   does not exist
	 */
	public final Object getParameter(String name)
	{
		if (!m_parameters.containsKey(name))
		{
			return null;
		}
		return m_parameters.get(name);
	}
	
	/**
	 * Gets the set of all <strong>input</strong> parameters for an
	 * experiment
	 * @return The parameters
	 */
	public final Parameters getParameters()
	{
		return m_parameters;
	}
	
	/**
	 * Gets the integer value of some parameter
	 * @param name The parameter name
	 * @return The parameter value, cast as a number
	 */
	public final Number getParameterNumber(String name)
	{
		Object out = getParameter(name);
		if (out instanceof Number)
		{
			return (Number) getParameter(name);
		}
		return 0;
	}

	/**
	 * Gets the string value of some parameter
	 * @param name The parameter name
	 * @return The parameter value, cast as a string, or null
	 *   if the parameter does not exist
	 */
	public final String getParameterString(String name)
	{
		Object out = getParameter(name);
		if (out != null)
		{
			return getParameter(name).toString();
		}
		return null;
	}
	
	/**
	 * Checks if the experiment is done
	 * @return The status of the experiment
	 */
	public final Status getStatus()
	{
		return m_status;
	}
	
	/**
	 * Sets the experiment's status. This should be done carefully, as
	 * the benchmark uses this status to stop, discard, queue, or otherwise
	 * manage experiments in a suite.
	 * @param s The status
	 */
	public final void setStatus(Status s)
	{
		m_status = s;
	}
	
	/**
	 * Returns the test's unique ID
	 * @return The id
	 * @see #setId(int)
	 */
	public final int getId()
	{
		return m_id;
	}
	
	@Override
	public int hashCode()
	{
		return m_parameters.hashCode() + m_name.hashCode();
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o == null || !(o instanceof Experiment))
		{
			return false;
		}
		return equals((Experiment) o);
	}
	
	/**
	 * Checks if an experiment is equal to another experiment.
	 * This is the case when they have the same name, and exactly
	 * the same input parameters and values.
	 * @param t The experiment to compare with
	 * @return true if both experiments are identical, false otherwise
	 */
	protected boolean equals(Experiment t)
	{
		if (t.getName().compareTo(m_name) != 0)
		{
			return false;
		}
		Parameters other_params = t.m_parameters;
		boolean result = m_parameters.equals(other_params);
		return result;
	}
	
	/**
	 * Checks if an experiment matches a set of parameters. This happens
	 * when all parameters specified in the argument are also defined
	 * in the experiment and have the same value. Note that the experiment may
	 * have other parameters not specified in the argument; we don't
	 * care about these.
	 * @param parameters The map of parameters to look for
	 * @return true if the experiment matches these parameters,
	 *   false otherwise
	 */
	public boolean matches(Parameters parameters)
	{
		return m_parameters.match(parameters);
	}
	
	/**
	 * Checks whether the prerequisites for running this experiment
	 * (required files, etc.) are fulfilled. Override this method if your
	 * experiment has prerequisites.
	 * @param input The experiment's parameters
	 * @return true if the experiment is ready to run, false otherwise
	 */
	public boolean prerequisitesFulilled(final Parameters input)
	{
		return true;
	}
	
	/**
	 * Fulfill the prerequisites for the experiment. This includes calling
	 * any additional commands, generating any files, etc. that the
	 * test will require when run.
	 * @param input The experiment's parameters
	 * @return true if the prerequisites were correctly generated,
	 *   false otherwise
	 */
	public boolean fulfillPrerequisites(final Parameters input)
	{
		// Do nothing
		return true;
	}
	
	/**
	 * Gets the experiment's results
	 * @return The experiment's results
	 */
	public final Parameters getResults()
	{
		return m_results;
	}
	
	@Override
	public String toString()
	{
		StringBuilder out = new StringBuilder();
		out.append(m_parameters);
		out.append(m_results);
		return out.toString();
	}

	/**
	 * Runs the experiment.
	 */
	@Override
	public final void run()
	{
		try
		{
			// Set as the host for this test the current hostname
			m_host = InetAddress.getLocalHost().getHostAddress();
		} 
		catch (UnknownHostException e) 
		{
			// Or null if it fails
			m_host = null;
		}
		m_startTime = System.currentTimeMillis() / 1000;
		boolean prerequisites = true;
		if (!prerequisitesFulilled(m_parameters))
		{
			// Before running, generate the prerequisites
			setStatus(Status.PREREQUISITES);
			prerequisites = fulfillPrerequisites(m_parameters);
		}
		if (prerequisites)
		{
			setStatus(Status.RUNNING);
			runExperiment(m_parameters, m_results);
		}
		else
		{
			setFailureMessage(
					"Experiment cancelled while generating prerequisites");
			setStatus(Status.FAILED);
		}
	}

	/**
	 * Creates a new empty instance of the experiment
	 * @return A new experiment
	 */
	public abstract Experiment newExperiment();
	
	/**
	 * Creates a new empty instance of the experiment
	 * @param test_id The experiment ID to give this new experiment
	 * @return A new experiment
	 */
	public Experiment newExperiment(int test_id)
	{
		Experiment t = newExperiment();
		t.setId(test_id);
		s_idCounter = Math.max(s_idCounter, test_id + 1);
		return t;
	}
	
	/**
	 * Converts an experiment status into a string
	 * @param s The experiment status
	 * @return The string
	 */
	public static String statusToString(Status s)
	{
		String out = "";
		switch (s)
		{
		case DONE:
			out = "DONE";
			break;
		case FAILED:
			out = "FAILED";
			break;
		case PREREQUISITES:
			out = "PREREQUISITES";
			break;
		case RUNNING:
			out = "RUNNING";
			break;
		case NOT_DONE:
			out = "NOT_DONE";
			break;
		case QUEUED:
			out = "QUEUED";
			break;
		case TIMEOUT:
			out = "TIMEOUT";
			break;
		}
		return out;
	}
	
	/**
	 * Converts a string into an experiment status
	 * @param s The string
	 * @return The status
	 */
	public static Status stringToStatus(String s)
	{
		s = s.trim();
		if (s.compareToIgnoreCase("DONE") == 0)
		{
			return Status.DONE;
		}
		else if (s.compareToIgnoreCase("FAILED") == 0)
		{
			return Status.FAILED;
		}
		else if (s.compareToIgnoreCase("RUNNING") == 0)
		{
			return Status.RUNNING;
		}
		else if (s.compareToIgnoreCase("NOT_DONE") == 0)
		{
			return Status.NOT_DONE;
		}		
		else if (s.compareToIgnoreCase("QUEUED") == 0)
		{
			return Status.QUEUED;
		}
		else if (s.compareToIgnoreCase("PREREQUISITES") == 0)
		{
			return Status.PREREQUISITES;
		}
		else if (s.compareToIgnoreCase("TIMEOUT") == 0)
		{
			return Status.TIMEOUT;
		}
		return Status.NOT_DONE;
	}
	
	/**
	 * Saves the experiment's current state into a JSON object
	 * @return A JSON object with the test's state
	 */
	public JsonMap serializeState()
	{
		JsonMap out = new JsonMap();
		out.put("name", m_name);
		out.put("status", statusToString(m_status));
		out.put("id", m_id);
		out.put("starttime", m_startTime);
		out.put("endtime", m_stopTime);
		out.put("host", m_host);
		out.put("failure-message", m_failureMessage);
		if (prerequisitesFulilled(m_parameters))
		{
			out.put("prerequisites", "true");
		}
		else
		{
			out.put("prerequisites", "false");
		}
		if (canRun(m_parameters))
		{
			out.put("can-run", "true");
		}
		else
		{
			out.put("can-run", "false");
		}
		JsonMap in_params = new JsonMap();
		for (String param_name : m_parameters.keySet())
		{
			Object value = m_parameters.get(param_name);
			in_params.put(param_name, value);
		}
		out.put("input", in_params);
		JsonMap out_params = new JsonMap();
		for (String param_name : m_results.keySet())
		{
			Object value = m_results.get(param_name);
			out_params.put(param_name, value);
		}		
		out.put("results", out_params);
		return out;
	}
	
	/**
	 * Mirrors an existing experiment. This will make the current experiment
	 * instance copy all parameters, values and state of the test passed
	 * as parameter, <strong>except the ID and the name</strong>.
	 * @param t The experiment to mirror
	 * @return This experiment
	 */
	Experiment mirror(Experiment t)
	{
		m_parameters = t.m_parameters;
		m_results = t.m_results;
		m_startTime = t.m_startTime;
		m_stopTime = t.m_stopTime;
		m_status = t.m_status;
		m_failureMessage = t.m_failureMessage;
		return this;
	}
	
	/**
	 * Sets the state of the experiment to the contents of a JSON structure
	 * @param state The JSON structure
	 */
	public void deserializeState(JsonMap state)
	{
		JsonMap in_params = (JsonMap) state.get("input");
		JsonMap out_params = (JsonMap) state.get("results");
		m_startTime = state.getNumber("starttime").intValue();
		m_stopTime = state.getNumber("endtime").intValue();
		m_status = stringToStatus(state.getString("status"));
		if (m_status == Status.QUEUED || m_status == Status.RUNNING ||
				m_status == Status.PREREQUISITES)
		{
			// The test was running; put it back to "not-done"
			m_status = Status.NOT_DONE;
		}
		if (m_status == Status.DONE || m_status == Status.FAILED)
		{
			// We get the host name only if the test is finished
			if (state.containsKey("host"))
			{
				m_host = state.getString("host");
			}
		}
		m_failureMessage = state.getString("failure-message");
		for (String param_name : in_params.keySet())
		{
			JsonElement param_value = in_params.get(param_name);
			if (param_value instanceof JsonNumber)
			{
				m_parameters.put(param_name, ((JsonNumber) param_value).numberValue());
			}
			else if (param_value instanceof JsonString)
			{
				m_parameters.put(param_name, ((JsonString) param_value).stringValue());
			}
			else
			{
				m_parameters.put(param_name, param_value);
			}
		}
		for (String param_name : out_params.keySet())
		{
			JsonElement param_value = out_params.get(param_name);
			if (param_value instanceof JsonNumber)
			{
				m_results.put(param_name, ((JsonNumber) param_value).numberValue());
			}
			else if (param_value instanceof JsonString)
			{
				m_results.put(param_name, ((JsonString) param_value).stringValue());
			}
			else
			{
				m_results.put(param_name, param_value);
			}
		}
	}
	
	/**
	 * Checks if the current experiment is "compatible" with the parameters
	 * present in the JSON state passed as an argument. By default, an
	 * experiment is compatible if it has the same <code>name</code> value as
	 * the one given in the JSON; other test classes may override this method
	 * to provide a finer condition.
	 * @param state The parameters
	 * @return true if experiment is compatible, false otherwise
	 */
	protected boolean isCompatible(JsonMap state)
	{
		String state_name = state.getString("name");
		return state_name.compareTo(m_name) == 0;
	}
	
	/**
	 * Checks if the current experiment is "compatible" with the parameters
	 * present in the experiment passed as an argument.
	 * @param t The experiment
	 * @return true if experiment is compatible, false otherwise
	 * @see #isCompatible(JsonMap)
	 */
	protected boolean isCompatible(Experiment t)
	{
		return t.m_name.compareTo(m_name) == 0;
	}
	
	/**
	 * Cleans an experiment. This means removing any prerequisites
	 * the experiment may have.
	 * It is the responsibility of the test writer to make sure that
	 * {@link #clean(Parameters)} undoes the work done in
	 * {@link #fulfillPrerequisites(Parameters)}.
	 * <p>
	 * <b>NOTE:</b> it is probably not well advised to call this method while
	 * the experiment is running, but no check is done to that effect.
	 * <i>Caveat emptor</i>!
	 * <p>
	 * Other note: if other experiments in your experiment suite have
	 * the <em>same</em>
	 * prerequisites, they will be affected by this cleaning too. Again,
	 * beware!
	 * @param input The experiment's input parameters
	 */
	public void clean(Parameters input)
	{
		// Do nothing
	}
	
	/**
	 * Completely resets the the experiment. This means:
	 * <ul>
	 * <li>Putting it back to the <tt>NOT_DONE</tt> state</li>
	 * <li>Cleaning any prerequisites (through {@link #clean(Parameters)})</li>
	 * <li>Clearing any results the experiment has generated</li> 
	 * </ul>
	 */
	public final void reset()
	{
		setStatus(Status.NOT_DONE);
		clean(m_parameters);
		m_results.clear();
	}
	
	/**
	 * Resets the experiment's state only. This works like {@link #reset()},
	 * but without cleaning the prerequisites.
	 */
	public final void resetState()
	{
		setStatus(Status.NOT_DONE);
		m_results.clear();
	}
	
	/**
	 * Stops the experiment and gives it a status
	 * @param s The status of the experiment (normally <code>FAILED</code> or
	 * <code>DONE</code>)
	 */
	public void stopWithStatus(Status s)
	{
		m_stopTime = System.currentTimeMillis() / 1000;
		setStatus(s);
	}
	
	/**
	 * Waits for a number of seconds, doing nothing. If the experiment
	 * gets interrupted while waiting, it ends with the status
	 * <tt>FAILED</tt>.
	 * @param seconds The number of seconds to wait
	 */
	public void waitFor(float seconds)
	{
		try
		{
			Thread.sleep((long)(1000 * seconds));
		}
		catch (InterruptedException e) 
		{
			// This happens if the user cancels the test manually
			stopWithStatus(Status.FAILED);
			return;
		}

	}
}
