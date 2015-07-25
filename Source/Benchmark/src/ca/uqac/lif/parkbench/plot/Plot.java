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
package ca.uqac.lif.parkbench.plot;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ca.uqac.lif.parkbench.Benchmark;
import ca.uqac.lif.parkbench.CommandRunner;
import ca.uqac.lif.parkbench.Test;

public abstract class Plot
{
	/**
	 * The terminal used for the plot's output
	 */
	public static enum Terminal {PDF, PNG, GIF, SVG, JPEG};

	/**
	 * The default terminal to use if none is specified
	 */
	public static final Terminal DEFAULT_TERMINAL = Terminal.PNG;

	/**
	 * The set of tests associated to that plot
	 */
	protected Set<Test> m_tests;

	/**
	 * The path to launch GnuPlot
	 */
	protected static String s_path = new GnuPlotCommand().grab();

	/**
	 * The plot's title
	 */
	protected String m_title;

	/**
	 * The plot's name. This is different from the plot's <em>title</em>, which
	 * is displayed in the graph
	 */
	protected String m_name;

	/**
	 * The time to wait before polling GnuPlot's result 
	 */
	protected static long s_waitInterval = 100;

	/**
	 * Creates an empty plot
	 * @param title The plot's title
	 */
	public Plot(String title)
	{
		super();
		m_title = title;
		m_tests = new HashSet<Test>();
		m_name = "";
	}

	/**
	 * Sets the plot's name
	 * @param name The name
	 * @return A pointer to this plot
	 */
	public Plot setName(String name)
	{
		m_name = name;
		return this;
	}

	/**
	 * Gets the plot's name.
	 * @return The name. If the name is empty, will return the plot's title.
	 */
	public String getName()
	{
		if (m_name.isEmpty())
		{
			return m_title;
		}
		return m_name;
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
		return toGnuPlot(DEFAULT_TERMINAL);
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
	public Plot setPath(String path)
	{
		s_path = path;
		return this;
	}

	public final byte[] getImage()
	{
		return getImage(DEFAULT_TERMINAL);
	}

	/**
	 * Runs GnuPlot on a file and returns the resulting graph
	 * @return The (binary) contents of the image produced by Gnuplot
	 */
	public final byte[] getImage(Terminal term)
	{
		String instructions = toGnuPlot(term);
		byte[] image = null;
		String[] command = {s_path};
		CommandRunner runner = new CommandRunner(command, instructions);
		runner.start();
		// Wait until the command is done
		while (runner.isAlive())
		{
			// Wait 0.1 s and check again
			try
			{
				Thread.sleep(s_waitInterval);
			}
			catch (InterruptedException e) 
			{
				// This happens if the user cancels the command manually
				runner.stopCommand();
				runner.interrupt();
				return null;
			}
		}
		image = runner.getBytes();
		return image;
	}

	/**
	 * Clears the plot
	 * @return
	 */
	public Plot clear()
	{
		m_tests.clear();
		return this;
	}

	/**
	 * Returns the terminal string associated to this plot 
	 * @param term The terminal
	 * @return A string understood by Gnuplot for the terminal's name
	 */
	public static String getTerminalString(Terminal term)
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

	public static Terminal stringToTerminal(String s)
	{
		s = s.trim();
		if (s.compareToIgnoreCase("png") == 0)
		{
			return Terminal.PNG;
		}
		if (s.compareToIgnoreCase("gif") == 0)
		{
			return Terminal.GIF;
		}
		if (s.compareToIgnoreCase("pdf") == 0)
		{
			return Terminal.PDF;
		}
		if (s.compareToIgnoreCase("svg") == 0)
		{
			return Terminal.SVG;
		}
		if (s.compareToIgnoreCase("jpg") == 0)
		{
			return Terminal.JPEG;
		}
		return DEFAULT_TERMINAL;
	}
}
