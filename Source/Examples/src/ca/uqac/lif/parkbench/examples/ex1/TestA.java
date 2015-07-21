package ca.uqac.lif.parkbench.examples.ex1;

import ca.uqac.lif.parkbench.Parameters;
import ca.uqac.lif.parkbench.Test;

/**
 * A dummy test that simulates some processing.
 * This test takes numerical parameters <i>n</i> and <i>k</i>,
 * and sends as its output a single parameter <i>value</i>, which
 * is <i>n</i> x <i>k</i>. To simulate processing, an instance of TestA
 * waits that same number of seconds before declaring it has
 * finished. 
 */
public class TestA extends Test
{
	public TestA()
	{
		super("Test A");
	}
	
	@Override
	public Test newTest()
	{
		return new TestA();
	}

	@Override
	public void runTest(final Parameters params, Parameters results)
	{
		// Get the value of test parameters "k" and "n"
		int n = params.getNumber("n").intValue();
		int k = params.getNumber("k").intValue();
		// Multiply those values and put that as the result parameter "value"
		int out = n * k;
		results.put("value", out);
		// Sleep n * k seconds to simulate processing
		waitFor(out);
		// Don't forget to set the status to DONE when finished
		stopWithStatus(Status.DONE);
	}

}
