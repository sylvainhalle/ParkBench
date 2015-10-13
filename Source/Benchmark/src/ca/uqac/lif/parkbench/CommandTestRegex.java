
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
 */package ca.uqac.lif.parkbench;

import java.util.List;

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
	 * @param test_name The name of the test to instantiate
	 * @param command_pattern The pattern to build a command
	 */
	public CommandTestRegex(String test_name, String command_pattern)
	{
		super(test_name);
		m_commandPattern = command_pattern;
	}

	@Override
	protected void createCommand(Parameters parameters,
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
