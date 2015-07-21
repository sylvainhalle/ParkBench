package ca.uqac.lif.parkbench.examples.sorting;

import ca.uqac.lif.parkbench.Test;

/**
 * Quick sort algorithm, as found here:
 * http://www.java2novice.com/java-sorting-algorithms/quick-sort/
 */
public class QuickSortTest extends SortTest
{
	public QuickSortTest()
	{
		super("Quick Sort");
	}
	
	@Override
	public Test newTest()
	{
		return new QuickSortTest();
	}

	@Override
	public void sort(int[] array)
	{
		int length = array.length;
		quickSort(array, 0, length - 1);
	}
	
	private void quickSort(int[] array, int lowerIndex, int higherIndex) {
        
        int i = lowerIndex;
        int j = higherIndex;
        // calculate pivot number, I am taking pivot as middle index number
        int pivot = array[lowerIndex+(higherIndex-lowerIndex)/2];
        // Divide into two arrays
        while (i <= j) {
            /**
             * In each iteration, we will identify a number from left side which 
             * is greater then the pivot value, and also we will identify a number 
             * from right side which is less then the pivot value. Once the search 
             * is done, then we exchange both numbers.
             */
            while (array[i] < pivot) {
                i++;
            }
            while (array[j] > pivot) {
                j--;
            }
            if (i <= j) {
                exchangeNumbers(array, i, j);
                //move index to next position on both sides
                i++;
                j--;
            }
        }
        // call quickSort() method recursively
        if (lowerIndex < j)
            quickSort(array, lowerIndex, j);
        if (i < higherIndex)
            quickSort(array, i, higherIndex);
    }
 
    private void exchangeNumbers(int[] array, int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

}
