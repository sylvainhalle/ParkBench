package ca.uqac.lif.parkbench.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import ca.uqac.lif.parkbench.DataFormatter;

public class Map2D<T,U,V>
{
	Map<T,Map<U,V>> m_contents;
	
	/**
	 * The data formatter for the x values
	 */
	protected DataFormatter<Number> m_formatterX;
	
	/**
	 * The data formatter for the y values
	 */
	protected DataFormatter<Number> m_formatterY;
	
	/**
	 * The columns, in the order they have been arranged the last time
	 * the plot was asked for
	 */
	protected Vector<U> m_columns;
	
	public Map2D()
	{
		this(null, null);
	}
	
	public Map2D(DataFormatter<Number> formatter_x, DataFormatter<Number> formatter_y)
	{
		super();
		m_contents = new HashMap<T,Map<U,V>>();
		m_columns = new Vector<U>();
		if (formatter_x != null)
		{
			m_formatterX = formatter_x;
		}
		else
		{
			m_formatterX = new PassthroughFormatter();
		}
		if (formatter_y != null)
		{
			m_formatterY = formatter_y;
		}
		else
		{
			m_formatterY = new PassthroughFormatter();
		}		
	}
	
	/**
	 * Add a new data point to the plot
	 * @param key
	 * @param column
	 * @param value
	 */
	public void put(T key, U column, V value)
	{
		Map<U,V> values = m_contents.getOrDefault(key, new HashMap<U,V>());
		values.put(column, value);
		m_contents.put(key, values);
	}

	/**
	 * The columns, in the order they have been arranged the last time
	 * the plot was asked for
	 * @return A vector of columns
	 */
	public Vector<U> getColumns()
	{
		return m_columns;
	}
	
	public String toCsv()
	{
		StringBuilder out = new StringBuilder();
		Vector<U> columns = orderColumns();
		m_columns = columns;
		Set<T> keys = m_contents.keySet();
		for (T key : keys)
		{
			out.append(keyFormat(key));
			Map<U,V> values = m_contents.get(key);
			for (U col_name : columns)
			{
				out.append(",");
				if (values.containsKey(col_name))
				{
					V value = values.get(col_name);
					out.append(valueFormat(value));
				}
			}
			out.append("\n");
		}
		return out.toString();
	}
	
	/**
	 * Puts all columns in an ordered vector
	 * @return The vector of all column names
	 */
	protected Vector<U> orderColumns()
	{
		Vector<U> out = new Vector<U>();
		Set<U> columns = new HashSet<U>();
		// Extract all column names that occur for one of the keys
		for (T k : m_contents.keySet())
		{
			Map<U,V> values = m_contents.get(k);
			Set<U> cols = values.keySet();
			columns.addAll(cols);
		}
		// Iterate through the set of gathered names and put them in a vector
		for (U column : columns)
		{
			out.add(column);
		}
		return out;
	}
	
	public T keyFormat(T k)
	{
		return k;
	}
	
	public V valueFormat(V v)
	{
		return v;
	}
	
	protected static class PassthroughFormatter implements DataFormatter<Number>
	{
		public PassthroughFormatter()
		{
			super();
		}
		
		@Override
		public String format(Number x)
		{
			if (x == null)
			{
				return "-";
			}
			return x.toString();
		}
		
	}
}
