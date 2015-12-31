package ca.uqac.lif.parkbench;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import ca.uqac.lif.cornipickle.json.JsonMap;
import ca.uqac.lif.cornipickle.util.AnsiPrinter;
import ca.uqac.lif.util.FileReadWrite;

public class TextInterface
{
	private final Benchmark m_benchmark;

	private final AnsiPrinter m_stdout;

	private final BufferedReader m_console = new BufferedReader(new InputStreamReader(System.in));
	
	private int m_numColumns = 2;

	private static final AnsiPrinter.Color s_normalColor = AnsiPrinter.Color.BLACK;
	private static final AnsiPrinter.Color s_runningColor = AnsiPrinter.Color.LIGHT_GREEN;
	private static final AnsiPrinter.Color s_prereqColor = AnsiPrinter.Color.BROWN;
	private static final AnsiPrinter.Color s_readyColor = AnsiPrinter.Color.YELLOW;
	private static final AnsiPrinter.Color s_queuedColor = AnsiPrinter.Color.YELLOW;
	private static final AnsiPrinter.Color s_failedColor = AnsiPrinter.Color.RED;

	private final Set<Integer> m_selectedTests;

	public TextInterface(Benchmark b, AnsiPrinter stdout)
	{
		super();
		m_selectedTests = new HashSet<Integer>();
		m_benchmark = b;
		m_stdout = stdout;
	}

	public void run()
	{
		boolean run = true;
		while (run)
		{
			try {
				m_stdout.setForegroundColor(s_normalColor);
				m_stdout.println("(S)tatus (L)ist S(e)lect (A)pply Sett(i)ngs Sa(v)e (Q)uit ");
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
				else if (line.compareToIgnoreCase("A") == 0)
				{
					m_stdout.print("(S)tart S(t)op (R)eset (C)ancel");
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
		m_stdout.setForegroundColor(s_normalColor);
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
		int total_tests = 0;
		for (Test t : tests)
		{
			total_tests++;
			int id = t.getId();
			m_stdout.setForegroundColor(s_normalColor);
			if (m_selectedTests.contains(id))
				m_stdout.printf("[X]");
			else
				m_stdout.printf("[ ]");
			switch (t.getStatus())
			{
			case FAILED:
				m_stdout.setForegroundColor(s_failedColor);
				break;
			case RUNNING:
				m_stdout.setForegroundColor(s_runningColor);
				break;
			case PREREQUISITES:
				m_stdout.setForegroundColor(s_prereqColor);
				break;
			}
			m_stdout.printf(" %2d %s", id, padToLength(t.getName(), 16));
			Parameters params = t.getParameters();
			for (String param : parameter_names)
			{
				if (params.containsKey(param))
				{
					m_stdout.printf(" %s", padToLength(params.get(param).toString(), 8));
				}
				else
				{
					m_stdout.printf("         ", padToLength(params.get(param).toString(), 8));
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

}
