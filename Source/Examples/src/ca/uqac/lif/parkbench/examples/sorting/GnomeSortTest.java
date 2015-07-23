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
package ca.uqac.lif.parkbench.examples.sorting;

import ca.uqac.lif.parkbench.Test;

/**
 * Gnome sort algorithm, as found here:
 * http://dickgrune.com/Programs/gnomesort.html
 */
public class GnomeSortTest extends SortTest
{
	public GnomeSortTest()
	{
		super("Gnome Sort");
	}

	@Override
	public Test newTest()
	{
		return new GnomeSortTest();
	}

	@Override
	public void sort(int[] array)
	{
		int n = array.length;
		int i = 0;

		while (i < n) {
			if (i == 0 || array[i-1] <= array[i]) i++;
			else {int tmp = array[i]; array[i] = array[i-1]; array[--i] = tmp;}
		}
	}
}
