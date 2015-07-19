package ca.uqac.lif.parkbench;

public class IntegerFormatter implements DataFormatter<Number>
{
	public IntegerFormatter()
	{
		super();
	}
	
	public String format(Number o)
	{
		Integer i = new Integer(o.intValue());
		return i.toString();
	}
}
