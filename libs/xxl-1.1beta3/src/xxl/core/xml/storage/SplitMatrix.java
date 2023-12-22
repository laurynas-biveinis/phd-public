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

package xxl.core.xml.storage;

import java.util.HashMap;

/**
 * The split matrix is a way for tailoring the split algorithm of
 * the EXTree to application requirements.
 * The matrix holds values for pairs of parent/child nodes determining
 * whether these nodes should be held in one record, should be split or
 * can be treated by the algorithm randomly.
 * It is no necessary to add all possible node combintations to the
 * matrix, nonexistent entrys always return OTHER.
 */
public class SplitMatrix {

    /**
     * This value in a matrix entry means that a child node should be separated from
     * the parent node.
     */
    public static final int STANDALONE = 0;

    /**
     * This value in a matrix entry means that the algorithm may decide,
     * what to do with parent/child nodes.
     */
    public static final int OTHER = 1;

    /**
     * This value in a matrix entry means that the father and child node should
     * not be separated.
     */
    public static final int CLUSTER = Integer.MAX_VALUE;

    /** 
     * That's where we start.
     */
    private int[][] INIT = {{1,1,1,1}, {1,1,1,1}, {1,1,1,1},{1,1,1,1}};

    /** 
     * The matrix itself.
     */
    protected int[][] matrix;

    /** 
     * Which label to map to which index of the matrix.
     */
    protected HashMap mappings;

    /**
     * Whether to tread attribute nodes as having CLUSTER entrys in the
     * matrix for all possible parents
     */
    protected boolean clusterAttributes;

    /**
     * Maximum index used so far.
     */
    private int maxId;

    /**
     * Creates a new instance of SplitMatrix 
     */
    public SplitMatrix() {
        matrix = INIT;
        mappings = new HashMap();
        maxId = -1;
        clusterAttributes = false;
    }

    /**
     * Creates a new instance of SplitMatrix
     * @param _clusterAttributes True: always store attribute nodes  with their parents.
     * 	False: Tread them as any other node type.
     */
    public SplitMatrix(boolean _clusterAttributes) {
        matrix = INIT;
        mappings = new HashMap();
        maxId = -1;
        clusterAttributes = _clusterAttributes;
    }

    /**
     * Adds a new entry to the split matrix, determining the split algorithms
     * behaviour when encountering the given parent-child combination.
     * @param parent The label of the parent noce.
     * @param child The label of the child node.
     * @param value The desired behaviour.
     */
    public void addMatrixEntry(String parent, String child, int value) {

        int parentId, childId;

        if (!mappings.containsKey(parent)) parentId = addMapping(parent);
        else parentId = ((Integer) mappings.get(parent)).intValue();

        if (!mappings.containsKey(child)) childId = addMapping(child);
        else childId = ((Integer) mappings.get(child)).intValue();

        matrix[parentId][childId]=value;
    }

    /**
     * Gets the value of the splitmatrix for the given parent-child combination.
     * @param parent The label of the parent node.
     * @param child The label of the child node.
     * @return The entry for the parent-child combination, OTHER if no entry
     * exists for the nodes.
     */
    public int getMatrixEntry(String parent, String child) {
        if (mappings.containsKey(parent) && mappings.containsKey(child)) {
            return matrix[((Integer) mappings.get(parent)).intValue()]
            [((Integer) mappings.get(child)).intValue()];
        }
        else
        	return OTHER;
    }

    /**
     * Gets the value of the splitmatrix for the given parent-child combination.
     * @param parentNode The label of the parent node.
     * @param childNode The label of the child node.
     * @return The entry for the parent-child combination, OTHER if no entry
     * exists for the nodes.
     */
    public int getMatrixEntry(Node parentNode, Node childNode) {
        if (parentNode.getType() == Node.MARKUP_NODE && childNode.getType() == Node.MARKUP_NODE){
            if (clusterAttributes && ((MarkupNode)childNode).isAttribute()) return OTHER;
            String parent = ((MarkupNode) parentNode).getTagName();
            String child = ((MarkupNode) childNode).getTagName();
            return getMatrixEntry(parent, child);
        }
        else
        	return OTHER;
    }

    private int addMapping(String label) {
        mappings.put(label, new Integer(++maxId));
        if (maxId >= matrix.length) {
            int len = matrix.length*2;
            int[][] newMatrix  = new int[len][len];
            for (int i = 0;i < len; i++)
                for (int j = 0; j < len; j++)
                    if (i < matrix.length && j < matrix.length)
                        newMatrix[i][j] = matrix[i][j];
                    else newMatrix[i][j] = 1;
            matrix = newMatrix;
        }
        return maxId;
    }

    /**
     * Very primitive tests for this class.
     * @param args Command line arguments are ignored.
     */
    public static void main(String args[])  {
        SplitMatrix matrix = new SplitMatrix();
        matrix.addMatrixEntry("p","c",1);
        matrix.addMatrixEntry("p","d",2);
        matrix.addMatrixEntry("g","c",3);
        matrix.addMatrixEntry("g","c",4);
        matrix.addMatrixEntry("g","e",5);
        matrix.addMatrixEntry("x","y",6);
        System.out.println(matrix.getMatrixEntry("p","c"));
        System.out.println(matrix.getMatrixEntry("p","d"));
        System.out.println(matrix.getMatrixEntry("g","c"));
        System.out.println(matrix.getMatrixEntry("g","d"));
        System.out.println(matrix.getMatrixEntry("a","c"));
        System.out.println(matrix.getMatrixEntry("a","a"));
        System.out.println(matrix.getMatrixEntry("g","c"));
        System.out.println(matrix.getMatrixEntry("g","e"));
        System.out.println(matrix.getMatrixEntry("x","y"));
    }
}
