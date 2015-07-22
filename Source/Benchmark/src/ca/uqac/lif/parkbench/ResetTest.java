package ca.uqac.lif.parkbench;

import java.util.Map;

import ca.uqac.lif.httpserver.CallbackResponse;
import ca.uqac.lif.httpserver.RequestCallback;

import com.sun.net.httpserver.HttpExchange;

public class ResetTest extends BenchmarkCallback
{

	public ResetTest(Benchmark b)
	{
		super(RequestCallback.Method.GET, "/reset", b);
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
				outcome &= m_benchmark.resetTest(test_id);
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