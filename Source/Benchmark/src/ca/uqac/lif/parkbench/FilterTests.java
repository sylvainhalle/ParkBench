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

import java.util.Collection;

import ca.uqac.lif.cornipickle.json.JsonList;
import ca.uqac.lif.httpserver.CallbackResponse;
import ca.uqac.lif.httpserver.RequestCallback;

import com.sun.net.httpserver.HttpExchange;

public class FilterTests extends BenchmarkCallback
{

	public FilterTests(Benchmark b)
	{
		super(RequestCallback.Method.GET, "/filter", b);
	}

	@Override
	public CallbackResponse process(HttpExchange t)
	{
		CallbackResponse response = new CallbackResponse(t);
		String expression = t.getRequestURI().getQuery();
		String[] equalities = expression.split(",");
		Parameters p = new Parameters();
		String name_criterion = "";
		String status_criterion = "";
		for (String equality : equalities)
		{
			String[] parts = equality.split("=");
			String param_name = parts[0].trim();
			String param_value = parts[1].trim();
			if (param_value.startsWith("\""))
			{
				// Put a string
				String real_string = param_value.substring(1, param_value.length() - 1);
				if (param_name.compareTo("name") == 0)
				{
					name_criterion = real_string;
				}
				else if (param_name.compareTo("status") == 0)
				{
					status_criterion = real_string;
				}
				else
				{
					p.put(param_name, real_string);
				}
			}
			else
			{
				p.put(param_name, Float.parseFloat(param_value));
			}
		}
		Collection<Test> tests = m_benchmark.getTests(p);
		JsonList list = new JsonList();
		for (Test test : tests)
		{
			if (name_criterion.isEmpty() 
					|| name_criterion.compareTo(test.getName()) == 0)
			{
				// Test has the correct name
				if (status_criterion.isEmpty()
						|| status_criterion.compareToIgnoreCase(Test.statusToString(test.getStatus())) == 0)
				{
					// Test has the correct status
					list.add(test.getId());
				}
			}
		}
		response.setContents(list.toString());
		response.setContentType(CallbackResponse.ContentType.JSON);
		return response;
	}
	
	

}
