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


import ca.uqac.lif.parkbench.Test;

/**
 * Bubble sort algorithm, as found here:
 * http://mathbits.com/MathBits/Java/arrays/Bubble.htm
 */
public class BubbleSortTest extends SortTest
{
	public BubbleSortTest()
	{
		super("Bubble Sort");
	}

	@Override
	public Test newTest()
	{
		return new BubbleSortTest();
	}

	@Override
	public void sort(int[] array)
	{
		int j;
		boolean flag = true;   // set flag to true to begin first pass
		int temp;   //holding variable

		while ( flag )
		{
			flag= false;    //set flag to false awaiting a possible swap
			for( j=0;  j < array.length -1;  j++ )
			{
				if ( array[ j ] < array[j+1] )   // change to > for ascending sort
				{
					temp = array[ j ];                //swap elements
					array[ j ] = array[ j+1 ];
					array[ j+1 ] = temp;
					flag = true;              //shows a swap occurred 
				}
			}
		} 		
	}
}
