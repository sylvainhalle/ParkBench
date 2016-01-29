/*
    ParkBench, a versatile benchmark environment
    Copyright (C) 2015 Sylvain Hallé

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


import ca.uqac.lif.parkbench.Benchmark;
import ca.uqac.lif.parkbench.Test;
import ca.uqac.lif.parkbench.TestSuite;
import ca.uqac.lif.parkbench.plot.PlanarPlot;
import ca.uqac.lif.parkbench.plot.ScatterPlot;

public class MyTestSuite extends TestSuite
{
	public static void main(String[] args)
	{
		initialize(args, new MyTestSuite());
	}

	public void setup(Benchmark b)
	{
		// Give a name to the benchmark
		b.setName("Sorting Algorithms");

		// Initialize tests
		Test[] tests_to_create = {
				new QuickSortTest(),
				new ShellSortTest(),
				new BubbleSortTest(),
				new GnomeSortTest()
		};
		for (int length = 5000; length <= 40000; length += 5000)
		{
			for (Test t : tests_to_create)
			{
				b.addTest(t.newTest().setParameter("size", length));
			}
		}

		// Add a plot
		PlanarPlot plot = new ScatterPlot("Sorting time")
			.withLines()
			.setParameterX("size", "List size")
			.setParameterY("time", "Time (ms)")
			.setLogscaleY();
		plot.addTests(b);
		b.addPlot(plot);
	}
}
