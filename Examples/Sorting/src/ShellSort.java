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


import ca.uqac.lif.parkbench.Experiment;

/**
 * Shell sort algorithm, as found here:
 * http://stackoverflow.com/a/17543917
 */
public class ShellSort extends SortExperiment
{
	public ShellSort()
	{
		super("Shell Sort");
	}

	@Override
	public Experiment newExperiment()
	{
		return new ShellSort();
	}

	@Override
	public void sort(int[] array)
	{
		int j;
		for( int gap = array.length / 2; gap > 0; gap /= 2 )
		{
			for( int i = gap; i < array.length; i++ )
			{
				int tmp = array[ i ];
				for( j = i; j >= gap && tmp < array[ j - gap ]; j -= gap )
				{
					array[ j ] = array[ j - gap ];
				}
				array[ j ] = tmp;
			}
		}
	}
}
