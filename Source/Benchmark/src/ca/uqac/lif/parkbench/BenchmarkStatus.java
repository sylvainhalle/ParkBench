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

import ca.uqac.lif.json.JsonMap;
import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.RequestCallback;

import com.sun.net.httpserver.HttpExchange;

public class BenchmarkStatus extends BenchmarkCallback
{

	public BenchmarkStatus(Benchmark b)
	{
		super(RequestCallback.Method.GET, "/status", b);
	}
	
	BenchmarkStatus(Benchmark b, String method_name)
	{
		super(RequestCallback.Method.GET, method_name, b);
	}

	@Override
	public CallbackResponse process(HttpExchange t)
	{
		JsonMap out = m_benchmark.serializeState();
		CallbackResponse response = new CallbackResponse(t);
		response.setContents(out.toString("", true));
		response.setContentType(CallbackResponse.ContentType.JSON);
		return response;
	}

}