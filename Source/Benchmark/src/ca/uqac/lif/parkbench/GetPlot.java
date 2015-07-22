
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
 */package ca.uqac.lif.parkbench;

import java.util.Map;

import ca.uqac.lif.httpserver.CallbackResponse;
import ca.uqac.lif.httpserver.RequestCallback;
import ca.uqac.lif.parkbench.graph.GnuPlot;

import com.sun.net.httpserver.HttpExchange;

public class GetPlot extends BenchmarkCallback
{
	public GetPlot(Benchmark b)
	{
		super(RequestCallback.Method.GET, "/plot", b);
	}
	
	@Override
	public CallbackResponse process(HttpExchange t)
	{
		CallbackResponse response = new CallbackResponse(t);
		GnuPlot plot = m_benchmark.getPlot(0);
		Map<String,String> params = getParameters(t);
		String terminal = GnuPlot.getTerminalString(GnuPlot.DEFAULT_TERMINAL);
		if (params.containsKey("terminal"))
		{
			terminal = params.get("terminal");
		}
		if (params.containsKey("download") && params.get("download").compareToIgnoreCase("true") == 0)
		{
			// Download image
			response.setHeader("Content-Disposition", "attachment; filename=graph." + terminal);
		}
		byte[] image = plot.getImage(GnuPlot.stringToTerminal(terminal));
		response.setContents(image);
		//response.setContentType(CallbackResponse.ContentType.PNG);
		return response;
	}
}
