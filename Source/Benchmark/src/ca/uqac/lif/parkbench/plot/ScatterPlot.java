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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import ca.uqac.lif.parkbench.Parameters;
import ca.uqac.lif.parkbench.Experiment;

public class ScatterPlot extends PlanarPlot
{
	/**
	 * Whether to put line segments between points of the same series
	 */
	protected boolean m_withLines = false;
	
	/**
	 * Creates an empty scatterplot with title
	 * @param title The title
	 */
	public ScatterPlot(String title)
	{
		super(title);
	}
	
	/**
	 * Tells the plot to put line segments between points of the same series
	 * @return This plot
	 */
	public ScatterPlot withLines()
	{
		m_withLines = true;
		return this;
	}
	
	/**
	 * Tells the plot <em>not</em> to put line segments between points of 
	 * the same series
	 * @return This plot
	 */
	public ScatterPlot noLines()
	{
		m_withLines = false;
		return this;
	}
	
	@Override
	public String toGnuPlot(Terminal term)
	{
		// Build the set of parameters to group by (x)
		Set<String> group_by = new HashSet<String>();
		group_by.add(m_paramNameX);
		// Fill a 2D map with data
		Map2D<Float,Parameters,Number> map = new Map2D<Float,Parameters,Number>();
		map.normalizeRows(m_normalizedRows);
		for (Experiment t : m_tests)
		{
			Parameters params = new Parameters(t.getParameters());
			params.removeAll(m_ignoredParameters);
			// Put test's name into params
			params.put("name", t.getName());
			Float value_x = ((Number) params.get(m_paramNameX)).floatValue();
			params.removeAll(group_by);
			Parameters results = t.getResults();
			Number value_y = (Number) results.getNumber(m_paramNameY);
			map.put(value_x, params, value_y);
		}
		// Create plot string
		StringBuilder out = new StringBuilder();
		out.append(super.createHeader(term));
		out.append("plot ");
		int column_count = 2;
		Vector<Parameters> columns = map.getColumns();
		String csv_data = map.toCsv();
		String style = " with points ";
		if (m_withLines)
		{
			style = " with linespoints ";
		}
		StringBuilder data_part = new StringBuilder();
		Map<Parameters,String> legends = createLegends(columns);
		for (Parameters p : columns)
		{
			if (column_count > 2)
			{
				out.append(", ");
			}
			out.append("\"-\" using 1:").append(column_count).append(style).append("title \"").append(legends.get(p)).append("\"");
			column_count++;
			// In Gnuplot, if we use the special "-" filename, we must repeat
			// the data as many times as we use it in the plot command; it does not remember it
			data_part.append(csv_data).append("end\n");
		}
		out.append("\n");
		out.append(data_part);
		return out.toString();
	}
	

}
