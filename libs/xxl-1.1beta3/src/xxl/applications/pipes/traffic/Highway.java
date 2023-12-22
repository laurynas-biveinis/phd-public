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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import xxl.core.cursors.AbstractMetaDataCursor;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.MetaDataCursor;
import xxl.core.cursors.sources.io.FileInputCursor;
import xxl.core.cursors.unions.Sequentializer;
import xxl.core.io.converters.Converter;
import xxl.core.pipes.sinks.VisualSink;
import xxl.core.relational.metaData.ColumnMetaDataResultSetMetaData;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.relational.metaData.StoredColumnMetaData;
import xxl.core.util.metaData.CompositeMetaData;

/**
 * This class contains methods to connect some real data from a highway to xxl
 * (http://www.clearingstelle-verkehr.de/cs/verkehrsdaten/katalog/katalogberichte/fsp/fsp_metadaten).
 * The data can be used in xxl as source with the getSourceByDate() methods or as cursor with the getCursorBydate()
 * methods. <br>
 * Before using the source methods, the original data have to be transformed with the the makeAllSourceFiles method, that
 * divide the large files to several small. the format of the generated files is as follows: <br>
 * " time(long) \t distance to Marina(int) \t laneNumber(byte) \t speed(double) \t length(double) \t direction"
 * the time is measured in ms after January 1, 1970, 00:00:00 GMT.
 * (The original data format : distance to Marina, laneNumber, time, speed, length)
 *  The cursor methods allow the use of both data by selecting the appropriate Converter.<br>
 *  Important: The original files need to be converted. Use 
 *  Highway.makeAllSourcesFiles(<path to files directory>, <path to separate tempdirectory>);
 *  to do that or start the main method of Highway with the corresponding two arguments.
 */
public class Highway {
		
	/**
	 * Constant, taking part in the name of generated files.
	 */
	protected static final String LANE_PREFIX = "_lane";
	
	/**
	 * Constant, the value is the position of the direction (N or S) in the original file. 
	 */
	protected static final int DIRECTION_POS = 9 ;
	
	/**
	 * Constant, the value is the position of the direction (N or S) in the generated file. 
	 */
	protected static final int DIRECTION_POS_NEW = 11 ;
	
	/**
	 * The position of the day in the original file.
	 */
	protected static final int DAY_POS = 4 ;
	
	/**
	 * The position of the month in the original file.
	 */
	protected static final int MONTH_POS = 2 ;
	
	/**
	 * The position of the year in the original file.
	 */
	protected static final int YEAR_POS = 6 ;
	
	/**
	 * A constant containing the number of data the original file.
	 * These are 5: distance to Marina, laneNumber, time, speed, length.
	 */
	protected static final int NUMBER_OF_ORG_DATA = 5;
	
	/**
	 * A constant containing the number of data in a generated file.
	 * These are currently 6: time, distance to Marina, laneNumber, speed, length & direction.
	 */
	protected static final int NUMBER_OF_MODIFIED_DATA = 6;
	
	/**
	 * The position of the distance to the city marina in the generated file.
	 */
	protected static final int DISTANCE_TO_CITY_POS=13;
	
	/**
	 * A constant, signing that all lanes are interesting.
	 */
	protected static final short ALL_LANES = -1;
	
	/**
	 * A constant, signing that all sensors are interesting.
	 */
	protected static final int ALL_SENSORS = -1;
	
	/**
	 * This char is used to separate different filenames in a string. It it used by highwaysource to determine
	 * the next file, if the current delivered al tuples.  
	 */
	public static final char SEPARATOR = '$'; 
	
	/**
	 * Provides a Converter, that reads or writes modified highway files.
	 * These files contain the follwing information in one line: 
	 * distance to Marina, laneNumber, time, speed, length	 
	 * @return Converter
	 */
	public static Converter originalDataConverter() {
		return new Converter(){
			
			/**
			 * Returns a object[] instance by reading one line of the underlying file. The 
			 * result contains distance to Marina, laneNumber, time, speed, length.
			 */
			public Object read(DataInput dataInput, Object object) throws IOException {
				String s = dataInput.readLine(); 
				if (s == null) throw new EOFException();
				StringTokenizer st =  new StringTokenizer(s);
				Object[] data = new Object[NUMBER_OF_ORG_DATA];
				int i;
				for (i=0; i < data.length && st.hasMoreTokens();i++) {
					switch (i) {
						case 0: {
							data[i] = new Integer(Integer.parseInt(st.nextToken()));							
							break;
						}
						case 1: {
							data[i] = new Byte(Byte.parseByte(st.nextToken()));
							break;
						}
						case 2: {
							data[i] = new Long(Long.parseLong(st.nextToken()));	
							break;
						}
						case 3: {
							try {
								data[i] = new Double(Double.parseDouble(st.nextToken()));	
							}
							catch(NumberFormatException e) {
								// ignore invalid values
								data[i] = new Double(0);
							}							
							break;
						}
						case 4: {
							try {
								data[i] = new Double(Double.parseDouble(st.nextToken()));	
							}
							catch(NumberFormatException e) {
								// ignore invalid values
								data[i] = new Double(0);
							}
							break;
						}
					}					
				}
				if (i < NUMBER_OF_ORG_DATA-1 || st.hasMoreElements()) 
					throw new IllegalStateException("expected data size doesn't match found data size");
				return data;
			}
			
			/**
			 * Writes a object[] instance to a file. It is assumed, that the array length is NUMBER_OF_MODIFIED_DATA
			 * long and that it contains distance to Marina, laneNumber, time, speed, length.			 
			 */
			public void write(DataOutput dataOutput, Object object) throws IOException {
				dataOutput.writeBytes(((Integer)((Object[])object)[0]).toString() +"\t"); 
				dataOutput.writeBytes(((Byte)((Object[])object)[1]).toString() +"\t");
				dataOutput.writeBytes(((Long)((Object[])object)[2]).toString() +"\t");
				dataOutput.writeBytes(((Double)((Object[])object)[3]).toString() +"\t");
				dataOutput.writeBytes(((Double)((Object[])object)[4]).toString() +"\r\n");
			}
			
		};
	}
	
