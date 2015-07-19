package ca.uqac.lif.parkbench.examples.ex1;

import ca.uqac.lif.parkbench.Parameters;
import ca.uqac.lif.parkbench.Test;

public class TestB extends Test
{
	public TestB()
	{
		super();
		setParameter("name", "B");
	}
	
	@Override
	public Test newTest()
	{
		return new TestB();
	}

	@Override
	public void run()
	{
		setStatus(Status.RUNNING);
		// Get the value of test parameters "k" and "n"
		Parameters params = getParameters();
		Number n = params.getNumber("n");
		Number k = params.getNumber("k");
		// Multiply those values and put that as the result parameter "value"
		Number out = n.floatValue() * k.floatValue();
		Parameters results = getResults();
		results.put("value", out);
		// Sleep n * k seconds to simulate processing
		try {
			Thread.sleep(1000 * n.intValue() * k.intValue());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Don't forget to set the status to DONE when finished
		setStatus(Status.DONE);
	}

}
