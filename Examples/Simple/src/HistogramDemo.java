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
import ca.uqac.lif.parkbench.plot.ClusteredHistogram;

public class HistogramDemo extends ExperimentSuite
{
	public static void main(String[] args)
	{
		new HistogramDemo().initialize(args);
	}
	
	public void setup(Benchmark b)
	{
		ClusteredHistogram histogram = new ClusteredHistogram("Histogram demo");
		histogram.setParameterX("k").setParameterY("value");
		for (int k = 1; k <= 4; k++)
		{
			{
				Experiment t = new ExperimentA().setParameter("n", 1).setParameter("k", k);
				b.addExperiment(t);
				histogram.addExperiment(t);
			}
			{
				Experiment t = new ExperimentB().setParameter("n", 1).setParameter("k", k);
				b.addExperiment(t);
				histogram.addExperiment(t);
			}			
		}
		b.addPlot(histogram);
	}	
}
