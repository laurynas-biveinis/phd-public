/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.xml.relational.sax;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import xxl.core.cursors.AbstractCursor;
import xxl.core.functions.Function;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.util.WrappingRuntimeException;
import xxl.core.util.concurrency.AsynchronousChannel;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.xml.relational.StringToSQLTypeConverterMap;

/**
 *	An implementation of the java.sql.ResultSet interface. <br><br>
 *	This class uses a sax parser to parse a document. The user can define the location where the data/metadata
 *	is placed. A DataStepByStepHandler is used in combination with a DecoratorXPathHandler. The constructor creates 
 *	a new thread in which the parser starts parsing the document.The DataStepByStepHandler sends the data though a channel
 *	and stops the parser thread behind every row. By calling the next() method, the data will be taken out of the channel, so
 *	that the parser thread can continue his work and so on.
 *	<br><br>
 *	<b>Note: </b> If the user has specified an XPath expression for the metadata, the document must contain the metadata, otherwise 
 *	an exception will be thrown. If a document doesn't contain metadata, the user has to use the constructor method without the metadata. In
 *	this case, the XMLMetaDataCursor creates "default" Metadata (class DefaultMetadata). When the XMLMetaDataCursor tries to create this metadata, it is
 *	necessary, that the number of columns is known. Therefore, it's necessary, to read the first row, before the default metadata can be generated.
 */
public class XMLMetaDataCursor extends AbstractCursor implements xxl.core.cursors.MetaDataCursor {
	
	/** SAXParserFactory which is used to generate a new SAXParser */
	private SAXParserFactory spf;
	
	/** Handler which has subhandlers for XPath Expressions and for the data */
	private DecoratorXPathHandler xph;

	/** Handler gathering meta data */
	private MetadataHandler mdh;
	
	/** This channel is used to communicate */
	private AsynchronousChannel eventChannel;
	
	/** Sends a message when the parser thread terminates */
	private AsynchronousChannel termThreadChannel;
	
	/** Metadata generated */
	private CompositeMetaData<Object, Object> globalMetaData;
	
	private ResultSetMetaData rsmd;
	
	/** Conversion between Strings and Java types */
	private StringToSQLTypeConverterMap converterMap;
	
	/** Next Object of the iteration (if there is one) */
	private Object next;
	
	/** Marker Object which is sent in the channel after the meta data has been
	 * detected and processed by the ContentHandler. */
	private Object metaDataMarker;
		
	/** The currently active InputStream */
	private InputStream inputstream;
	
	/** Information from the constructor call */
	private Function inputStreamFactory;
	/** Information from the constructor call */
	private Function createTuple;
	/** Information from the constructor call */
	private String xPathtoData;
	/** Information from the constructor call */
	private String xPathtoMetadata;
	/** Information from the constructor call */
	private Map idMap;

	/**
	 *	Creats an XMLMetaDataCursor with default metadata.
	 *	@param inputStreamFactory A function that returns the document.
	 *	@param xPathtoData This XPath expression describes the location of the data
	 *	@param SQLTypes A string[] containing the sqltype-names which are used in the document
	 *	@param idMap A user specified identifier map to configure the XML processing. See the description in the Sax class for details.
	 *	@param createTuple Function that maps an Object array (column values) and a 
	 *	  	rmd object to a new result Tuple. xxl.relational.ArrayTuple.FACTORY_METHOD
	 *	  	can be used.
	 */
	public XMLMetaDataCursor (Function inputStreamFactory, String xPathtoData, String[] SQLTypes, Map idMap, Function createTuple) {
		this (inputStreamFactory, xPathtoData, null, SQLTypes, idMap,createTuple);
	}
	
