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


import ca.uqac.lif.parkbench.Parameters;
import ca.uqac.lif.parkbench.Experiment;

/**
 * A dummy test that simulates some processing.
 * This test takes numerical parameters <i>n</i> and <i>k</i>,
 * and sends as its output a single parameter <i>value</i>, which
 * is <i>n</i> x <i>k</i> x 2. To simulate processing, an instance of TestB
 * waits that same number of seconds before declaring it has
 * finished.
 * <p>
 * In contrast with TestA, TestB also simulates having some prerequisites
 * to generate before starting. It waits <i>n</i> x <i>k</i> seconds
 * before starting the test itself. 
 */
public class ExperimentB extends Experiment
{
	public ExperimentB()
	{
		super("Test B");
	}
	
	@Override
	public Experiment newExperiment()
	{
		return new ExperimentB();
	}
	
	@Override
	public boolean prerequisitesFulilled(final Parameters input)
	{
		return false;
	}
	
	@Override
	public boolean fulfillPrerequisites(final Parameters params)
	{
		// Get the value of test parameters "k" and "n"
		Number n = params.getNumber("n");
		Number k = params.getNumber("k");
		// Sleep n * k seconds to simulate processing
		try
		{
			Thread.sleep(1000 * n.intValue() * k.intValue() * 2);
		}
		catch (InterruptedException e) 
		{
			// This happens if the user cancels the test manually
			stopWithStatus(Status.FAILED);
			return false;
		}
		return true;
	}

	@Override
	public void runExperiment(final Parameters params, Parameters results)
	{
		// Get the value of test parameters "k" and "n"
		Number n = params.getNumber("n");
		Number k = params.getNumber("k");
		// Multiply those values and put that as the result parameter "value"
		Number out = n.floatValue() * k.floatValue() * 2;
		results.put("value", out);
		// Sleep n * k seconds to simulate processing
		try
		{
			Thread.sleep(1000 * n.intValue() * k.intValue() * 2);
		}
		catch (InterruptedException e) 
		{
			// This happens if the user cancels the test manually
			stopWithStatus(Status.FAILED);
			return;
		}
		// Don't forget to set the status to DONE when finished
		stopWithStatus(Status.DONE);
	}

}
