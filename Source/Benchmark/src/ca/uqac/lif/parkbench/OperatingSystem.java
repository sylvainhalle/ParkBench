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

public class OperatingSystem
{
	/**
	 * The operating system type
	 */
	public enum Type {LINUX, MACOS, WINDOWS, FREEBSD, SUNOS};
	
	/**
	 * Get the operating system type. At the moment, only distinguishes
	 * between Linux, Windows and MacOS.
	 * @return The type
	 */
	public static Type getType()
	{
		String name = System.getProperty("os.name");
		if (name.contains("Windows"))
		{
			return Type.WINDOWS;
		}
		if (name.contains("Mac"))
		{
			return Type.MACOS;
		}
		if (name.contains("FreeBSD"))
		{
			return Type.FREEBSD;
		}
		if (name.contains("SunOS"))
		{
			return Type.SUNOS;
		}
		return Type.LINUX;
	}
}
