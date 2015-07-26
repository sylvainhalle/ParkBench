
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
import ca.uqac.lif.httpserver.CallbackResponse.ContentType;
import ca.uqac.lif.httpserver.RequestCallback;
import ca.uqac.lif.httpserver.Server;
import ca.uqac.lif.parkbench.plot.Plot;

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
		Map<String,String> params = getParameters(t);
		String terminal = Plot.getTerminalString(Plot.DEFAULT_TERMINAL);
		if (!params.containsKey("id"))
		{
			// Bad request: should always contain an ID
			response.setCode(CallbackResponse.HTTP_BAD_REQUEST);
			return response;
		}
		int plot_id = Integer.parseInt(params.get("id"));
		Plot plot = m_benchmark.getPlot(plot_id);
		if (params.containsKey("terminal"))
		{
			terminal = params.get("terminal");
		}
		if (params.containsKey("raw"))
		{
			// Send Gnuplot data
			String filename = Server.urlEncode(plot.getName()) + ".gp";
			response.setAttachment(filename);
			response.setContents(plot.toGnuPlot(Plot.stringToTerminal(terminal)));
		}
		else
		{
			// Produce image
			response.setContentType(terminalToContentType(terminal));
			if (params.containsKey("download") && params.get("download").compareToIgnoreCase("true") == 0)
			{
				String filename = Server.urlEncode(plot.getName()) + "." + terminal;
				response.setAttachment(filename);
			}
			byte[] image = plot.getImage(Plot.stringToTerminal(terminal));
			response.setContents(image);
		}			
		return response;
	}
	
	/**
	 * Returns the appropriate MIME content type based on the Gnuplot terminal
	 * @param terminal The terminal
	 * @return The content type
	 */
	protected static ContentType terminalToContentType(String terminal)
	{
		if (terminal.compareToIgnoreCase("jpg") == 0)
		{
			return ContentType.JPEG;
		}
		return ContentType.PNG;
	}
}
