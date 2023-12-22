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

package xxl.core.relational.query.operators;

import java.sql.ResultSetMetaData;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xxl.core.cursors.Cursor;
import xxl.core.functions.Function;
import xxl.core.functions.Switch;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.sources.Source;
import xxl.core.relational.QueryConverter;
import xxl.core.relational.QueryTranslator;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.relational.query.Node;
import xxl.core.relational.query.Nodes;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataException;
import xxl.core.util.metaData.MetaDataProvider;

/**
 * This class provides static methods for creating <i>ready to use</i>
 * instances of a {@link Node node} in a directed acyclic graph that is able to
 * send its data via a socket or to use a socket to receive its data. Nodes of
 * that kind can be used to distribute parts of a query graph amoung different
 * machines/runtime environments. Beside these methods, this class contains
 * constants for identifying local metadata fragments inside an operator node's
 * global metadata, methods for accessing them and local metadata factories for
 * updating them.
 * 
 * @see Node
 */
public class Sockets {
	
	// metadata identifier and metadata fragment constants
	
	/**
	 * This constant can be used to denote that a node represents a
	 * socket-operator in the directed acyclic graph.
	 */
	public static final String OPERATOR_TYPE = "SOCKET";
	
	/**
	 * This constant is used to identify the type of a socket-operator inside
	 * its global metadata.
	 */
	public static final String TYPE = "SOCKET->TYPE";
	
	/**
	 * An enumeration providing the local metadata fragments used in a
	 * socket-operator's global metadata for identifing the socket-operator's
	 * type.
	 */
	public static enum Type {
		
		/**
		 * This constant can be used to denote that an operator in the directed
		 * acyclic graph reads its input from a socket.
		 */
		INPUT,
		
		/**
		 * This constant can be used to denote that an operator in the directed
		 * acyclic graph writes its output to a socket.
		 */
		OUTPUT
	}
	
	/**
	 * This constant is used to identify the host of the sender of this
	 * socket-operator's communication channel inside its global metadata.
	 */
	public static final String HOST = "SOCKET->HOST";
	
	/**
	 * This constant is used to identify the port used by the sender of this
	 * socket-operator's communication channel inside its global metadata.
	 */
	public static final String PORT = "SOCKET->PORT";
	
	/**
	 * This constant is used to identify the ID of a socket-operator inside its
	 * global metadata.
	 */
	public static final String ID = "SOCKET->ID";
	
	// metadata fragment factories
	
	/**
	 * A local metadata factory that can be used to create/update the metadata
	 * fragment describing the relational data of a socket-input operator. It
	 * simply returns the relational metadata of the socket-input operator's
	 * child node.
	 */
	public static final Function<Object, ResultSetMetaData> RESULTSET_METADATA_FACTORY = new Function<Object, ResultSetMetaData>() {
		@Override
		public ResultSetMetaData invoke(Object identifier, Object operator) {
			return ResultSetMetaDatas.getResultSetMetaData(((Node)operator).getChild(0));
		}
	};
	
	// static 'constructors'

	/**
	 * Creates a new socket-operator that is able to send/receive relational
	 * data from or to another socket-operator.
	 * 
	 * @param type the type of the socket-operator. Determines whether the
	 *        operator can be used for sending or receiving its data.
	 * @param host the name of the sending socket's host.
	 * @param port the port number on the given host the sending socket is
	 *        connected to.
	 * @param id the ID of the sending socket necessary for managing the
	 *        communication between different sockets using the same port.
	 * @return a new socket-operator that is able to send/receive relational
	 *         data and its according relational metadata from or to another
	 *         socket-operator.
	 */
	public static final Node newSocket(Type type, String host, int port, String id) {
		Node operator = Operators.newOperator(OPERATOR_TYPE, type == Type.INPUT ? 0 : 1, type == Type.OUTPUT ? 0 : Nodes.VARIABLE);
		CompositeMetaData<Object, Object> globalMetaData = operator.getMetaData();
		Map<Object, Class<?>> signature = operator.getSignature();
		
		globalMetaData.add(TYPE, type);
		globalMetaData.add(HOST, host);
		globalMetaData.add(PORT, port);
		globalMetaData.add(ID, id);
		
		signature.put(TYPE, Type.class);
		signature.put(HOST, String.class);
		signature.put(PORT, Integer.class);
		signature.put(ID, String.class);
		
		operator.updateMetaData();
		
		return operator;
	}
	
