package ca.uqac.lif.parkbench.examples.ex2;

import ca.uqac.lif.parkbench.CommandTestRegex;
import ca.uqac.lif.parkbench.Parameters;
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
	protected void readOutput(String output, Parameters results)
	{
		results.put("value", output.trim());
	}
	
	public Test newTest()
	{
		return new TestA();
	}
}
