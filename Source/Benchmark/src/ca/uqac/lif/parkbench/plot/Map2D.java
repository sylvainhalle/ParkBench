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
package ca.uqac.lif.parkbench.plot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import ca.uqac.lif.parkbench.DataFormatter;

public class Map2D<T extends Comparable<? super T>,U,V>
{
	Map<T,Map<U,V>> m_contents;
	
	/**
	 * The data formatter for the x values
	 */
	protected DataFormatter<T> m_formatterX;
	
	/**
	 * The data formatter for the y values
	 */
	protected DataFormatter<V> m_formatterY;
	
	/**
	 * Whether the data on each row is normalized (i.e. expressed
	 * as the ratio to the smallest value of the line)
	 */
	protected boolean m_normalizedRows = false;
	
	/**
	 * The columns, in the order they have been arranged the last time
	 * the plot was asked for
	 */
	protected Vector<U> m_columns;
	
	public Map2D()
	{
		this(null, null);
	}
	
	public Map2D(DataFormatter<T> formatter_x, DataFormatter<V> formatter_y)
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
			m_formatterX = new PassthroughFormatter<T>();
		}
		if (formatter_y != null)
		{
			m_formatterY = formatter_y;
		}
		else
		{
			m_formatterY = new ZeroFormatter<V>();
		}		
	}
	
	/**
	 * Sets whether the data on each row is normalized (i.e. expressed
	 * as the ratio to the smallest value of the line)
	 * @param b Set to true to normalize, false otherwise
	 * @return This map
	 */
	public Map2D<T,U,V> normalizeRows(boolean b)
	{
		m_normalizedRows = true;
		return this;
	}
	
	/**
	 * Add a new data point to the plot
	 * @param key The key
	 * @param column The column to put the value
	 * @param value The value to put in the map
	 */
	public void put(T key, U column, V value)
	{
		if (!m_contents.containsKey(key))
		{
			m_contents.put(key, new HashMap<U,V>());
		}
		Map<U,V> values = m_contents.get(key);
		values.put(column, value);
		m_contents.put(key, values);
	}

	/**
	 * The columns, in the order they have been arranged the last time
	 * the plot was asked for.
	 * @return A vector of columns
	 */
	public Vector<U> getColumns()
	{
		// The columns are computed by orderColumns, so we call it
		m_columns = orderColumns();
		return m_columns;
	}
	
	public String toCsv()
	{
		StringBuilder out = new StringBuilder();
		Vector<U> columns = orderColumns();
		m_columns = columns;
		ArrayList<T> sorted_keys = new ArrayList<T>();
		sorted_keys.addAll(m_contents.keySet());
		Collections.sort(sorted_keys);
		for (T key : sorted_keys)
		{
			StringBuilder line = new StringBuilder();
			line.append(m_formatterX.format(key));
			Map<U,V> values = m_contents.get(key);
			Vector<V> out_values = new Vector<V>();
			for (U col_name : columns)
			{
				if (values.containsKey(col_name))
				{
					out_values.add(values.get(col_name));
				}
				else
				{
					out_values.add(null);
				}
			}
			/* TODO: find a way to normalize
			if (m_normalizedRows)
			{
				// Find smallest value in row
				boolean first_value = true;
				double smallest = 0;
				for (V value : out_values)
				{
					double cur_value = ((Number) value).doubleValue();
					if (first_value)
					{
						first_value = false;
						smallest = cur_value;
					}
					else
					{
						if (cur_value < smallest)
						{
							cur_value = smallest;
						}
					}
				}
				if (smallest != 0)
				{
					for (int i = 0; i < out_values.size(); i++) // Now normalize
					{
						double cur_value = ((Number) out_values.get(i)).doubleValue();
						double new_value = cur_value / smallest;
						out_values.setElementAt((V) new_value, i);
					}					
				}
			}*/
			for (V value : out_values)
			{
				line.append(",");
				if (value != null)
				{
					line.append(m_formatterY.format(value));
				}
				else
				{
					line.append("0");
				}
			}
			line.append("\n");
			out.append(line);
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
	
	protected static class PassthroughFormatter<X> implements DataFormatter<X>
	{
		public PassthroughFormatter()
		{
			super();
		}
		
		@Override
		public String format(X x)
		{
			if (x == null)
			{
				return "";
			}
			return x.toString();
		}
		
	}
	
	protected static class ZeroFormatter<X> implements DataFormatter<X>
	{
		public ZeroFormatter()
		{
			super();
		}
		
		@Override
		public String format(X x)
		{
			if (x == null)
			{
				return "0";
			}
			return x.toString();
		}
		
	}
}
