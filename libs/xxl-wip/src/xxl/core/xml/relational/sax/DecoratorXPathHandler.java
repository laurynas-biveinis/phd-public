/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.xml.relational.sax;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import xxl.core.util.concurrency.Channel;

/**
 *	An utility class that provides some XPath functionality. <br><br>
 *
 *	Every instance of this class has a default Handler that handles every event at any location.
 *	The user can define arrays of handler and of XPath expressions. If the XPath expression i is reached, the 
 *	XPathHandler sends the following events to the handler that belongs to this XPath. The default Handler handles every 
 *	event.<br>
 *	<br>
 *	<b>Note:</b>
 *
 * 	When this Handler reaches an XPath, it optionally puts an Integer into the channel. If the chanel isn't empty, the current thread
 *	will be halted, until another method (in another thread) empties the channel. On the one hand, it causes a few extra lines of code,
 *	but on the other hand, it simplifies the control over the XPathHandler and also over the underlying handlers.
 *	<br><br><b>Example:</b>
 *
 *	<code><pre>
 *		.....
 *		.....							
 *	
 *		int[] number = new int[1];			// determines how often the ContentHandler can handle events
 *		String[] xpaths = new String[1];		// an XPath expression describes the location where the handler should work
 *		ContentHandler[] hbs = new ContentHandler[1];	// the Handler
 *	
 *		OutputStream out = new FileOutputStream("test.xml");		
 *
 * 		xpaths[0] = "/table/people/row[2]";
 *		hbs[0] =   new MyHelloHandler(out);  //a simple handler, which inserts a hello tag into the OutputStream
 *		number[0] = 1;
 *
 *		Channel xPathChannel = new AsynchronousChannel();
 *
 *		final DecoratorXPathHandler xph = new DecoratorXPathHandler (new DefaultHandler(), xpaths, hbs, number, xPathChannel, true, true, null);
 *	
 *		//this is a new thread in which the SAX parser parses the document.
 *		parserThread = (new Thread() {
 *			public void run() {
 *				try {									
 *					parser.parse (document, xph);																		
 *				}
 *				catch (Exception e) {
 *					System.out.println(e);
 *				}				
 *			}										
 *		});
 *
 *		parserThread.start();				
 *
 *		Integer xnr = (Integer) xPathChannel.take(); // which XPath location is reached ?
 *
 *		.....
 *		.....
 *
 *	</pre></code>
 */
public class DecoratorXPathHandler extends org.xml.sax.helpers.DefaultHandler {

	private ContentHandler[] handlerbase;

	private Stack location;
	
	private Map status;

	private List[] xpath;
		
	private int[] number;
	
	private int count;
	
	private int levelcount;
	
	private boolean reached=false;
	
	private int level=0;
	
	private int xnr;
	
	private String aktuell;

	private ContentHandler defaulthandler;
	
	private Channel channel;
	
	private boolean sendIntegersAtXPathLocations;
	
	private boolean sendEndMarker;
	
	Object endMarker;
	
	/**
		Creates the Handler
		@param defaulthandler The handler which handles every event
		@param xpath A string array containing XPath expressions
		@param hb A Contenthandler array that contains some handler which belong to the XPath expressions.
		@param number Indicates how often the handler shall handle events.
		@param channel Information channel which informs about the reached XPath positions inside the document.
			At the end of the document, there can also be a signal inside this channel.
		@param sendIntegersAtXPathLocations true iff the channel should get information when an XPath location
			is reached.
		@param sendEndMarker Determines if an end marker is sent at the end of the document.
		@param endMarker Determines which element is sent at the end of the document (if one).
	*/
	public DecoratorXPathHandler (ContentHandler defaulthandler, String[] xpath, ContentHandler[] hb, int[] number, Channel channel, boolean sendIntegersAtXPathLocations, boolean sendEndMarker, Object endMarker) {
		this.channel = channel;
		this.defaulthandler = defaulthandler;
		this.handlerbase = hb;
		this.number = number;
		this.sendIntegersAtXPathLocations = sendIntegersAtXPathLocations;
		this.sendEndMarker = sendEndMarker;
		this.endMarker = endMarker;
		
		for (int i=0;i< number.length;i++)
			this.number[i] = this.number[i]-1;
		
		this.xpath = new ArrayList[xpath.length];
		
		this.count = 0;
		this.levelcount = 0;
		
		int z=0;
		String t;	

		while (z < xpath.length) {
			this.xpath[z] =  new ArrayList();
			t = "";
			int i=0;
			while (i < xpath[z].length() ) {  // modify the xpath expressions. if there is no "[i]", insert [1].
				if (i>0) {
					if (xpath[z].charAt(i) == '/') {
						if ((xpath[z].charAt(i-1) != ']')&&((xpath[z].charAt(i-1) != '*'))) t = t +"[1]";
						this.xpath[z].add (t);
						t = "";
					}
					else 
						t = t + xpath[z].charAt(i);
				}
				i++;
			}
			if ((t.charAt(t.length()-1) != ']')&&((xpath[z].charAt(i-1) != '*')))
				t=t+"[1]";
			this.xpath[z].add (t);

			z++;
		}
		location = new Stack();
		status = new HashMap();
	}

