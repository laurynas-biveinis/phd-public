/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2006 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307,
USA

	http://www.xxl-library.de

bugs, requests for enhancements: request@xxl-library.de

If you want to be informed on new versions of XXL you can 
subscribe to our mailing-list. Send an email to 
	
	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body. 
*/

package xxl.applications.pipes.traffic;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import xxl.core.pipes.processors.SourceProcessor;
import xxl.core.pipes.sinks.VisualSink;
import xxl.core.pipes.sources.AbstractSource;
import xxl.core.util.timers.Timer;
import xxl.core.util.timers.TimerUtils;

/**
 * This class simulates a sensor from a highway ( see 
 * http://www.clearingstelle-verkehr.de/cs/verkehrsdaten/katalog/katalogberichte/fsp/fsp_metadaten)
 * It is initialised and provided from the class SourceFilesToSource. Usage examples are in Highway. 
 */
public class HighwaySource extends AbstractSource {

	/**
	 * Four hours in ms.
	 */
	public final static long FOUR_HOURS = 14400000;
	
	/**
	 * Nine hours in ms.
	 */
	public final static long NINE_HOURS = 32400000;

	/**
	 * One hour in ms.
	 */
	public final static long ONE_HOUR = 3600000;	
	
	/**
	 * If a time difference is greater than this value, it will be ignored, for example
	 * 1 hour 1 minute --> 0 ms
	 */
	public final static long SKIP_TIME =  ONE_HOUR;
	
	/**
	 * An array, containing ids that will be used in the transfer method.	 
	 */
	protected int[] ids;
	
	/**
	 * An array, containing files that will be used to deliver tuples.
	 */
	protected RandomAccessFile[] files;

	
	/**
	 * This array contains a timestamp from every source. the source with the lowest
	 * value will deliver the next tuple. 
	 */
	protected long[] times;
	
	/**
	 * Tokenizer[i] contains a line of files[i].
	 */
	protected StringTokenizer[] tokenizer;
	
	/**
	 * FileTokenizer is used to decompose the filenames. 
	 */
	protected StringTokenizer[] fileTokenizer;
	
	/**
	 * The minimal entry in the 'times' array.
	 */
	protected long timeMinimum;
	
	/**
	 * The source, that has currently the minimal entry in the times array.
	 */
	protected int selectedSource;
	
	/**
	 * A flag, signing the new values for timeMinimum has been computed. 
	 */	
	protected boolean minimumMaked;
	
	/**
	 * This long is needed to adapt the time values.
	 */
	protected long difference;
	
	/**
	* A factor to speed up or slow down the original tupel rate. for instance
	* 0,5 will increase the speed by 100%, 2 decrease the speed to 50% of the 
	* original rate.
	*/
	protected float factor;
	
	protected boolean syn;
	
	protected long synDifference;

	/**
	 * Creates a new HighwaySource instance. The arguments are used to determine,
	 * what files are needed and what lane number belongs to a file.
	 * @param fileNames all filenames for this source.
	 * @param factor a factor to speed up or slow down the original tupel rate. For instance
	 * 0,5 will increase the speed by 100%, 2 decrease the speed to 50% of the 
	 * original rate.
	 */
	public HighwaySource(String[] fileNames, float factor) {
		super();
		this.factor = factor;
		files = new RandomAccessFile[fileNames.length];
		ids = new int[fileNames.length];
		times = new long[ids.length];
		
		for (int i=0; i<fileNames.length; i++)
			ids[i] = fileNames[i].charAt(fileNames[i].length()-1)-48;
		tokenizer = new StringTokenizer[fileNames.length];
		fileTokenizer = new StringTokenizer[fileNames.length];
		for (int i=0; i< fileNames.length; i++)
			fileTokenizer[i] = new StringTokenizer(fileNames[i], "$");
		for (int i=0; i< ids.length; i++) {						
			try {
				files[i] = new RandomAccessFile(fileTokenizer[i].nextToken(), "r");
				String line = files[i].readLine();
				tokenizer[i] = new StringTokenizer(line == null ? " " : line);
				if (tokenizer[i].hasMoreTokens())times[i] = Long.parseLong(tokenizer[i].nextToken()) ;
				else times[i] = -1;
			}
			catch (IOException e) {							
				e.printStackTrace();
			} 
		}		
		final HighwaySource reference = this;
		processor = new SourceProcessor(
			new Iterator() {
				HighwaySource source = reference;
				public boolean hasNext(){
					return source.nextSleepTime() != -1;
				}
				public Object next() {
					return new Long(source.nextSleepTime());
				}
				public void remove() {
					throw new UnsupportedOperationException(); 		
				}
			}
			, (Timer) TimerUtils.FACTORY_METHOD.invoke()
			,false
		);
		processor.registerSource(this);
		timeMinimum = times[0];		
		selectedSource = 0;
		if (times.length >1)
			for (int i=1; i<times.length; i++) {
				if (times[i] != -1 && times[i] < timeMinimum) {
					timeMinimum = times[i];
					selectedSource = i;
				}
			}			
		difference = timeMinimum;		
		for (int i=0; i<times.length; i++) {
			if (times[i] != -1) {
				times[i] -= difference;
			}
		}
		System.out.print("");
	}
	
