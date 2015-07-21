package ca.uqac.lif.parkbench.examples.sorting;

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
