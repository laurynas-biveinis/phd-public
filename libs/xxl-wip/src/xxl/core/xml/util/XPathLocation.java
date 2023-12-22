/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.xml.util;

import xxl.core.util.Arrays;

/**
 * This class represents an XPath location. It stores
 * the information inside arrays and is very efficient.
 */
public class XPathLocation {
	/**
	 * Contains an int field of 50 elements with each field set to 1.  
	 */
	private static final int COUNT_FIELD_1[] = new int[] {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};
	
	/**
	 * Contains a boolean field of 50 elements with each field set to false.  
	 */
	private static final boolean ANCESTOR_FIELD_FALSE[] = new boolean[] {
			false, false, false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false, false, false,
		};
	
	/**
	 * Number of field entries allocated, when the fields
	 * are too small. 
	 */
	private static final int INCREMENT_FIELDS = 10;

	/**
	 * Markup Strings of the XPath expression.
	 */
	protected String markups[];

	/**
	 * Number of a tag on a level. 0 means, that the tag count is
	 * always matched.
	 */
	protected int counts[];

	/**
	 * ancestorFlag[x]==true means ancestor relationship ("\\").
	 */
	protected boolean ancestorFlag[];

	/**
	 * Number of markups that are stored inside the location.
 	 */
 	protected int size;

	/**
	 * Stores the level, where the next matching is desired.
	 */
	protected int matchLevel;
	
	/**
	 * Constructs a XPathLocation object and initializes it 
	 * with a XPath expression.
	 * @param location XPath expression
	 */
	public XPathLocation(String location) {
		if (location.charAt(0)!='/')
			throw new RuntimeException("XPath expression is not valid");
		matchLevel=0;
		
		// Determine the number of markup parts inside the XPath expression.
		int position=0; // 0 is '/', 1 can be '/' or first character of markup
		size=0;
		// count the number of '/' which are not '//'.
		do {
			size++;
			// position is '/', position+1 can be '/' or the first character of markup
			position += 2;
			position = location.indexOf('/',position);
		} while (position!=-1);
		
		// Initialize the arrays.
		markups = new String[size];
		counts = new int[size];
		ancestorFlag = new boolean[size];

		position=0; // 0 is '/', 1 can be '/' or first character of markup
		int parts=0;
		int startposition;
		int endposition;

		do {
			// position is '/', position+1 can be '/' or the first character of markup
			if (location.charAt(position+1)=='/') {
				ancestorFlag[parts]=true;
				startposition = position+1;
			}
			else
				startposition = position;

			position += 2;
			position = location.indexOf('/',position);
			
			if (position==-1)
				endposition = location.length();
			else
				endposition = position;

			// go backward to []
			if (location.charAt(endposition-1)==']') {
				endposition -= 2;
				int no = 0;
				int value=1;
				while (location.charAt(endposition)!='[') {
					no += (value*(location.charAt(endposition)-'0'));
					value *= 10;
					endposition--;
				}
				counts[parts]=no;
			}
			else {
				counts[parts]=0; // always matched!
			}
			
			markups[parts] = location.substring(startposition+1, endposition);
			
			parts++;
		} while (position!=-1);
	}

	/**
	 * Constructs an empty XPathLocation.
	 */
	public XPathLocation() {
		size = 0;
		matchLevel = 0;
		markups = new String[INCREMENT_FIELDS];
		counts = new int[INCREMENT_FIELDS];
		ancestorFlag = new boolean[INCREMENT_FIELDS];
	}

	/**
	 * Clones the XPathLocation.
	 * @return a new equal XPathLocation object which is independant 
	 * 	of the original one.
	 */
	public Object clone() {
		XPathLocation xpl = new XPathLocation();
		
		xpl.size = size;
		xpl.matchLevel = matchLevel;
		
		// Clone the array, not the elements, because the elements are immutable.
		xpl.markups = (String[]) markups.clone();
		
		if (counts==COUNT_FIELD_1)
			xpl.counts = counts;
		else
			xpl.counts = Arrays.copy(counts, 0, size);
		if (ancestorFlag==ANCESTOR_FIELD_FALSE)
			xpl.ancestorFlag = ancestorFlag;
		else
			xpl.ancestorFlag = Arrays.copy(ancestorFlag, 0, size);
		return xpl;
	}

	/**
	 * Sets each markup count field to 1. 
	 */
	public final void setMarkupCountTo1() {
		if (size<=COUNT_FIELD_1.length)
			counts = COUNT_FIELD_1;
		else
			for (int i=0; i<size; i++)
				counts[i] = 1;
	}
	