	/**
	 * Creates a new HighwaySource instance. the arguments are used to determine,
	 * what files are needed and what lane number belongs to a file.
	 * @param fileNames all filenames for this source.
	 */	
	public HighwaySource( String[] fileNames) {
		this(fileNames, 1);
	}
	
	/**
	 * Computes the minimal time from all times in the currently used
	 * line of each file in "files". It is used in an iterator for the sleep time
	 * of the sourceprocessor of this source. 
	 * @return int the next sleep time. 
	 */
	public long nextSleepTime() {		
		if (!minimumMaked) {
			long oldMinimum = timeMinimum; 
			timeMinimum = Long.MAX_VALUE;		
			selectedSource = 0;
			if (times.length >0)
				for (int i=0; i<times.length; i++) {
					if (times[i] != -1 && times[i] < timeMinimum) {
						timeMinimum = times[i];
						selectedSource = i+1;
					}
				}
			if (timeMinimum != Long.MAX_VALUE) {
				for (int i=0; i<times.length; i++) {
					if (times[i] != -1) 
						times[i] -= timeMinimum;
				}
				difference += timeMinimum ;
				
				if (timeMinimum - oldMinimum >= SKIP_TIME){
					timeMinimum = 0;
				}
			}
			if (syn) {
				for (long l:times)
					l += (long)(synDifference*factor);
				timeMinimum += (long)(synDifference*factor);
				syn = false;
			}
			minimumMaked = true;
		}
		return	timeMinimum != Long.MAX_VALUE? (long)(Math.round(timeMinimum * factor)): 0;
	}
	
	/**
	 * Computes the next object, that will be passed to the transfer method.
	 * @return Object the next object, that will be passed to the transfer method.
	 * @throws NoSuchElementException if all tuples have been passed.
	 */
	public Object next() throws NoSuchElementException {		
		if (timeMinimum == Long.MAX_VALUE)
			throw new NoSuchElementException();
		Vehicle v = null;
		try {
		v = new Vehicle(times[selectedSource-1]+difference, Integer.parseInt(tokenizer[selectedSource-1].nextToken()),
			Byte.parseByte(tokenizer[selectedSource-1].nextToken()), Double.parseDouble(tokenizer[selectedSource-1].nextToken()),
			Double.parseDouble(tokenizer[selectedSource-1].nextToken()), (tokenizer[selectedSource-1].nextToken()).charAt(0));
		} catch (Exception e) {						
			System.out.println("File might be corrupt");
			e.printStackTrace();
		}
		try {
			String s = files[selectedSource-1].readLine();
			if (s != null && s.length() >20)
				tokenizer[selectedSource-1] = new StringTokenizer(s);
			else {
				if (fileTokenizer[selectedSource-1].hasMoreTokens()) {						
					// while schleife fängt den fall ab, dass datei leer ist.										
					do {						
						files[selectedSource-1] = new RandomAccessFile(fileTokenizer[selectedSource-1].nextToken(),"r");
					} while(files[selectedSource-1].length()<20 && fileTokenizer[selectedSource-1].hasMoreTokens());
					if (files[selectedSource-1].length()<20)
						times[selectedSource-1] = -1;
					else {						
						s = files[selectedSource-1].readLine();
						tokenizer[selectedSource-1] = new StringTokenizer(s);
					}
				}
				else					
					times[selectedSource-1] = -1;
			}
			if (tokenizer[selectedSource-1].hasMoreTokens())
				times[selectedSource-1] = Long.parseLong(tokenizer[selectedSource-1].nextToken()) - difference;
			else 
				times[selectedSource-1] = -1;
			minimumMaked = false;
		} catch (IOException e) {						
			e.printStackTrace();
		}							
		return v;
	}
	
	public long getTMTimeStamp() {
			return difference + timeMinimum;		
	}

	public void synchronizeTime(long difference) {
		syn = true;
		synDifference = difference;
	}
	/**
	 * Executes an example if the right arguments are passed.
	 * @param args containing the following information: <br>
	 * args[0] = path to inputfileName. <br>
	 * It is assumed, that the highway files have beeen created.
	 * A lot of tests are in 'xxl.applications.pipes.Tests' (method: highwayTest)
	 */
	public static void main(String[] args) {
		System.out.println("A lot of tests are in 'xxl.applications.pipes.Tests' (method: highwayTest)");
		new VisualSink(Highway.getSourceByDate(args[0], new GregorianCalendar(1993, 3, 1).getTime(), 'N', 58700, (short)1, (float)0.1), true);
	}
}