	/**
	 * Provides a Converter, that reads or writes modified highway files. These files
	 * contain the follwing information in one line: 
	 * time, distance to Marina, laneNumber, speed, length & direction
	 * @return Converter
	 */
	public static Converter modifiedDataConverter() {
		return new Converter(){
			/**
			 * Returns a object[] instance by reading one line of the underlying file. The 
			 * result contains time, distance to Marina, laneNumber, speed, length & direction.
			 */
			public Object read(DataInput dataInput, Object object) throws IOException {
				String s = dataInput.readLine(); 
				if (s == null) throw new EOFException();
				StringTokenizer st =  new StringTokenizer(s);
				Object[] data = new Object[NUMBER_OF_MODIFIED_DATA];
				int i;
				for (i=0; i < data.length && st.hasMoreTokens();i++) {
					switch (i) {
						case 0: {
							data[i] = new Long(Long.parseLong(st.nextToken()));
							break;
						}
						case 1: {
							data[i] = new Integer(Integer.parseInt(st.nextToken()));
							break;
						}
						case 2: {
							data[i] = new Byte(Byte.parseByte(st.nextToken()));
							break;
						}
						case 3: {
							data[i] = new Double(Double.parseDouble(st.nextToken()));
							break;
						}
						case 4: {
							data[i] = new Double(Double.parseDouble(st.nextToken())); 
							break;
						}
						case 5: {
							data[i] = new Character(st.nextToken().charAt(0)); 
							break;
						}
					}					
				}
				if (i < NUMBER_OF_MODIFIED_DATA-1 || st.hasMoreElements()) 
					throw new IllegalStateException("expected data size doesn't match found data size");
				return data;
			}
			
			/**
			 * Writes a object[] instance to a file. It is assumed, that the array length is NUMBER_OF_MODIFIED_DATA
			 * long and that it contains time, distance to Marina, laneNumber, speed, length & direction.			 
			 */
			public void write(DataOutput dataOutput, Object object) throws IOException {
				dataOutput.writeBytes(((Long)((Object[])object)[0]).toString() +"\t"); 
				dataOutput.writeBytes(((Integer)((Object[])object)[1]).toString() +"\t");
				dataOutput.writeBytes(((Double)((Object[])object)[2]).toString() +"\t");
				dataOutput.writeBytes(((Byte)((Object[])object)[3]).toString() +"\t");
				dataOutput.writeBytes(((Double)((Object[])object)[4]).toString() +"\t");
				dataOutput.writeBytes(((Character)((Object[])object)[5]).toString() +"\r\n");
			}
			
		};
	}

	/**
	 * Provides a Converter, that reads or writes modified highway files. These files
	 * contain the following information in one line: 
	 * time, distance to Marina, laneNumber, speed, length & direction 
	 * @return Converter
	 */
	public static Converter modifiedDataToVehicleConverter() {
		return new Converter(){

			/**
			 * Returns a new vehicle instance by reading one line of the underlying file.
			 */
			public Object read(DataInput dataInput, Object object) throws IOException {
				String s = dataInput.readLine(); 
				if (s == null) throw new EOFException();
				StringTokenizer st =  new StringTokenizer(s);				
				long time = Long.parseLong(st.nextToken());
				int distance = Integer.parseInt(st.nextToken());
				byte lane= Byte.parseByte(st.nextToken());
				double speed = Double.parseDouble(st.nextToken());
				double length = Double.parseDouble(st.nextToken());
				char direction = st.nextToken().charAt(0);
				return new Vehicle(time, distance, lane, speed, length, direction);				

			}

			/**
			 * Writes a vehicle instance to a file.
			 */
			public void write(DataOutput dataOutput, Object object) throws IOException {
				dataOutput.writeBytes(((Vehicle)object).getTimeStamp()+"\t");
				dataOutput.writeBytes(((Vehicle)object).getDistanceToCity()+"\t");
				dataOutput.writeBytes(((Vehicle)object).getLane()+"\t");
				dataOutput.writeBytes(((Vehicle)object).getSpeed()+"\t");
				dataOutput.writeBytes(((Vehicle)object).getLength()+"\t");
				dataOutput.writeBytes(((Vehicle)object).getDirection()+"\r\n");				
			}			
		};
	}
	
