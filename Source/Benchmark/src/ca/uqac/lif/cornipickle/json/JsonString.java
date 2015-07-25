/*
    Cornipickle, validation of layout bugs in web applications
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
package ca.uqac.lif.cornipickle.json;

public class JsonString extends JsonElement
{
  String m_string;
  
  public JsonString(String s)
  {
    super();
    m_string = s;
  }
  
  public String stringValue()
  {
    return m_string;
  }
  
  @Override
  public String toString(String indent, boolean compact)
  {
    StringBuilder out = new StringBuilder();
    out.append("\"").append(m_string).append("\"");
    return out.toString();
  }
  
  public static String escape(String s)
  {
	  String out = new String(s);
	  out = out.replaceAll("\"", "\\\\\"");
	  out = out.replaceAll("\n", "\\\\n");
	  out = out.replaceAll("\t", "\\\\t");
	  return out;
  }
  
  @Override
  public int hashCode()
  {
	  return m_string.hashCode();
	  
  }
  
  @Override
  public boolean equals(Object o)
  {
	  if (o == null || !(o instanceof JsonString))
	  {
		  return false;
	  }
	  return equals((JsonString) o);
  }
  
  protected boolean equals(JsonString o)
  {
	  return m_string.compareTo(o.m_string) == 0;
  }

}
