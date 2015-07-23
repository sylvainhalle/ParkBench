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

import ca.uqac.lif.httpserver.CallbackResponse;
import ca.uqac.lif.httpserver.Server;

import com.sun.net.httpserver.HttpExchange;

public class SaveBenchmark extends BenchmarkStatus
{
	public SaveBenchmark(Benchmark b)
	{
		super(b, "/save");
	}

	@Override
	public CallbackResponse process(HttpExchange t)
	{
		CallbackResponse response = super.process(t);
		// Tell the browser to download the document rather than display it
		String filename = Server.urlEncode(m_benchmark.getName()) + ".json";
		response.setAttachment(filename);
		return response;
	}	
}