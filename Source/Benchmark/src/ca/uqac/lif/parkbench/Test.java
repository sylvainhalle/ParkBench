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
	 * The status of the test
	 */
	public static enum Status {DONE, FAILED, RUNNING, NOT_DONE, QUEUED};
	
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
	protected static int s_idCounter = 0;
	
	/**
	 * A map from parameter names to values, to store the results
	 * of the execution of the test
	 */
	private Parameters m_results;
	
	/**
	 * Checks whether the test is done
	 */
	protected Status m_status;
	
	/**
	 * Name for this family of tests
	 */
	protected final String m_name;
	
	/**
	 * Unique ID for this test. This number is meaningless and is
	 * used only to interact with the GUI
	 */
	protected int m_id;
	
	/**
	 * The test's latest start time. This value only has a meaning
	 * if the test's status is not NOT_DONE or QUEUED.
	 */
	protected long m_startTime;
	
	/**
	 * The test's latest stop time. This value only has a meaning
	 * if the test's status is DONE or FAILED.
	 */
	protected long m_stopTime;
	
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
	 */
	public final void setParameter(String name, Object value)
	{
		m_parameters.put(name, value);
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
	
	protected final Parameters getParameters()
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
		return m_parameters.hashCode();
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
	 * @return true if the test is ready to run, false otherwise
	 */
	public boolean prerequisitesFulilled()
	{
		return true;
	}
	
	/**
	 * Fulfill the prerequisites for the test. This includes calling
	 * any additional commands, generating any files, etc. that the
	 * test will require when run.
	 */
	public void fulfillPrerequisites()
	{
		// Do nothing
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
		setStatus(Status.RUNNING);
		runTest(m_parameters, m_results);
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
		if (prerequisitesFulilled())
		{
			out.put("prerequisites", "true");
		}
		else
		{
			out.put("prerequisites", "false");
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
	 * Sets the state of the test to the contents of a JSON structure
	 * @param state The JSON structure
	 */
	public void deserializeState(JsonMap state)
	{
		JsonMap in_params = (JsonMap) state.get("input");
		m_startTime = state.getNumber("starttime").intValue();
		m_stopTime = state.getNumber("endtime").intValue();
		m_status = stringToStatus(state.getString("status"));
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
}
