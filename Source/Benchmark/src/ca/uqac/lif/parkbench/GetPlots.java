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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import ca.uqac.lif.httpserver.CallbackResponse;
import ca.uqac.lif.httpserver.CallbackResponse.ContentType;
import ca.uqac.lif.httpserver.RequestCallback;
import ca.uqac.lif.httpserver.Server;
import ca.uqac.lif.parkbench.plot.PdftkCommand;
import ca.uqac.lif.parkbench.plot.Plot;

import com.sun.net.httpserver.HttpExchange;

/**
 * Creates a single PDF document where each page is a plot from the
 * benchmark
 */
public class GetPlots extends BenchmarkCallback
{
	
	/**
	 * Interval (in ms) before checking the output of the command again
	 */
	protected static long s_waitInterval = 100;
	
	public GetPlots(Benchmark b)
	{
		super(RequestCallback.Method.GET, "/all-plots", b);
	}
	
	@Override
	public CallbackResponse process(HttpExchange t)
	{
		CallbackResponse response = new CallbackResponse(t);
		Collection<Plot> all_plots = m_benchmark.getAllPlots();
		List<String> filenames = new LinkedList<String>();
		int file_cnt = 0;
		for (Plot plot : all_plots)
		{
			String filename = "plot-" + file_cnt + ".pdf";
			// Get plot's image and write to temporary file
			byte[] image = plot.getImage(Plot.Terminal.PDF);
			File f = new File(filename);
			try 
			{
				FileOutputStream fos = new FileOutputStream(f);
				fos.write(image, 0, image.length);
				fos.flush();
				fos.close();
				if (image.length > 0)
				{
					// Don't add filename if pdftk produced a zero-sized file
					filenames.add(filename);
				}
			}
			catch (FileNotFoundException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// Now run pdftk to merge all the plots into a single PDF
		List<String> command = new LinkedList<String>();
		command.add(new PdftkCommand().grab());
		command.addAll(filenames);
		command.add("cat");
		command.add("output");
		command.add("-");
		CommandRunner runner = new CommandRunner(command, null);
		runner.start();
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
		// pdftk is done; read the output
		byte[] file_contents = runner.getBytes();
		response.setContentType(ContentType.PDF);
		String filename = Server.urlEncode(m_benchmark.getName()) + ".pdf";
		response.setAttachment(filename);
		response.setContents(file_contents);
		return response;
	}	
}
