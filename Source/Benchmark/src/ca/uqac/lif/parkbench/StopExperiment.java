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

import java.util.Map;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.RequestCallback;

import com.sun.net.httpserver.HttpExchange;

public class StopExperiment extends BenchmarkCallback
{

	public StopExperiment(Benchmark b)
	{
		super(RequestCallback.Method.GET, "/stop", b);
	}

	@Override
	public CallbackResponse process(HttpExchange t)
	{
		Map<String,String> params = getParameters(t);
		String id_param = params.get("id");
		String[] test_ids = id_param.split(",");
		boolean outcome = true;
		for (String test_id_string : test_ids)
		{
			if (!test_id_string.isEmpty())
			{
				int test_id = Integer.parseInt(test_id_string);
				outcome &= m_benchmark.stopExperiment(test_id);
			}
		}
		CallbackResponse response = new CallbackResponse(t);
		if (outcome == false)
		{
			// Test does not exist
			response.setCode(CallbackResponse.HTTP_NOT_FOUND);
		}
		return response;
	}
}
