package ca.uqac.lif.parkbench.examples.ex3;

import ca.uqac.lif.parkbench.Benchmark;
import ca.uqac.lif.parkbench.Scatterplot;
import ca.uqac.lif.parkbench.Test;

public class TestSuite
{

	public static void main(String[] args)
	{
		Test[] tests_to_create = {
				new TestShort()
		};
		Benchmark benchmark = new Benchmark();
		for (int n = 0; n < 4; n++)
		{
			for (Test t : tests_to_create)
			{
				Test new_t = t.newTest();
				new_t.setParameter("n", n);
				benchmark.addTest(new_t);
			}
		}

		benchmark.setDryRun(false);
		//benchmark.allocateThreads(4);
		benchmark.runAllTests();
		System.out.println(benchmark);
		Scatterplot plot = new Scatterplot("Title");
		plot.setParameterX("n");
		plot.setParameterY("value");
		plot.addTests(benchmark);
		String plot_file = plot.toGnuPlot();
		System.out.println(plot_file);
	}

}
