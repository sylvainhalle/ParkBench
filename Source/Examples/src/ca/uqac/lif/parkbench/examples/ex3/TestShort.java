package ca.uqac.lif.parkbench.examples.ex3;

import ca.uqac.lif.parkbench.Parameters;
import ca.uqac.lif.parkbench.Test;

public class TestShort extends Test
{
	public TestShort()
	{
		super();
		setParameter("name", "Short");
	}
	
	@Override
	public Test newTest()
	{
		return new TestShort();
	}

	@Override
	public void run()
	{
		// Get the value of test parameter "n"
		Parameters params = getParameters();
		Number n = params.getNumber("n");
		Runtime rt = Runtime.getRuntime();
		// Put a dummy value into results
		Parameters results = getResults();
		results.put("value", 0);
		// Wait an amount of time proportional to n (to simulate processing)
		try {
			rt.wait(n.longValue());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Don't forget to set the status to DONE when finished
		setStatus(Status.DONE);
	}
}