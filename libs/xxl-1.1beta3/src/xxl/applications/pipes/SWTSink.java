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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import xxl.core.pipes.memoryManager.heartbeat.Heartbeat;
import xxl.core.pipes.processors.Processor;
import xxl.core.pipes.queryGraph.Graph;
import xxl.core.pipes.sinks.AbstractSink;
import xxl.core.pipes.sinks.Sink;
import xxl.core.pipes.sinks.SinkIsDoneException;
import xxl.core.pipes.sources.Enumerator;
import xxl.core.pipes.sources.Source;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataManagement;

/**
 * Sink component in a query graph that provides a simple graphical
 * user interface (GUI) using SWT. Therefore it needs swt.jar  and plattfrom 
 * dependent libraries during run-time. With the help of this GUI a user is
 * able to start, stop, pause and resume the processing of a sink. <BR>
 * Moreover the string representation of the elements streaming in can be 
 * displayed. If this sink stops its processing, the number of elements
 * that streamed in and the time spent for their processing are
 * displayed. 
 * @param <I> 
 */
public class SWTSink<I> extends Composite implements Sink<I>, Heartbeat {

	/**
	 * A factory method for swtsink.
	 * 
	 * @param source This sink gets subscribed to the specified source.
	 * @param ID This sink uses the given ID for subscription.
	 * @param name The string displayed in outer frame.
	 * @param printObjects If <CODE>true</CODE> the string representation of the
	 * 		elements is displayed in the GUI, otherwise not.
	 * @param println If <CODE>true</CODE> the elements' string representation is 
	 * 		separted by a CR/LF, otherwise not.
	 * @param refreshTime the refresh time for the gui.

	 */
	public static final <I> void FACTORY_METHOD(Source<? extends I> source, int sourceID, String name, boolean printObjects, boolean println, boolean printHB) {		
		Display display = Display.getDefault();			
		Shell shell = new Shell(display);
		new SWTSink<I>(source, sourceID, name, printObjects, println, shell, SWT.MIN, true, printHB);
		shell.setLayout(new org.eclipse.swt.layout.FillLayout());
		Rectangle shellBounds = shell.computeTrim(0,0,379,234);
		shell.setSize(shellBounds.width, shellBounds.height);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}
	
	/**
	 * A factory method for swtsink.
	 * 
	 * @param source This sink gets subscribed to the specified source.
	 */
	public static final <I> void FACTORY_METHOD(Source<? extends I> source) {
		FACTORY_METHOD(source, 0, "SWTSink", true, true, false);
	}
	
	public class InternalSink extends AbstractSink<I> implements Heartbeat {
		
		protected final StringBuffer buffer = new StringBuffer();
		protected int countHB = 0;
		
		public InternalSink(Source<? extends I> source, int sourceID) {
			super(source, sourceID);
		}
						
		protected Processor writer = new Processor(100) {
			@Override
			public void process() {
				Thread intern = null;
				if (buffer.length() > 0 && ! exit) {
					intern = new Thread("SWTSink_Intern") {
						protected String s;
						
						{
							synchronized (buffer) {
								s = buffer.toString();
								buffer.delete(0, buffer.length());
							}
						}
						
						@Override
						public void run() {
							if (! textOut.isDisposed()) {										
								if (textOut.getCharCount() > textMax)											
									textOut.setText(textOut.getText(textMax/2, textOut.getCharCount()));
								textOut.append(s);
							}											
												
						}
					};
				}
				if (intern != null)
					getDisplay().asyncExec(intern);
				if (!exit)
					writer.pauseUntilWakeUp();
			}				

			@Override
			protected void onTermination() {
				buffer.delete(0, buffer.length());
			}

		};
					