	/**
	 *	Creats an XMLMetaDataCursor.
	 *	@param inputStreamFactory A function that returns the document.
	 *	@param xPathtoData This XPath expression describes the location of the data
	 *	@param xPathtoMetadata This XPath expression describes the location of the metadata
	 *	@param SQLTypes A string[] containing the sqltype-names which are used in the document
	 *	@param idMap A user specified identifier map to configure the XML processing. See the description in the Sax class for details.
	 *	@param createTuple Function that maps an Object array (column values) and a 
	 *	  	rmd object to a new result Tuple. xxl.relational.ArrayTuple.FACTORY_METHOD
	 *	  	can be used.
	 */
	public XMLMetaDataCursor (Function inputStreamFactory, String xPathtoData, String xPathtoMetadata, String[] SQLTypes, Map idMap, Function createTuple) {
		this.idMap = idMap;
		this.xPathtoData = xPathtoData;
		this.xPathtoMetadata = xPathtoMetadata;
		this.inputStreamFactory = inputStreamFactory;
		this.createTuple = createTuple;

		spf = SAXParserFactory.newInstance();
		metaDataMarker = new Object(); // dummy Object
			
		// this converterMap maps SQL-Types to StringToSQLTypeConverters
		// every StringToSQLTypeConverter can convert a simple string to an object with 
		// a converter specific SQL type.
		if (SQLTypes == null)
			converterMap = new StringToSQLTypeConverterMap();
		else
			converterMap = new StringToSQLTypeConverterMap (SQLTypes);
		
		eventChannel = new AsynchronousChannel();
		termThreadChannel = new AsynchronousChannel();
		
		createThread(xPathtoMetadata!=null).start();
		
		// the first channel which answeres is the xPathChannel.
		Object event = eventChannel.take();

		// In the best case, the metadata area should be reached first
		if (event == null)
			throw new RuntimeException("no data and metadata found");

		globalMetaData = new CompositeMetaData<Object, Object>();
		
		if (xPathtoMetadata!=null) {
			if (event != metaDataMarker) {
				// data found
				
				// the worst case: the metadata is behind the data, so it's 
				// necessary to ingore the data and to search for the metadata
				
				xph.nextLocation(); // ???
				
				while (true) {
					event = eventChannel.take();
					
					if (event == null) 
						throw new RuntimeException ("no metadata found");
					else if (event == metaDataMarker)
						break;
					
					xph.nextLocation(); // ???
				}
				
				// the parser has found the metadata, so we can parse the document again to read the data
				globalMetaData.add(
					ResultSetMetaDatas.RESULTSET_METADATA_TYPE,
					rsmd = new XMLResultSetMetaData(
						mdh.getColumns(), 
						mdh.getAttributes(), 
						new StringToSQLTypeConverterMap(), 
						idMap
					)
				);

				terminateThread();

				// recreate a new parser thread (without meta data search)
				createThread(false).start();
			}
			else {
				// meta data found

				// metadata was in front of the data
				// meta data has been processed
				globalMetaData.add(
					ResultSetMetaDatas.RESULTSET_METADATA_TYPE,
					rsmd = new XMLResultSetMetaData(
						mdh.getColumns(),
						mdh.getAttributes(),
						new StringToSQLTypeConverterMap(),
						idMap
					)
				);

				// possible optimization: remove MetaDataHandler
			}
		}
	}
	
	/** Creates the thread for a SAX parser */
	private Thread createThread(boolean withMetadata) {
	
		//use the factory to create an inputstream
		inputstream = (InputStream) inputStreamFactory.invoke();
		
		// create a new DataStepByStepHandler, this handler uses the dataChannel and the identifier map
		// do not send an end marker
		DataStepByStepHandler sbsh = new DataStepByStepHandler(eventChannel, idMap, false, null);
		
		int[] number;
		String[] xpaths;
		ContentHandler[] hbs;
		
		if (withMetadata) {
			mdh = new MetadataHandler(eventChannel,idMap,metaDataMarker);

			// the DecoratorXPathHandler shall handle two ContentHandlers
			number = new int[2];
			xpaths = new String[2];
			hbs = new ContentHandler[2];
			
			xpaths[0] = xPathtoMetadata;
			hbs[0] =  mdh;	// metadata handler
			number[0] = 1;			// metadata consists of one row
			
			xpaths[1] = xPathtoData;
			hbs[1] = sbsh;			// data handler
			number[1] = Integer.MAX_VALUE;  // no limit
		}
		else {
			mdh = null;
			
			// the DecoratorXPathHandler shall handle two ContentHandlers
			number = new int[1];
			xpaths = new String[1];
			hbs = new ContentHandler[1];
			
			xpaths[0] = xPathtoData;
			hbs[0] = sbsh;			// data handler
			number[0] = Integer.MAX_VALUE;  // no limit
		}
		
		// create a DecoratorXPathHandler
		// only send null at the end
		xph = new DecoratorXPathHandler (
			new DefaultHandler(), xpaths, hbs, number, eventChannel, false, true, null
		);
		
		// this is a new thread in which the SAX parser parses the document.
		// by using two channels, this thread will sometimes be halted.
		return new Thread() {
			public void run() {
				try {
					spf.newSAXParser().parse (inputstream, xph);
				}
				catch (ParserConfigurationException e) {
					throw new WrappingRuntimeException(e);
				}
				catch (SAXException e) {
					throw new WrappingRuntimeException(e);
				}
				catch (IOException e) {
					throw new WrappingRuntimeException(e);
				}
				termThreadChannel.put(null);
			}
		};
	}
	
