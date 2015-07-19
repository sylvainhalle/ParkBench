package ca.uqac.lif.parkbench;

import ca.uqac.lif.httpserver.CallbackResponse;

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
		String filename = m_benchmark.getName();
		response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".json");
		return response;
	}	
}