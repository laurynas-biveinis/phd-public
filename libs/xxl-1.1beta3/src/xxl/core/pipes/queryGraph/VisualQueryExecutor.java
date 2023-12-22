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

package xxl.core.pipes.queryGraph;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import xxl.core.pipes.memoryManager.heartbeat.HBQueryExecutor;
import xxl.core.pipes.memoryManager.heartbeat.HeartbeatGenerator;
import xxl.core.pipes.sinks.AbstractSink;
import xxl.core.pipes.sinks.Printer;
import xxl.core.pipes.sinks.VisualSink;
import xxl.core.pipes.sources.Enumerator;

public class VisualQueryExecutor extends HBQueryExecutor implements WindowListener {

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
	
	public VisualQueryExecutor(long period, HeartbeatGenerator hbGenerator) {
		super(period, hbGenerator);
		init();
	}

	public VisualQueryExecutor(long period) {
		this(period, null);
	}

	public VisualQueryExecutor() {
		this(500);
	}

	protected void init() {
		frame = new JFrame("Query Executor");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.addWindowListener(this);
		frame.getContentPane().setLayout(new BorderLayout());
		paint();
		frame.setResizable(false);
		frame.pack();
		frame.setVisible(true);
	}
	
	@Override
	public synchronized void startAllQueries() {
		super.startAllQueries();
		startButton.setIcon(new ImageIcon(VisualSink.class.getResource("icons/duke.running.gif")));		
		buttonPanel.repaint();
	}
	
	@Override
	public synchronized void stopQuery(AbstractSink<?> query) throws IllegalArgumentException {
		super.stopQuery(query);
		if (queries.size() == 0)
			disableButtons();
	}
	
	@Override
	public synchronized void stopAllQueries() {
		super.stopAllQueries();
		disableButtons();
	}
	
	public void disableButtons() {
		startButton.setIcon(new ImageIcon(VisualSink.class.getResource("icons/dukeplug.gif")));
		startButton.setEnabled(false);
		stopButton.setEnabled(false);
		buttonPanel.repaint();		
	}
	/**
	 * Paints the frame.
	 */
	protected void paint() {
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1,2));

		startButton = new JButton("Start all registered queries.");
		startButton.setIcon(new ImageIcon(VisualSink.class.getResource("icons/dukeplug.gif")));
		startButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					startAllQueries();
				}
			}
		);

		stopButton = new JButton("Stop all registered queries.");
		stopButton.setIcon(new ImageIcon(VisualSink.class.getResource("icons/dukeWaveRed.gif")));
		stopButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					stopAllQueries();
				}
			}
		);
		buttonPanel.add(startButton);
		buttonPanel.add(stopButton);
		frame.getContentPane().add(buttonPanel);
	}
	
	@Override
	protected void onTermination() {
		super.onTermination();
		disableButtons();
	}
	
	public void windowOpened(WindowEvent e) { }

	public void windowClosing(WindowEvent e) {	}

	public void windowClosed(WindowEvent e) {
		terminate = true;
	}

	public void windowIconified(WindowEvent e) { }

	public void windowDeiconified(WindowEvent e) { }

	public void windowActivated(WindowEvent e) { }

	public void windowDeactivated(WindowEvent e) { }

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Enumerator e = new Enumerator(1000, 5);
		Printer<Integer> p = new Printer<Integer>(e);
		VisualQueryExecutor exec = new VisualQueryExecutor();
		exec.registerQuery(p);
	}
	
}
