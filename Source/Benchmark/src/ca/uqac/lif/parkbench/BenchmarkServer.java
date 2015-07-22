package ca.uqac.lif.parkbench;

import ca.uqac.lif.httpserver.InnerFileServer;

public class BenchmarkServer extends InnerFileServer
{
	public BenchmarkServer(String server_name, int port, Benchmark b)
	{
		super(BenchmarkServer.class);
		setServerName(server_name);
		setServerPort(port);
		registerCallback(0, new BenchmarkStatus(b));
		registerCallback(0, new SystemInfo(b));
		registerCallback(0, new RunTest(b));
		registerCallback(0, new StopTest(b));
		registerCallback(0, new ResetTest(b));
		registerCallback(0, new GetPlot(b));
		registerCallback(0, new SaveBenchmark(b));
	}
}
