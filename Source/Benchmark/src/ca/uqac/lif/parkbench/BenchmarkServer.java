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

import ca.uqac.lif.httpserver.InnerFileServer;

public class BenchmarkServer extends InnerFileServer
{
	public BenchmarkServer(String server_name, int port, Benchmark b)
	{
		super(BenchmarkServer.class);
		setServerName(server_name);
		setServerPort(port);
		registerCallback(0, new BenchmarkStatus(b));
		registerCallback(0, new SystemInfo(b));
		registerCallback(0, new RunTest(b));
		registerCallback(0, new StopTest(b));
		registerCallback(0, new CleanTest(b));
		registerCallback(0, new GetPlot(b));
		registerCallback(0, new GetPlots(b));
		registerCallback(0, new SaveBenchmark(b));
		registerCallback(0, new FilterTests(b));
	}
}
