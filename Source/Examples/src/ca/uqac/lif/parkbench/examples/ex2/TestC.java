package ca.uqac.lif.parkbench.examples.ex2;

import java.io.IOException;
import java.util.Map;

import ca.uqac.lif.parkbench.CommandRunner;
import ca.uqac.lif.parkbench.CommandTestRegex;
import ca.uqac.lif.parkbench.Parameters;
import ca.uqac.lif.parkbench.Test;
import ca.uqac.lif.util.FileReadWrite;

/**
 * A simple test that calls a command on the command-line, passing
 * some of the test's parameters as command-line arguments.
 * This test has a prerequisite
 */
public class TestC extends CommandTestRegex
{
	public TestC()
	{
		super("D:/Workspaces/ParkBench/Source/MyTestSuite/command-c.bat <filename>");
		setParameter("name", "C");
	}
	
	@Override
	public boolean prerequisitesFulilled()
	{
		String filename = getPrerequisitesFilename();
		return CommandRunner.fileExists(filename);
	}
	
	@Override
	public void fulfillPrerequisites()
	{
		String filename = getPrerequisitesFilename();
		String file_contents = fillFile();
		try
		{
			FileReadWrite.writeToFile(filename, file_contents);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	protected void createResultsFromOutput(String output, Map<String,Object> results)
	{
		results.put("value", output.trim());
	}
	
	@Override
	public Test newTest()
	{
		return new TestC();
	}
	
	protected String getPrerequisitesFilename()
	{
		Parameters params = getParameters();
		Number n = params.getNumber("n");
		String filename = "input-for-command-c-" + n + ".txt";
		return filename;
	}
	
	protected String fillFile()
	{
		StringBuilder out = new StringBuilder();
		Parameters params = getParameters();
		Number n = params.getNumber("n");
		out.append(n);
		return out.toString();
	}
}
