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
import ca.uqac.lif.parkbench.Test;

public class ClusteredHistogram extends PlanarPlot 
{
	/**
	 * Creates an empty histogram with title
	 * @param title The title
	 */
	public ClusteredHistogram(String title)
	{
		super(title);
	}

	@Override
	public String toGnuPlot(Terminal term) 
	{
		// Build the set of parameters to group by (x)
		Set<String> group_by = new HashSet<String>();
		group_by.add(m_paramNameX);
		// Fill a 2D map with data
		Map2D<String,Parameters,Number> map = new Map2D<String,Parameters,Number>();
		map.normalizeRows(m_normalizedRows);
		for (Test t : m_tests)
		{
			Parameters params = new Parameters(t.getParameters());
			params.removeAll(m_ignoredParameters);
			// Put test's name into params
			params.put("name", t.getName());
			String value_x = params.get(m_paramNameX).toString();
			params.removeAll(group_by);
			Parameters results = t.getResults();
			Number value_y = results.getNumber(m_paramNameY);
			map.put(value_x, params, value_y);
		}
		// Create plot string
		StringBuilder out = new StringBuilder();
		out.append(super.createHeader(term));
		out.append("set xtics rotate out\n");
		out.append("set style data histogram\n");
		out.append("set auto x\n");
		out.append("set yrange [0:*]\n");
		out.append("set style histogram clustered gap 1\n");
		out.append("set style fill solid border rgb \"black\"\n");
		Vector<Parameters> columns = map.getColumns();
		String csv_data = map.toCsv();
		out.append("plot");
		StringBuilder data_part = new StringBuilder();
		Map<Parameters,String> legends = createLegends(columns);
		int col_count = 2;
		for (Parameters p : columns)
		{
			if (col_count > 2)
			{
				out.append(",");
			}
			out.append(" \"-\" using ").append(col_count).append(":xtic(1) title \"").append(legends.get(p)).append("\"");
			// In Gnuplot, if we use the special "-" filename, we must repeat
			// the data as many times as we use it in the plot command; it does not remember it
			data_part.append(csv_data).append("end\n");
			col_count++;
		}
		out.append("\n");
		out.append(data_part);
		return out.toString();
	}
}
