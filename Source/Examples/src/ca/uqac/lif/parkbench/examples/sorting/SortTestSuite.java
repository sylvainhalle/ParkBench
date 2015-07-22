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
package ca.uqac.lif.parkbench.examples.sorting;

import ca.uqac.lif.parkbench.Benchmark;
import ca.uqac.lif.parkbench.Test;
import ca.uqac.lif.parkbench.TestSuite;
import ca.uqac.lif.parkbench.graph.Scatterplot;

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
		for (int length = 5000; length <= 100000; length += 5000)
		{
			for (Test t : tests_to_create)
			{
				Test new_t = t.newTest();
				new_t.setParameter("size", length);
				b.addTest(new_t);
			}
		}
		// Prepare plot
		Scatterplot plot = new Scatterplot("Sorting time");
		plot.setPath("C:/Program Files/gnuplot/binary/gnuplot");
		plot.setParameterX("size");
		plot.setParameterY("time");
		b.addPlot(plot);		
	}

}
