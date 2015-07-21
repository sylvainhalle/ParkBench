package ca.uqac.lif.parkbench;

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
