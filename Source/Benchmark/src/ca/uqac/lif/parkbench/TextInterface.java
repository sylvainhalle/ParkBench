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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ca.uqac.lif.cornipickle.json.JsonMap;
import ca.uqac.lif.cornipickle.util.AnsiPrinter;
import ca.uqac.lif.cornipickle.util.AnsiPrinter.Color;
import ca.uqac.lif.parkbench.Test.Status;
import ca.uqac.lif.parkbench.plot.Plot;
import ca.uqac.lif.util.FileReadWrite;

/**
 * A simple text interface to interact with the contents of a benchmark.
 * In comparison with the web interface, the text interface is much lighter
 * in terms of resources. In particular, it does not auto-refresh the
 * benchmark's status and generates graphs only on demand (eliminating
 * periodic calls to Gnuplot). Otherwise, it provides the same functionalities
 * for selecting, running and saving tests. Use it in environments where
 * memory or CPU is limited.
 * 
 * @author Sylvain Hallé
 *
 */
public class TextInterface
{
	/**
	 * The benchmark this text interface will interact with
	 */
	private final Benchmark m_benchmark;

	/**
	 * A printer to the standard output
	 */
	private final AnsiPrinter m_stdout;

	/**
	 * A reader from the standard input
	 */
	private final BufferedReader m_console = new BufferedReader(new InputStreamReader(System.in));
	
	/**
	 * The number of columns used in the display (currently only used for
	 * the list of tests)
	 */
	private int m_numColumns = 2;
	
	// Status colors
	private static final AnsiPrinter.Color s_runningColor = AnsiPrinter.Color.LIGHT_GREEN;
	private static final AnsiPrinter.Color s_prereqColor = AnsiPrinter.Color.BROWN;
	//private static final AnsiPrinter.Color s_readyColor = AnsiPrinter.Color.YELLOW;
	private static final AnsiPrinter.Color s_queuedColor = AnsiPrinter.Color.LIGHT_GRAY;
	private static final AnsiPrinter.Color s_failedColor = AnsiPrinter.Color.RED;
	private static final AnsiPrinter.Color s_doneColor = AnsiPrinter.Color.GREEN;

	/**
	 * The set of tests currently selected
	 */
	private final Set<Integer> m_selectedTests;
	
	/**
	 * The set of plots currently selected
	 */
	private final Set<Integer> m_selectedPlots;

	/**
	 * Instantiates a text interface
	 * @param b The benchmark to interact with
	 * @param stdout A printer to the standard output
	 */
	public TextInterface(Benchmark b, AnsiPrinter stdout)
	{
		super();
		m_selectedTests = new HashSet<Integer>();
		m_selectedPlots = new HashSet<Integer>();
		m_benchmark = b;
		m_stdout = stdout;
	}

