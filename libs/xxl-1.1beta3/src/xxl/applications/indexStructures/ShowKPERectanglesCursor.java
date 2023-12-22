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

package xxl.applications.indexStructures;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Iterator;

import xxl.core.cursors.SecureDecoratorCursor;
import xxl.core.spatial.KPE;
import xxl.core.spatial.points.Point;
import xxl.core.spatial.rectangles.Rectangle;

/**
 * This cursor draws the rectangles that are includes in the KPEs of the
 * used iterator.
 */
public class ShowKPERectanglesCursor extends SecureDecoratorCursor {
	
	/**
	 * The frame showing the Ractangles.
	 */
	protected Frame f = null;

	/**
	 * The <tt>Graphics</tt> Object of {link #f}.
	 */
	Graphics gr_frame = null;

	/**
	 * An image used for drawing.
	 */
	protected BufferedImage image;
	
	/**
	 * The <tt>Graphics</tt> Object of {link #image}.
	 */
	Graphics gr_im;
	
	/**
	 * The width of {@link #f}.
	 */
	int sizeX;
	
	/**
	 * The height of {@link #f}.
	 */
	int sizeY;
	
	/**
	 * The delay between drawing the rectangles.
	 */
	int delay;
	
	/**
	 * A counter for the rectangles.
	 */
	int rCount;
	
	/**
	 * The universe all rectangle lie within. 
	 */
	Rectangle universe;
	
	/**
	 * Translation in x direction.
	 */
	double xt = 0.0;
	
	/**
	 * Translation in y direction.
	 */
	double yt = 0.0;
	
	/**
	 * Dilation in x direction.
	 */
	double wt = 1.0;
	
	/**
	 * Dilation in y direction.
	 */
	double ht = 1.0;
	
	/**
	 * Creates a new <tt>ShowKPERectanglesCursor</tt>.
	 * 
	 * @param it an iterator providing the rectangles to display
	 * @param universe a rectangle containing all rectangles in <tt>it</tt>
	 * @param delay the delay between drawing the rectangles
	 */
	public ShowKPERectanglesCursor(Iterator it, Rectangle universe, int delay) {
		super(it);
		this.universe = universe;
		this.delay = delay;
		rCount=0;
	}
	
	/** 
	 * Creates the frame for displaying.
	 * 
	 * @param sizeX width of the frame
	 * @param sizeY height of the frame
	 * @return the creates frame
	 */
	public Frame createFrame(int sizeX, int sizeY) {
		f = new Frame() {
			public void paint(Graphics gr) {
				super.paint(gr);
				if (gr_frame!=null)
					gr_frame.drawImage(image,0,0,f);
			}
		};
		f.setSize(sizeX,sizeY);
		this.sizeX = f.getWidth();
		this.sizeY = f.getHeight();
		f.setVisible(true);
		f.addWindowListener(
			new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					f.dispose();
				    //System.exit(0);
				}
			}
		);
		initImage();
		gr_frame = f.getGraphics();		
		return f;
	}

	/**
	 * Initializes {@link #image}.
	 *
	 */
	public void initImage() {
		image = new BufferedImage(sizeX,sizeY,BufferedImage.TYPE_BYTE_BINARY);
		gr_im = image.getGraphics();
		gr_im.setColor(Color.white);
		gr_im.fillRect(0,0,sizeX,sizeY);
		gr_im.setColor(Color.black);
	}
	
	/** Returns the next element in the iteration.
	 *
   * @return the next element in the iteration.
	 */
	public Object next() {
		KPE k = (KPE) super.next();
		Point left = ((Rectangle)(k.getData())).getCorner(false);
		double deltas[] = ((Rectangle)(k.getData())).deltas();
		
		double x = (left.getValue(0)-universe.getCorner(false).getValue(0))/universe.deltas()[0];
		double y = (left.getValue(1)-universe.getCorner(false).getValue(1))/universe.deltas()[1];
		double w = deltas[0]/universe.deltas()[0];
		double h = deltas[1]/universe.deltas()[1];
		
		x = (x-xt) / wt;
		y = (y-yt) / ht;
		w = w/wt;
		h = h/ht;
		
		if ((x>=0) && (x<1.0) && (y>=0) && (y<1.0)) {
			gr_im.drawRect((int) (x*sizeX),sizeX-1-(int) (y*sizeY),(int) (w*sizeX),(int) (h*sizeY));
			if ((rCount++)>100*(delay+1)) {
				gr_frame.drawImage(image,0,0,f);
				rCount=0;
			}
		}			
		try { 
			Thread.sleep(delay); 
		} 
		catch (InterruptedException e) {};	
		return k;
	}

	/** 
	 * Paints the image to the frame.
	 * 
	 * @param gr a Graphics object for painting to the frame 
	 */
	public void paint(Graphics gr) {
		gr.drawImage(image,0,0,f);
	}

	/**
	 * Return the frame.
	 * 
	 * @return {@link #f}
	 */
	public Frame getFrame() {
		return f;
	}
}