	/**
	 * Terminates the parser thread and closes the InputStream.
	 */
	private void terminateThread() {
		// no more events needed!
		xph.setHandler(new ContentHandler[] { new DefaultHandler(), new DefaultHandler() });
		// There can only be one object inside each channel (at most)
		if (!eventChannel.isEmpty())
			eventChannel.take();
		if (!eventChannel.isEmpty())
			eventChannel.take();
		
		// wait for the termination of the thread (may take some while, because
		// SAX reads the rest of the document...).
		termThreadChannel.take();
		
		try {
			inputstream.close();
		}
		catch (IOException e) {
			throw new WrappingRuntimeException(e);
		}
	}

	/**
	 * Moves the cursor down one row from its current position and computes the next tuple.
	 * A cursor is initially positioned before the first row.
	 * The first call to next makes the first row the current row.
	 * The second call makes the second row the current row, and so on. 
	 * <p>	
	 * Implementation: Takes data from the "communication" dataChannel, this causes the parser 
	 * thread to continue.
	 * @return true iff there is another object in the iteration.
	 */
	public boolean hasNextObject() {
		Object o = eventChannel.take();
		
		if (o != null) {
			try {
				// If no metadata is available, default metadata will be created here.
				// It's necessary to create the metadata here, because this is the first time
				// we know the number of columns of the table!
				if (rsmd == null)
					globalMetaData.add(
						ResultSetMetaDatas.RESULTSET_METADATA_TYPE,
						rsmd = new DefaultMetadata(((List) o).size())
					);
				
				int columns = rsmd.getColumnCount();
				List<Object> ol = (List<Object>)o;
				for (int columnIndex=1 ; columnIndex<=columns ; columnIndex++)
					if (ol.get(columnIndex-1)!=null)
						ol.set(columnIndex-1, converterMap.getObject((String) ol.get(columnIndex-1),rsmd.getColumnTypeName(columnIndex)));
				
				next = createTuple.invoke(ol);
				return true;
			}
			catch (SQLException e) {
				throw new WrappingRuntimeException(e);
			}
		}

		return false;
	}
	
	/**
	 * Returns the next Object of the Iteration.
	 * @return the next Object of the Iteration.
	 */
	public Object nextObject() {
		return next;
	}

	/**
	 *	Retrieves the number, types and properties of the MetaDataCursor's columns.
	 *	
	 *	Return a default description if no metadata is available.
	 *	@return the description of a ResultSet's columns
	 */	
	public Object getMetaData() {
		return globalMetaData;
	}
	
	/**
	 *	Moves the cursor to the first row.
	 */
	public void reset() {
		super.reset();

		terminateThread();
		createThread(false).start();
	}
	
	/**
	 * @see xxl.core.cursors.Cursor#supportsReset()
	 */
	public boolean supportsReset() {
		return true;
	}
	
	/**
	 *	Frees the resources.
	 */
	public void close() {
		if (isClosed) return;
		super.close();

		terminateThread();
	}
}
