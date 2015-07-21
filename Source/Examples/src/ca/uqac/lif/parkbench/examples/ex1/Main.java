package ca.uqac.lif.parkbench.examples.ex1;

import ca.uqac.lif.parkbench.Benchmark;
import ca.uqac.lif.parkbench.Test;
import ca.uqac.lif.parkbench.TestSuite;

public class Main extends TestSuite
{
	public static void main(String[] args)
	{
		initialize(args, new Main());
	}
	
	public void setup(Benchmark b)
	{
		Test[] tests_to_create = {
				new TestA(),
				new TestB()
		};
		for (int k = 1; k < 3; k++)
		{
			for (int n = 1; n < 4; n++)
			{
				for (Test t : tests_to_create)
				{
					Test new_t = t.newTest();
					new_t.setParameter("n", n);
					new_t.setParameter("k", k);
					b.addTest(new_t);
				}
			}
		}
	}
}
