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

	http://www.mathematik.uni-marburg.de/DBS/xxl

bugs, requests for enhancements: xxl@mathematik.uni-marburg.de

If you want to be informed on new versions of XXL you can 
subscribe to our mailing-list. Send an email to 
	
	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body. 
*/

package xxl.core.relational.query.operators;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.Cursors;
import xxl.core.cursors.sources.ArrayCursor;
import xxl.core.cursors.sources.SingleObjectCursor;
import xxl.core.functions.Function;
import xxl.core.functions.Switch;
import xxl.core.relational.QueryConverter;
import xxl.core.relational.QueryTranslator;
import xxl.core.relational.query.Node;
import xxl.core.relational.query.Nodes;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataException;
import xxl.core.util.metaData.MetaDataProvider;
import xxl.core.util.metaData.TransparentCompositeMetaData;

/**
 * This class provides static methods for creating <i>ready to use</i>
 * instances of an {@link Node equivalence node} in a directed acyclic graph.
 * Beside these methods, it contains constants for identifying local metadata
 * fragments inside an equivalence node's global metadata, methods for
 * accessing them and local metadata factories for updating them.
 * 
 * @see Node
 */
public class Equivalences {
	
	// metadata identifier and metadata fragment constants
	
	/**
	 * This constant can be used to denote that a node represents an
	 * equivalence node in the directed acyclic graph.
	 */
	public static final String OPERATOR_TYPE = "EQUIVALENCE";
	
	/**
	 * This constant is used to identify an equivalance node's choice, i.e.,
	 * the index of the child node that represents the equivalence node's query
	 * at this time, inside its global metadata.
	 */
	public static final String CHOICE = "EQUIVALENCE->CHOICE";
	
	/**
	 * This constant is used to identify the physical representation of an
	 * equivalance node, i.e., the physical representation of the equivalence
	 * node's choice, inside its global metadata.
	 */
	public static final String PHYSICAL_REPRESENTATION = "EQUIVALENCE->PHYSICAL_REPRESENTATION";
	
	/**
	 * This constant is used to identify the additional utilization of an
	 * equivalance node, i.e., the number of nodes that are part of an
	 * equivalence node's query that cannot be reused.
	 */
	public static final String ADDITIONAL_UTILIZATION = "EQUIVALENCE->ADDITIONAL_UTILIZATION";

	/**
	 * A <i>all-signatures</i> strategy that simple returns the equivalence
	 * node's chosen child node.
	 */
	public static final Function<Node, Iterator<Node>> SIGNATURE_OF_CHOICE = new Function<Node, Iterator<Node>>() {
		@Override
		public Iterator<Node> invoke(Node equivalence) {
			return new SingleObjectCursor<Node>(equivalence.getChild(getChoice(equivalence)));
		}
	};

	// static 'constructors'
	
