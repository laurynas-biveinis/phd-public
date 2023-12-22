package xxl.tests.xml.util;

import xxl.core.xml.util.XPathLocation;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class XPathLocation.
 */
public class TestXPathLocation {

	/**
	 * Contains a simple example of the usage of XPathLocation.
	 * @param args Parameters are ignored.
	 */
	public static void main (String args[]) {
		long t1, t2;
		long count = 100000;
		
		String xp1 = "/PLAY[1]/ACT[1]/SCENE[1]/SPEECH[2]";
		String xp2 = "/PLAY[32]/ACT[1]//SCENE[1]/SPEECH[2]";
		String xp3 = "/PLAY/ACT[11120]//SCENE[1]/SPEECH[2]";
		String xp4 = "/PLAY/ACT[1]//SCENE[1]/S";
		String xp5 = "/PLAY/A/SCENE[1]//S";
		
		String xps[] = new String[] {xp1, xp2, xp3, xp4, xp5};

		for (int i=0; i<xps.length; i++) {
			System.out.println("XPath expression: "+xps[i]);
			System.out.println(new XPathLocation(xps[i]));
			System.out.println();
		}
		
		XPathLocation xp = new XPathLocation();
		xp.append("PLAY", 32, false);
		xp.append("ACT", 1, false);
		xp.append("SCENE", 5, true);
		xp.append("SPEECH", 2, false);
		System.out.println("XPath location constructed with append: "+xp);
		xp.removeLast();
		System.out.println("After a removeLast(): "+xp);
		xp.setMarkupCountTo1();
		xp.setAncestorFlagsToFalse();
		System.out.println("No ancestor operators and count=1: "+xp);
		XPathLocation xpc = (XPathLocation) xp.clone();
		System.out.println("Clone XPathLocation: "+xpc);
		xp.removeLast();
		System.out.println("After a removeLast(): "+xp);
		xp.append("SCENE", 3, true);
		xp.append("SPEECH", 222, false);
		System.out.println("Append SCENE[3]/SPEECH[222]: "+xp);
		xp.append("LINE", 42, false);
		System.out.println("Append LINE[42]: "+xp);
		System.out.println("The previously cloned XPathLocation: "+xpc);
		
		int countParts=-1;
		System.out.println("Speed test...");
		t1 = System.currentTimeMillis();
		while (count-->0) {
			for (int i=0; i<xps.length; i++) {
				XPathLocation xpl = new XPathLocation(xps[i]);
				countParts = xpl.getNumberOfParts();
			}
		}
		t2 = System.currentTimeMillis();
		System.out.println("Parts of the XPath expression: "+countParts);
		System.out.println("Time: "+(t2-t1)+"ms");
	}

}
