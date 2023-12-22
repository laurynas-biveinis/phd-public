package xxl.tests.jts.visual;

import java.awt.Color;
import java.awt.Shape;

import xxl.connectivity.jts.Geometry2DFactory;
import xxl.connectivity.jts.visual.VisualOutput;
import xxl.connectivity.jts.visual.VisualOutputCursor;
import xxl.core.spatial.rectangles.DoublePointRectangle;
import xxl.core.spatial.rectangles.Rectangle;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class VisualOutput.
 */
public class TestVisualOutput {
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args){
		Rectangle universe = new DoublePointRectangle(new double[]{-10,-10}, new double[]{0,0});
		VisualOutput out = new VisualOutput("", universe, 700);
		
		Rectangle r = new DoublePointRectangle(new double[]{-10,-5}, new double[]{0,-3});
		Shape s = VisualOutputCursor.rectangleToShape.invoke(r);	
		out.transformAndDraw(s, Color.RED);
		out.draw(Geometry2DFactory.createFromWKT("Polygon((0 -3, 0 -5, -10 -5, -10 -3, 0 -3))"), Color.BLUE);
		out.repaint();
	}

}
