package ca.uqac.lif.parkbench.examples.ex1;

import ca.uqac.lif.parkbench.Parameters;
import ca.uqac.lif.parkbench.Test;

public class TestB extends Test
{
	public TestB()
	{
		super("B");
	}
	
	@Override
	public Test newTest()
	{
		return new TestB();
	}

	@Override
	public void runTest(final Parameters params, Parameters results)
	{
		// Get the value of test parameters "k" and "n"
		Number n = params.getNumber("n");
		Number k = params.getNumber("k");
		// Multiply those values and put that as the result parameter "value"
		Number out = n.floatValue() * k.floatValue();
		results.put("value", out);
		// Sleep n * k seconds to simulate processing
		try {
			Thread.sleep(1000 * n.intValue() * k.intValue());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Don't forget to set the status to DONE when finished
		stopWithStatus(Status.DONE);
	}

}
