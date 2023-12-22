package xxl.tests.jts;

import xxl.connectivity.jts.ExtendedDistanceOp;
import xxl.connectivity.jts.Geometry2DAdapter;
import xxl.connectivity.jts.Geometry2DFactory;
import xxl.core.spatial.geometries.Geometry2DException;
import xxl.core.spatial.rectangles.Rectangle;

import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Geometry2DAdapter.
 */
public class TestGeometry2DAdapter {


    /** USE CASE: Runs various tests on Geometry2DAdapter- objects like 
     * 	evaluating spatial predicates and distance operators, conversion from xxl to jts 
     *  and a robustness test.  
	 * @param args Use Case Parameter: obsolete 
     * @throws com.vividsolutions.jts.io.ParseException 
	 */   
    public static void main(String[] args) throws com.vividsolutions.jts.io.ParseException{
        
        Geometry2DAdapter g1= null, g2=null, g3=null;
        
        System.out.println("\n\nCreate some Geometry2DAdapter objects" +
        					 "\n=======================================");
        
        g1 = Geometry2DFactory.createFromWKT("POLYGON ((0.6 0.4, 10.3 10.1, 10.6 0, 0.6 0.4))", new PrecisionModel(1));
        g2 = Geometry2DFactory.createFromWKT("POLYGON ((5 0, 5 5, 15 5, 15 0, 5 0))");                    
        g3 = Geometry2DFactory.createRectangle(Geometry2DFactory.createPoint(5,0),15,-5);
        System.out.println( "\n  g1= "+g1+
        					"\n  g2= "+g2+
        					"\n  g3= "+g3);
                
        
        System.out.println("\n\nTesting spatial predicates" +
		 					 "\n===========================\n"+
		 					 "\n g1.overlaps(g2)= "+g1.overlaps(g2)+                        
        					 "\n g1.distance(g2)= "+g1.distance(g2)+
        					 "\n g2.equals(g3)  = "+g2.equals(g3));
        g3.union(g1);
        System.out.println(  "\n g3.union(g1)= "+g3+        
        					 "\n g3.contains(g2)= "+g3.contains(g2));
        
        
        System.out.println("\n\nTesting graphical output" +
	 			 "\n===========================\n" +
				 "\n red   -> g1 "+                        
				 "\n green -> g2 "+
				 "\n blue  -> g3 "+
				 "\n gray  -> g1.intersection(g2)");

		xxl.connectivity.jts.visual.VisualOutput output = new xxl.connectivity.jts.visual.VisualOutput("Test::Geometry2DAdapter", g1.union(g2).union(g3).getMBR(), 400);
		output.fill(g1.intersection(g2), java.awt.Color.lightGray);
		output.draw(g1, java.awt.Color.red);
		output.draw(g2, java.awt.Color.green);
		output.draw(g3, java.awt.Color.blue);
		output.repaint();
		
        System.out.println("\n\nTesting conversion from XXL to Geometry2DAdapter" +
        					 "\n==================================================\n");
        
        Rectangle d1 = new xxl.core.spatial.rectangles.DoublePointRectangle(new double[]{0,0},new double[]{2,2});
        Rectangle d2 = new xxl.core.spatial.rectangles.FloatPointRectangle(new float[]{5,5},new float[]{10,10});
        System.out.println("  d1= "+d1+"\n  d2= "+d2);
        
        Geometry2DAdapter x1 = null, x2 = null;

        x1 = Geometry2DFactory.xxlRectangleToPolygon2D(d1);
        x2 = Geometry2DFactory.xxlRectangleToPolygon2D(d2);
        	        
        System.out.println("  d1 ~> x1 : "+ x1 +
        		 		 "\n  d2 ~> x2 : "+ x2 +
        				 "\nForce Geometry2DException:");
        
        Geometry2DAdapter x3 = null;        
        try{
            Rectangle d3 = new xxl.core.spatial.rectangles.DoublePointRectangle(new double[]{0,},new double[]{1});
            System.out.println("\n  d3= "+d3+"\n  d3 ~> x3 :");
            x3= Geometry2DFactory.xxlRectangleToPolygon2D(d3);
            System.out.println(x3);
        } catch(Geometry2DException e){ e.printStackTrace(System.out);} 
        
        System.out.println("\n\nTesting Distance- Operator" +
        		             "\n===========================\n"+
        		             "\n x1.distance(g3)= "+x1.distance(g3)+",  closest Points:"+java.util.Arrays.asList(ExtendedDistanceOp.closestPoints(x1,g3))+
        					 "\n x1.maxDistance(g3)= "+ExtendedDistanceOp.maxDistance(x1,g3)+", furthest Points:"+java.util.Arrays.asList(ExtendedDistanceOp.furthestPoints(x1,g3)));
        	        

        String wkt1 = "POLYGON ((708653.498611049 2402311.54647056, 708708.895756966 2402203.47250014, 708280.326454234 2402089.6337791, 708247.896591321 2402252.48269854, 708367.379593851 2402324.00761653, 708248.882609455 2402253.07294874, 708249.523621829 2402244.3124463, 708261.854734465 2402182.39086576, 708262.818392579 2402183.35452387, 708653.498611049 2402311.54647056))";
        String wkt2 = "POLYGON ((708258.754920656 2402197.91172757, 708257.029447455 2402206.56901508, 708652.961095455 2402312.65463437, 708657.068786251 2402304.6356364, 708258.754920656 2402197.91172757))";
        
        Geometry2DAdapter wg1 = Geometry2DFactory.createFromWKT(wkt1);
	    Geometry2DAdapter wg2 = Geometry2DFactory.createFromWKT(wkt2);
	    
        System.out.println("\n\nTesting Robustness" +
        		             "\n=====================\n"+
        		             "\nThe intersection of these two polygons would cause a TopologyException, if the EnhancedPrecisionOp wasn't used!+" +
        		             "\n Polygon 1: "+wg1+
        		             "\n Polygon 2: "+wg2+
        		             "\n Intersection: "+wg1.intersection(wg2));
	    
        Geometry2DAdapter empty = Geometry2DFactory.createGeometryCollection2D(null);
        System.out.println("\n\nTesting empty geometry semantics" +
	    		             "\n=================================\n"+
	    		             "\n empty = "+empty+
	    		             "\n empty.intersects(g3)="+empty.intersects(g3));
        
   }

}
