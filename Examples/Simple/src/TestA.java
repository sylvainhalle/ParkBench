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
import ca.uqac.lif.parkbench.Test;

/**
 * A dummy test that simulates some processing.
 * This test takes numerical parameters <i>n</i> and <i>k</i>,
 * and sends as its output a single parameter <i>value</i>, which
 * is <i>n</i> x <i>k</i>. To simulate processing, an instance of TestA
 * waits that same number of seconds before declaring it has
 * finished. 
 */
public class TestA extends Test
{
	public TestA()
	{
		super("Test A");
	}
	
	@Override
	public Test newTest()
	{
		return new TestA();
	}

	@Override
	public void runTest(final Parameters params, Parameters results)
	{
		// Get the value of test parameters "k" and "n"
		int n = params.getNumber("n").intValue();
		int k = params.getNumber("k").intValue();
		// Multiply those values and put that as the result parameter "value"
		int out = n * k;
		results.put("value", out);
		// Sleep n * k seconds to simulate processing
		waitFor(out);
		// Don't forget to set the status to DONE when finished
		stopWithStatus(Status.DONE);
	}

}
