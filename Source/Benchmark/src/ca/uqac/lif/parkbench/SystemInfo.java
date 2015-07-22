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

import ca.uqac.lif.cornipickle.json.JsonMap;
import ca.uqac.lif.httpserver.CallbackResponse;
import ca.uqac.lif.httpserver.RequestCallback;

import com.sun.net.httpserver.HttpExchange;

public class SystemInfo extends BenchmarkCallback
{

	public SystemInfo(Benchmark b)
	{
		super(RequestCallback.Method.GET, "/info", b);
	}

	@Override
	public CallbackResponse process(HttpExchange t)
	{
		JsonMap out = new JsonMap();
		out.put("threads", m_benchmark.threadCount());
		out.put("osname", System.getProperty("os.name"));
		out.put("osarch", System.getProperty("os.arch"));
		out.put("osversion", System.getProperty("os.version"));
		//out.put("numcpu", System.getenv("NUMBER_OF_PROCESSORS")); // Windows only
		out.put("numcpu", Runtime.getRuntime().availableProcessors());
		CallbackResponse response = new CallbackResponse(t);
		response.setContents(out.toString("", true));
		response.setContentType(CallbackResponse.ContentType.JSON);
		return response;
	}
}