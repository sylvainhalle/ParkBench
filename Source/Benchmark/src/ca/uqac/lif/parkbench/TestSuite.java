package ca.uqac.lif.parkbench;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class TestSuite
{
	public static void initialize(String[] args, TestSuite reference)
	{
		Cli cli = new Cli(args);
		Benchmark b = new Benchmark();
		Method m;
		try {
			Class<?> reference_class = reference.getClass();
			Class<?>[] c_arg = new Class[1];
			c_arg[0] = Benchmark.class;
			m = reference_class.getDeclaredMethod("setup", c_arg);
			m.invoke(reference, b);
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cli.start(b);		
	}
	
	public abstract void setup(Benchmark b);
}