	/**
	 * Creates a new socket-input operator that is able to receive relational
	 * data from a socket-output operator available at the specified port
	 * number on the given host using the given id.
	 * 
	 * @param localMetaData the relational metadata describing the data this
	 *        socket-input operator receives from the socket-output operator
	 *        specified by host, port and id.
	 * @param mode determines whether this socket-input is an active or a
	 *        passive operator, i.e., whether this socket-input transfers the
	 *        data received from the specified socket-output to its parent
	 *        operators or the parent operators must request the data from the
	 *        socket-input.
	 * @param host the name of the sending socket's host.
	 * @param port the port number on the given host the sending socket is
	 *        connected to.
	 * @param id the ID of the sending socket necessary for managing the
	 *        communication between different sockets using the same port.
	 * @return a new socket-input operator that is able to receive relational
	 *         data from a socket-output operator available at the specified
	 *         port number on the given host using the given id.
	 */
	public static final Node newInputSocket(ResultSetMetaData localMetaData, Operators.Mode mode, String host, int port, String id) {
		Node operator = newSocket(Type.INPUT, host, port, id);
		CompositeMetaData<Object, Object> globalMetaData = operator.getMetaData();
		Map<Object, Class<?>> signature = operator.getSignature();
		
		globalMetaData.add(Operators.MODE, mode);
		globalMetaData.add(ResultSetMetaDatas.RESULTSET_METADATA_TYPE, localMetaData);
		
		signature.put(Operators.MODE, Operators.Mode.class);
		signature.put(ResultSetMetaDatas.RESULTSET_METADATA_TYPE, ResultSetMetaData.class);
		
		operator.updateMetaData();
		
		return operator;
	}
	
	/**
	 * Creates a new socket-output operator that is able to send relational
	 * data to a socket-input operator. The socket-output operator is available
	 * at the specified port number on the given host using the given id.
	 * 
	 * @param input the operator node providing the relational data for the
	 *        socket-output operator.
	 * @param host the name of the sending socket's host.
	 * @param port the port number on the given host the sending socket is
	 *        connected to.
	 * @param id the ID of the sending socket necessary for managing the
	 *        communication between different sockets using the same port.
	 * @return a new socket-output operator that is able to send relational
	 *         data and its according relational metadata to a socket-input
	 *         operator. The socket-output operator is available at the
	 *         specified port number on the given host using the given id.
	 */
	public static final Node newOutputSocket(Node input, String host, int port, String id) {
		Node operator = newSocket(Type.OUTPUT, host, port, id);
		Switch<Object, Object> globalMetaDataFactory = operator.getGlobalMetaDataFactory();
		
		globalMetaDataFactory.put(Operators.MODE, Operators.MODE_METADATA_FACTORY);
		globalMetaDataFactory.put(ResultSetMetaDatas.RESULTSET_METADATA_TYPE, RESULTSET_METADATA_FACTORY);
		
		if (Nodes.getType(input) != Operators.NODE_TYPE)
			throw new IllegalArgumentException("only operators can be used as child nodes of an output socket operator");
		
		operator.addChild(input);
		
		return operator;
	}
	
	// metadata fragment accessors
	
	/**
	 * Returns the type of the given socket-operator.
	 * 
	 * @param operator the socket-operator whose type should be returned.
	 * @return the type of the given socket-operator.
	 */
	public static final Type getType(Node operator) {
		return (Type)operator.getMetaData().get(TYPE);
	}
	
	/**
	 * Returns the host of the sender of this given socket-operator's
	 * communication channel.
	 * 
	 * @param operator the socket-operator whose used host should be returned.
	 * @return the host of the sender of this given socket-operator's
	 *         communication channel.
	 */
	public static final String getHost(Node operator) {
		return (String)operator.getMetaData().get(HOST);
	}
	
	/**
	 * Returns the used port of the sender of this given socket-operator's
	 * communication channel.
	 * 
	 * @param operator the socket-operator whose used port should be returned.
	 * @return the used port of the sender of this given socket-operator's
	 *         communication channel.
	 */
	public static final int getPort(Node operator) {
		return (Integer)operator.getMetaData().get(PORT);
	}
	
	/**
	 * Returns the ID of the sending socket necessary for managing the
	 * communication between different sockets using the same port.
	 * 
	 * @param operator the socket-operator whose ID should be returned.
	 * @return the ID of the sending socket necessary for managing the
	 *         communication between different sockets using the same port.
	 */
	public static final String getID(Node operator) {
		return (String)operator.getMetaData().get(ID);
	}
	
	// XML I/O
	
	/**
	 * A factory method that can be used to transform the XML representation of
	 * a socket operator into the node itself.
	 */
	public static Function<Object, Node> XML_READER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Element element = (Element)args.get(2);
			
			Cursor<Element> children = QueryConverter.getChildren(element, QueryConverter.OPERATOR_ELEMENT);
			
