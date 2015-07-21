package ca.uqac.lif.parkbench.examples.ex1;

import ca.uqac.lif.parkbench.Parameters;
import ca.uqac.lif.parkbench.Test;

/**
 * A dummy test that simulates some processing.
 * This test takes numerical parameters <i>n</i> and <i>k</i>,
 * and sends as its output a single parameter <i>value</i>, which
 * is <i>n</i> x <i>k</i> x 2. To simulate processing, an instance of TestB
 * waits that same number of seconds before declaring it has
 * finished.
 * <p>
 * In contrast with TestA, TestB also simulates having some prerequisites
 * to generate before starting. It waits <i>n</i> x <i>k</i> seconds
 * before starting the test itself. 
 */
public class TestB extends Test
{
	public TestB()
	{
		super("Test B");
	}
	
	@Override
	public Test newTest()
	{
		return new TestB();
	}
	
	@Override
	public boolean prerequisitesFulilled(final Parameters input)
	{
		return false;
	}
	
	@Override
	public void fulfillPrerequisites(final Parameters params)
	{
		// Get the value of test parameters "k" and "n"
		Number n = params.getNumber("n");
		Number k = params.getNumber("k");
		// Sleep n * k seconds to simulate processing
		try
		{
			Thread.sleep(1000 * n.intValue() * k.intValue() * 2);
		}
		catch (InterruptedException e) 
		{
			// This happens if the user cancels the test manually
			stopWithStatus(Status.FAILED);
			return;
		}
	}

	@Override
	public void runTest(final Parameters params, Parameters results)
	{
		// Get the value of test parameters "k" and "n"
		Number n = params.getNumber("n");
		Number k = params.getNumber("k");
		// Multiply those values and put that as the result parameter "value"
		Number out = n.floatValue() * k.floatValue() * 2;
		results.put("value", out);
		// Sleep n * k seconds to simulate processing
		try
		{
			Thread.sleep(1000 * n.intValue() * k.intValue() * 2);
		}
		catch (InterruptedException e) 
		{
			// This happens if the user cancels the test manually
			stopWithStatus(Status.FAILED);
			return;
		}
		// Don't forget to set the status to DONE when finished
		stopWithStatus(Status.DONE);
	}

}
