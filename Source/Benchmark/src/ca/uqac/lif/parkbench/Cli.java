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

import ca.uqac.lif.json.JsonMap;
import ca.uqac.lif.json.JsonParser;
import ca.uqac.lif.json.JsonParser.JsonParseException;
import ca.uqac.lif.cornipickle.util.AnsiPrinter;
import ca.uqac.lif.util.CliParser;
import ca.uqac.lif.util.CliParser.Argument;
import ca.uqac.lif.util.CliParser.ArgumentMap;
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
	 * Version string. This must be written here, rather than reading it
	 * from the containing manifest. The reason is that the parkbench jar
	 * is likely to be contained within a test suite jar, and all manifest
	 * information (including this version number) will be overwritten by that
	 * of the containing jar.
	 */
	protected static final String s_versionString = "0.5";

	/**
	 * Default server name
	 */
	protected static String s_defaultServerName = "localhost";

	/**
	 * Default port to listen to
	 */
	protected static int s_defaultPort = 21212;

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
	
	protected Argument[] m_testSuiteArguments;

	public Cli(String[] args, Argument[] arguments)
	{
		super();
		m_args = args;
		m_testSuiteArguments = arguments;
	}
	
	public Cli(String[] args)
	{
		this(args, new Argument[0]);
	}

	public void start(final Benchmark benchmark, ExperimentSuite test_suite)
	{
		String server_name = s_defaultServerName;
		int server_port = s_defaultPort;
		boolean interactive_mode = false, text_interactive = false;
		int num_threads = s_defaultNumThreads;
		boolean merge = false;

		final AnsiPrinter stderr = new AnsiPrinter(System.err);
		final AnsiPrinter stdout = new AnsiPrinter(System.out);

		// Properly close print streams when closing the program
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
		CliParser c_line = setupOptions();
		Argument[] arguments = test_suite.setupCommandLineArguments();
		for (Argument arg : arguments)
		{
			c_line.addArgument(arg);
		}
		ArgumentMap a_map = c_line.parse(m_args);
		if (a_map.hasOption("verbosity"))
		{
			m_verbosity = Integer.parseInt(a_map.getOptionValue("verbosity"));
		}
		if (m_verbosity > 0)
		{
			showHeader(stdout);
		}
		if (a_map.hasOption("version"))
		{
			stderr.println("(C) 2015-2016 Sylvain Hallé et al., Université du Québec à Chicoutimi");
			stderr.println("This program comes with ABSOLUTELY NO WARRANTY.");
			stderr.println("This is a free software, and you are welcome to redistribute it");
			stderr.println("under certain conditions. See the file LICENSE for details.\n");
			System.exit(ERR_OK);
		}
		if (a_map.hasOption("help"))
		{
			c_line.printHelp("ParkBench " + s_versionString + ", a versatile benchmark environment", stderr);
			System.exit(ERR_OK);
		}
		if (a_map.hasOption("p"))
		{
			server_port = Integer.parseInt(a_map.getOptionValue("p"));
		}
		if (a_map.hasOption("s"))
		{
			server_name = a_map.getOptionValue("s");
		}
		if (a_map.hasOption("server"))
		{
			interactive_mode = true;
		}
		if (a_map.hasOption("server"))
		{
			merge = true;
		}
		if (a_map.hasOption("interactive"))
		{
			text_interactive = true;
		}
		if (a_map.hasOption("threads"))
		{
			num_threads = Integer.parseInt(a_map.getOptionValue("threads"));
		}
		benchmark.setThreads(num_threads);
		
		// Now that the main loop has parsed arguments, send them to the
		// test suite for further processing
		test_suite.readCommandLine(a_map);
		test_suite.setup(benchmark);

		// The remaining arguments are configuration files to read
		List<String> remaining_args = a_map.getOthers();
		JsonParser parser = new JsonParser();
		for (String filename : remaining_args)
		{
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
		
		// Deploy any files
		String deploy_dir = "deploy";
		System.out.println("Deploying files to " + deploy_dir);
		DeployHandler.deployAll(deploy_dir, 1);

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
		else if (text_interactive) 
		{
			TextInterface ti = new TextInterface(benchmark, stdout);
			ti.run();
			benchmark.stop();
			System.exit(ERR_OK);
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
			benchmark.queueAllExperiments();
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
	static CliParser setupOptions()
	{
		CliParser options = new CliParser();
		options.addArgument(new CliParser.Argument()
		.withDescription("Display command line usage")
		.withLongName("help"));
		options.addArgument(new CliParser.Argument()
		.withDescription("Use server mode")
		.withLongName("server"));
		options.addArgument(new CliParser.Argument()
		.withDescription("Use interactive text mode")
		.withLongName("interactive")
		.withShortName("i"));
		options.addArgument(new CliParser.Argument()
		.withDescription("Merge JSON file with existing test suite")
		.withLongName("merge")
		.withShortName("m"));
		options.addArgument(new CliParser.Argument()
		.withDescription("Set server name or IP address x (default: " + s_defaultServerName + ")")
		.withLongName("servername")
		.withShortName("s")
		.withArgument("x"));
		options.addArgument(new CliParser.Argument()
		.withDescription("Listen on port x (default: " + s_defaultPort + ")")
		.withLongName("port")
		.withShortName("p")
		.withArgument("x"));
		options.addArgument(new CliParser.Argument()
		.withDescription("Use x threads (default: " + s_defaultNumThreads + ")")
		.withLongName("threads")
		.withShortName("t")
		.withArgument("x"));
		options.addArgument(new CliParser.Argument()
		.withDescription("Show version information")
		.withLongName("version"));
		return options;
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
	
	protected static void showHeader(PrintStream ps)
	{
		ps.println("ParkBench " + s_versionString + ", a versatile benchmark environment");
		ps.println("(C) 2015-2016 Sylvain Hallé, Université du Québec à Chicoutimi");
	}

	public static void main(String[] args)
	{
		Cli cli = new Cli(args);
		showHeader(System.out);
		cli.println(System.out, "You are running parkbench.jar, which is only a library to create\nyour own test suites. As a result nothing will happen here. Read the \nonline documentation to learn how to use ParkBench.", 1000);
	}

}
