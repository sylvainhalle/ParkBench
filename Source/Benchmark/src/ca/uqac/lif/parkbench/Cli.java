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

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import ca.uqac.lif.cornipickle.json.JsonFastParser;
import ca.uqac.lif.cornipickle.json.JsonMap;
import ca.uqac.lif.cornipickle.json.JsonParser;
import ca.uqac.lif.cornipickle.json.JsonParser.JsonParseException;
import ca.uqac.lif.cornipickle.util.AnsiPrinter;
import ca.uqac.lif.util.FileReadWrite;

public class Cli
{
	/**
	 * Return codes
	 */
	public static final int ERR_OK = 0;
	public static final int ERR_IO = 3;
	public static final int ERR_ARGUMENTS = 4;
	public static final int ERR_RUNTIME = 6;
	
	/**
	 * Version string
	 */
	protected static String s_versionString = "0.2.4";
	
	/**
	 * Default server name
	 */
	protected String m_defaultServerName = "localhost";

	/**
	 * Default port to listen to
	 */
	protected int m_defaultPort = 21212;
	
	/**
	 * Verbosity level for CLI
	 */
	protected int m_verbosity = 1;
	
	/**
	 * The number of refresh loops between two saves of the benchmark's status
	 */
	protected static final int s_saveInterval = 10;
	
	/**
	 * The number of threads used by default
	 */
	protected static final int s_defaultNumThreads = 2;
	
	/**
	 * Command-line arguments
	 */
	protected String[] m_args;
	
	public Cli(String[] args)
	{
		super();
		m_args = args;
	}