	/**
	 * Runs the text interface. This will start an interactive loop offering
	 * functions to the user.
	 */
	public void run()
	{
		boolean run = true;
		while (run)
		{
			try {
				m_stdout.resetColors();
				m_stdout.println("\nTests: (S)tatus  (L)ist  S(e)lect  (A)pply  Sett(i)ngs  Sa(v)e  (G)raphs  (?)Help  (Q)uit ");
				String line = null;

				line = m_console.readLine();
				if (line == null)
				{
					continue;
				}
				if (line.compareToIgnoreCase("Q") == 0)
				{
					run = false;
				}
				else if (line.compareToIgnoreCase("S") == 0)
				{
					printTestStatus();
					m_stdout.println("");
				}
				else if (line.compareToIgnoreCase("L") == 0)
				{
					printTestList();
				}
				else if (line.compareToIgnoreCase("?") == 0)
				{
					printHelp();
				}
				else if (line.compareToIgnoreCase("G") == 0)
				{
					// Get list of graphs
					plotMenu();
				}
				else if (line.compareToIgnoreCase("A") == 0)
				{
					m_stdout.print("(S)tart S(t)op (R)eset (C)ancel (D)etails ");
					String choice = m_console.readLine();
					if (choice == null || choice.compareToIgnoreCase("C") == 0)
					{
						continue;
					}
					if (choice.compareToIgnoreCase("S") == 0)
					{
						for (int test_num : m_selectedTests)
						{
							m_benchmark.queueTest(test_num);
						}
					}
					else if (choice.compareToIgnoreCase("D") == 0)
					{
						long sec_time = (System.nanoTime() / 1000000000);
						for (int test_num : m_selectedTests)
						{
							Test t = m_benchmark.getTest(test_num);
							m_stdout.printf("Test %d: %s %s\n", test_num, t.getName(), t.getStatus().toString());
							Parameters params = t.getParameters();
							if (t.getStatus() == Status.RUNNING)
							{
								Number start_time = (Number) params.get("starttime");
								m_stdout.printf("Started %d sec. ago", sec_time - start_time.longValue());
							}
							m_stdout.print("");
							for (String key : params.keySet())
							{
								String value = params.get(key).toString();
								m_stdout.printf("%s=%s ", key, value);
							}
							m_stdout.print("");
						}
					}
					else if (choice.compareToIgnoreCase("T") == 0)
					{
						for (int test_num : m_selectedTests)
						{
							m_benchmark.stopTest(test_num);
						}
					}
					else if (choice.compareToIgnoreCase("R") == 0)
					{
						for (int test_num : m_selectedTests)
						{
							m_benchmark.resetTest(test_num);
						}
					}
				}
				else if (line.compareToIgnoreCase("V") == 0)
				{
					// Save
					m_stdout.printf("Filename [%s]: ", m_benchmark.getName() + ".json");
					String filename = m_console.readLine();
					if (filename == null || filename.trim().isEmpty())
					{
						filename = m_benchmark.getName() + ".json";
					}
					FileReadWrite.writeToFile(filename, m_benchmark.serializeState().toString());
					m_stdout.printf("Benchmark saved to %s\n", filename);
				}
				else if (line.compareToIgnoreCase("E") == 0)
				{
					m_stdout.print("Select (a)ll/(n)one or number: ");
					String number = m_console.readLine();
					if (number.compareToIgnoreCase("a") == 0)
					{
						for (Test t : m_benchmark.getTests())
						{
							m_selectedTests.add(t.getId());
						}
					}
					else if (number.compareToIgnoreCase("n") == 0)
					{
						m_selectedTests.clear();
					}
					else
					{
						String[] t_nbs = number.split(",");
						for (String t_number : t_nbs)
						{
							int t_nb = Integer.parseInt(t_number.trim());
							if (m_selectedTests.contains(t_nb))
							{
								m_selectedTests.remove(t_nb);
							}
							else
							{
								m_selectedTests.add(t_nb);
							}						

						}
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	/**
	 * Prints a summary of tests
	 */
	private void printTestStatus()
	{
		JsonMap state = m_benchmark.serializeState();
		JsonMap status_map = (JsonMap) state.get("status");
		m_stdout.resetColors();
		m_stdout.printf("Queued: %2d Prereq: %2d Running: %2d Done: %2d Failed: %2d",
				status_map.getNumber("status-queued").intValue(),
				status_map.getNumber("status-prerequisites").intValue(),
				status_map.getNumber("status-running").intValue(),
				status_map.getNumber("status-done").intValue(),
				status_map.getNumber("status-failed").intValue());
	}

	/**
	 * Prints the list of tests registered in the benchmark
	 */
	private void printTestList()
	{
		Set<Test> tests = m_benchmark.getTests();
		String[] parameter_names = getParameterNames();
		m_stdout.print("\nList of tests\n-------------\n");
		int name_length = 16, param_length = 8; 
		for (int i = 0; i < m_numColumns; i++)
		{
			m_stdout.printf("    S   # %s ", padToLength("Name", name_length));
			for (String parameter_name : parameter_names)
			{
				m_stdout.printf("%s ", padToLength(parameter_name, param_length));
			}
			m_stdout.print(" ");
		}
		m_stdout.println("");
		int total_tests = 0;
		for (Test t : tests)
		{
			total_tests++;
			int id = t.getId();
			m_stdout.resetColors();
			if (m_selectedTests.contains(id))
				m_stdout.printf("[X]");
			else
				m_stdout.printf("[ ]");
			switch (t.getStatus())
			{
			case FAILED:
				m_stdout.fg(Color.BLACK).bg(s_failedColor).print(" F ");
				break;
			case QUEUED:
				m_stdout.fg(Color.BLACK).bg(s_queuedColor).print(" Q ");
				break;
			case RUNNING:
				m_stdout.fg(Color.BLACK).bg(s_runningColor).print(" R ");
				break;
			case PREREQUISITES:
				m_stdout.fg(Color.BLACK).bg(s_prereqColor).print(" P ");
				break;
			case NOT_DONE:
				if (t.prerequisitesFulilled(t.getParameters()))
				{
					m_stdout.resetColors().print("   ");
				}
				else
				{
					m_stdout.fg(Color.BLACK).bg(s_prereqColor).print(" p ");
				}
				break;
			case DONE:
				m_stdout.fg(Color.BLACK).bg(s_doneColor).print(" D ");
				break;
			case TIMEOUT:
				m_stdout.fg(Color.BLACK).bg(s_failedColor).print(" T ");
				break;
			}
			m_stdout.resetColors().printf(" %2d %s", id, padToLength(t.getName(), 16));
			Parameters params = t.getParameters();
			for (String param : parameter_names)
			{
				if (params.containsKey(param))
				{
					Object p_value = params.get(param);
					String p_value_string = "";
					if (p_value != null)
					{
						p_value_string = p_value.toString();
					}
					m_stdout.printf(" %s", padToLength(p_value_string, 8));
				}
				else
				{
					m_stdout.printf("         ", padToLength("", 8));
				}
			}
			if (total_tests % m_numColumns == 0)
			{
				m_stdout.println();
			}
			else
			{
				m_stdout.print("  ");
			}
		}
	}
	
	private void printHelp()
	{
		m_stdout.println("Help is on the way");
	}

	private String[] getParameterNames()
	{
		Set<String> p_names = m_benchmark.getTestParameterNames();
		String[] param_names = new String[p_names.size()];
		int i = 0;
		for (String p_name : p_names)
		{
			param_names[i++] = p_name;
		}
		return param_names;

	}

	private static String padToLength(String s, int length)
	{
		if (s == null)
		{
			s = "";
		}
		int str_len = s.length();
		if (str_len == length)
		{
			return s;
		}
		if (str_len > length)
		{
			return s.substring(0, length);
		}
		StringBuilder sb = new StringBuilder();
		sb.append(s);
		for (int i = str_len; i < length; i++)
		{
			sb.append(" ");
		}
		return sb.toString();
	}
	
	private void plotMenu()
	{
		boolean run = true;
		while (run)
		{
			try
			{
				printPlotList();
				m_stdout.resetColors()
				.print("\nGraphs: S(e)lect S(h)ow  Sa(v)e  Back to (t)ests ");
				String line = null;
				line = m_console.readLine();
				if (line == null)
				{
					continue;
				}
				if (line.compareToIgnoreCase("T") == 0)
				{
					run = false;
				}
				else if (line.compareToIgnoreCase("E") == 0)
				{
					m_stdout.print("Select (a)ll/(n)one or number: ");
					String number = m_console.readLine();
					if (number.compareToIgnoreCase("a") == 0)
					{
						for (Plot p : m_benchmark.getAllPlots())
						{
							m_selectedPlots.add(p.getId());
						}
					}
					else if (number.compareToIgnoreCase("n") == 0)
					{
						m_selectedPlots.clear();
					}
					else
					{
						String[] t_nbs = number.split(",");
						for (String t_number : t_nbs)
						{
							int t_nb = Integer.parseInt(t_number.trim());
							if (m_selectedPlots.contains(t_nb))
							{
								m_selectedPlots.remove(t_nb);
							}
							else
							{
								m_selectedPlots.add(t_nb);
							}						
						}
					}
				}
				else if (line.compareToIgnoreCase("H") == 0)
				{
					for (Plot p : m_benchmark.getAllPlots())
					{
						if (m_selectedPlots.contains(p.getId()))
						{
							byte[] image_bytes = p.getImage(Plot.Terminal.DUMB);
							String image_text = new String(image_bytes);
							m_stdout.printf("%s\n",  image_text);
						}
					}					
				}
				else if (line.compareToIgnoreCase("V") == 0)
				{
					for (Plot p : m_benchmark.getAllPlots())
					{
						if (m_selectedPlots.contains(p.getId()))
						{
							String plot_title = p.getName();
							m_stdout.printf("Saving %s.gp\n", plot_title);
							FileReadWrite.writeToFile(plot_title + ".gp", p.toGnuPlot());							
						}
					}
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
				break;
			}
			
		}
	}
	
	private void printPlotList()
	{
		Collection<Plot> plot_set = m_benchmark.getAllPlots();
		m_stdout.print("\nList of plots\n-------------\n");
		for (Plot p : plot_set)
		{
			String plot_name = p.getName();
			int plot_id = p.getId();
			if (m_selectedPlots.contains(plot_id))
			{
				m_stdout.printf("[X] ");
			}
			else
			{
				m_stdout.printf("[ ] ");
			}
			m_stdout.printf("%2d %s\n", plot_id, padToLength(plot_name, 16));
		}
	}

}
