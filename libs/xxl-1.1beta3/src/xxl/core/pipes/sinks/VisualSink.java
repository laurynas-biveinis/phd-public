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

package xxl.core.pipes.sinks;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import xxl.core.pipes.memoryManager.heartbeat.Heartbeat;
import xxl.core.pipes.operators.Pipes;
import xxl.core.pipes.sources.Enumerator;
import xxl.core.pipes.sources.Source;

/**
 * Sink component in a query graph that provides a simple graphical
 * user interface (GUI). With the help of this GUI a user is
 * able to start and stop the processing. <BR>
 * Moreover, the string representation of the elements streaming in can be 
 * displayed. If this sink stops its processing, the number of elements
 * that streamed in and the time spent for their processing are
 * displayed.
 * Note that VisualSink does not rely on temporal objects, but implements
 * the interface Heartbeat.
 * 
 * @since 1.1
 */
public class VisualSink<I> extends AbstractSink<I> implements Heartbeat {
	
	/**
	 * Counts the elements streaming in.
	 */
	protected long count;
	
	/**
	 * Counts the heartbeats streaming in.
	 */
	protected long countHB;
	
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
	 * Flag that signals if heartbeats are enabled. Unless this flag is enabled,
	 * printHB has no effect.
	 */
	protected volatile boolean activateHeartbeats;
	
	/**
	 * If <CODE>true</CODE> the elements' string representation is 
	 * separated by a CR/LF, otherwise not.
	 */
	protected boolean println;
	
	/**
	 * If <CODE>true</CODE> the elements' ID is printed, otherwise not.
	 */
	protected boolean printID;

	/**
	 * Point in time, where this sink started
	 * its processing. Return-value of the
	 * method <CODE>System.currentTimeMillis()</CODE>.
	 */
	protected long start = Long.MIN_VALUE;

	/**
	 * The frame.
	 */
	protected JFrame frame;
	
	/**
	 * A panel for the buttons.
	 */
	protected JPanel buttonPanel;
	
	/**
	 * The start button.
	 */
	protected JButton startButton;
		
	/**
	 * The stop button.
	 */
	protected JButton stopButton;
	
	/**
	 * The text area for the elements' string representation.
	 */
	protected JTextArea textArea;
	
	/**
	 * Main-panel.
	 */
	protected JPanel panel;

	protected boolean started = false;
	
