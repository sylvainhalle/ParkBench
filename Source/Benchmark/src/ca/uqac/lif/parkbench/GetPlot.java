package ca.uqac.lif.parkbench;

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
		GnuPlot plot = m_benchmark.getPlot(0);
		byte[] image = plot.getImage();
		CallbackResponse response = new CallbackResponse(t);
		response.setContents(image);
		response.setContentType(CallbackResponse.ContentType.PNG);
		return response;
	}

}
