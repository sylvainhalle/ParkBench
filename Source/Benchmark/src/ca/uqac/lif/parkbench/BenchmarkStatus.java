package ca.uqac.lif.parkbench;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ca.uqac.lif.cornipickle.json.JsonList;
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

	@Override
	public CallbackResponse process(HttpExchange t)
	{
		JsonMap out = new JsonMap();
		JsonList list = new JsonList();
		Set<Test> tests = m_benchmark.getTests();
		Set<String> test_params = m_benchmark.getTestParameterNames();
		JsonList param_list = new JsonList();
		for (String param_name : test_params)
		{
			param_list.add(param_name);
		}
		out.put("param-names", param_list);
		Map<String,Integer> test_status = fillStatusMap();
		for (Test test : tests)
		{
			JsonMap m = test.serializeState();
			String status = Test.statusToString(test.getStatus());
			m.put("status", status);
			if (!test_status.containsKey(status))
			{
				test_status.put(status, 1);
			}
			putInStatusMap(test, test_status);
			list.add(m);
		}
		out.put("tests", list);
		JsonMap status_map = new JsonMap();
		for (String status : test_status.keySet())
		{
			status_map.put(status, test_status.get(status));
		}
		out.put("status", status_map);
		CallbackResponse response = new CallbackResponse(t);
		response.setContents(out.toString("", true));
		response.setContentType(CallbackResponse.ContentType.JSON);
		return response;
	}
	
	protected static Map<String,Integer> fillStatusMap()
	{
		Map<String,Integer> out = new HashMap<String,Integer>();
		out.put("status-done", 0);
		out.put("status-failed", 0);
		out.put("status-ready", 0);
		out.put("status-not-ready", 0);
		out.put("status-queued", 0);
		out.put("status-running", 0);
		return out;
	}
	
	protected static void putInStatusMap(Test t, Map<String,Integer> map)
	{
		switch (t.getStatus())
		{
		case DONE:
			map.put("status-done", map.get("status-done") + 1);
			break;
		case FAILED:
			map.put("status-failed", map.get("status-failed") + 1);
			break;
		case NOT_DONE:
			if (t.prerequisitesFulilled())
			{
				map.put("status-ready", map.get("status-ready") + 1);
			}
			else
			{
				map.put("status-not-ready", map.get("status-not-ready") + 1);
			}
			break;
		case RUNNING:
			map.put("status-running", map.get("status-running") + 1);
			break;
		case QUEUED:
			map.put("status-queued", map.get("status-queued") + 1);
			break;
		default:
			break;
		}
	}
	

}