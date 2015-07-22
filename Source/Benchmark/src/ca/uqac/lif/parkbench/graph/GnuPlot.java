package ca.uqac.lif.parkbench.graph;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ca.uqac.lif.parkbench.Benchmark;
import ca.uqac.lif.parkbench.CommandRunner;
import ca.uqac.lif.parkbench.Test;

public abstract class GnuPlot
{
	/**
	 * The terminal used for the plot's output
	 */
	public static enum Terminal {PDF, PNG, GIF, SVG, JPEG};
	
	/**
	 * The default terminal to use if none is specified
	 */
	protected static final Terminal s_defaultTerminal = Terminal.PNG;
	
	/**
	 * The set of tests associated to that plot
	 */
	protected Set<Test> m_tests;
	
	/**
	 * The path to launch GnuPlot
	 */
	protected static String s_path = "gnuplot";
	
	/**
	 * The plot's title
	 */
	protected String m_title;
	
	/**
	 * Creates an empty plot
	 * @param title The plot's title
	 */
	public GnuPlot(String title)
	{
		super();
		m_title = title;
		m_tests = new HashSet<Test>();
	}
	
	/**
	 * Adds a test to the plot
	 * @param t The test to add
	 */
	public void addTest(Test t)
	{
		m_tests.add(t);
	}
	
	/**
	 * Adds a collection of tests to the plot
	 * @param tests The tests
	 */
	public void addTests(Collection<Test> tests)
	{
		m_tests.addAll(tests);
	}
	
	/**
	 * Add all tests from a benchmark to the plot
	 * @param b The benchmark
	 */
	public void addTests(Benchmark b)
	{
		addTests(b.getTests());
	}
	
	/**
	 * Creates a GnuPlot file from the plot's data
	 * @return A character string representing the GnuPlot file to
	 *   generate this plot
	 */
	public String toGnuPlot()
	{
		return toGnuPlot(s_defaultTerminal);
	}

	/**
	 * Creates a GnuPlot file from the plot's data
	 * @param term The terminal for the graph
	 * @return A character string representing the GnuPlot file to
	 *   generate this plot
	 */
	public abstract String toGnuPlot(Terminal term);
	
	/**
	 * Sets the path to run the GnuPlot executable
	 * @param path The path
	 * @return An instance of this GnuPlot object
	 */
	public GnuPlot setPath(String path)
	{
		s_path = path;
		return this;
	}
	
	/**
	 * Runs GnuPlot on a file and returns the resulting graph
	 * @return The (binary) contents of the image produced by Gnuplot
	 */
	public final byte[] getImage()
	{
		String instructions = toGnuPlot();
		byte[] image = null;
		String[] command = {s_path};
		try 
		{
			image = CommandRunner.runCommandBytes(command, instructions);
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return image;
	}
	
	/**
	 * Clears the plot
	 * @return
	 */
	public GnuPlot clear()
	{
		m_tests.clear();
		return this;
	}
	
	/**
	 * Returns the terminal string associated to this plot 
	 * @param term The terminal
	 * @return A string understood by Gnuplot for the terminal's name
	 */
	protected static String getTerminalString(Terminal term)
	{
		String out = "";
		switch (term)
		{
		case GIF:
			out = "gif";
			break;
		case PDF:
			out = "pdf";
			break;
		case PNG:
			out = "png";
			break;
		case SVG:
			out = "svg";
			break;
		case JPEG:
			out = "jpg";
			break;
		default:
			break;
		}
		return out;
	}
}
