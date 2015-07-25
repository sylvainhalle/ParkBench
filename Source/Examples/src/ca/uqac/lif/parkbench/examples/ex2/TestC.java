package ca.uqac.lif.parkbench.examples.ex2;

import java.io.IOException;

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
		super("C", "D:/Workspaces/ParkBench/Source/MyTestSuite/command-c.bat <filename>");
	}
	
	@Override
	public boolean prerequisitesFulilled(Parameters input)
	{
		String filename = getPrerequisitesFilename();
		return CommandRunner.fileExists(filename);
	}
	
	@Override
	public boolean fulfillPrerequisites(Parameters params)
	{
		String filename = getPrerequisitesFilename();
		String file_contents = fillFile();
		try
		{
			FileReadWrite.writeToFile(filename, file_contents);
		}
		catch (IOException e)
		{
			return false;
		}
		return true;
	}
	
	@Override
	protected void readOutput(String output, Parameters results)
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
