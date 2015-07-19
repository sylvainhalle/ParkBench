package ca.uqac.lif.parkbench;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class GnuPlot
{
	/**
	 * The terminal used for the plot's output
	 */
	public static enum Terminal {PDF, PNG, GIF};
	
	/**
	 * The default terminal to use if none is specified
	 */
	protected static final Terminal s_defaultTerminal = Terminal.PNG;
	
	/**
	 * The set of tests associated to that plot
	 */
	protected Set<Test> m_tests;
	
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
}