		@Override
		public void done(int sourceID) {				
			super.done(sourceID);
			if (! exit && ! isDisposed()) {					
				getDisplay().asyncExec(new Runnable() {
				    public void run() {
				        if (!isDisposed()) {
					    	startButton.setEnabled(false);
			    			stopButton.setEnabled(false);
			    			if (!exit) {
						    	buffer.append("\n");
								buffer.append("total no. of elements: "+count+"\n");
								if (SWTSink.this.printHB)
									buffer.append("total no. of heartbeats: "+countHB+"\n");
								buffer.append("runtime (ms): "+(start == 0 ? 0 : System.currentTimeMillis()-start)+"\n");
						    }
			    			writer.wakeUp();
						    writer.terminate();
				        }
				    }
				});				
			}
		}
		
		@Override
		public void processObject(final I o, int sourceID) throws IllegalArgumentException {
			if (!started) {
				start = System.currentTimeMillis();
				count = 0;
				started = true;
				startButton.setEnabled(false);
			}
			count++;				
			if (printObjects) {
			    if (o instanceof Object[]) {
		        	Object[]os = (Object[])o;
			        buffer.append("[");
			        for (int i = 0; i < os.length-1; i++) {
			        	buffer.append(os[i].toString()+", ");
			        }
			        buffer.append(os[os.length-1]+"]"+(println ? "\n" : "\t"));
			    }
				else
					buffer.append(o.toString()+ (println ? "\n" : "\t"));
			}
			writer.wakeUp();
		}
		
		public void heartbeat(long timeStamp, int sourceID) {
			countHB++;
			if (SWTSink.this.printHB) {
				buffer.append(new String("heartbeat: "+timeStamp+" sourceID: "+sourceID)+ (println ? "\n" : "\t"));
				writer.wakeUp();
			}
		}

		public void activateHeartbeats(boolean on) {
			
		}
		
		@Override
		public void openAllSources() {
			super.openAllSources();
			writer.start();
		}
		
		@Override
		public void closeAllSources() {
			super.closeAllSources();
			if (!exit) {
		    	buffer.append("\n");
		    	buffer.append("total no. of elements: "+count+"\n");
				if (SWTSink.this.printHB)
					buffer.append("total no. of heartbeats: "+countHB+"\n");
				buffer.append("runtime (ms): "+(start == 0 ? 0 : System.currentTimeMillis()-start)+"\n");
		    }
			writer.wakeUp();
		    writer.terminate();
		}
		
		public StringBuffer getBuffer() {
			return buffer;
		}			
	}
	
	/**
	 * The start button.
	 */
	protected Button startButton;
	
	/**
	 * The stop button.
	 */
	protected Button stopButton;
	
	/**
	 * A Button, that allows changing the font and the color of the output test.
	 */
	protected Button formatButton;
	
	/**
	 * The text area for the elements' string representation.
	 */
	protected Text textOut;
	
	/**
	 * A composite for the textarea.
	 */
	protected Composite textComposite;
	
	/**
	 * A composite for the buttons.
	 */
	protected Composite buttonComposite;
	
	/**
	 * Counts the elements streaming in.
	 */
	protected long count;
	
	/**
	 * Flag that signals if the string representation of the
	 * elements is displayed in the GUI.
	 */
	protected boolean printObjects;
	
	/**
	 * Flag that signals if the string representation of the
	 * heartbeats is displayed in the GUI.
	 */
	protected boolean printHB;
	
	/**
	 * If <CODE>true</CODE> the elements' string representation is 
	 * separted by a CR/LF, otherwise not.
	 */
	protected boolean println;
	
	/**
	 * Point in time, where this sink started
	 * its processing. Return-value of the
	 * method <CODE>System.currentTimeMillis()</CODE>.
	 */
	protected long start;
	
	/**
	 * Flag, signing that start has been invoked.
	 */
	protected boolean started = false;
		
	/**
	 * Flag, signing that the window has been disposed.  
	 */
	protected volatile boolean exit = false;
	
	/**
	 * This sink proceeds the incoming tuple.
	 */
	protected InternalSink sink;
	
	/**
	 * The color of the text output.  
	 */
	protected Color color;
	
	/**
	 * The font of the text output.  
	 */
	protected Font font;
	
	/**
	 * The int avoids an crash of the textarea. If the string is longer that its value,
	 * it will be halved.
	 */
	protected int textMax = 200000;
		
