package xxl.tests.xml.storage;

import xxl.core.xml.storage.SplitMatrix;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class SplitMatrix.
 */
public class TestSplitMatrix {

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
