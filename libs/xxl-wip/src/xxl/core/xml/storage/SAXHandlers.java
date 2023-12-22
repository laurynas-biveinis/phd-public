/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.xml.storage;

import java.util.List;

import org.xml.sax.helpers.DefaultHandler;

import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;
import xxl.core.xml.util.XPathFunctionCallerHandler;
import xxl.core.xml.util.XPathLocation;

/**
 * This class contains some SAX Handlers which are
 * quite useful in conjunction with the EXTree.
 */
public class SAXHandlers {
	/**
	 * Returns a SAX handler for a given tree, which inserts
	 * the parsed XML.
	 * @param tree EXTree into which the insertion is performed.
	 * @return The SAX Handler.
	 */
	public static DefaultHandler getTreeInsertionHandler(final EXTree tree) {

		Function fc = new AbstractFunction() {  // the function for character (literals)
			public Object invoke(Object xPath, Object string) {
				if (tree.insertLiteralNode((XPathLocation) xPath, (String) string))
					return Boolean.TRUE;
				else
					throw new RuntimeException("Insertion Failed! ("+xPath+", "+string+")");
			}
		};

		Function ft = new AbstractFunction() { //the function for tags (markups)
			public Object invoke(Object xPath, Object string) {
				if (tree.insertMarkupNode((XPathLocation) xPath, (String) string))
					return Boolean.TRUE;
				else
					throw new RuntimeException("Insertion Failed! ("+xPath+", "+string+")");
			}
		};

		Function fa = new AbstractFunction<Object,Object>() { //the function for attributes
			public Object invoke(List<? extends Object> list) {
				if (tree.insertAttribute((XPathLocation) list.get(0) , (String) list.get(1), (String) list.get(2)))
					return Boolean.TRUE;
				else
					throw new RuntimeException("Insertion Failed! ("+list.get(0)+", "+list.get(1)+", "+list.get(2)+")");
			}
		};

		return new XPathFunctionCallerHandler(fc,ft,fa);
		// return new XPathFunctionCallerHandler(getDecoratorFunction(fc),ft,fa);
		// return new XPathFunctionCallerHandler(
		//	xxl.core.functions.Functions.printlnDecoratorFunction(fc, System.out, true, "literalNode(", ",", ")=", "\n"),
		//	xxl.core.functions.Functions.printlnDecoratorFunction(ft, System.out, true, "tagNode(", ",", ")=", "\n"),
		//	xxl.core.functions.Functions.printlnDecoratorFunction(fa, System.out, true, "attributeNode(", ",", ")=", "\n")
		// );
	}
}
