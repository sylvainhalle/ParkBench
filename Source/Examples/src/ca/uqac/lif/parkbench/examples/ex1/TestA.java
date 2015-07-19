package ca.uqac.lif.parkbench.examples.ex1;

import java.util.Map;

import ca.uqac.lif.parkbench.CommandTestRegex;
import ca.uqac.lif.parkbench.Test;

/**
 * A simple test that calls a command on the command-line, passing
 * some of the test's parameters as command-line arguments.
 */
public class TestA extends CommandTestRegex
{
	public TestA()
	{
		super("A", "D:/Workspaces/ParkBench/Source/MyTestSuite/command-a.bat <n> <k>");
	}
	
	@Override
	protected void createResultsFromOutput(String output, Map<String,Object> results)
	{
		results.put("value", output.trim());
	}
	
	public Test newTest()
	{
		return new TestA();
	}
}
