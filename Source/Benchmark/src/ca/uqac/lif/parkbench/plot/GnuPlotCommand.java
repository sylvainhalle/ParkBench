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

import ca.uqac.lif.parkbench.ObjectGrabber;
import ca.uqac.lif.parkbench.OperatingSystem;

/**
 * Returns the command to call GnuPlot, depending on the host OS
 */
public class GnuPlotCommand extends ObjectGrabber<String>
{
	@Override
	public String grab()
	{
		String out = "";
		OperatingSystem.Type type = OperatingSystem.getType();
		switch (type)
		{
		case WINDOWS:
			out = "C:/Program Files/gnuplot/binary/gnuplot.exe";
			break;
		default:
			out = "gnuplot";
			break;
		}
		return out;
	}

}
