package ca.uqac.lif.parkbench;

import ca.uqac.lif.cornipickle.json.JsonMap;
import ca.uqac.lif.httpserver.CallbackResponse;
import ca.uqac.lif.httpserver.RequestCallback;

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