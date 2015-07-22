/*
    ParkBench, a versatile benchmark environment
    Copyright (C) 2015 Sylvain Hallé

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
