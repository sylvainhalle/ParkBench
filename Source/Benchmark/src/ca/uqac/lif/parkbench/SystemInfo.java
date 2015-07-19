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
		out.put("numcpu", System.getenv("NUMBER_OF_PROCESSORS"));
		CallbackResponse response = new CallbackResponse(t);
		response.setContents(out.toString("", true));
		response.setContentType(CallbackResponse.ContentType.JSON);
		return response;
	}
}