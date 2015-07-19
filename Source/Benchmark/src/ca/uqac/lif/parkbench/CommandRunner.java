package ca.uqac.lif.parkbench;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandRunner
{
	/**
	 * 
	 * @param command An array of strings containing the command to call
	 *   and its arguments
	 * @throws IOException 
	 */
	public static String runCommand(String[] command) throws IOException
	{
		Runtime rt = Runtime.getRuntime();
		//String[] commands = {"system.exe","-get t"};
		Process proc = rt.exec(command);

		BufferedReader stdInput = new BufferedReader(new 
		     InputStreamReader(proc.getInputStream()));

		BufferedReader stdError = new BufferedReader(new 
		     InputStreamReader(proc.getErrorStream()));

		// read the output from the command
		StringBuilder stdout = new StringBuilder();
		String s = null;
		while ((s = stdInput.readLine()) != null)
		{
		    stdout.append(s).append("\n");
		}

		// read any errors from the attempted command
		StringBuilder stderr = new StringBuilder();
		while ((s = stdError.readLine()) != null)
		{
		    stderr.append(s).append("\n");
		}
		//int exit_value = proc.exitValue();
		return stdout.toString();
	}
	
	/**
	 * Checks whether a file exists in the filesystem
	 * @param filename The filename to look for
	 * @return true if file exists, false otherwise
	 */
	public static boolean fileExists(String filename)
	{
		File f = new File(filename);
		return f.exists();
	}
	
	/**
	 * Replace the extension of a filename with another. For example,
	 * one can replace /my/path/foo.bar with /my/path/foo.baz.
	 * @param filename The original filename
	 * @param extension The extension to replace with
	 * @return The modified filename
	 */
	public static String replaceExtension(String filename, String extension)
	{
		String without_extension = trimExtension(filename);
		return without_extension + "." + extension;
	}
	
	/**
	 * Trims the extension of a filename. For example, with /my/path/foo.bar,
	 * would return /my/path/foo
	 * @param filename The filename
	 * @return The filename without the extension
	 */
	public static String trimExtension(String filename)
	{
		int position = filename.lastIndexOf(".");
		if (position < 0)
			return filename;
		return filename.substring(0, position);
	}
}
