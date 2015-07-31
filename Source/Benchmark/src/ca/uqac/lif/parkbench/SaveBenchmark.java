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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ca.uqac.lif.cornipickle.json.JsonMap;
import ca.uqac.lif.httpserver.CallbackResponse;
import ca.uqac.lif.httpserver.RequestCallback;
import ca.uqac.lif.httpserver.Server;

import com.sun.net.httpserver.HttpExchange;

public class SaveBenchmark extends BenchmarkCallback
{
	/**
	 * Whether to zip the response
	 */
	protected static final boolean s_zip = true;
	
	public SaveBenchmark(Benchmark b)
	{
		super(RequestCallback.Method.GET, "/save", b);
	}

	@Override
	public CallbackResponse process(HttpExchange t)
	{
		JsonMap out = m_benchmark.serializeState();
		CallbackResponse response = new CallbackResponse(t);
		String filename = Server.urlEncode(m_benchmark.getName());
		if (s_zip)
		{
			// zip contents of JSON
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ZipOutputStream zos = new ZipOutputStream(bos);
			ZipEntry ze = new ZipEntry("Status.json");
			try 
			{
				zos.putNextEntry(ze);
				zos.write(out.toString().getBytes());
				zos.closeEntry();
				zos.close();
			}
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			response.setContents(bos.toByteArray());
			response.setContentType(CallbackResponse.ContentType.ZIP);	
			filename += ".zip";
		}
		else
		{
			// Send in clear text
			response.setContents(out.toString("", true));
			response.setContentType(CallbackResponse.ContentType.JSON);	
			filename += ".json";
		}
		// Tell the browser to download the document rather than display it
		response.setAttachment(filename);
		return response;
	}	
}