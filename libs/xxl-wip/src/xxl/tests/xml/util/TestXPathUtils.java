package xxl.tests.xml.util;

import xxl.core.xml.util.XPathUtils;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class XPathUtils.
 */
public class TestXPathUtils {

	/**
	 * Simple sample application of the static methods.
	 */
	public static void main(String[] args) {
		
		System.out.println("head: "+XPathUtils.head("/item[9]/link[42]"));
		System.out.println("tail: "+XPathUtils.tail("/item[9]/link[42]"));
		String[] sa = XPathUtils.split("item[9]");
		System.out.println(sa[0]);
		System.out.println(sa[1]);
		System.out.println(XPathUtils.tail("/item[1]"));
	}

}
