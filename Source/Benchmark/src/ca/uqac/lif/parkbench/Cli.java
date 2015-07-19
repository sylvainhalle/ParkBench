package ca.uqac.lif.parkbench;

import java.io.PrintStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import ca.uqac.lif.cornipickle.util.AnsiPrinter;

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
	 * Command-line arguments
	 */
	protected String[] m_args;
	
	public Cli(String[] args)
	{
		super();
		m_args = args;
	}

	public void start(Benchmark benchmark)
	{
		String server_name = m_defaultServerName;
		int server_port = m_defaultPort;
		boolean interactive_mode = false;
		
		final AnsiPrinter stderr = new AnsiPrinter(System.err);
		final AnsiPrinter stdout = new AnsiPrinter(System.out);
		stdout.setForegroundColor(AnsiPrinter.Color.BLACK);
		stderr.setForegroundColor(AnsiPrinter.Color.BLACK);

		// Propertly close print streams when closing the program
		// https://www.securecoding.cert.org/confluence/display/java/FIO14-J.+Perform+proper+cleanup+at+program+termination
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
		{
			@Override
			public void run()
			{
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
		
		// The remaining arguments are the Cornipickle files to read
		if (interactive_mode)
		{
			BenchmarkServer server = new BenchmarkServer(server_name, server_port, benchmark);
			server.startServer();
			println(stdout, "Server started on " + server_name + ":" + server_port, 1);
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
		return options;
	}

	/**
	 * Show the benchmark's usage
	 * @param options The options created for the command line parser
	 */
	private static void showUsage(Options options)
	{
		HelpFormatter hf = new HelpFormatter();
		hf.printHelp("java -jar Barkbench.jar [options]", options);
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
		out.println("ParkBench, a versatile benchmark environment");
	}
	
	protected void println(PrintStream out, String message, int verbosity_level)
	{
		if (verbosity_level >= m_verbosity)
		{
			out.println(message);
		}
	}

}
