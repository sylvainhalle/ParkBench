/*
  Copyright 2015 Sylvain Hallé
  Laboratoire d'informatique formelle
  Université du Québec à Chicoutimi, Canada
  
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
      http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
package ca.uqac.lif.util;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CliParser
{
	protected Set<Argument> m_arguments;
	
	public CliParser()
	{
		super();
		m_arguments = new HashSet<Argument>();
	}
	
	/**
	 * Adds a command-line argument to the parser
	 * @param a The argument
	 */
	public void addArgument(Argument a)
	{
		m_arguments.add(a);
	}
	
	/**
	 * Parses an array of command-line arguments and returns the result
	 * @param cli The array of strings (typically, the <code>args</code>
	 *   argument from <code>main()</code>
	 * @return A map from parameter's <em>short</em> names to their values,
	 *   or <code>null</code> if the parsing failed
	 */
	public ArgumentMap parse(String[] cli)
	{
		ArgumentMap parsed = new ArgumentMap();
		Argument current_argument = null;
		String current_value = "";
		for (int i = 0; i < cli.length; i++)
		{
			String argument = cli[i];
			if (argument.startsWith("-"))
			{
				// Beginning of an argument
				String arg_name = "";
				if (argument.startsWith("--"))
				{
					// Beginning of long argument
					arg_name = argument.substring(2);
				}
				else
				{
					// Beginning of short argument
					arg_name = argument.substring(1);
				}
				if (arg_name.isEmpty())
				{
					// Only dashes with nothing else: error
					return null;
				}
				if (current_argument == null && !current_value.isEmpty())
				{
					// We were not parsing an argument, but we have a value: put
					// with the others
					parsed.putOther(arg_name);
					current_value = "";
				}
				else
				{
					boolean r_val = handleEndOfArgument(current_argument, current_value, parsed);
					if (r_val == false)
					{
						// Parsing error
						return null;
					}
					current_value = "";
					current_argument = findArgument(arg_name);
					if (current_argument == null)
					{
						// This argument does not exist
						return null;
					}
				}
			}
			else
			{
				if (current_value.isEmpty())
				{
					if (current_argument != null && current_argument.m_valueName == null)
					{
						boolean r_val = handleEndOfArgument(current_argument, current_value, parsed);
						if (r_val == false)
						{
							// Parsing error
							return null;
						}
						current_argument = null;
					}
					current_value = argument;
				}
				else
				{
					boolean r_val = handleEndOfArgument(current_argument, current_value, parsed);
					if (r_val == false)
					{
						// Parsing error
						return null;
					}
					current_argument = null;
					current_value = argument;
				}
			}
		}
		boolean r_val = handleEndOfArgument(current_argument, current_value, parsed);
		if (r_val == false)
		{
			// Parsing error
			return null;
		}
		return parsed;
	}
	
	private boolean handleEndOfArgument(Argument current_argument, String current_value, ArgumentMap parsed)
	{
		if (current_argument != null)
		{
			// We were parsing an argument
			if (current_argument.m_valueName != null && current_value.isEmpty())
			{
				// The arg we were parsing expects a value, but we have
				// none: error
				return false;
			}
			parsed.put(current_argument, current_value);
		}	
		else
		{
			if (!current_value.isEmpty())
			{
				parsed.putOther(current_value);
			}
		}
		return true;
	}
	
	private Argument findArgument(String name)
	{
		for (Argument arg : m_arguments)
		{
			if (arg.m_shortName != null && arg.m_shortName.compareTo(name) == 0)
			{
				return arg;
			}
			if (arg.m_longName != null && arg.m_longName.compareTo(name) == 0)
			{
				return arg;
			}
		}
		return null;
	}
	
	/**
	 * Prints the usage of each command line switch
	 * @param header A string to display before the help
	 * @param out The print stream to send the output to (typically
	 *   <code>System.out</code> or <code>System.err</code>
	 */
	public void printHelp(String header, PrintStream out)
	{
		StringBuilder output = new StringBuilder();
		output.append(header).append("\n\n");
		for (Argument arg : m_arguments)
		{
			if (arg.m_shortName != null)
			{
				output.append("-").append(arg.m_shortName).append(" ");
			}
			if (arg.m_longName != null)
			{
				output.append("--").append(arg.m_longName).append(" ");
			}
			if (arg.m_valueName != null)
			{
				output.append(arg.m_valueName);
			}
			if (arg.m_description != null)
			{
				output.append("\t").append(arg.m_description);
			}
			output.append("\n");
		}
		out.print(output);
	}
	
	public static class Argument
	{
		/**
		 * Whether the command-line switch is followed by a value
		 */
		String m_valueName = null;
		
		/**
		 * The short form of the argument; generally one letter
		 */
		String m_shortName = null;
		
		/**
		 * The long form of the argument
		 */
		String m_longName = null;
		
		/**
		 * The description of this argument
		 */
		String m_description = null;
		
		/**
		 * Constructs an empty argument
		 */
		public Argument()
		{
			super();
		}
		
		/**
		 * Set the short name of the argument
		 * @param name The short name
		 * @return This argument
		 */
		public Argument withShortName(String name)
		{
			m_shortName = name;
			return this;
		}
		
		/**
		 * Set the short name of the argument
		 * @param name The short name
		 * @return This argument
		 */
		public Argument withLongName(String name)
		{
			m_longName = name;
			return this;
		}
		
		/**
		 * Set the description for this argument
		 * @param description The description
		 * @return This argument
		 */		
		public Argument withDescription(String description)
		{
			m_description = description;
			return this;
		}
		
		/**
		 * Set the name of the value for this argument
		 * @param name The name of the value
		 * @return This argument
		 */
		public Argument withArgument(String name)
		{
			m_valueName = name;
			return this;
		}
		
		@Override
		public int hashCode()
		{
			if (m_shortName == null)
			{
				if (m_longName == null)
				{
					return 0;
				};
				return m_longName.hashCode();
			}
			return m_shortName.hashCode();
		}
		
		@Override
		public boolean equals(Object o)
		{
			if (o == null || !(o instanceof Argument))
			{
				return false;
			}
			Argument a = (Argument) o;
			return ((m_shortName == null && a.m_shortName == null) ||
					m_shortName.compareTo(a.m_shortName) == 0);
		}
	}
	
	public class ArgumentMap
	{
		/**
		 * Map of named parameters and their values
		 */
		private final Map<Argument,String> m_arguments;
		
		/**
		 * List of unnamed parameters
		 */
		private final List<String> m_others;
		
		/**
		 * Instantiates an argument map
		 */
		public ArgumentMap()
		{
			super();
			m_arguments = new HashMap<Argument,String>();
			m_others = new LinkedList<String>();
		}
		
		/**
		 * Checks if an option is present in the parsed command-line arguments
		 * @param name The option to look for
		 * @return <code>true</code> if the option is present
		 */
		public boolean hasOption(String name)
		{
			Argument a = findArgument(name);
			if (a == null)
			{
				return false;
			}
			return m_arguments.containsKey(a);
		}
		
		/**
		 * Gets the value of an argument
		 * @param name The <em>short</em> name of the argument
		 * @return The value, or <code>null</code> if the argument
		 *   is not present
		 */
		public String getOptionValue(String name)
		{
			Argument a = findArgument(name);
			if (a == null)
			{
				return null;
			}
			return m_arguments.get(a);
		}
		
		/**
		 * Returns the list of all unnamed arguments
		 * @return The list
		 */
		public List<String> getOthers()
		{
			return m_others;
		}
		
		/**
		 * Puts an argument into the map with its value
		 * @param name The argument
		 * @param value Its value
		 * @return The element just put
		 */
		String put(Argument a, String value)
		{
			return m_arguments.put(a, value);
		}
		
		/**
		 * Puts an unnamed argument into the map
		 * @param name The argument
		 * @return <code>true</code> if the element was added,
		 *   <code>false</code> otherwise
		 */
		boolean putOther(String name)
		{
			return m_others.add(name);
		}
		
		@Override
		public String toString()
		{
			StringBuilder out = new StringBuilder();
			out.append(m_arguments).append(",").append(m_others);
			return out.toString();
		}
	}
}
