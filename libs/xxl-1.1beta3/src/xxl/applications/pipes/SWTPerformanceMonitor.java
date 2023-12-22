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

package xxl.applications.pipes;

import static xxl.core.pipes.operators.AbstractPipe.AbstractPipeMetaDataManagement.AVG_INPUT_RATE;
import static xxl.core.pipes.operators.AbstractPipe.AbstractPipeMetaDataManagement.VAR_INPUT_RATE;

import java.text.DecimalFormat;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

import xxl.core.pipes.metaData.PeriodicEvaluationMetaDataHandler;
import xxl.core.pipes.operators.filters.Filter;
import xxl.core.pipes.processors.Processor;
import xxl.core.pipes.queryGraph.VisualQueryExecutor;
import xxl.core.pipes.sinks.SinkIsDoneException;
import xxl.core.pipes.sinks.Tester;
import xxl.core.pipes.sources.Enumerator;
import xxl.core.pipes.sources.SourceIsClosedException;
import xxl.core.predicates.Predicate;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataHandler;

/**
 * This class provides a metadata monitoring tool. It uses the SWT and needs swt.jar
 * and plattfrom dependent libraries during run-time.
 */
public class SWTPerformanceMonitor extends Composite {
	
	/**
	 * The refresh rate for the metadata.
	 */
	public static final long period = 500;
	
	/**
	 * A factory method to create a metadata monitor for recent data.
	 * @param md the CompositeMetaData object that provides the metadata.
	 * @param historicMode 
	 * @param showMDOnStart 
	 */
	public final static void FACTORY_METHOD(CompositeMetaData md, boolean historicMode, boolean showMDOnStart) {
		FACTORY_METHOD(md, "Performance Monitor", historicMode, showMDOnStart);
	}
	
	/**
	 * @param md
	 */
	public final static void FACTORY_METHOD_SHOW_MD_ON_START(CompositeMetaData md) {
		FACTORY_METHOD(md, false, true);
	}
	
	/**
	 * A factory method to create a metadata monitor for all data.
	 * @param md the CompositeMetaData object that provides the metadata.
	 */
	public final static void HISTORIC_FACTORY_METHOD_SHOW_MD_ON_START(CompositeMetaData md) {
		FACTORY_METHOD(md, "Performance Monitor", true, true);
	}


	/**
	 * A factory method to create a metadata monitor for recent data.
	 * @param md the CompositeMetaData object that provides the metadata.
	 */
	public final static void FACTORY_METHOD(CompositeMetaData md) {
		FACTORY_METHOD(md, "Performance Monitor", false);
	}

	/**
	 * A factory method to create a metadata monitor for all data.
	 * @param md the CompositeMetaData object that provides the metadata.
	 */
	public final static void HISTORIC_FACTORY_METHOD(CompositeMetaData md) {
		FACTORY_METHOD(md, "Performance Monitor", true);
	}
	
	public final static void FACTORY_METHOD(CompositeMetaData md, String name, boolean historicMode) {
		FACTORY_METHOD(md, name, historicMode, false);
	}
	
	/**
	 * A factory method to create a metadata monitor.
	 * @param md the CompositeMetaData object that provides the metadata.
	 * @param name displays the given name in the shell.
	 */
	public final static void FACTORY_METHOD(CompositeMetaData md, String name, boolean historicMode, boolean showMDOnStart) {
		Display display = Display.getDefault();			
		Shell shell = new Shell(display, SWT.MIN |SWT.RESIZE);			
		new SWTPerformanceMonitor(md, name, shell, SWT.NONE, true, historicMode, showMDOnStart);
	    shell.setLayout(new FillLayout());
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}
		
	protected MetaDataHandler[] mdHandlers;
	
	/**
	 * A canvas to visualize the values of the metadata.
	 */
	protected Canvas[] canvas;
	
	/**
	 * The MetaDataAccessor refreshs the values periodically. 
	 */
	protected MetaDataAccessor mda;
	
	/**
	 * contains the buttons and the windows.
	 */
	protected Composite c;
	
	/**
	 * The CompositeMetaData delivers the required metadata.
	 */
	protected CompositeMetaData<Object,Object> cmd;
	
	/**
	 * Allows to scroll the composite c.
	 */
	protected ScrolledComposite sc;
	
	
	/**
	 * Should the monitor show just the recent data or data from its whole runtime?
	 */
	boolean historicMode = false;

	/** 
	 * An inner class that refreshes the metadata periodically.
	 */
	public class MetaDataAccessor extends Processor {
				
		protected Object[] values;
				
		public MetaDataAccessor(SWTPerformanceMonitor m, long period) {
			super(period);			
			this.values = new Object[m.mdHandlers.length];			
		}

