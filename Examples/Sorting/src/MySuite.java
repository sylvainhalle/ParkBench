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
import ca.uqac.lif.parkbench.Experiment;
import ca.uqac.lif.parkbench.ExperimentSuite;
import ca.uqac.lif.parkbench.plot.PlanarPlot;
import ca.uqac.lif.parkbench.plot.ScatterPlot;

public class MySuite extends ExperimentSuite
{
	public static void main(String[] args)
	{
		new MySuite().initialize(args);
	}

	public void setup(Benchmark b)
	{
		// Give a name to the benchmark
		b.setName("Sorting Algorithms");

		// Initialize experiments
		Experiment[] experiments_to_create = {
				new QuickSort(),
				new ShellSort(),
				new BubbleSort(),
				new GnomeSort()
		};
		for (int length = 5000; length <= 40000; length += 5000)
		{
			for (Experiment t : experiments_to_create)
			{
				b.addExperiment(t.newExperiment().setParameter("size", length));
			}
		}

		// Add a plot
		PlanarPlot plot = new ScatterPlot("Sorting time")
			.withLines()
			.setParameterX("size", "List size")
			.setParameterY("time", "Time (ms)")
			.setLogscaleY();
		plot.addExperiments(b);
		b.addPlot(plot);
	}
}