	/**
	 * Sets each ancestor flag field to false. 
	 */
	public final void setAncestorFlagsToFalse() {
		if (size<=ANCESTOR_FIELD_FALSE.length)
			ancestorFlag = ANCESTOR_FIELD_FALSE;
		else
			for (int i=0; i<size; i++)
				ancestorFlag[i] = false;
	}

	/**
	 * Matches the next part of the XPath expression with the
	 * String s. Before matching the first part of an XPath expression,
	 * you should set the current matching level to 0 (especially,
	 * when you use the same XPathLocation object for multiple matches).
     * @param s String to be matched.
	 * @param number Count of part to be matched. If the number is 0 
	 * 	then, the number is always matched.
	 * @return an integer signaling the following:<br>
	 *  0: the next markup part could not be matched.<br>
	 *  1: the next markup part is matched (the markup string is<br>
	 *		equal), but the number is not matched.<br>
	 *  -1: markup and number are matched. The match level is increased.<br>
	 *  -2: the part could be matched due to the fact, that it is an ancestor tag.
	 */
	public final int matchPart(String s, int number) {
		if (markups[matchLevel].equals(s)) {
			if (number==0 || number==counts[matchLevel] || counts[matchLevel]==0) {
				matchLevel++;
				return -1;
			}
			else
				return 1;
		}
		else
			return ancestorFlag[matchLevel]?-2:0;
	}

	/**
	 * Returns the current match level.
	 * @return the current match level.
	 */
	public final int getMatchLevel() {
		return matchLevel;
	}

	/**
	 * Sets the match level to the desired value.
	 * @param level the new match level.
	 */
	public final void setMatchLevel(int level) {
		matchLevel = level;
	}

	/**
	 * Determines, if the expression is completly matched
	 * or not.
	 * @return true iff the expression is matched.
	 */
	public final boolean isMatchingComplete() {
		return matchLevel==size;
	}

	/**
	 * Retrieves a markup part of an XPath expression.
	 * @param number of the markup inside the expression.
	 * @return the markup String.
	 */
	public final String getMarkup(int number) {
		return markups[number];
	}

	/**
	 * Retrieves the count of a markup of an XPath expression.
	 * @param number of the markup inside the expression.
	 * @return the count.
	 */
	public final int getMarkupCount(int number) {
		return counts[number];
	}

	/**
	 * Retrieves, if the markup with the associated number
	 * has an ancestor operator in front of itself or a
	 * normal descendant operator.
	 * @param number of the markup inside the expression.
	 * @return true if it is an ancestor operator, false,
	 *	if it is the descendant operator.
	 */
	public final boolean getAncestorFlag(int number) {
		return ancestorFlag[number];
	}

	/**
	 * Returns the number of parts inside the XPath expression.
	 * Example: /PLAY/ACT contains two parts.
	 * @return the number of parts.
	 */
	public final int getNumberOfParts() {
		return size;
	}

	/**
	 * Sets the count of the last tag in the location.
	 * @param number the new count of the last tag.
	 */
	public final void setLastCount(int number) {
		counts[size-1] = number;
	}

	/**
	 * Appends a new tag to the end. If the internally used arrays are
	 * to small, then new bigger arrays are allocated and the content is copied.
	 * @param markup the tag name of the new entry.
	 * @param count the count of the new entry.
	 * @param ancestor the ancestor flag of the new entry.
	 */ 
	public void append(String markup, int count, boolean ancestor) {
		if (size==markups.length) {
			String markupsNew[] = new String[size+INCREMENT_FIELDS];
			System.arraycopy(markups, 0, markupsNew, 0, size);
			markups = markupsNew;
		}
		if (counts==COUNT_FIELD_1 || size==counts.length) {
			int countsNew[] = new int[size+INCREMENT_FIELDS];
			System.arraycopy(counts, 0, countsNew, 0, size);
			counts = countsNew;
		}
		if (ancestorFlag==ANCESTOR_FIELD_FALSE || size==ancestorFlag.length) {
			boolean ancestorNew[] = new boolean[size+INCREMENT_FIELDS];
			System.arraycopy(ancestorFlag, 0, ancestorNew, 0, size);
			ancestorFlag = ancestorNew;
		}
		markups[size] = markup;
		counts[size] = count;
		ancestorFlag[size] = ancestor;
		size++;
	}

	/**
	 * Removes the last makup tag.
	 */
	public void removeLast() {
		size--;
	}

	/**
	 * Returns a String representation of a XPathLocation
	 * Object. For each tag, the name of the markup, the markup count
	 * and if the connection is direct is outputed.
	 * @return The string representation.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<size; i++) {
			if (ancestorFlag[i])
				sb.append("//");
			else
				sb.append("/");
			sb.append(markups[i]);
			sb.append("[");
			sb.append(counts[i]);
			sb.append("]");
		}
		return sb.toString();
	}
}
