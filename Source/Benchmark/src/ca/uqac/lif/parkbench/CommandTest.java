
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class CommandTest extends Test
{
	public String m_baseFolder;
	
	public CommandTest(String test_name)
	{
		this(test_name, "");
	}
	
	public CommandTest(String test_name, String base_folder)
	{
		super(test_name);
		m_dryRun = false;
		m_baseFolder = base_folder;
	}

	@Override
	public final void runTest(Parameters params, Parameters results)
	{
		List<String> command_list = new ArrayList<String>();
		createCommand(params, command_list);
		String command[] = new String[command_list.size()];
		command = command_list.toArray(command);
		try
		{
			if (getDryRun() == false)
			{
				String output = CommandRunner.runCommandString(command);
				readOutput(output, results);
			}
			else
			{
				System.out.println("Dry run: would execute " + command_list);
			}
			stopWithStatus(Status.DONE);
		}
		catch (IOException e)
		{
			stopWithStatus(Status.FAILED);
		}
	}
	
	/**
	 * Creates the command to execute based on the test's parameters
	 * @param parameters The test's parameters
	 * @param command The command to execute
	 */
	protected abstract void createCommand(Parameters parameters, List<String> command);
	
	/**
	 * Processes the output of the command and creates a set of
	 * result parameters from it
	 * @param output The output of the command
	 * @param results The set of result parameters
	 */
	protected abstract void readOutput(String output, Parameters results);
}