			Type type = Enum.valueOf(Type.class, element.getAttribute(QueryConverter.TYPE_ATTRIBUTE));
			String host = element.getAttribute(QueryConverter.HOST_ATTRIBUTE);
			int port = Integer.valueOf(element.getAttribute(QueryConverter.PORT_ATTRIBUTE));
			String id = element.getAttribute(QueryConverter.ID_ATTRIBUTE);
			
			switch (type) {
				case INPUT:
					if (children.hasNext())
						throw new MetaDataException("an input-socket operator must not have any input operators");
					
					return newInputSocket(QueryConverter.readResultSetMetaData(element), Enum.valueOf(Operators.Mode.class, element.getAttribute(QueryConverter.MODE_ATTRIBUTE)), host, port, id);
				case OUTPUT:
					if (!children.hasNext())
						throw new MetaDataException("an output-socket operator must have exactly one input operator");
					Node child = queryConverter.read(children.next());
					if (children.hasNext())
						throw new MetaDataException("an output-socket operator must not have more than one input operator");
					
					return newOutputSocket(child, host, port, id);
			}
			throw new MetaDataException("unknown socket type " + type);
		}
	};

	/**
	 * A factory method that can be used to transform a socket operator into
	 * its XML representation.
	 */
	public static Function<Object, Node> XML_WRITER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Document document = (Document)args.get(2);
			Element element = (Element)args.get(3);
			Node operator = (Node)args.get(4);
			
			Type type = getType(operator);
			
			element.setAttribute(QueryConverter.TYPE_ATTRIBUTE, type.toString());
			element.setAttribute(QueryConverter.HOST_ATTRIBUTE, getHost(operator));
			element.setAttribute(QueryConverter.PORT_ATTRIBUTE, Integer.toString(getPort(operator)));
			element.setAttribute(QueryConverter.ID_ATTRIBUTE, getID(operator));
			
			switch (type) {
				case INPUT:
					element.setAttribute(QueryConverter.MODE_ATTRIBUTE, Operators.getMode(operator).toString());
					QueryConverter.writeResultSetMetaData(ResultSetMetaDatas.getResultSetMetaData(operator), document, element);
					break;
				case OUTPUT:
			}
			
			queryConverter.writeChildren(operator.getChildren(), document, element);

			return null;
		}
	};
	
	// query translation
	
	/**
	 * A factory method that can be used to translate a logical socket operator
	 * into an appropriate implementation.
	 */
	public static final Function<Object, MetaDataProvider<CompositeMetaData<Object, Object>>> TRANSLATION_FUNCTION = new Function<Object, MetaDataProvider<CompositeMetaData<Object, Object>>>() {
		@Override
		public MetaDataProvider<CompositeMetaData<Object, Object>> invoke(List<? extends Object> args) {
			QueryTranslator queryTranslator = (QueryTranslator)args.get(0);
			final Node node = (Node)args.get(1);
			
			Cursor<Node> children = node.getChildren();
			
			// get the socket's mode
			Operators.Mode mode = Operators.getMode(node);
			
			switch (mode) {
				case ACTIVE: {
					// create an active implementation for the logical operator
					Type type = getType(node);
					String host = getHost(node);
					int port = getPort(node);
					String id = getID(node);
					
					switch (type) {
						case INPUT: {
							// a socket provides the input for the query
							
							// test for children
							if (children.hasNext())
								throw new MetaDataException("a input-socket operator must not have any input operators");
							
							throw new UnsupportedOperationException("at this point a source reading its data from a socket channel to " + id + "@" + host + ":" + port + " should be returned");
						} // INPUT
						case OUTPUT: {
							// test for children
							if (!children.hasNext())
								throw new MetaDataException("a output-socket operator must have exactly one input operator");
							Source<? extends TemporalObject<? extends Tuple>> source = (Source<? extends TemporalObject<? extends Tuple>>)queryTranslator.translate(children.next());
							if (children.hasNext())
								throw new MetaDataException("a output-socket operator must not have more than one input operators");
							
							// a socket consumes the output of the query
							throw new UnsupportedOperationException("at this point a sink writing the data received from " + source.getMetaData() + " to a socket channel available at " + id + "@" + host + ":" + port + " should be returned");
						} // OUTPUT
					} // switch (type)
					throw new MetaDataException("unknown socket operator mode " + mode);
				}
				case PASSIVE: {
					throw new UnsupportedOperationException("the use of sockets inside the cursor algebra is still unsupported");
				} // PASSIVE
			} // switch (mode)
			throw new MetaDataException("unknown socket operator mode " + mode);
		}
	};
		
	// private constructor
	
	/**
	 * The default constructor has private access in order to ensure
	 * non-instantiability.
	 */
	private Sockets() {
		// private access in order to ensure non-instantiability
	}
	
}
