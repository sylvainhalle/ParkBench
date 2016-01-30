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
package ca.uqac.lif.parkbench;

import ca.uqac.lif.util.CliParser.Argument;
import ca.uqac.lif.util.CliParser.ArgumentMap;

public abstract class ExperimentSuite
{	
	public final void initialize(String[] args)
	{
		Cli cli = new Cli(args);
		Benchmark b = new Benchmark();
		cli.start(b, this);
	}
	
	/**
	 * Sets up the benchmark associated to this suite.
	 * This means creating experiments and graphs
	 * @param b The benchmark to add the tests and graphs to
	 */
	public abstract void setup(Benchmark b);
	
	/**
	 * Processes the arguments parsed from the command line. This is useful
	 * only if the suite has defined its own arguments in 
	 * {@link  #setupCommandLineArguments()}. 
	 * @param arguments The map of arguments values parsed from the
	 *   command line 
	 */
	public void readCommandLine(ArgumentMap arguments)
	{
		// Do nothing
		return;
	}
	
	/**
	 * Adds command-line parameters specific to this test suite. These
	 * parameters will be added to ParkBench's own command-line parameters.
	 * @return The command line arguments to be added
	 */
	public Argument[] setupCommandLineArguments()
	{
		return new Argument[0];
	}
}
