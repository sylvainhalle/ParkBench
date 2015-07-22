package ca.uqac.lif.parkbench;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import ca.uqac.lif.util.FileReadWrite;

public class CommandRunner
{
	/**
	 * Runs a command and returns the contents of stdout as a String
	 * @param command The command to run
	 * @return The contents of stdout sent by the command
	 * @throws IOException
	 */
	public static String runCommandString(String[] command) throws IOException
	{
		return runCommandString(command, null);
	}

	/**
	 * Runs a command and returns the contents of stdout as an array of bytes
	 * @param command The command to run
	 * @return The contents of stdout sent by the command
	 * @throws IOException
	 */
	public static byte[] runCommandBytes(String[] command) throws IOException
	{
		return runCommandBytes(command, null);
	}

	/**
	 * Runs a command and returns the contents of stdout as a String
	 * @param command The command to run
	 * @param stdin If not set to null, this string will be sent to the stdin
	 *   of the command being run
	 * @return The contents of stdout sent by the command
	 * @throws IOException
	 */
	public static String runCommandString(String[] command, String stdin) throws IOException
	{
		byte[] bytes = runCommandBytes(command, stdin);
		String out = new String(bytes);
		return out;
	}

	/**
	 * Runs a command and returns the contents of stdout as an array of bytes
	 * @param command The command to run
	 * @param stdin If not set to null, this string will be sent to the stdin
	 *   of the command being run
	 * @return The contents of stdout sent by the command
	 * @throws IOException
	 */
	public static byte[] runCommandBytes(String[] command, String stdin) throws IOException
	{
		ProcessBuilder builder = new ProcessBuilder(command);
		Process process = builder.start();
		StreamGobbler error_gobbler = new StreamGobbler(process.getErrorStream(), "ERR");
		StreamGobbler output_gobbler = new StreamGobbler(process.getInputStream(), "IN");
		// Send data into stdin of process
		if (stdin != null)
		{
			OutputStream process_stdin = process.getOutputStream();
			byte[] stdin_bytes = stdin.getBytes();
			//System.out.println("Writing " + stdin_bytes.length + " bytes");
			process_stdin.write(stdin_bytes, 0, stdin_bytes.length);
			process_stdin.flush();
			//System.out.println("Flushed");
			process_stdin.close();
		}
		// Start gobblers
		error_gobbler.start();
		output_gobbler.start();
		try 
		{
			@SuppressWarnings("unused")
			int errCode = process.waitFor(); // We might do something with this value one day
		}
		catch (InterruptedException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		error_gobbler.interrupt();
		output_gobbler.interrupt();
		return output_gobbler.getBytes();
	}


	/**
	 * Constantly reads an input stream and captures its content.
	 * Inspired from <a href="http://stackoverflow.com/questions/14165517/processbuilder-forwarding-stdout-and-stderr-of-started-processes-without-blocki">Stack Overflow</a>
	 */
	protected static class StreamGobbler extends Thread
	{
		InputStream m_is;
		Vector<Byte> m_contents;
		String m_name;

		private StreamGobbler(InputStream is, String name)
		{
			super();
			m_contents = new Vector<Byte>();
			this.m_is = is;
			m_name = name;
		}

		@Override
		public void run()
		{
			try
			{
				//InputStreamReader isr = new InputStreamReader(is);
				byte[] buffer = new byte[8192];
				int len = 1;
				while ((len = m_is.read(buffer)) > 0)
				{   
					for (int i = 0; i < len; i++)
					{
						m_contents.add(buffer[i]);
					}
					if (m_is.available() == 0)
					{
						break;
					}
				}
				m_is.close();
			}
			catch (IOException ioe) 
			{
				ioe.printStackTrace();
			}
		}

		/**
		 * Returns the contents captured by the gobbler as an array of bytes
		 * @return The contents
		 */
		public byte[] getBytes()
		{
			int size = m_contents.size();
			byte[] out = new byte[size];
			int i = 0;
			for (byte b : m_contents)
			{
				out[i++] = b;
			}
			return out;
		}
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
	
	/**
	 * Deletes a file
	 * @param filename The filename
	 * @return true if the file could be deleted, false otherwise
	 */
	public static boolean deleteFile(String filename)
	{
		File f = new File(filename);
		return f.delete();
	}

	public static void main(String[] args) throws IOException
	{
		//String[] command = {"D:/Workspaces/ParkBench/testA.bat"};
		String[] command = {"C:/Program Files/gnuplot/binary/gnuplot.exe"};
		String gpfile = FileReadWrite.readFile("D:/Workspaces/ParkBench/test.gp");
		byte[] out = CommandRunner.runCommandBytes(command, gpfile);
		System.out.println(out.length);
	}

}
