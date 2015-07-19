package ca.uqac.lif.parkbench;

import java.util.List;
import java.util.Map;

/**
 * Runs a test by running a command, created from a regular expression
 * involving test parameters.
 * 
 * For example, suppose the test has a parameter named "length", and that
 * one wishes to launch the command <tt>mycmd</tt>, passing the value of
 * "length" through the argument <tt>--len</tt>. One can instantiate
 * this with the pattern:
 * <pre>
 * mycmd --len &lt;length&gt;
 * </pre>
 * During execution, the string <tt>&lt;length&gt;</tt> is replaced by
 * the current value of the parameter "length" and the command is executed.
 * @author Sylvain
 *
 */
public abstract class CommandTestRegex extends CommandTest
{
	/**
	 * The pattern to build a command
	 */
	protected String m_commandPattern = "";

	/**
	 * Instantiates a command test
	 * @param command_pattern The pattern to build a command
	 */
	public CommandTestRegex(String test_name, String command_pattern)
	{
		super(test_name);
		m_commandPattern = command_pattern;
	}

	@Override
	protected void createCommandFromParameters(Map<String, Object> parameters,
			List<String> command)
	{
		String[] parts = m_commandPattern.split(" ");
		for (String part : parts)
		{
			if (!part.startsWith("<"))
			{
				command.add(part);
			}
			else
			{
				String part_name = part.substring(1, part.length() - 1);
				Object part_value = getParameter(part_name);
				command.add(part_value.toString());
			}
		}
	}


}