	/**
	 * Creates a new equivalence node gathering the equivalent queries
	 * represented by the given operator nodes (the node having the specified
	 * index is the chosen one) and providing the required local metadata
	 * fragments and registers factory methods for this metadata fragments.
	 * 
	 * @param operators an iteration over the operator nodes representing the
	 *        equivalent queries gathered by the returned equivalence node.
	 * @param choice the choice of the returned equivalence node, i.e., the
	 *        index of the equivalence node's child that is actually chosen to
	 *        represent the query.
	 * @return a new equivalence node gathering the equivalent queries
	 *         represented by the given operator nodes (the node having the
	 *         specified index is the chosen one) and providing the required
	 *         local metadata fragments and registers factory methods for this
	 *         metadata fragments.
	 */
	public static final Node newEquivalence(Iterator<? extends Node> operators, int choice) {
		Node equivalence = new Node(new TransparentCompositeMetaData<Object, Object>(), new Switch<Object, Object>(), new HashMap<Object, Class<?>>());
		CompositeMetaData<Object, Object> globalMetaData = equivalence.getMetaData();
		Switch<Object, Object> globalMetaDataFactory = equivalence.getGlobalMetaDataFactory();
		Map<Object, Class<?>> signature = equivalence.getSignature();
		
		globalMetaDataFactory.put(equivalence, Nodes.GLOBAL_METADATA_FACTORY);
		globalMetaData.put(Nodes.TYPE, Operators.NODE_TYPE);
		globalMetaData.put(Nodes.NUMBER_OF_CHILDREN, Nodes.VARIABLE);
		globalMetaData.put(Nodes.NUMBER_OF_PARENTS, Nodes.VARIABLE);
		globalMetaData.put(Nodes.ALL_SIGNATURES_STRATEGY, SIGNATURE_OF_CHOICE);
		globalMetaData.put(Operators.TYPE, OPERATOR_TYPE);
		globalMetaData.put(CHOICE, choice);
		
		signature.put(Nodes.TYPE, String.class);
		signature.put(Nodes.NUMBER_OF_CHILDREN, Integer.class);
		signature.put(Nodes.NUMBER_OF_PARENTS, Integer.class);
		signature.put(Operators.TYPE, String.class);
		
		Node operator;
		for (int i = 0; operators.hasNext(); i++) {
			if (Nodes.getType(operator = operators.next()) != Operators.NODE_TYPE)
				throw new IllegalArgumentException("only operators can be used as child nodes of an equivalence operator");
			
			equivalence.addChild(operator);
			if (i == choice)
				((TransparentCompositeMetaData<Object, Object>)globalMetaData).setCompositeMetaData(operator.getMetaData());
			
			HashMap<Node, Node> uniques = new HashMap<Node, Node>();
			for (int j = 0; j < operator.getActualNumberOfChildren(); j++) {
				Node child = operator.getChild(j);
				if (Operators.getType(child) != OPERATOR_TYPE) {
					if (!uniques.containsKey(child))
						uniques.put(child, newEquivalence(child));
					operator.replaceChild(j, uniques.get(child));
				}
			}
		}
		
		return equivalence;
	}
	
	/**
	 * Creates a new equivalence node gathering the equivalent queries
	 * represented by the given operator nodes (the node having the specified
	 * index is the chosen one) and providing the required local metadata
	 * fragments and registers factory methods for this metadata fragments.
	 * 
	 * @param choice the choice of the returned equivalence node, i.e., the
	 *        index of the equivalence node's child that is actually chosen to
	 *        represent the query.
	 * @param operators the operator nodes representing the equivalent queries
	 *        gathered by the returned equivalence node.
	 * @return a new equivalence node gathering the equivalent queries
	 *         represented by the given operator nodes (the node having the
	 *         specified index is the chosen one) and providing the required
	 *         local metadata fragments and registers factory methods for this
	 *         metadata fragments.
	 */
	public static final Node newEquivalence(int choice, Node... operators) {
		return newEquivalence(new ArrayCursor<Node>(operators), choice);
	}
	
	/**
	 * Creates a new equivalence node representing the query defined by the
	 * given operator node and providing the required local metadata fragments
	 * and registers factory methods for this metadata fragments.
	 * 
	 * @param operator the operator node defining the query that should be
	 *        represented by the returned equivalence node.
	 * @return a new equivalence node representing the query defined by the
	 *         given operator node and providing the required local metadata
	 *         fragments and registers factory methods for this metadata
	 *         fragments.
	 */
	public static final Node newEquivalence(Node operator) {
		return newEquivalence(new SingleObjectCursor<Node>(operator), 0);
	}
	
	// metadata fragment accessors
	
	/**
	 * Returns the choice of the given equivalence node, i.e., the index of the
	 * child node that represents the equivalence node's query at this time.
	 * 
	 * @param equivalence the equivalence node whose choice should be returned.
	 * @return the choice of the given equivalence node, i.e., the index of the
	 *         child node that represents the equivalence node's query at this
	 *         time.
	 */
	public static final int getChoice(Node equivalence) {
		return (Integer)equivalence.getMetaData().get(CHOICE);
	}
	