	/** 
	 * Creates a new terminal sink in a query graph that provides
	 * a simple graphical user interface.
	 *
	 * @param source This sink gets subscribed to the specified source.
	 * @param ID This sink uses the given ID for subscription.
	 * @param name The string displayed in outer frame.
	 * @param printObjects If <CODE>true</CODE> the string representation of the
	 * 		elements is displayed in the GUI, otherwise not.
	 * @param println If <CODE>true</CODE> the elements' string representation is 
	 * 		separted by a CR/LF, otherwise not.
	 * @param parent the parent composite of this swtsink.  
	 * @param style the style of this composite.
	 * @param internalCall a flag, signing that the constructor is invoked by a factory method.
	 * @param refreshTime the refresh time for the gui.
	 */	
	protected SWTSink(final Source<? extends I> source, int sourceID, String name, final boolean printObjects, final boolean println, Composite parent, int style, boolean internalCall, boolean printHB) {	
		super(parent, style);		
		
		if (internalCall) {
			getShell().setText(name);
			getShell().setImage(new Image(Display.getDefault(), getClass().getResourceAsStream("/xxl/applications/pipes/sigmoddemo/images/xxl.gif")));
		}
		buttonComposite = new Composite(this, SWT.NULL);
		startButton = new Button(buttonComposite,SWT.PUSH| SWT.CENTER);
		stopButton = new Button(buttonComposite,SWT.PUSH| SWT.CENTER);
		formatButton = new Button(buttonComposite,SWT.PUSH| SWT.CENTER);
		textOut = new Text(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		
		this.setSize(new Point(379,234));
		this.setEnabled(true);
		final Color NewSWTAppbackground = new Color(Display.getDefault(),128,128,128);
		this.setBackground(NewSWTAppbackground);
		this.printObjects = printObjects;
		this.println = println;
		this.printHB = printHB;

		FormData buttonCompositeLData = new FormData();
		buttonCompositeLData.left = new FormAttachment(0);
		buttonCompositeLData.top = new FormAttachment(0);
		buttonCompositeLData.right = new FormAttachment(100);			
		buttonComposite.setLayoutData(buttonCompositeLData);			
		buttonComposite.setEnabled(true);
		
		startButton.setText("Start");
		startButton.setToolTipText("starts the processing of this sink");
		stopButton.setText("Stop");
		stopButton.setToolTipText("stops the processing of this sink");
		formatButton.setText("Font...");
		formatButton.setToolTipText("use this button to change the font and the color of the text");
		formatButton.setFont(new Font(Display.getDefault(), "Courier", 10, SWT.BOLD));

		FillLayout buttonCompositeLayout = new FillLayout(256);
		buttonComposite.setLayout(buttonCompositeLayout);
		buttonCompositeLayout.type = SWT.HORIZONTAL;
		buttonCompositeLayout.marginWidth = 0;
		buttonCompositeLayout.marginHeight = 0;
		buttonCompositeLayout.spacing = 0;
		buttonComposite.layout();
		
		FormData textOutLData = new FormData();
		textOutLData.left = new FormAttachment(0);
		textOutLData.top = new FormAttachment(buttonComposite);
		textOutLData.right = new FormAttachment(100);
		textOutLData.bottom = new FormAttachment(100);
		textOut.setLayoutData(textOutLData);

		FormLayout thisLayout = new FormLayout();
		this.setLayout(thisLayout);
		thisLayout.marginWidth = 0;
		thisLayout.marginHeight = 0;
		thisLayout.spacing = 0;
		this.layout();
		
		color = textOut.getForeground();
		font = textOut.getFont();
		
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				NewSWTAppbackground.dispose();
				exit = true;	
				sink.closeAllSources();
			}
		});
		
		startButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!started) {
					start = System.currentTimeMillis();
					count = 0;
					sink.openAllSources();
					started = true;
					startButton.setEnabled(false);
				}
			}			
		});

		stopButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				startButton.setEnabled(false);
				stopButton.setEnabled(false);				
				sink.closeAllSources();
			}			
		});
		
		formatButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				FontDialog dialog = new FontDialog(new Shell(getShell()));
				dialog.setRGB(color.getRGB());
				dialog.setFontList(font.getFontData());
				dialog.open();
				if (dialog.getFontList() != null) {
					font = new Font(Display.getCurrent(), dialog.getFontList());
					textOut.setFont(font);
				}
				if (dialog.getRGB() != null) {
					color = new Color(Display.getCurrent(), dialog.getRGB().red, dialog.getRGB().green, dialog.getRGB().blue);
					textOut.setForeground(color);
				}
			}			
		});
		this.sink = new InternalSink(source, sourceID);
	}
	
	/** 
	 * Creates a new terminal sink in a query graph that provides
	 * a simple graphical user interface.
	 *
	 * @param source This sink gets subscribed to the specified source.
	 * @param ID This sink uses the given ID for subscription.
	 * @param name The string displayed in outer frame.
	 * @param printObjects If <CODE>true</CODE> the string representation of the
	 * 		elements is displayed in the GUI, otherwise not.
	 * @param println If <CODE>true</CODE> the elements' string representation is 
	 * 		separted by a CR/LF, otherwise not.
	 * @param parent the parent composite of this swtsink.  
	 * @param style the style of this composite.
	 * @param refreshTime the refresh time for the gui.
	 */	
	public SWTSink(final Source<? extends I> source, int sourceID, String name, final boolean printObjects, final boolean println, Composite parent, int style, int refreshTime) {
		this(source, sourceID, name, printObjects, println, parent, style, false, false);
	}
	
	/** 
	 * Creates a new terminal sink in a query graph that provides
	 * a simple graphical user interface. The refresh time is set to 100 ms.
	 *
	 * @param source This sink gets subscribed to the specified source.
	 * @param ID This sink uses the given ID for subscription.
	 * @param name The string displayed in outer frame.
	 * @param printObjects If <CODE>true</CODE> the string representation of the
	 * 		elements is displayed in the GUI, otherwise not.
	 * @param println If <CODE>true</CODE> the elements' string representation is 
	 * 		separted by a CR/LF, otherwise not.
	 * @param parent the parent composite of this swtsink.  
	 * @param style the style of this composite. 
	 */	
	public SWTSink(final Source<? extends I> source, int sourceID, String name, final boolean printObjects, final boolean println, Composite parent, int style) {
		this(source, sourceID, name, printObjects, println, parent, style, false, false);
	}

	/** 
	 * Creates a new terminal sink in a query graph that provides
	 * a simple graphical user interface. <BR>
	 * The <CODE>DEFAULT_ID</CODE> is used for subscription.
	 * The elements' string representation is separted by a CR/LF.
	 * The input rate is not measured. The refresh time is set to 100 ms.
	 *
	 * @param source This sink gets subscribed to the specified source.
	 * @param name The string displayed in outer frame.
	 * @param printObjects If <CODE>true</CODE> the string representation of the
	 * 		elements is displayed in the GUI, otherwise not.
	 * @param parent the parent composite of this swtsink.  
	 * @param style the style of this composite.
	 */ 
	public SWTSink(Source<? extends I> source, String name, boolean printObjects, Composite parent, int style) {
		this(source, DEFAULT_ID, name, printObjects, true, parent, style);
	}

	/** 
	 * Creates a new terminal sink in a query graph that provides
	 * a simple graphical user interface. <BR>
	 * The <CODE>DEFAULT_ID</CODE> is used for subscription.
	 * The elements' string representation is separted by a CR/LF.
	 * The input rate is not measured. <BR>
	 * The frame's name is set to "Graphical User Interface (Sink)".
	 *
	 * @param source This sink gets subscribed to the specified source.
	 * @param printObjects If <CODE>true</CODE> the string representation of the
	 * 		elements is displayed in the GUI, otherwise not.
	 * @param parent the parent composite of this swtsink.  
	 * @param style the style of this composite.
	 */ 
	public SWTSink(Source<? extends I> source, boolean printObjects, Composite parent, int style) {
		this(source, "Graphical User Interface (Sink)", printObjects, parent, style);
	}

	/** 
	 * Creates a new terminal sink in a query graph that provides
	 * a simple graphical user interface. <BR>
	 * The <CODE>DEFAULT_ID</CODE> is used for subscription.
	 * The elements' string representation is separted by a CR/LF.
	 * The input rate is not measured. <BR>
	 * The frame's name is set to "Graphical User Interface (Sink)".
	 * No string representation of the incoming elements is display.
	 *
	 * @param source This sink gets subscribed to the specified source.
	 * @param parent the parent composite of this swtsink. 
	 * @param style the style of this composite.
	 */ 
	public SWTSink(Source<? extends I> source, Composite parent, int style) {
		this(source, false, parent, style);		
	}
	
		/** 
	 * Creates a new terminal sink in a query graph that provides
	 * a simple graphical user interface. <BR>
	 * The <CODE>DEFAULT_ID</CODE> is used for subscription.
	 * The elements' string representation is separted by a CR/LF.
	 * The input rate is not measured. <BR>
	 * The frame's name is set to "Graphical User Interface (Sink)".
	 * No string representation of the incoming elements is display.
	 *
	 * @param source This sink gets subscribed to the specified source.
	 */
	public SWTSink(Source<? extends I> source) {
		this(source, new Shell(Display.getDefault()), SWT.NULL);
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.sinks.Sink#process(java.lang.Object, int)
	 */
	public void process(I o, int sourceID) throws IllegalArgumentException {
		sink.process(o, sourceID);
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.sinks.Sink#done(int)
	 */
	public void done(int sourceID) {		
		sink.done(sourceID);		
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.sinks.Sink#addSource(xxl.core.pipes.sources.Source, int)
	 */
	public boolean addSource(Source<? extends I> source, int sourceID) throws SinkIsDoneException {		
		return sink.addSource(source, sourceID);
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.sinks.Sink#removeSource(xxl.core.pipes.sources.Source, int)
	 */
	public boolean removeSource(Source<? extends I> source, int sourceID) throws SinkIsDoneException {
		return sink.removeSource(source, sourceID);
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.sinks.Sink#getSource(int)
	 */
	public Source<? extends I> getSource(int index) throws SinkIsDoneException, IllegalArgumentException, IndexOutOfBoundsException {
		return sink.getSource(index);
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.sinks.Sink#getSourceID(xxl.core.pipes.sources.Source)
	 */
	public int getSourceID(Source<? extends I> source) throws SinkIsDoneException, IllegalArgumentException {	
		return sink.getSourceID(source);
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.sinks.Sink#getNoOfSources()
	 */
	public int getNoOfSources() throws SinkIsDoneException {
		return sink.getNoOfSources();
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.sinks.Sink#isDone()
	 */
	public boolean isDone() {		
		return sink.isDone();
	}

	/* (non-Javadoc)
	 * @see xxl.core.util.MetaDataProvider#getMetaData()
	 */
	public CompositeMetaData<Object, Object> getMetaData() {		
		return sink.getMetaData();
	}
		
	public boolean receivedDone(int sourceID) {
		return sink.receivedDone(sourceID);
	}
	
	public void openAllSources() {
		sink.openAllSources();
	}
	
	public void closeAllSources() {
		sink.closeAllSources();
	}
	
	public void createMetaDataManagement() {
		sink.createMetaDataManagement();
	}
	
	public MetaDataManagement<Object,Object> getMetaDataManagement() {
		return sink.getMetaDataManagement();
	}
	
	public void activateHeartbeats(boolean on) {
		
	}
	
	public void heartbeat(long timeStamp, int sourceID) {
		sink.heartbeat(timeStamp, sourceID);
	}
	
	public void setGraph(Graph graph) {
		sink.setGraph(graph);
	}

	public Graph getGraph() {
		return sink.getGraph();
	}
	
	/**
	 * Executes an example.
	 */
	public static void main(String[] args){
		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/
		Enumerator e = new Enumerator(1000, 5);
		SWTSink.FACTORY_METHOD(e);
	}
}