	/**
	 * The default MetadataObject for the modified highway files.
	 * @return the default MetadataObject for the modified highway files.
	 */
	public final static ResultSetMetaData GET_MODIFIED_METADATA() {
		
		return new ColumnMetaDataResultSetMetaData(
			new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, true, 18, "time",               "time",               "", 18, 0, "", "", Types.BIGINT,   true, false, false),
			new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, true,  9, "distance to marina", "distance to marina", "",  9, 0, "", "", Types.INTEGER,  true, false, false),
			new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, true,  7, "lane",               "lane",               "",  7, 0, "", "", Types.SMALLINT, true, false, false),
			new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, true, 15, "speed",              "speed",              "", 15, 0, "", "", Types.DOUBLE,   true, false, false),
			new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, true, 15, "length",             "length",             "", 15, 0, "", "", Types.DOUBLE,   true, false, false),
			new StoredColumnMetaData(false, true,  true, false, ResultSetMetaData.columnNullable, false, 1, "direction",          "direction",          "",  0, 0, "", "", Types.VARCHAR,  true, false, false)
		);
	}
	
	/**
	 * The default MetadataObject for the original highway files.
	 * @return the default MetadataObject for the original highway files.
	 */
	public final static ResultSetMetaData GET_ORIGINAL_METADATA() {
		
		return new ColumnMetaDataResultSetMetaData(
			new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, true,  9, "distance to marina", "distance to marina", "",  9, 0, "", "", Types.INTEGER,  true, false, false),
			new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, true,  7, "lane",               "lane",               "",  7, 0, "", "", Types.SMALLINT, true, false, false),
			new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, true, 18, "time",               "time",               "", 18, 0, "", "", Types.BIGINT,   true, false, false),
			new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, true, 15, "speed",              "speed",              "", 15, 0, "", "", Types.DOUBLE,   true, false, false),
			new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, true, 15, "length",             "length",             "", 15, 0, "", "", Types.DOUBLE,   true, false, false)
		);
	}

	
	/**
	 * The outputname depends on the given file inputFileName. example: if the inputFile is 
	 * "lp021693_N_speeds_and_lengths.dat" outputFileName will be "16-02-1993-N"
	 * (the rest of the filename depends on the distance to the city marina and the number
	 * of the lane, example: "16-02-1993-N_57300_lane1").
	 */
	protected static String outputFileName;
	
	/**
	 * A list containing all absolute paths and filenames of the temp files.
	 */
	protected static LinkedList fileList;
	
	/**
	 * This long is computed with the name of the fileName.
	 */
	protected static long basisDate;
	
	/**
	 * This char is computed with the name of the fileName.
	 */
	protected static char direction;
	
	
	/**
	 * Computes basisDate, direction and a outputFileName of a highway file.
	 * the outputname depends on the given file inputFileName. example: If the inputFile is 
	 * "lp021693_N_speeds_and_lengths.dat" the names of the generated files are
	 * 16-02-1993-N-81840-lane_0 (where 81840 is the distance to the city marina).
	 * @param inputFileName ther name of the input file.
	 */
	protected static void computeValues(String inputFileName) {				
		String month = inputFileName.substring(MONTH_POS,MONTH_POS+2);
		String day = inputFileName.substring(DAY_POS,DAY_POS+2);
		String year = inputFileName.substring(YEAR_POS,YEAR_POS+2);
		direction = inputFileName.charAt(DIRECTION_POS);		
		DateFormat d = DateFormat.getDateInstance();		
		Date date = null;
		try {
			date = (d.parse(day+"."+month+".19"+year));
		}
		catch (ParseException e) {				
			e.printStackTrace();
		}		
		basisDate = date.getTime();
		outputFileName = day+"-"+month+"-19"+year+"-"+direction;		
	}

	/**
	 * Returns an array of MetaDataCursor, that match the given date
	 * in the given directory.
	 * @param path the directory, where generated files are stored.
	 * @param date the date.
	 * @param converter the converter for the underlying fileinputcursor.
	 * @param md the resultsetmetadata for the cursor.
	 * @return MetaDataCursor[] 
	 */ 
	public static MetaDataCursor[] getMDCursorByDate(String path, Date date, Converter converter, final ResultSetMetaData md) {
		return getMDCursorByDate(path, date, ' ', converter, md);
	}
	
	/**
	 * Returns an array of MetaDataCursor, that match the given date
	 * and the given direction in the given directory.
	 * @param path the directory, where generated files are stored
	 * @param date the date
	 * @param direction the direction possible values are 'N' for north, 'S' for south or ' '
	 * for both
	 * @param converter the converter for the underlying fileinputcursor.
	 * @param md the resultsetmetadata for the cursor.
	 * @return MetaDataCursor[]
	 */
	public static MetaDataCursor[] getMDCursorByDate(String path, Date date, char direction, Converter converter, ResultSetMetaData md) {
		Cursor[] cursors = getCursorByDate(path, date, direction, converter);
		AbstractMetaDataCursor[] mdCursors = new AbstractMetaDataCursor[cursors.length];
		final CompositeMetaData<Object, Object> metadata = new CompositeMetaData<Object, Object>();
		metadata.add(ResultSetMetaDatas.RESULTSET_METADATA_TYPE, md);
		for (int i=0; i<cursors.length; i++) {
			mdCursors[i] = new AbstractMetaDataCursor(cursors[i]) {
				public Object getMetaData(){
					return metadata;
				}
			};
		}
		return mdCursors;		
	}
	/**
	* Returns an array of MetaDataCursor, that match the given date, the given direction and the given distance to
	* the city marina in the given directory.
	* @param path the directory, where generated files are stored.
	* @param date the date.
	* @param direction the direction possible values are 'N' for north, 'S' for south or ' '
	* for both.
	* @param distanceToCity the distance to the city marina. if a value less than 1 is given,
	* this parameter will be ignored.
    * @param lane the selected lane number. If a value less than 0 is given,
	* this parameter will be ignored.
	* @param converter the converter for the underlying fileinputcursor.
	* @param md the resultsetmetadata for the cursor.
	* @return MetaDataCursor[]
	*/	
	public static MetaDataCursor getMDCursorByDate(String path, Date date, char direction, int distanceToCity, short lane, Converter converter, final ResultSetMetaData md) {		
		final CompositeMetaData<Object, Object> metadata = new CompositeMetaData<Object, Object>();
		metadata.add(ResultSetMetaDatas.RESULTSET_METADATA_TYPE, md);
		return new AbstractMetaDataCursor( getCursorByDate(path, date, direction, distanceToCity, lane, converter)) {
			public Object getMetaData() {
				return metadata;
			}
		};
	}
	
	// ----------------------------------------------------------------------------------------------
	
	/**
	 *Returns an array of Cursor, that match the given date
	 * in the given directory.
	 * @param path the directory, where generated files are stored.
 	 * @param from the startdate including the startdate.
	 * @param to the enddate excluding the enddate.
	 * @param converter the converter that is used to get the data.
	 * @return Cursor[] 
	 */
	public static Cursor[] getCursorsByDate(String path, Date from, Date to, Converter converter) {
		return getCursorsByDate(path, from, to, ' ', converter);		
	}
	
	/**
	 * returns an array of Cursor, that match the given date
	 * and the given direction in the given directory.
	 * @param path the directory, where generated files are stored
	 * @param from the startdate including the startdate.
	 * @param to the enddate excluding the enddate.
	 * @param direction the direction possible values are 'N' for north, 'S' for south or ' '
	 * for both
	 * @param converter the converter that is used to get the data
	 * @return Cursor[]
	 */
	public static Cursor[] getCursorsByDate(String path, Date from, Date to, char direction, Converter converter) {
		return getCursorsByDate(path, from, to, direction, ALL_SENSORS,  converter);	
	}
	
	/**
	* Returns an array of Cursor, that match the given date, the given direction and the given distance to
	* the city marina in the given directory.
	* @param path the directory, where generated files are stored.
	* @param from the startdate including the startdate.
	* @param to the enddate excluding the enddate.
	* @param direction the direction possible values are 'N' for north, 'S' for south or ' '
	* for both
	* @param distanceToCity the distance to the city marina. if a value less than 1 is given,
	* this parameter will be ignored.
	* @param converter the converter that is used to get the data
	* @return Cursor[]
	*/
	public static Cursor[] getCursorsByDate(String path, Date from, Date to, char direction, int distanceToCity, Converter converter) {
		List list = findFiles(path, from, to, direction, distanceToCity, ALL_LANES);			
		int count = 0;		
		for (Iterator it = list.iterator(); it.hasNext();it.next())
			count++;		
		Sequentializer[] seqs = new Sequentializer[count];		
		count = 0;
		for (Iterator it = list.iterator(); it.hasNext();) {
			String s = (String) it.next();
			int count2 = 0;		
			for (StringTokenizer tok = new StringTokenizer(s, ""+SEPARATOR); tok.hasMoreTokens(); tok.nextToken())
				count2++;
			FileInputCursor[] cursors = new FileInputCursor[count2];
			count2 =0;
			for (StringTokenizer tok = new StringTokenizer(s, ""+SEPARATOR); tok.hasMoreTokens(); )
				cursors[count2++] = new FileInputCursor(converter, new File(tok.nextToken()));
			seqs[count++] = new Sequentializer(cursors);
		}				
		return seqs;		
	}
	
	/**
	* Returns a Cursor, that match the given date, the given direction and the given distance to
	* the city marina in the given directory.
	* @param path the directory, where generated files are stored
	* @param from the startdate including the startdate.
	* @param to the enddate excluding the enddate.
	* @param direction the direction possible values are 'N' for north, 'S' for south or ' '
	* for both
	* @param distanceToCity the distance to the city marina. if a value less than 1 is given,
	* this parameter will be ignored.
    * @param lane the selected lane number. If a value less than 0 is given,
	* this parameter will be ignored.
	* @param converter the converter that is used to get the data
	* @return Cursor
	*/
	public static Cursor getCursorByDate(String path, Date from, Date to, char direction, int distanceToCity, short lane, Converter converter) {	
		String s = (String)findFiles(path, from, to, direction, distanceToCity, lane).get(0);
		int count = 0;		
		for (StringTokenizer tok = new StringTokenizer(s, ""+SEPARATOR); tok.hasMoreTokens(); tok.nextToken())
			count++;
		FileInputCursor[] cursors = new FileInputCursor[count];
		count =0;
		for (StringTokenizer tok = new StringTokenizer(s, ""+SEPARATOR); tok.hasMoreTokens(); )
			cursors[count++] = new FileInputCursor(converter, new File(tok.nextToken()));
		return new Sequentializer(cursors);
	}

	// ----------------------------------------------------------------------------------------------

	/**
	 * Returns an array of Cursor, that match the given date
	 * in the given directory
	 * @param path the directory, where generated files are stored
	 * @param date the date
	 * @param converter the converter that is used to get the data
	 * @return Cursor[] 
	 */
	public static Cursor[] getCursorsByDate(String path, Date date, Converter converter) {
		return getCursorByDate(path, date, ' ', converter);
	}
	
	/**
	 * Returns an array of Cursor, that match the given date
	 * and the given direction in the given directory.
	 * @param path the directory, where generated files are stored
	 * @param date the date
	 * @param direction the direction possible values are 'N' for north, 'S' for south or ' '
	 * for both
	 * @param converter the converter that is used to get the data
	 * @return Cursor[]
	 */
	public static Cursor[] getCursorByDate(String path, Date date, char direction, Converter converter) {
		return getCursorsByDate(path, date, direction, ALL_SENSORS,  converter);
	}
	
	/**
	* Returns an array of Cursor, that match the given date, the given direction and the given distance to
	* the city marina in the given directory.
	* @param path the directory, where generated files are stored
	* @param date the date
	* @param direction the direction possible values are 'N' for north, 'S' for south or ' '
	* for both
	* @param distanceToCity the distance to the city marina. if a value less than 1 is given,
	* this parameter will be ignored.
	* @param converter the converter that is used to get the data
	* @return Cursor[]
	*/
	public static Cursor[] getCursorsByDate(String path, Date date, char direction, int distanceToCity, Converter converter) {
		List list = findFiles(path, date, direction, distanceToCity, ALL_LANES);			
		int count = 0;
		for (Iterator it = list.iterator(); it.hasNext();)			
			for (Iterator it2 = ((List)it.next()).iterator(); it2.hasNext(); count++, it2.next()) 
				;			
		
		FileInputCursor[] cursors = new FileInputCursor[count];		
		count = 0;
		for (Iterator it = list.iterator(); it.hasNext();)			
			for (Iterator it2 = ((List)it.next()).iterator(); it2.hasNext();) 
				cursors[count++] = new FileInputCursor(converter, new File((String) it2.next()));
		return cursors;
	}
	
   /**
	* Returns a Cursor, that match the given date, the given direction and the given distance to
	* the city marina in the given directory.
	* @param path the directory, where generated files are stored
	* @param date the date
	* @param direction the direction possible values are 'N' for north, 'S' for south or ' '
	* for both
	* @param distanceToCity the distance to the city marina. if a value less than 1 is given,
	* this parameter will be ignored.
    * @param lane the selected lane number. If a value less than 0 is given,
	* this parameter will be ignored.
	* @param converter the converter that is used to get the data
	* @return Cursor
	*/
	public static Cursor getCursorByDate(String path, Date date, char direction, int distanceToCity, short lane, Converter converter) {		
		return new FileInputCursor(converter, new File((String)((List)findFiles(path, date, direction, distanceToCity, lane).get(0)).get(0)));
	}
	
	// ----------------------------------------------------------------------------------------------
	
	/**
	 * Returns an array of sources, containing all sources that match the given date
	 * in the given directory
	 * @param path the directory, where generated files are stored
	 * @param date the date
  	 * @param factor a factor to speed up or slow down the original tupel rate. for instance
	 * 0,5 will increase the speed by 100%, 2 decrease the speed to 50% of the 
	 * original rate.
	 * @return HighwaySource[]
	 *  
	 */
	public static HighwaySource[] getSourcesByDate(String path, Date date, float factor) {
		return getSourcesByDate(path, date, ' ', factor);
	}
	
	/**
	 * Returns an array of sources, containing all sources that match the given date
	 * in the given directory
	 * @param path the directory, where generated files are stored
	 * @param date the date
	 * @return HighwaySource[] 
	 */
	public static HighwaySource[] getSourcesByDate(String path, Date date) {
		return getSourcesByDate(path, date, ' ');
	}	
	
	/**
 	 * Returns an array of sources, containing all sources that match the given date
	 * and the given direction in the given directory.
	 * @param path the directory, where generated files are stored
	 * @param date the date
	 * @param direction the direction possible values are 'N' for north, 'S' for south or ' '
	 * for both
  	 * @param factor a factor to speed up or slow down the original tupel rate. for instance
	 * 0,5 will increase the speed by 100%, 2 decrease the speed to 50% of the 
	 * original rate.
	 * @return HighwaySource[]
	 */
	public static HighwaySource[] getSourcesByDate(String path, Date date, char direction, float factor) {
		LinkedList list = findFiles(path, date, direction, ALL_SENSORS, ALL_LANES);
		HighwaySource[] sources = new HighwaySource[list.size()];
		for (int i=0;i<sources.length; i++) {
			List l = (List)list.get(i);
			final String[] fileNames = new String[l.size()];
			int j=0;
			for (Iterator it = l.iterator(); it.hasNext(); j++) {				
				fileNames[j] = (String) it.next(); 						
			}			
			if (fileNames.length > 0)sources[i] = new HighwaySource(fileNames, factor);			
		}		
		return sources;
	}

	/**
	 * Returns an array of sources, containing all sources that match the given date
	 * and the given direction in the given directory.
	 * @param path the directory, where generated files are stored
	 * @param date the date
	 * @param direction the direction possible values are 'N' for north, 'S' for south or ' '
	 * for both
	 * @return HighwaySource[]
	 */	
	public static HighwaySource[] getSourcesByDate(String path, Date date, char direction) {
		return getSourcesByDate(path, date, direction, 1);
	}
	
	/**
	* Returns an source, that matches the given date, the given direction and the given distance to
	* the city marina in the given directory.
	* @param path the directory, where generated files are stored
	* @param date the date
	* @param direction the direction possible values are 'N' for north, 'S' for south or ' '
	* for both
	* @param distanceToCity the distance to the city marina. if a value less than 1 is given,
	* this parameter will be ignored.
	* @param factor a factor to speed up or slow down the original tupel rate. for instance
	* 0,5 will increase the speed by 100%, 2 decrease the speed to 50% of the 
	* original rate.
	* @return HighwaySource[]
	*/
	public static HighwaySource getSourceByDate(String path, Date date, char direction, int distanceToCity, float factor) {		
		return getSourceByDate(path, date, direction, distanceToCity, ALL_LANES, factor);
	}
	
	/**
 	 * Returns a source, that match the given date, the given lane and the given direction
 	 * in the given directory.
	 * @param path the directory, where generated files are stored
	 * @param date the date
	 * @param direction the direction possible values are 'N' for north, 'S' for south or ' '
	 * for both
	 * @param distanceToCity the distance to the city marina. If a value less than 1 is given,
	 * this parameter will be ignored.
	 * @param lane the selected lane number. If a value less than 0 is given,
	 * this parameter will be ignored.
  	 * @param factor a factor to speed up or slow down the original tupel rate. for instance
	 * 0,5 will increase the speed by 100%, 2 decrease the speed to 50% of the 
	 * original rate.
	 * @return HighwaySource[]
	 */
	public static HighwaySource getSourceByDate(String path, Date date, char direction, int distanceToCity, short lane, float factor) {
		List l = (List)findFiles(path, date, direction, distanceToCity, lane).get(0);
		final String[] fileNames = new String[l.size()];
		int j=0;
		for (Iterator it = l.iterator(); it.hasNext();j++) 
			fileNames[j] = (String)it.next();
		return new HighwaySource(fileNames, factor);
	}
	
	/**
	 * Returns an source, that matches the given date, the given direction and the given distance to
	 * the city marina in the given directory.
	 * @param path the directory, where generated files are stored.
	 * @param date the date
	 * @param direction the direction possible values are 'N' for north, 'S' for south or ' '
	 * for both
	 * @param distanceToCity the distance to the city marina. if a value less than 1 is given,
	 * this parameter will be ignored.
	 * @return HighwaySource[]
	 */	
	public static HighwaySource getSourceByDate(String path, Date date, char direction, int distanceToCity) {
		return getSourceByDate(path, date, direction, distanceToCity, 1);
	}
	// --------------------------------------------------------------------------
   /**
	* Returns a source[], that matches the given date in the given directory.
	* @param path the directory, where generated files are stored
	* @param from the startdate including the startdate.
	* @param to the enddate excluding the enddate.	
	* @param factor a factor to speed up or slow down the original tupel rate. for instance
	* 0,5 will increase the speed by 100%, 2 decrease the speed to 50% of the 
	* original rate.
	* @return HighwaySource
	*/
	public static HighwaySource[] getSourcesByDate(String path, Date from, Date to, float factor) {
		return getSourcesByDate(path, from, to, ' ', factor);
	}
	
	/**
	* Returns a source[], that matches the given date in the given directory.
	* @param path the directory, where generated files are stored
	* @param from the startdate including the startdate.
	* @param to the enddate excluding the enddate.
	* 0,5 will increase the speed by 100%, 2 decrease the speed to 50% of the 
	* original rate.
	* @return HighwaySource
	*/
	public static HighwaySource[] getSourcesByDate(String path, Date from, Date to) {
		return getSourcesByDate(path, from, to, ' ');
	}
	
	/**
	* Returns a source[], that matches the given date and the given direction in the given directory.
	* @param path the directory, where generated files are stored
	* @param from the startdate including the startdate.
	* @param to the enddate excluding the enddate.
	* @param direction the direction possible values are 'N' for north, 'S' for south or ' '
	* for both.
	* @param factor a factor to speed up or slow down the original tupel rate. for instance
	* 0,5 will increase the speed by 100%, 2 decrease the speed to 50% of the 
	* original rate.
	* @return HighwaySource
	*/
	public static HighwaySource[] getSourcesByDate(String path, Date from, Date to, char direction, float factor) {
		LinkedList list = findFiles(path, from, to, direction, ALL_SENSORS, ALL_LANES);
		HighwaySource[] sources = new HighwaySource[list.size()];
		for (int i=0;i<sources.length; i++) {
			List l = (List)list.get(i);
			final String[] fileNames = new String[l.size()];
			int j=0;
			for (Iterator it = l.iterator(); it.hasNext(); j++) {				
				fileNames[j] = (String) it.next(); 						
			}			
			if (fileNames.length > 0)sources[i] = new HighwaySource(fileNames, factor);			
		}
		return sources;
	}

	/**
	* Returns a source[], that matches the given date and the given direction in the given directory.
	* @param path the directory, where generated files are stored
	* @param from the startdate including the startdate.
	* @param to the enddate excluding the enddate.
	* @param direction the direction possible values are 'N' for north, 'S' for south or ' '
	* for both.
	* @return HighwaySource
	*/
	public static HighwaySource[] getSourcesByDate(String path, Date from, Date to, char direction) {
		return getSourcesByDate(path, from, to, direction, 1);
	}
	
	/**
	* Returns a source, that matches the given date, the given direction and the given distance to
	* the city marina in the given directory.
	* @param path the directory, where generated files are stored
	* @param from the startdate including the startdate.
	* @param to the enddate excluding the enddate.
	* @param direction the direction possible values are 'N' for north, 'S' for south or ' '
	* for both
	* @param distanceToCity the distance to the city marina. if a value less than 1 is given,
	* this parameter will be ignored.
  	* @param factor a factor to speed up or slow down the original tupel rate. for instance
	* 0,5 will increase the speed by 100%, 2 decrease the speed to 50% of the 
	* original rate.
	* @return HighwaySource
	*/
	public static HighwaySource getSourceByDate(String path, Date from, Date to, char direction, int distanceToCity, float factor) {		
		return getSourceByDate(path, from, to, direction, distanceToCity, ALL_LANES, factor);
	}
	
   /**
	* Returns a source, that matches the given date, the given direction and the given distance to
	* the city marina in the given directory.
	* @param path the directory, where generated files are stored
	* @param from the startdate including the startdate.
	* @param to the enddate excluding the enddate.
	* @param direction the direction possible values are 'N' for north, 'S' for south or ' '
	* for both
	* @param distanceToCity the distance to the city marina. if a value less than 1 is given,
	* this parameter will be ignored.
    * @param lane the selected lane number. If a value less than 0 is given,
	* this parameter will be ignored.
	* @param factor a factor to speed up or slow down the original tupel rate. for instance
	* 0,5 will increase the speed by 100%, 2 decrease the speed to 50% of the 
	* original rate.
	* @return HighwaySource
	*/
	public static HighwaySource getSourceByDate(String path, Date from, Date to, char direction, int distanceToCity, short lane, float factor) {		
		List l = findFiles(path, from, to, direction, distanceToCity, lane);
		final String[] fileNames = new String[l.size()];
		int j=0;
		for (Iterator it = l.iterator(); it.hasNext();j++) 
			fileNames[j] = (String)it.next();
		return new HighwaySource(fileNames, factor);
		
	}

	/**
	* Returns a Source, that matches the given date, the given direction and the given distance to
	* the city marina in the given directory.
	* @param path the directory, where generated files are stored
	* @param from the startdate including the startdate.
	* @param to the enddate excluding the enddate.
	* @param direction the direction possible values are 'N' for north, 'S' for south or ' '
	* for both
	* @param distanceToCity the distance to the city marina. if a value less than 1 is given,
	* this parameter will be ignored.
	* this parameter will be ignored.
	* @return HighwaySource
	*/
	public static HighwaySource getSourceByDate(String path, Date from, Date to, char direction, int distanceToCity) {
		return getSourceByDate(path, from, to, direction, distanceToCity, 1);
	}
	
	//-----------------------------------------------------------
	
	/**
	* Returns an list of HashMaps source with Filename - source mappings, that match the given date, the given direction
	* and the given distance to the city marina in the given directory.
	* @param path the directory, where generated files are stored
	* @param date the date
	* @param direction the direction possible values are 'N' for north, 'S' for south or ' '
	* for both
	* @param distanceToCity the distance to the city marina. if a value less than 1 is given,
	* this parameter will be ignored.
	* @param lane the lane number.
	* @return LinkedList a list of files, that match the given values.
	* @throws IllegalArgumentException if a wrong path is passed or no date matches 
	* the files in the directory
	*/
	protected static LinkedList findFiles(String path, Date date, char direction, int distanceToCity, short lane) {		
		File f = new File(path);
		if ((!f.exists()) || (!f.isDirectory()))
			throw new IllegalArgumentException("path must contain a valid path to the source files (passed path was: "+path+")");
		LinkedList list = new LinkedList();
		String[] files = f.list();
		if (date == null) throw new IllegalArgumentException("date must not be null");
		Calendar c = Calendar.getInstance();		
		c.setTime(date);
		String dateString = (c.get(Calendar.DAY_OF_MONTH)<10 ? "0" : "")+c.get(Calendar.DAY_OF_MONTH)+"-"+ (c.get(Calendar.MONTH)<10 ? "0" : "")+c.get(Calendar.MONTH)+"-"+c.get(Calendar.YEAR);		
		char tempDirection =' ' ;
		String tempDistance ="";
		ArrayList fileList= null;
		// assumptions: fileNames are ordered, max. 9 lanes
		for (int i=0; i< files.length; i++)	{
			// possible extension : compute all sources in the current directory
			if  (files[i].startsWith(dateString)) {											
				if (direction == files[i].charAt(DIRECTION_POS_NEW) || direction == ' '){					
					if (tempDirection != files[i].charAt(DIRECTION_POS_NEW)) {						
						tempDirection = files[i].charAt(DIRECTION_POS_NEW);
						if (fileList != null && fileList.size() >0)
							list.add(fileList);
						fileList = new ArrayList();
						
					}										
					if (distanceToCity == Integer.parseInt(files[i].substring(DISTANCE_TO_CITY_POS, DISTANCE_TO_CITY_POS+5))  || distanceToCity == ALL_SENSORS) {							
						if (tempDistance.compareTo(files[i].substring(DISTANCE_TO_CITY_POS, DISTANCE_TO_CITY_POS+5))!= 0) {							
							tempDistance = files[i].substring(DISTANCE_TO_CITY_POS, DISTANCE_TO_CITY_POS+5);							
							if (fileList != null && fileList.size() >0)
								list.add(fileList);
							fileList = new ArrayList();

						}
						if (files[i].charAt(files[i].length()-1)-48 == lane || lane == ALL_LANES) {
							if (f.listFiles()[i].length() >0)
								fileList.add(path+files[i]);
						}						
					}
				}	
			}
		}
		if (fileList != null && fileList.size() >0)
			list.add(fileList);		
		if (list.size()==0)
			throw new IllegalArgumentException("No file matches the given values:"
			+path+" (path) "+dateString+" (date) "+ direction+" (direction) "+ distanceToCity+" (distanceToCity)");	
		return list;
	}
	
	/**
	 * @param path the path to the directory with the files.
	 * @param from the startdate including the startdate.
	 * @param to the enddate excluding the day from the enddate.
	 * @param direction the direction ('N', 'S' or ' ' for both).
	 * @param distanceToCity the distance to the city marina or oakland.
	 * @param lane the lane number.
	 * @return LinkedList a list of files, that match the given values.
	 * @throws IllegalArgumentException if a wrong path is passed or no date matches 
	 * the files in the directory
	 */
	protected static LinkedList findFiles(String path, Date from, Date to, char direction, int distanceToCity, short lane) {
		File f = new File(path);
		if ((!f.exists()) || (!f.isDirectory()))
			throw new IllegalArgumentException("path must contain a valid path to the source files");		
		String[] files = f.list();
		if (from == null || to==null ) throw new IllegalArgumentException("to and from must not be null");		
		Calendar c = Calendar.getInstance();
		c.setTime(from);
		String[][] dates = new String[2][3];
		for (int i=0; i<dates.length; i++){
			dates[i][0] = (c.get(Calendar.DAY_OF_MONTH)<10 ? "0" : "")+c.get(Calendar.DAY_OF_MONTH) ;
			dates[i][1] = (c.get(Calendar.MONTH)<10 ? "0" : "")+c.get(Calendar.MONTH);
			dates[i][2] = ""+c.get(Calendar.YEAR);
			c.setTime(to);
		}			
		List matches = new ArrayList();
		// remove the files, that are not in the time interval
		for (int i=0; i< files.length; i++)
			if (dates[0][0].compareTo(files[i].substring(0, 2)) <=0 && dates[1][0].compareTo(files[i].substring(0, 2)) >0 &&
				dates[0][1].compareTo(files[i].substring(3, 5)) <=0 && dates[1][1].compareTo(files[i].substring(3, 5)) >=0 &&
				dates[0][2].compareTo(files[i].substring(6, 10)) <=0 && dates[1][2].compareTo(files[i].substring(6, 10)) >=0 ) 
					matches.add(files[i]);						
		
		// build up a String for HighwaySource: <file 1>$<file 2>$...$<file n>
		// map : <direction>_<distance to city>_lane<lane number> --> <file 1>$<file 2>$...$<file n>
		HashMap temp = new HashMap();
		String currentFile = null;
		String currentKey = null;
		for (int i=0; i<matches.size(); i++) {
			currentFile = (String)matches.get(i);
			currentKey = currentFile.substring(DIRECTION_POS_NEW, currentFile.length());			
			if ((direction == currentFile.charAt(DIRECTION_POS_NEW) || direction == ' ') &&
				(distanceToCity == Integer.parseInt(currentFile.substring(DISTANCE_TO_CITY_POS, DISTANCE_TO_CITY_POS+5))  || distanceToCity == -1) &&
				(currentFile.charAt(files[i].length()-1)-48 == lane || lane == ALL_LANES))
				if (temp.containsKey(currentKey))
					if (distanceToCity != ALL_SENSORS)
						temp.put(currentKey, ((String)temp.get(currentKey))+SEPARATOR+path+currentFile);
					else 
						temp.put(currentKey, ((String)temp.get(currentKey))+SEPARATOR+path+currentFile);
				else 
					if (distanceToCity != ALL_SENSORS)
						temp.put(currentKey, path+currentFile);
					else // weiteres mapping erforderlich, daher noch keine pfadangabe
						temp.put(currentKey, currentFile);
		}		
		if (distanceToCity != ALL_SENSORS) {
			if (temp.values().size()==0)
				throw new IllegalArgumentException("No file matches the given values:"
					+path+" (path) "+from+" - "+to+" (date interval) "+ direction+" (direction) "
					+ distanceToCity+" (distanceToCity)");
			return new LinkedList(temp.values());
		}
		// map : <direction>_<distance to city> --> List 1[<file 1>$<file 2>$...$<file n>], ..., List m[<file 1>$<file 2>$...$<file n>] 
		HashMap temp2 = new HashMap();
		for (Iterator it=temp.values().iterator(); it.hasNext(); ) {
			currentFile = (String)it.next();
			currentKey = currentFile.substring(DIRECTION_POS_NEW, DISTANCE_TO_CITY_POS+5);							
			if (direction == currentFile.charAt(DIRECTION_POS_NEW) || direction == ' ') {
				if (temp2.containsKey(currentKey)){
					((List)temp2.get(currentKey)).add(new String(path+currentFile));
				}				
				else {
					ArrayList l = new ArrayList();
					l.add(path+currentFile);
					temp2.put(currentKey, l);
				}
			}
		}
		if (temp2.values().size()==0)
			throw new IllegalArgumentException("No file matches the given values:"
				+path+" (path) "+from+" - "+to+" (date interval) "+ direction+" (direction) "
				+ distanceToCity+" (distanceToCity)");
		return new LinkedList(temp2.values());
	}
	/**
	 * Produces a source file that can be used by getsourcebydate and getcursorbydate methods.
	 * to create one source that delivers tuples in the same way as the highway sensors.
	 * Invalid measuring is ignored.
	 * @param fileName the original file from "http://www.clearingstelle-verkehr.de/cs/verkehrsdaten/katalog/katalogberichte/fsp/fsp_metadaten"
	 * that will be proceeded.
	 * @param path the path to the file.
	 */
	protected static void makeSourceFile(String fileName , String path) {
		BufferedReader file = null;
		StringBuffer sb = new StringBuffer();
		
		try {			
			file = new BufferedReader(new FileReader(fileName), 2000000);
			String s = file.readLine();
			while(s != null){
				sb.append(s+"\r\n");
				s = file.readLine();	
			}			
		}
		catch(IOException e) {
			System.out.println("File "+fileName+" not found");
		}
		if (file!=null) 
			try { file.close();}
			catch(Exception e) {}
		StringTokenizer tokenizer;
		tokenizer = new StringTokenizer(sb.toString());
		int sourceNumber = 0;
		int row = 0;			
		boolean newFile= false;
		boolean ignoreValue = false;
		byte lane = -1;
		long timeStamp = -1;
		double speed = -1;
		double length = -1;
		BufferedWriter output = null;
		int distanceToCity = 0;							
		while (tokenizer.hasMoreTokens()) {
			switch (row) {				
				case 0:{					
					distanceToCity = Integer.parseInt(tokenizer.nextToken());
					if (sourceNumber != distanceToCity) {
						//source++;
						sourceNumber = distanceToCity;
					}
					break;
				}
				case 1:{
					byte b = Byte.parseByte(tokenizer.nextToken());
					if (lane != b) {
						newFile = true;												
						lane = b;					
					}
					break;
				}
				case 2:{					
					// 1/60 sec --> 1 ms
					timeStamp =  ((Long.parseLong(tokenizer.nextToken())*1000)/60) +  basisDate;						
					break;
				}
				case 3:{
					String s = tokenizer.nextToken();
					// ignore invalid measuring
					if (s.compareTo("In")==0) {
						ignoreValue = true;
					}
					else
						speed = Double.parseDouble(s);
					break;
				}
				case 4:{
					String s = tokenizer.nextToken();
					// ignore invalid measuring
					if (s.compareTo("Na")==0 || s.compareTo("In")==0) {
						ignoreValue = true;
					}
					else					
						length = Double.parseDouble(s);
					break;
				}
			}		
			if (row<4) row++;
			else {
				row = 0;							
				try {
					if (newFile) {
						if (output != null)							
							//Thread.sleep(1000);
							output.close();			
						output = new BufferedWriter(new FileWriter(path+outputFileName+"_"+distanceToCity+LANE_PREFIX+lane), 1000000);						
						newFile = false;	
					}
					if (!ignoreValue)
						output.write(timeStamp+"\t"+distanceToCity+"\t"+lane+"\t"+speed+"\t"+length+"\t"+direction+"\r\n");					
					ignoreValue = false;
				} catch (IOException e1) {					
					e1.printStackTrace();
				}	
			}
		}
		if (output != null)
			try {
				output.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
	}
	
	/**
	 * Produces source files that can be used by getsourcebydate and getcursorbydate methods.
	 * Invalid measuring is ignored.
	 * @param inputPath the path to the directory
	 * @param inputFileName the file that contains the highway data 
	 * @param tempDir the path to a directory that should be used as temp space.
	 * if tempDir is null, the generation will take place in the memory.	 
	 */
	public static void makeSourceFiles(String inputPath, String inputFileName, String tempDir ) {
		if (inputPath == null || inputFileName == null )
			throw new IllegalStateException("inputFileName and " +
				"outputFileName must not be null");
		computeValues(inputFileName);		
		if (tempDir != null ){
			fileList = new LinkedList();
			System.out.println("proceeding "+inputPath+inputFileName);
			splitHighwayFile(inputPath+inputFileName, tempDir);
			System.out.println("generating source files...");
			while (!fileList.isEmpty()) {						
				System.out.println(" "+(String)fileList.get(0));
				makeSourceFile((String)fileList.get(0), inputPath);
				File file = new File((String)fileList.get(0));
				file.delete();				
				fileList.remove(0);
			}
		}
		else {
			System.out.println("generating source files...");
			makeSourceFile(inputPath+inputFileName, inputPath);
		}
		System.out.println("done");
	}	
	
	/**
	 * Makes all source files in the given directory.
	 * @param inputPath the path to the directory
	 * @param tempDir the path to a directory that should be used as temp space
	 * If tempDir is null, the generation will take place in the memory.
	 */
	public static void makeAllSourcesFiles(String inputPath, String tempDir) {
		File f = new File(inputPath);
		if (f.isDirectory()) {
			File[] files = f.listFiles();
			for (int i=0; i< files.length; i++) {
				if (files[i].getName().startsWith("lp") && files[i].getName().endsWith("_speeds_and_lengths.dat")){
					makeSourceFiles(inputPath, files[i].getName(), tempDir);
				}
					
			}
		}
		else throw new IllegalArgumentException("inputPath must point to a directory");
	}
	
	/**
	 * Splits a File with highway data to several small temp files.
	 * Every temp file contains the data of one source. the temp filenames
	 * are saved in fileList.
	 * @param fullQualifiedFileName a string containing the absolute path and the filename.
	 * of the highway data, that will be split.
	 * @param tempDir the directory where the tmpfiles should be stored.
	 * if tempDir doesn't exist it will be created
	 */
	protected static void splitHighwayFile(String fullQualifiedFileName, String tempDir) {
		BufferedReader file =null;
		int fileNumber=0;
		File temp = new File(tempDir);
		if (! temp.exists())
			try {
				temp.createNewFile();
			}
			catch (IOException e1) {				
				e1.printStackTrace();
			}
		try {
			file = new BufferedReader(new FileReader(fullQualifiedFileName), 10000000); 
			String s = null;							
			int sourceNumber =-1;
			BufferedWriter fw = null;			
			System.out.println("spliting file ...");
			s = file.readLine();
			while (s != null) {
				if (sourceNumber != Integer.parseInt(s.substring(0,5))) {
					if (fw != null) fw.close();					
					fw = new BufferedWriter(new FileWriter(tempDir+"temp"+fileNumber), 2000000);
					System.out.println(" "+tempDir+"temp"+fileNumber);
					fileList.add(new String(tempDir+"temp"+fileNumber));
					sourceNumber = Integer.parseInt(s.substring(0,5));
					fileNumber++;					
				}
				fw.write(s+"\r\n");
				s = file.readLine();
			}			 
			file.close();
			fw.flush();
			fw.close();					
		}
		catch(IOException e) {
			e.printStackTrace();			
		}
	}
	
	/**
	 * Executes an example if the right arguments are passed.
	 * @param args containing the following information: <br>
	 * args[0] = path to inputfileName <br>	 
	 * args[1] = tempDir <br>
	 * A lot of tests are in 'xxl.applications.pipes.Tests' (method: highwayTest).
	 */
	public static void main(String[] args) {
		System.out.println("A lot of tests are in 'xxl.applications.pipes.Tests' (method: highwayTest)");
		if (args.length == 2) {
			System.out.println("Starting conversion. This will take a while.");
			makeAllSourcesFiles(args[0], args[1]);
		}
		if (args.length >=1)
			new VisualSink(Highway.getSourceByDate(args[0], new GregorianCalendar(1993, 3, 1).getTime(), 'N', 58700, (short)1, (float)0.1), true);
		else
			System.out.println("At least the path to inputfileName must be given via argument");
	}
}
