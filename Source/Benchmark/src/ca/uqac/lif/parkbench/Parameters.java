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
 */package ca.uqac.lif.parkbench;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * A named set of input or output data for a test
 * @author Sylvain Hallé
 */
public class Parameters extends HashMap<String,Object>
{
	/**
	 * Dummy UID
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates an empty set of parameters
	 */
	public Parameters()
	{
		super();
	}
	
	/**
	 * Creates a set of parameters by copying the contents of another
	 * set
	 * @param params The parameters to copy
	 */
	public Parameters(Parameters params)
	{
		super();
		putAll(params);
	}
	
	/**
	 * Gets the set of all distinct test parameters other than those
	 * in some set
	 * @param tests The set of tests
	 * @param to_exclude The set of parameter names to ignore
	 * @return The set of distinct parameters
	 */
	public static Set<Parameters> getSets(Collection<Test> tests, Collection<String> to_exclude)
	{
		Set<Parameters> out = new HashSet<Parameters>();
		for (Test t : tests)
		{
			Parameters params = new Parameters(t.getParameters());
			params.putAll(t.getParameters());
			params.removeAll(to_exclude);
			out.add(params);
		}
		return out;
	}
	
	/**
	 * Group all test parameters according to the values of some of their
	 * parameters
	 * @param tests The set of tests
	 * @param criteria The parameter names. All tests with the same values for
	 *   these keys will be put in the same group
	 * @return A map from values of parameters in <tt>criteria</tt> to sets of
	 *   parameters having these values
	 */
	public static Map<Parameters,Set<Parameters>> groupBy(Collection<Test> tests, Collection<String> criteria)
	{
		Map<Parameters,Set<Parameters>> out = new HashMap<Parameters,Set<Parameters>>();
		for (Test t : tests)
		{
			Parameters params = new Parameters(t.getParameters());
			params.keepOnly(criteria);
			if (!out.containsKey(params))
			{
				Set<Parameters> new_value = new HashSet<Parameters>();
				out.put(params, new_value);
			}
			Set<Parameters> p_set = out.get(params);
			p_set.add(t.getParameters());
			out.put(params, p_set);
		}
		return out;
	}
	
	/**
	 * Removes all keys specified in col
	 * @param col The keys to remove
	 */
	public void removeAll(Collection<String> col)
	{
		for (String s : col)
		{
			remove(s);
		}
	}
	
	/**
	 * Removes all keys, except those specified in col 
	 * @param col The keys to keep
	 */
	public void keepOnly(Collection<String> col)
	{
		Parameters new_params = new Parameters();
		for (String k : col)
		{
			if (containsKey(k))
			{
				Object o = get(k);
				new_params.put(k, o);				
			}
		}
		clear();
		putAll(new_params);
	}
	
	@Override
	public int hashCode()
	{
		int out = 0;
		for (Object o : keySet())
		{
			out += o.hashCode();
		}
		return out;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o == null || !(o instanceof Parameters))
		{
			return false;
		}
		return equals((Parameters) o);
	}
	
	protected boolean equals(Parameters params)
	{
		if (params == null)
		{
			return false;
		}
		if (params.size() != size())
		{
			return false;
		}
		return params.match(this) && this.match(params);
	}
	
	/**
	 * Checks if a test matches a set of parameters. This happens
	 * when all parameters specified in the argument are also defined
	 * in the present set and have the same value. Note that the set may
	 * have other parameters not specified in the argument; we don't
	 * care about these.
	 * @param parameters The map of parameters to look for
	 * @return true if the test matches these parameters, false otherwise
	 */
	public boolean match(Parameters parameters)
	{
		for (String k : parameters.keySet())
		{
			if (!containsKey(k))
			{
				return false;
			}
			Object o1 = parameters.get(k);
			Object o2 = get(k);
			if ((o1 == null && o2 != null) || (o1 != null && o2 == null))
			{
				return false;
			}
			if (o1 != null && o2 != null)
			{
				// We must do a rather tedious comparison here, as e.g.
				// two instances of numbers n1 and n2 may have the same
				// value, yet not have n1.equals(n2). :-(
				if (o1 instanceof Number && o2 instanceof Number)
				{
					if (((Number) o1).floatValue() != ((Number) o2).floatValue())
					{
						return false;
					}
				}
				else if (o1 instanceof String && o2 instanceof String)
				{
					if (o1.toString().compareTo(o2.toString()) != 0)
					{
						return false;
					}
				}
				else if (!o1.equals(o2))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * In a collection of parameters, returns the first that matches
	 * parameters given as argument
	 * @param col The collection to look in
	 * @param params The parameters to match
	 * @return The parameters matching <tt>params</tt>, or null if not found
	 */
	public static Parameters getMatching(Collection<Parameters> col, Parameters params)
	{
		for (Parameters p : col)
		{
			if (p.match(params))
			{
				return p;
			}
		}
		return null;
	}
	
	/**
	 * Create an ordered collection out of an arbitrary collection
	 * of parameters
	 * @param col The collection 
	 * @return A vector with the contents of the collection
	 */
	public static Vector<Parameters> forceOrdering(Collection<Parameters> col)
	{
		Vector<Parameters> out = new Vector<Parameters>();
		for (Parameters pars : col)
		{
			out.add(pars);
		}
		return out;
	}
	
	/**
	 * Returns the value of a parameter, trying to cast it as a number
	 * @param name The parameter name
	 * @return The value, null if conversion failed or parameter not found
	 */
	public Number getNumber(String name)
	{
		Object o = get(name);
		if (o instanceof Number)
		{
			return (Number) o;
		}
		if (o instanceof String)
		{
			String s = (String) o;
			Number n = Float.parseFloat(s);
			return n;
		}
		return null;
	}

}
