package ca.uqac.lif.parkbench.examples.ex1;

import ca.uqac.lif.parkbench.Benchmark;
import ca.uqac.lif.parkbench.Cli;
import ca.uqac.lif.parkbench.Test;

public class Main
{

	public static void main(String[] args)
	{
		Cli cli = new Cli(args);
		Benchmark b = setupBenchmark();
		cli.start(b);
	}
	
	protected static Benchmark setupBenchmark()
	{
		Benchmark benchmark = new Benchmark(3);
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
					benchmark.addTest(new_t);
				}
			}
		}
		return benchmark;
	}
}