	/**
	 * Returns the physical representation of the given equivalence node, i.e.,
	 * the physical representation of the given equivalence node's choice.
	 * 
	 * @param equivalence the equivalence node whose physical representation
	 *        should be returned.
	 * @return the physical representation of the given equivalence node, i.e.,
	 *         the physical representation of the given equivalence node's
	 *         choice.
	 */
	@SuppressWarnings("unchecked")
	public static final MetaDataProvider<CompositeMetaData<Object, Object>> getPhysicalRepresentation(Node equivalence) {
		return (MetaDataProvider<CompositeMetaData<Object, Object>>)equivalence.getMetaData().get(PHYSICAL_REPRESENTATION);
	}

	/**
	 * Returns the additional utilization of the given equivalence node, i.e.,
	 * the number of nodes that are part of the query represented by the given
	 * equivalence node that cannot be reused.
	 * 
	 * @param equivalence the equivalence node whose additional utilization
	 *        should be returned.
	 * @return the additional utilization of the given equivalence node, i.e.,
	 *         the number of nodes that are part of the query represented by
	 *         the given equivalence node that cannot be reused.
	 */
	public static final int getAdditionalUtilization(Node equivalence) {
		return (Integer)equivalence.getMetaData().get(ADDITIONAL_UTILIZATION);
	}
	
	// XML I/O
	
	/**
	 * A factory method that can be used to transform the XML representation of
	 * an equivalence node into the node itself.
	 */
	public static final Function<Object, Node> XML_READER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Element element = (Element)args.get(2);
			
			return newEquivalence(
				queryConverter.readChildren(element),
				Integer.valueOf(element.getAttribute(QueryConverter.CHOICE_ATTRIBUTE))
			);
		}
	};

	/**
	 * A factory method that can be used to transform an equivalence node into
	 * its XML representation.
	 */
	public static final Function<Object, Node> XML_WRITER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Document document = (Document)args.get(2);
			Element element = (Element)args.get(3);
			Node equivalence = (Node)args.get(4);
				
			element.setAttribute(QueryConverter.CHOICE_ATTRIBUTE, Integer.toString(getChoice(equivalence)));
			queryConverter.writeChildren(equivalence.getChildren(), document, element);
			
			return null;
		}
	};
	
	// query translation
	
	/**
	 * A factory method that can be used to translate (remove) an equivalence
	 * node and replace it by the implementation of the chosen child node.
	 */
	public static final Function<Object, MetaDataProvider<CompositeMetaData<Object, Object>>> TRANSLATION_FUNCTION = new Function<Object, MetaDataProvider<CompositeMetaData<Object, Object>>>() {
		@Override
		public MetaDataProvider<CompositeMetaData<Object, Object>> invoke(List<? extends Object> args) {
			QueryTranslator queryTranslator = (QueryTranslator)args.get(0);
			Node node = (Node)args.get(1);
			
			if (node.getMetaData().contains(PHYSICAL_REPRESENTATION))
				return getPhysicalRepresentation(node);
			
			Cursor<Node> children = node.getChildren();
			
			int choice = getChoice(node);
			
			try {
				MetaDataProvider<CompositeMetaData<Object, Object>> physicalRepresentation = queryTranslator.translate(Cursors.nth(children, choice));
				
				node.getMetaData().put(PHYSICAL_REPRESENTATION, physicalRepresentation);
				
				return physicalRepresentation;
			}
			catch (NoSuchElementException nsee) {
				throw new MetaDataException("the chosen query plan (operator at index " + choice + ") does not exist");
			}
		}
	};
		
	// private constructor

	/**
	 * The default constructor has private access in order to ensure
	 * non-instantiability.
	 */
	private Equivalences() {
		// private access in order to ensure non-instantiability
	}
	
}