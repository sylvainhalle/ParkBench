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

import ca.uqac.lif.cornipickle.json.JsonElement;
import ca.uqac.lif.cornipickle.json.JsonMap;
import ca.uqac.lif.cornipickle.json.JsonNumber;
import ca.uqac.lif.cornipickle.json.JsonString;

/**
 * A test is a set of named parameters
 * @author Sylvain
 */
public abstract class Test implements Runnable
{
	/**
	 * The status of the test. The meaning of each value is:
	 * <ul>
	 * <li><tt>NOT_DONE</tt>: the test has not started yet</li>
	 * <li><tt>QUEUED</tt>: the test is in the waiting queue</li>
	 * <li><tt>PREREQUISITES</tt>: the test has prerequisites that are in
	 *   the process of being generated</li>
	 * <li><tt>RUNNING</tt>: the test is currently running</li>
	 * <li><tt>DONE</tt>: the execution of the test has finished without error</li>
	 * <li><tt>FAILED</tt>: the execution of the test has failed, or was
	 *   manually cancelled</li>
	 * </ul>
	 */
	public static enum Status {DONE, FAILED, RUNNING, NOT_DONE,
		QUEUED, PREREQUISITES};
	
	/**
	 * Determines if the test is to be executed for real, or
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
	 * of the execution of the test
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
	
	public Test(String name)
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
	}
	
	Test(String name, int test_id)
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
	}
	
	public abstract void runTest(final Parameters params, Parameters results);
	
	/**
	 * Gets the test's name
	 * @return The name
	 */
	public String getName()
	{
		return m_name;
	}
	
	/**
	 * Gets the failure message
	 * @return The message
	 */
	public String getFailureMessage()
	{
		return m_failureMessage;
	}
	
	/**
	 * Sets a message explaining the failure of that test, if any
	 * @param message The message
	 * @return This test
	 */
	public Test setFailureMessage(String message)
	{
		m_failureMessage = message;
		return this;
	}
	
	/**
	 * Determines if the test can run, given the set of parameters
	 * it is provided. <b>NOTE:</b> this must not be confused with
	 * whether a test has prerequisites that need to be fulfilled.
	 * The present method must return false when it is <em>impossible</em>
	 * to run the test with such input values.
	 * @param input The test's input parameters
	 * @return True if the test can potentially run, false otherwise
	 */
	public boolean canRun(Parameters input)
	{
		return true;
	}

	/**
	 * Determines if the test is to be executed for real, or
	 * just printed for debugging purposes.
	 * @param b Set to true for a "dry run", false otherwise
	 */
	public final void setDryRun(boolean b)
	{
		m_dryRun = b;
	}
	
	public final boolean getDryRun()
	{
		return m_dryRun;
	}
	
	/**
	 * Sets the test's ID
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
	 * @return A reference to the test itself. This return value can be
	 *   ignored; it is there so that one can chain calls to setParameter in
	 *   the same line (e.g.: <tt>test.setParameter(...).setParameter(...)</tt>
	 */
	public final Test setParameter(String name, Object value)
	{
		m_parameters.put(name, value);
		return this;
	}
	
	/**
	 * Get the value of some parameter
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
	
	public final Parameters getParameters()
	{
		return m_parameters;
	}
	
	/**
	 * Get the integer value of some parameter
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
	 * Get the integer value of some parameter
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
	 * Checks if the test is done
	 * @return True if the test is done, false otherwise
	 */
	public final Status getStatus()
	{
		return m_status;
	}
	
	/**
	 * Sets the tests's status
	 * @param s The status
	 */
	public final void setStatus(Status s)
	{
		m_status = s;
	}
	