	public void start(final Benchmark benchmark)
	{
		String server_name = m_defaultServerName;
		int server_port = m_defaultPort;
		boolean interactive_mode = false;
		int num_threads = s_defaultNumThreads;
		boolean merge = false;
		
		final AnsiPrinter stderr = new AnsiPrinter(System.err);
		final AnsiPrinter stdout = new AnsiPrinter(System.out);
		//stdout.setForegroundColor(AnsiPrinter.Color.BLACK);
		//stderr.setForegroundColor(AnsiPrinter.Color.BLACK);

		// Propertly close print streams when closing the program
		// https://www.securecoding.cert.org/confluence/display/java/FIO14-J.+Perform+proper+cleanup+at+program+termination
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				stderr.println("\nStopping server");
				benchmark.stop();
				stderr.close();
				stdout.close();
			}
		}));

		// Parse command line arguments
		Options options = setupOptions();
		CommandLine c_line = setupCommandLine(m_args, options, stderr);
		assert c_line != null;
		if (c_line.hasOption("verbosity"))
		{
			m_verbosity = Integer.parseInt(c_line.getOptionValue("verbosity"));
		}
		if (m_verbosity > 0)
		{
			showHeader(stdout);
		}
		if (c_line.hasOption("version"))
		{
			stderr.println("(C) 2015 Sylvain Hallé et al., Université du Québec à Chicoutimi");
			stderr.println("This program comes with ABSOLUTELY NO WARRANTY.");
			stderr.println("This is a free software, and you are welcome to redistribute it");
			stderr.println("under certain conditions. See the file LICENSE for details.\n");
			System.exit(ERR_OK);
		}
		if (c_line.hasOption("h"))
		{
			showUsage(options);
			System.exit(ERR_OK);
		}
		if (c_line.hasOption("p"))
		{
			server_port = Integer.parseInt(c_line.getOptionValue("p"));
		}
		if (c_line.hasOption("s"))
		{
			server_name = c_line.getOptionValue("s");
		}
		if (c_line.hasOption("i"))
		{
			interactive_mode = true;
		}
		if (c_line.hasOption("i"))
		{
			merge = true;
		}
		if (c_line.hasOption("t"))
		{
			num_threads = Integer.parseInt(c_line.getOptionValue("p"));
		}
		benchmark.setThreads(num_threads);
		
		// The remaining arguments are configuration files to read
		List<String> remaining_args = c_line.getArgList();
		JsonParser parser = new JsonFastParser();
		for (String filename : remaining_args)
		{
			//stdout.setForegroundColor(AnsiPrinter.Color.BROWN);
			if (merge)
			{
				println(stdout, "Merging with benchmark state " + filename, 1);
			}
			else
			{
				println(stdout, "Overwriting with benchmark state " + filename, 1);
			}
			String file_contents;
			try
			{
				file_contents = FileReadWrite.readFile(filename);
				JsonMap state = (JsonMap) parser.parse(file_contents);
				benchmark.deserializeState(state, merge);
			}
			catch (IOException e) 
			{
				stderr.println("Error reading file " + filename);
			}
			catch (JsonParseException e) 
			{
				stderr.println("Error parsing contents of file " + filename);
			}
		}
		String save_filename = benchmark.getName() + ".json";
		
		if (interactive_mode)
		{
			BenchmarkServer server = new BenchmarkServer(server_name, server_port, benchmark);
			server.startServer();
			println(stdout, "Server started on " + server_name + ":" + server_port, 1);
			/*int loop_count = 1;
			while (!benchmark.isFinished())
			{
				try
				{
					// Wait one second
					Thread.sleep(1000);
				}
				catch (InterruptedException e) 
				{
					// Do nothing
				}
				loop_count = (loop_count + 1) % s_saveInterval;
				if (loop_count == 0)
				{
					// Periodical save of the benchmark
					try
					{
						JsonMap state = benchmark.serializeState();
						FileReadWrite.writeToFile(save_filename, state.toString());
					}
					catch (IOException e) 
					{
						println(stderr, "Error writing to file " + save_filename, 2);
					}
				}
			}*/
		}
		else
		{
			// Run all tests
			int loop_count = 1;
			
			println(stdout, "Running " + benchmark.getName() 
					+ " test suite in batch mode, using " 
					+ benchmark.threadCount() + " threads", 1000);
			println(stdout, "Saving results in " + save_filename, 1000);
			long start_time = System.currentTimeMillis() / 1000;
			benchmark.queueAllTests();
			println(stdout, "Queued  Prereq  Running Done   Failed Time", 1000);
			while (!benchmark.isFinished())
			{
				long current_time = System.currentTimeMillis() / 1000;
				JsonMap state = benchmark.serializeState();
				JsonMap status_map = (JsonMap) state.get("status");
				String line = String.format("%6d  %6d  %7d %6d %6d %d s", 
						status_map.getNumber("status-queued").intValue(),
						status_map.getNumber("status-prerequisites").intValue(),
						status_map.getNumber("status-running").intValue(),
						status_map.getNumber("status-done").intValue(),
						status_map.getNumber("status-failed").intValue(),
						current_time - start_time);
				print(stdout, line + "\r", 1000);
				try
				{
					// Wait one second
					Thread.sleep(1000);
				}
				catch (InterruptedException e) 
				{
					// Do nothing
				}
				loop_count = (loop_count + 1) % s_saveInterval;
				if (loop_count == 0)
				{
					// Periodical save of the benchmark
					try
					{
						FileReadWrite.writeToFile(save_filename, state.toString());
					}
					catch (IOException e) 
					{
						println(stderr, "Error writing to file " + save_filename, 2);
					}
				}
			}
			try
			{
				// We are done; save benchmark status one last time
				JsonMap state = benchmark.serializeState();
				FileReadWrite.writeToFile(save_filename, state.toString());
			}
			catch (IOException e) 
			{
				println(stderr, "Error writing to file " + save_filename, 2);
			}
			println(stdout, "\nDone", 1000);
			benchmark.stop();
		}
	}
	
	/**
	 * Sets up the options for the command line parser
	 * @return The options
	 */
	private Options setupOptions()
	{
		Options options = new Options();
		Option opt;
		opt = Option.builder("h")
				.longOpt("help")
				.desc("Display command line usage")
				.build();
		options.addOption(opt);
		opt = Option.builder("i")
				.longOpt("interactive")
				.desc("Use interactive mode")
				.build();
		options.addOption(opt);
		opt = Option.builder("m")
				.longOpt("merge")
				.desc("Merge JSON file with existing test suite")
				.build();
		options.addOption(opt);
		opt = Option.builder("s")
				.longOpt("servername")
				.argName("x")
				.hasArg()
				.desc("Set server name or IP address x (default: " + m_defaultServerName + ")")
				.build();
		options.addOption(opt);
		opt = Option.builder("p")
				.longOpt("port")
				.argName("x")
				.hasArg()
				.desc("Listen on port x (default: " + m_defaultPort + ")")
				.build();
		options.addOption(opt);
		opt = Option.builder("t")
				.longOpt("threads")
				.argName("x")
				.hasArg()
				.desc("Use x threads (default: " + s_defaultNumThreads + ")")
				.build();
		options.addOption(opt);
		return options;
	}

	/**
	 * Show the benchmark's usage
	 * @param options The options created for the command line parser
	 */
	private static void showUsage(Options options)
	{
		HelpFormatter hf = new HelpFormatter();
		hf.printHelp("java -jar Barkbench.jar [options] [filename]", options);
	}
	
	/**
	 * Sets up the command line parser
	 * @param args The command line arguments passed to the class' {@link main}
	 * method
	 * @param options The command line options to be used by the parser
	 * @return The object that parsed the command line parameters
	 */
	private static CommandLine setupCommandLine(String[] args, Options options, PrintStream stderr)
	{
		CommandLineParser parser = new DefaultParser();
		CommandLine c_line = null;
		try
		{
			// parse the command line arguments
			c_line = parser.parse(options, args);
		}
		catch (org.apache.commons.cli.ParseException exp)
		{
			// oops, something went wrong
			stderr.println("ERROR: " + exp.getMessage() + "\n");
			//HelpFormatter hf = new HelpFormatter();
			//hf.printHelp(t_gen.getAppName() + " [options]", options);
			System.exit(ERR_ARGUMENTS);
		}
		return c_line;
	}

	private static void showHeader(PrintStream out)
	{
		out.println("ParkBench " + s_versionString + ", a versatile benchmark environment");
	}
	
	protected void println(PrintStream out, String message, int verbosity_level)
	{
		if (verbosity_level >= m_verbosity)
		{
			out.println(message);
		}
	}
	
	protected void print(PrintStream out, String message, int verbosity_level)
	{
		if (verbosity_level >= m_verbosity)
		{
			out.print(message);
		}
	}
	
	public static void main(String[] args)
	{
		Cli cli = new Cli(args);
		showHeader(System.out);
		cli.println(System.out, "You are running parkbench.jar, which is only a library to create\nyour own test suites. As a result nothing will happen here. Read the \nonline documentation to learn how to use ParkBench.", 1000);
	}

}
