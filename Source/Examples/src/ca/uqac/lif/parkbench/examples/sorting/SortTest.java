package ca.uqac.lif.parkbench.examples.sorting;

import java.io.IOException;
import java.util.Random;

import ca.uqac.lif.parkbench.CommandRunner;
import ca.uqac.lif.parkbench.Parameters;
import ca.uqac.lif.parkbench.Test;
import ca.uqac.lif.util.FileReadWrite;

public abstract class SortTest extends Test
{
	public SortTest(String name)
	{
		super(name);
	}
	
	@Override
	public final boolean prerequisitesFulilled(final Parameters input)
	{
		return CommandRunner.fileExists(sortFilename(input));
	}
	
	@Override
	public final void fulfillPrerequisites(Parameters input)
	{
		// Generates a random list of integers of given size, and saves it
		// to a file
		Random random = new Random();
		String filename = sortFilename(input);
		StringBuilder out = new StringBuilder();
		int size = input.getNumber("size").intValue();
		int range = 2 * size;
		for (int i = 0; i < size; i++)
		{
			int number = random.nextInt(range);
			if (i > 0)
			{
				out.append(",");
			}
			out.append(number);
		}
		try
		{
			FileReadWrite.writeToFile(filename, out.toString());
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected final String sortFilename(Parameters input)
	{
		int size = input.getNumber("size").intValue();
		return "list-" + size + ".txt";
	}
	
	@Override
	public void runTest(final Parameters input, Parameters results)
	{
		int[] array = getArray(input);
		if (array == null)
		{
			stopWithStatus(Status.FAILED);
			return;
		}
		long start_time = System.nanoTime();
		sort(array);
		long end_time = System.nanoTime();
		results.put("time", (end_time - start_time) / 1000000f);
		stopWithStatus(Status.DONE);
	}
	
	protected final int[] getArray(Parameters input)
	{
		int size = input.getNumber("size").intValue();
		int[] array = new int[size];
		String filename = sortFilename(input);
		try
		{
			String contents = FileReadWrite.readFile(filename);
			String[] str = contents.split(",");
			for (int i = 0; i < size; i++)
			{
				array[i] = Integer.parseInt(str[i].trim());
			}
		}
		catch (IOException e) 
		{
			return null;
		}
		return array;
	}
	
	protected abstract void sort(int[] array);

}
