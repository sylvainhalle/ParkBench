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

public class MySuite extends ExperimentSuite
{
	public static void main(String[] args)
	{
		new MySuite().initialize(args);
	}
	
	@Override
	public void setup(Benchmark b)
	{
		Experiment[] tests_to_create = {
				new ExperimentA(),
				new ExperimentB()
		};
		for (int k = 1; k < 3; k++)
		{
			for (int n = 1; n < 4; n++)
			{
				for (Experiment t : tests_to_create)
				{
					Experiment new_t = t.newExperiment();
					new_t.setParameter("n", n);
					new_t.setParameter("k", k);
					b.addExperiment(new_t);
				}
			}
		}
	}
}