	/**
	 * Notifies the startElement event to the defaulthandler and also to a ContentHandler (in this sequence), if
	 * a correlating XPath expression is reached. Puts an Integer in the channel (if channel is not null), if an 
	 * XPath is reached. This Integer shows which XPath is reached. Note: A channel can contain at most one object,
	 * if a channel already contains an object and tries to put another object in the channel,
	 * the current thread will be halted.	
	 * @param uri The namespace URI
	 * @param localName The local name (without prefix), or the empty string if Namespace processing is not being performed.
	 * @param qName The qualified name (with prefix), or the empty string if qualified names are not available.
	 * @param attributes The specified or defaulted attributes.
	 * @throws SAXException
	 */	
	public void startElement(java.lang.String uri, java.lang.String localName, java.lang.String qName, Attributes attributes)   throws SAXException { 
		level++;
		location.push (qName);
		String s = getString(location.iterator());
		if (status.containsKey(s)) 
			status.put(s, new Integer ((((Integer) status.get(s)).intValue()+1))  );
		else 
			status.put(s, new Integer(1));
		
		location.pop();
		location.push(qName+"["+status.get(s)+"]");
		
		s = getString(location.iterator());
		
		int z=0;
		
		Iterator it1, it2;
		boolean equal;
		String s1,s2;
				
		if (reached) {
			if (level == levelcount) count++;
			if (count > number[xnr]) {
				reached = false;
				count = 0;
			}
			else {
				int li = aktuell.lastIndexOf("/");
				if (! s.startsWith(aktuell.substring(0, li))) {
					reached=false;
					count =0;
				}
			}
		}

		if (! reached)  {
			while (z < xpath.length) {
				equal = true;
				if (this.xpath[z].size() == this.location.size() ) {
					it1 = this.xpath[z].iterator();
					it2 = this.location.iterator();
					
					while (it1.hasNext()) {
						s1 = ((String)it1.next());
						s2=  ((String)it2.next());
						
						if (!(s1.equals(s2))&&(!(s1.equals("*")))) {equal=false; break;}
					}
					if (s.equals(xpath[z])) {reached=true; xnr=z;}
				}
			 	else
			 		equal = false;
	
				if (equal) {
					aktuell = getString(location.iterator());
					reached = true;
					xnr = z;
					break;
				}
				z++;
			}

			if (reached) { 
				levelcount = level;
				if (sendIntegersAtXPathLocations)
					channel.put (new Integer(xnr));
			}
		}

		defaulthandler.startElement(uri, localName, qName, attributes);	

		if (reached)
			handlerbase[xnr].startElement(uri, localName, qName, attributes);
	}

	/**
	 * Notifies the characters to the defaulthandler and also to a ContentHandler, if
	 * a correlating XPath expression is reached.
	 * @param uri The namespace URI
	 * @param localName The local name (without prefix), or the empty string if Namespace processing is not being performed.
	 * @param qName The qualified name (with prefix), or the empty string if qualified names are not available.
	 * @throws SAXException
	 */
	public void endElement(java.lang.String uri, java.lang.String localName, java.lang.String qName)  throws SAXException {
		defaulthandler.endElement(uri, localName, qName);

		if (reached)
			handlerbase[xnr].endElement(uri, localName, qName);
		String s = getString(location.iterator()); 

		//System.out.println("/"+qName);
		Iterator i = status.keySet().iterator();
		String t;
		
		while (i.hasNext()) {
			t = (String) i.next();
			if ((t.startsWith(s))&&(t.length() > s.length()))
				i.remove();
		}

		location.pop();
		level--;

		s = getString(location.iterator()); 
		if (reached) {
			if (count > number[xnr]) {
				reached = false;
				count = 0;
			}
			else {
				int li = aktuell.lastIndexOf("/");
				if (! s.startsWith(aktuell.substring(0, li))) {
					reached=false;
					count =0;
				}
			}
		}
	}
	
	/**
	 * Notifies this event to every ContentHandler.
	 * @throws SAXException
	 */
	public void startDocument() throws SAXException {
		defaulthandler.startDocument();
		for (int i=0; i< handlerbase.length; i++)
			handlerbase[i].startDocument();
	}

	/**
	 * Notifies this event to every ContentHandler.
	 * @throws SAXException
	 */
	public void endDocument() throws SAXException {
		defaulthandler.endDocument();
	
		for (int i=0; i< handlerbase.length; i++)
			handlerbase[i].endDocument();
		if (sendEndMarker)
			channel.put(endMarker);
	}

	/**
	 * Notifies the characters to the defaulthandler and also to a ContentHandler, if
	 * a correlating XPath expression is reached.
	 * @param ch The characters.
	 * @param start The start position in the character array.
	 * @param length The number of characters to use from the character array.
	 * @throws SAXException
	 */
	public void characters(char[] ch, int start, int length) throws SAXException {
		defaulthandler.characters(ch, start, length);
		if (reached) 
			handlerbase[xnr].characters(ch, start, length);	
	}

	/**
		Sets the reached flag to false.
	*/
	public void nextLocation() {
		reached = false;
		count = 0;
	}	
	
	/**
	 * Sets the content handler.
	 * @param contenthandler The new content handler.
	 */
	public void setHandler (ContentHandler[] contenthandler) {
		handlerbase = contenthandler;
	}
	
	/**
	 * Returns a string for a given iterator, that contains some strings.
	 * The string consists of the strings, separated by a "/".
	 * @param i An iterator that contains some strings.
	 * @return A string that contains the strings from the iterator, separated by "/".	
	 */
	private String getString(Iterator i) {
		String s="";
		while (i.hasNext())
			s = s + "/"+((String) i.next());
		return s;
	}	
}
