package ca.uqac.lif.parkbench.examples.sorting;

import ca.uqac.lif.parkbench.Benchmark;
import ca.uqac.lif.parkbench.Test;
import ca.uqac.lif.parkbench.TestSuite;

public class SortTestSuite extends TestSuite
{
	public static void main(String[] args)
	{
		initialize(args, new SortTestSuite());
	}

	public void setup(Benchmark b)
	{
		Test[] tests_to_create = {
				new QuickSortTest(),
				new BubbleSortTest()
		};
		for (int length = 1; length < 10000; length += 2000)
		{
			for (Test t : tests_to_create)
			{
				Test new_t = t.newTest();
				new_t.setParameter("size", length);
				b.addTest(new_t);
			}
		}
	}

}