	/**
	 * Returns the test's unique ID
	 * @return The id
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
		if (o == null || !(o instanceof Test))
		{
			return false;
		}
		return equals((Test) o);
	}
	
	protected boolean equals(Test t)
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
	 * Checks if a test matches a set of parameters. This happens
	 * when all parameters specified in the argument are also defined
	 * in the test and have the same value. Note that the test may
	 * have other parameters not specified in the argument; we don't
	 * care about these.
	 * @param parameters The map of parameters to look for
	 * @return true if the test matches these parameters, false otherwise
	 */
	public boolean matches(Parameters parameters)
	{
		return m_parameters.match(parameters);
	}
	
	/**
	 * Checks whether the prerequisites for running this test (required
	 * files, etc.) are fulfilled. Override this method if your test
	 * has prerequisites.
	 * @param input The test's parameters
	 * @return true if the test is ready to run, false otherwise
	 */
	public boolean prerequisitesFulilled(final Parameters input)
	{
		return true;
	}
	
	/**
	 * Fulfill the prerequisites for the test. This includes calling
	 * any additional commands, generating any files, etc. that the
	 * test will require when run.
	 * @param input The test's parameters
	 * @return true if the prerequisites were correctly generated,
	 *   false otherwise
	 */
	public boolean fulfillPrerequisites(final Parameters input)
	{
		// Do nothing
		return true;
	}
	
	/**
	 * Gets the test's results
	 * @return The test's results
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
	 * Runs the test
	 */
	@Override
	public final void run()
	{
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
			runTest(m_parameters, m_results);
		}
		else
		{
			setFailureMessage("Test cancelled while generating prerequisites");
			setStatus(Status.FAILED);
		}
	}

	/**
	 * Creates a new empty instance of the test
	 * @return
	 */
	public abstract Test newTest();
	
	/**
	 * Creates a new empty instance of the test
	 * @return
	 */
	public Test newTest(int test_id)
	{
		Test t = newTest();
		t.setId(test_id);
		s_idCounter = Math.max(s_idCounter, test_id + 1);
		return t;
	}
	
	/**
	 * Converts a test status into a string
	 * @param s The test status
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
		}
		return out;
	}
	
	/**
	 * Converts a string into a test status
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
		return Status.NOT_DONE;
	}
	
	/**
	 * Saves the test's current state into a JSON object
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
		out.put("failure-message", JsonString.escape(m_failureMessage));
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
	 * Mirrors an existing test. This will make the current test
	 * instance copy all parameters, values and state of the test passed
	 * as parameter, <strong>except the ID and the name</strong>.
	 * @param t The test to mirror
	 * @return This test
	 */
	Test mirror(Test t)
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
	 * Sets the state of the test to the contents of a JSON structure
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
	 * Cleans a test. This means removing any prerequisites the test may have.
	 * It is the responsibility of the test writer to make sure that {@link #clean()}
	 * undoes the work done in {@link #fulfillPrerequisites(Parameters)}.
	 * <p>
	 * <b>NOTE:</b> it is probably not well advised to call this method while
	 * the test is running, but no check is done to that effect. <i>Caveat
	 * emptor</i>!
	 * <p>
	 * Other note: if other tests in your test suite have the <em>same</em>
	 * prerequisites, they will be affected by this cleaning too. Again,
	 * beware!
	 * @param input The test's input parameters
	 */
	public void clean(Parameters input)
	{
		// Do nothing
	}
	
	/**
	 * Resets the test's state. This means:
	 * <ul>
	 * <li>Putting the test back to the <tt>NOT_DONE</tt> state</li>
	 * <li>Cleaning any prerequisites (through {@link #clean()})</li>
	 * <li>Clearing any results the test has generated</li> 
	 * </ul>
	 */
	public final void reset()
	{
		setStatus(Status.NOT_DONE);
		clean(m_parameters);
		m_results.clear();
	}
	
	/**
	 * Stops the tests and gives it a status
	 * @param s The status of the test (normally FAILED or DONE)
	 */
	public void stopWithStatus(Status s)
	{
		m_stopTime = System.currentTimeMillis() / 1000;
		setStatus(s);
	}
	
	/**
	 * Waits for a number of seconds, doing nothing. If the test
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