		@Override
		public void process() {
			try {
		     	if (SWTPerformanceMonitor.this.mdHandlers != null){ 					            		
					for (int i = 0; i < SWTPerformanceMonitor.this.mdHandlers.length; i++)	{
						values[i] = SWTPerformanceMonitor.this.mdHandlers[i].getMetaData();
					}
					SWTPerformanceMonitor.this.paint(values);				    
		    	}
		    } catch (SourceIsClosedException se) {
		        terminate();
		        Display.getDefault().asyncExec(
		            new Runnable() {
			            public void run() {
			            	SWTPerformanceMonitor.this.dispose();
			            }
		            }
		        );
		    } catch (SinkIsDoneException se) {
		        terminate();
		        Display.getDefault().asyncExec(
		            new Runnable() {
			            public void run() {
			            	SWTPerformanceMonitor.this.dispose();
			            }
		            }
		        );
		    }
		}	
	}
	
	protected SWTPerformanceMonitor (CompositeMetaData<Object,Object> md, String name, Composite parent, int style, boolean internalCall, boolean historicMode) {
		this(md,name,parent,style,internalCall, false, historicMode);		
	}
	
	/**
	 * Creates a new SWTPerformanceMonitor with the specified parameters. It is only invoked
	 * by the Factory Methods and by the pipes demo.
	 * @param md the metadata provider.
	 * @param name the name of the composite.
	 * @param parent allows to place the monitor in arbitary composite.
	 * @param style an swt style.
	 * @param internalCall a flag to determine, if the shell name should be changed.
	 * @param showMDOnStart shows all metadata if set to true 
	 */
	protected SWTPerformanceMonitor (CompositeMetaData<Object,Object> md, String name, Composite parent, int style, boolean internalCall, boolean historicMode,final boolean showMDOnStart) {	
		super(parent, style);
		this.historicMode=historicMode;
		cmd = md;		
		if (internalCall) {
			getShell().setText(name);
			getShell().setImage(new Image(Display.getDefault(), getClass().getResourceAsStream("/xxl/applications/pipes/sigmoddemo/images/xxl.gif")));
		}		
		FormLayout thisLayout = new FormLayout();		
		this.setLayout(thisLayout);
		
		final Group group = new Group(this, SWT.NONE);				
     	group.setText("Available Meta Data");
	    GridLayout groupLayout = new GridLayout();
	    groupLayout.numColumns = 4;
	    if (!internalCall)
	    	groupLayout.makeColumnsEqualWidth = true;
	    group.setLayout(groupLayout);
	    
	    FormData dataGroup = new FormData();
	    dataGroup.top = new FormAttachment(0);
	    dataGroup.left = new FormAttachment(0);
	    dataGroup.right = new FormAttachment(100);	    
		group.setLayoutData(dataGroup);		

		sc = new ScrolledComposite(this, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NONE);
	  	c = new Composite(sc, SWT.NONE);
	  	sc.setContent(c);	  	
	  	
	  	GridLayout compLayout = new GridLayout();
	  	compLayout.numColumns = (getShell().getBounds().width/(340));
	  	// n*300 / n*320 for groups / (n+2) due to n columns and 2 for both idents
	  	// tested for resolutions from 800*600 - 1920*1440
	  	int balance = 10;	  	
	  	balance = internalCall ?
	  			(getShell().getBounds().width-(compLayout.numColumns * 300))/((compLayout.numColumns+2)) :
	  			(getShell().getBounds().width-(compLayout.numColumns * 320))/((compLayout.numColumns+2)) ;
	  	compLayout.marginWidth = balance;
		compLayout.verticalSpacing = 10;				
		compLayout.horizontalSpacing =  balance;
	  	c.setLayout(compLayout);
	  	sc.setFocus();
	  	
	  	FormData dataSC = new FormData();
	  	dataSC.top = new FormAttachment(group);
	  	dataSC.left = new FormAttachment(0);
	  	dataSC.right = new FormAttachment(100);
	  	dataSC.bottom = new FormAttachment(100);	  	
	  	sc.setLayoutData(dataSC);
		Iterator<Object> ids = md.identifiers();
		
		this.mdHandlers = new MetaDataHandler[md.size()];
		this.canvas = new Canvas[md.size()];
		for(int i = 0; ids.hasNext(); i++) {
			final String next = (String)ids.next();
			mdHandlers[i] = (MetaDataHandler<? extends Number>)md.get(next);
			Button b = new Button(group, SWT.TOGGLE | SWT.CENTER);
			b.setText(next.toString());
			GridData data = new GridData();
			// data.widthHint = 150;
			data.heightHint = 30;
			data.horizontalAlignment = GridData.CENTER;
			data.grabExcessHorizontalSpace = true;
			b.setLayoutData(data);

			final int j = i;
			SelectionListener listener = new SelectionListener() {
				public boolean activated = false;
				
				public void widgetSelected(SelectionEvent e) {
					if (!activated)
						canvas[j] = addCanvas(next.toString());
					else {
						canvas[j].getParent().dispose();
						c.setSize(c.computeSize(SWT.DEFAULT, SWT.DEFAULT));
						c.layout();
					}
					activated = !activated;
				}

				public void widgetDefaultSelected(SelectionEvent arg0) {
				}
			};
			
			b.addSelectionListener(listener);
			
			if (showMDOnStart) {
				b.notifyListeners(SWT.Selection, null);
				b.setSelection(true);
				b.redraw();
			}
			
			group.setSize(group.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		    group.layout();		    
		}
		this.layout();		
		this.addDisposeListener(new DisposeListener(){
			public void widgetDisposed(DisposeEvent arg0) {
				stopMetaDataAccessor();				
			}
		});
		startMetaDataAccessor();
	}
	
	/**
	 * Creates a new SWTPerformanceMonitor with the specified parameters. 
	 * @param md the metadata provider.
	 * @param parent allows to place the monitor in arbitary composite.
	 * @param style an swt style.
	 */
	public SWTPerformanceMonitor (CompositeMetaData md, Composite parent, int style) {
	    this(md, "Performance Monitor", parent, style, false, false);
	}
	
	/**
	 * Start the metadata accessor that reads metadata periodically and paints them. 
	 */
	public void startMetaDataAccessor() {
		this.mda = new MetaDataAccessor(this, period);
		mda.start();
	}
	
	/**
	 * Terminates the metadata accessor. 	 
	 */
	public void stopMetaDataAccessor() {
		// terminate threads that periodically update metadata
		for (MetaDataHandler mdh : mdHandlers) {
			if (mdh instanceof PeriodicEvaluationMetaDataHandler)
				((PeriodicEvaluationMetaDataHandler)mdh).close();
		}
		// terminate MetaDataAccessor
		this.mda.terminate();
	}
	
	/**
	 * Paints the metadata. Each meata data is visualized in a separate window.
	 * @param newValues the new values of the meta data.
	 */
	public void paint(final Object newValues) {		
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {				
				for (int i = 0; i < canvas.length; i++) {
					if (canvas[i] != null && !canvas[i].isDisposed()) {
						Event event = new Event();
						event = new Event();
						event.display = Display.getDefault();
						event.gc = new GC(canvas[i]);
						event.data = ((Object[])newValues)[i];
						canvas[i].notifyListeners(SWT.Paint, event);						
						event.gc.dispose();						
					}
				}				
			}
		});
	}
	
	/**
	 * Adds a canvas in the composite c. This happens, when a metadata button 
	 * is activated.
	 * @param name the name of the canvas.
	 * @return the canvas.
	 */
	public Canvas addCanvas(String name) {
		Group group = new Group(c, SWT.NONE);
		group.setText(name);
						
		Canvas canvas = new Canvas(group, SWT.BORDER);
		final int height = 200;
		final int width = 300;
		canvas.setSize(width, height);
		canvas.setLocation(15, 20);
		if (historicMode) 
			canvas.addPaintListener(
				new PaintListener() {
					
					protected double [] values = new double[128]; 
					protected int [] pixels = new int[128];
					protected double max = 0;
					protected int i = 0;	
					protected int forget = 1;
					protected int counter=0;
					protected GC gc;
					protected Display d;
					protected final double maxFactor = 1.10d;
					protected final DecimalFormat df = new DecimalFormat("0.000E0");
					protected double v = 0;
					
					public void paintControl(PaintEvent e) {						
						if (e.data != null) {							
							counter++;
							if (counter<forget) return;
							counter=0;
							
							gc = e.gc;
							d = e.widget.getDisplay();
							gc.setForeground(d.getSystemColor(SWT.COLOR_GREEN));
							gc.setBackground(d.getSystemColor(SWT.COLOR_BLACK));
							gc.fillRectangle(0, 0, width, height);							
							v = ((Number)e.data).doubleValue();
							if (Double.isNaN(v)) v = -1;													
						    double oldMax = max;
							max = Math.max(v, max);
							values[i] = v;
							pixels[i]= (int)((v*height)/(max*maxFactor));
							i++;
							if (i==128) {
								for (int k=1; k<64; k++) {
									values[k]=values[2*k];
									pixels[k]=pixels[2*k];
								}	       
								i=64;
								forget*=2;
							}
							if (max - oldMax > oldMax*maxFactor/height) { // resize values but only if increase in maxium is visible (at least 1 px)
								for (int j = 0; j < i; j++)  
									pixels[j]= (int)((values[j]*height)/(max*maxFactor));
							}								
														
							// visualization: right to left
							if (i>1) for (int j = 0; j<i-1; j++) {
								int x1 = 250*j/(i-1); 
								int x2 = 250*(j+1)/(i-1);
								gc.drawLine(50+x1, height-pixels[j], 50+x2, height-pixels[j+1]);
							}
							
							gc.setForeground(d.getSystemColor(SWT.COLOR_YELLOW));
							gc.drawLine(50, 0, 50, 200);
							gc.drawString(df.format(max*maxFactor), 3, 2);
							gc.drawString("0", 40, 182);
							gc.drawString("current", 7, 75);
							if (e.data != null && Double.isNaN(((Number)e.data).doubleValue()))  // workaround for GUI if value is undefined
								gc.drawString("NaN", 3, 90);
							else
								gc.drawString(df.format(v), 3, 90);
						}
					}
				}
			);		
		else 
			canvas.addPaintListener(
				new PaintListener() {
					
					protected int incx = 5;
					protected int[] values = new int[50]; 
					protected double max = 0;
					protected int i = 0;	
					protected GC gc;
					protected Display d;
					protected final double maxFactor = 1.10d;
					protected final DecimalFormat df = new DecimalFormat("0.000E0");
					protected double v = 0;
					protected boolean first = true;
					
					public void paintControl(PaintEvent e) {
						gc = e.gc;
						d = e.widget.getDisplay();
						gc.setForeground(d.getSystemColor(SWT.COLOR_GREEN));
						gc.setBackground(d.getSystemColor(SWT.COLOR_BLACK));
						gc.fillRectangle(0, 0, width, height);
						
						if (e.data != null) {
		 					if (i == values.length) {
		 						i = 0; 
		 						first = false;
		 					} 
		
							v = ((Number)e.data).doubleValue();
							if (Double.isNaN(v)) // workaround for GUI if value is undefined
								values[i++] = -1;
							else {
							    double oldMax = max;
								max = Math.max(v, max);
								if (max - oldMax > oldMax*maxFactor/height) { // resize values but only if increase in maxium is visible (at least 1 px)
									for (int j = 0; j < values.length; j++)  
									    values[j] = (int)((values[j]*oldMax)/max);
								}
								values[i++] = (int)((v*height)/(max*maxFactor));
							}
														
							// visualization: right to left
							int x = first ? 50+250-(i*incx) : 50; 
							if (!first) {
								for (int j = i; j < values.length - 1; j++) 
								    gc.drawLine(x, height-values[j], x += incx, height-values[j+1]);	
								gc.drawLine(x, height-values[values.length-1], x += incx, height-values[0]);	
							}
							
							for (int j = 0; j < i - 1; j++) {
								gc.drawLine(x, height-values[j], x += incx, height-values[j+1]);	
							}
							
							gc.setForeground(d.getSystemColor(SWT.COLOR_YELLOW));
							gc.drawLine(50, 0, 50, 200);
							gc.drawString(df.format(max*maxFactor), 3, 2);
							gc.drawString("0", 40, 182);
							gc.drawString("current", 7, 75);
							if (e.data != null && Double.isNaN(((Number)e.data).doubleValue()))  // workaround for GUI if value is undefined
								gc.drawString("NaN", 3, 90);
							else
								gc.drawString(df.format(v), 3, 90);
						}
					}
				}
			);
		GridData data = new GridData();		
		data.widthHint= 320;
		data.heightHint = 220;
		data.horizontalAlignment = GridData.CENTER;
		data.grabExcessHorizontalSpace = true;		
		group.setLayoutData(data);
		
		group.setSize(SWT.DEFAULT, SWT.DEFAULT);
		group.layout();
		
		c.setSize(c.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	    c.layout();	
		return canvas;
	}
	
	/**
	 * Provides an example. 
	 * @param args is ignored.
	 */
	public static void main(String[] args) {		
		Enumerator e = new Enumerator(10000, 10);	

		final Filter<Integer> f = new Filter<Integer>(e, new Predicate<Integer>() {
			@Override
			public boolean invoke(Integer i) {
				return i%2 == 0;
			}
		});
		f.getMetaDataManagement().include(
			 AVG_INPUT_RATE, VAR_INPUT_RATE
		);
				
		Tester<Integer> tester = new Tester<Integer>(f);
		VisualQueryExecutor exec = new VisualQueryExecutor();
		exec.registerQuery(tester);
		//FACTORY_METHOD(f.getMetaData());
		HISTORIC_FACTORY_METHOD(f.getMetaData());
	}	
}