	public VisualSink(String name, boolean printObjects, boolean println) {
		this.printObjects = printObjects;
		this.println = println;
		frame = new JFrame(name);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.setSize(650, 300);
		paint();
		frame.setVisible(true);
		this.printHB = false;
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
	 * 		separated by a CR/LF, otherwise not.
	 */ 
	public VisualSink(Source<? extends I> source, int ID, String name, boolean printObjects, boolean println, boolean activateAndPrintHB) {
		if (!Pipes.connect(source, this, ID))
			throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes."); 		
		this.printObjects = printObjects;
		this.println = println;
		this.printHB = this.activateHeartbeats = activateAndPrintHB;
		frame = new JFrame(name);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.setSize(650, 300);
		paint();
		frame.setVisible(true);
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
	 * 		separated by a CR/LF, otherwise not.
	 */ 
	public VisualSink(Source<? extends I> source, int ID, String name, boolean printObjects, boolean println) {
		this(source, ID, name, printObjects, println, false);
	}

	/** 
	 * Creates a new terminal sink in a query graph that provides
	 * a simple graphical user interface. <BR>
	 * The <CODE>DEFAULT_ID</CODE> is used for subscription.
	 * The elements' string representation is separated by a CR/LF.
	 * The input rate is not measured.
	 *
	 * @param source This sink gets subscribed to the specified source.
	 * @param name The string displayed in outer frame.
	 * @param printObjects If <CODE>true</CODE> the string representation of the
	 * 		elements is displayed in the GUI, otherwise not.
	 */ 
	public VisualSink(Source<? extends I> source, String name, boolean printObjects) {
		this(source, DEFAULT_ID, name, printObjects, true);
	}

	/** 
	 * Creates a new terminal sink in a query graph that provides
	 * a simple graphical user interface. <BR>
	 * The <CODE>DEFAULT_ID</CODE> is used for subscription.
	 * The elements' string representation is separated by a CR/LF.
	 * The input rate is not measured. <BR>
	 * The frame's name is set to "Graphical User Interface (Sink)".
	 *
	 * @param source This sink gets subscribed to the specified source.
	 * @param printObjects If <CODE>true</CODE> the string representation of the
	 * 		elements is displayed in the GUI, otherwise not.
	 */ 
	public VisualSink(Source<? extends I> source, boolean printObjects) {
		this(source, "Graphical User Interface (Sink)", printObjects);
	}

	/** 
	 * Creates a new terminal sink in a query graph that provides
	 * a simple graphical user interface. <BR>
	 * The <CODE>DEFAULT_ID</CODE> is used for subscription.
	 * The elements' string representation is separated by a CR/LF.
	 * The input rate is not measured. <BR>
	 * The frame's name is set to "Graphical User Interface (Sink)".
	 * No string representation of the incoming elements is displayed.
	 *
	 * @param source This sink gets subscribed to the specified source.
	 */ 
	public VisualSink(Source<? extends I> source) {
		this(source, true);
	}

	/**
	 * The counter is incremented. If the flag
	 * <CODE>printObjects</CODE> is set, the string representation
	 * of the element is appended to the text area.
	 *
	 * @param o The element streaming in.
	 * @param ID The ID this sink specified during its subscription by an underlying source. 
	 * @throws java.lang.IllegalArgumentException If the given element or ID is incorrect.
 	 */
	@Override
	public void processObject(I o, int sourceID) throws IllegalArgumentException {
		if (!started) start();
		count++;
		if (printObjects) {
			if (printID)
				textArea.append("sourceID: "+sourceID+" object: ");
			textArea.append(o.toString());
			if (println)
				textArea.append("\n");
			else
				textArea.append("\t");
		}
	}

	/**
	 * Sets the temporal starting point to <CODE>System.currentTimeMillis()</CODE>
	 * and calls <CODE>super.openAllSources()</CODE>. 
	 * 
	 * @throws xxl.core.pipes.sinks.SinkIsDoneException If the sink has already finished its processing.
	 * @see AbstractSink#start()
	 */
	public void start() throws SinkIsDoneException {
		start = System.currentTimeMillis();
		count = 0;
		super.openAllSources();
		started = true;
		startButton.setIcon(new ImageIcon(VisualSink.class.getResource("icons/duke.running.gif")));
	}

	/**
	 * Stops the processing of this sink by calling <CODE>super.stop()</CODE>.
	 * Writes the counter and the time passed from the starting point
	 * to the text area.
	 *
	 * @throws xxl.core.pipes.sinks.SinkIsDoneException If the sink has already finished its processing.
	 */
	public void stop() throws SinkIsDoneException {
		super.closeAllSources();
		startButton.setIcon(new ImageIcon(VisualSink.class.getResource("icons/dukeplug.gif")));
		startButton.setEnabled(false);
		stopButton.setEnabled(false);
		panel.repaint();
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.sinks.AbstractSink#done(int)
	 */
	@Override
	public void done(int sourceID) {
		super.done(sourceID);
		if (isDone) {
			textArea.append("\n");
			textArea.append("total no. of elements: "+count+"\n");
			if (activateHeartbeats && printHB) 
				textArea.append("total no. of heartbeats: "+countHB+"\n");
			textArea.append("runtime (ms): "+(start == 0 ? 0 : System.currentTimeMillis()-start)+"\n");
			startButton.setIcon(new ImageIcon(VisualSink.class.getResource("icons/dukeplug.gif")));
			startButton.setEnabled(false);
			panel.repaint();
		}
	}
	
	/**
	 * Paints the frame.
	 */
	public void paint() {
		panel = new JPanel();
		panel.setLayout(new BorderLayout());

		buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1,2));

		startButton = new JButton("START");
		startButton.setIcon(new ImageIcon(VisualSink.class.getResource("icons/dukeplug.gif")));
		startButton.addActionListener(
			new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					if (!started) {
						start();
						started = true;
						startButton.setIcon(new ImageIcon(VisualSink.class.getResource("icons/duke.running.gif")));
					}
				}
			}
		);

		stopButton = new JButton("STOP");
		stopButton.setIcon(new ImageIcon(VisualSink.class.getResource("icons/dukeWaveRed.gif")));
		stopButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					stop();
					panel.repaint();
				}
			}
		);
		buttonPanel.add(startButton);
		buttonPanel.add(stopButton);
		panel.add(buttonPanel, BorderLayout.NORTH);

		textArea = new JTextArea();
		textArea.setTabSize(4);		
		panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
		frame.getContentPane().add(panel);
	}
	
	/**
	 * Increments the heartbeat counter if activateHeartbeats is true.
	 * Displays a string representation of the heartbeat in the GUI
	 * if printHB is true.
	 */
	public void heartbeat(long timeStamp, int ID){
		if (activateHeartbeats){
			processingWLock.lock();
			try {
				countHB++;
				if (printHB) {				
					textArea.append("Heartbeat: "+timeStamp+" sourceID: "+ID);
					if (println)
						textArea.append("\n");
					else
						textArea.append("\t");
				}
			}
			finally {
				processingWLock.unlock();	
			}
		}
	}
	
	public JFrame getFrame() {
		return frame;
	}

	public void setPrintHB(boolean printHB) {
		this.printHB = printHB;
	}
	
	public void setPrintID(boolean printID) {
		this.printID = printID;
	}

	public void setPrintln(boolean println) {
		this.println = println;
	}

	public void setPrintObjects(boolean printObjects) {
		this.printObjects = printObjects;
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.memoryManager.heartbeat.Heartbeat#activateHeartbeats(boolean)
	 */
	public void activateHeartbeats(boolean on) {
		this.activateHeartbeats = on;
	}

	public static void main(String[] args){
		
		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/
		new VisualSink<Integer>(new Enumerator(100, 10), true);	
	}
}