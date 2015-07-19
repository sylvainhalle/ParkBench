package ca.uqac.lif.parkbench;

import ca.uqac.lif.httpserver.RestCallback;

public abstract class BenchmarkCallback extends RestCallback
{
	protected Benchmark m_benchmark;
	
	public BenchmarkCallback(BenchmarkCallback.Method m, String path, Benchmark b)
	{
		super(m, path);
		m_benchmark = b;
	}
}
