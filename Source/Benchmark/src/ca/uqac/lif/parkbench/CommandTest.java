package ca.uqac.lif.parkbench;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class CommandTest extends Test
{
	public String m_baseFolder;
	
	public CommandTest()
	{
		this("");
	}
	
	public CommandTest(String base_folder)
	{
		super();
		m_dryRun = false;
		m_baseFolder = base_folder;
	}

	@Override
	public void run()
	{
		setStatus(Status.RUNNING);
		List<String> command_list = new ArrayList<String>();
		createCommandFromParameters(getParameters(), command_list);
		String command[] = new String[command_list.size()];
		command = command_list.toArray(command);
		try
		{
			if (getDryRun() == false)
			{
				String output = CommandRunner.runCommand(command);
				createResultsFromOutput(output, getResults());
			}
			else
			{
				System.out.println("Dry run: would execute " + command_list);
			}
			setStatus(Status.DONE);
		}
		catch (IOException e)
		{
			setStatus(Status.FAILED);
		}
	}
	
	/**
	 * Creates the command to execute based on the test's parameters
	 * @param parameters The test's parameters
	 * @param command The command to execute
	 */
	protected abstract void createCommandFromParameters(Map<String,Object> parameters, List<String> command);
	
	/**
	 * Processes the output of the command and creates a set of
	 * result parameters from it
	 * @param output The output of the command
	 * @param results The set of result parameters
	 */
	protected abstract void createResultsFromOutput(String output, Map<String,Object> results);
}
