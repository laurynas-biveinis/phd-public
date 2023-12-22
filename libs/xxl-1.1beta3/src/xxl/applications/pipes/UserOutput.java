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

	http://www.mathematik.uni-marburg.de/DBS/xxl

bugs, requests for enhancements: xxl@mathematik.uni-marburg.de

If you want to be informed on new versions of XXL you can
subscribe to our mailing-list. Send an email to

	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body.
*/

package xxl.applications.pipes;
import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.swing.JOptionPane;

import xxl.core.math.functions.AggregationFunction;
import xxl.core.math.statistics.parametric.aggregates.Average;
import xxl.core.math.statistics.parametric.aggregates.Maximum;
import xxl.core.math.statistics.parametric.aggregates.Minimum;
import xxl.core.math.statistics.parametric.aggregates.Variance;

/** Dies ist eine Hilfsklasse, die es erlaubt, verschiedene Strategien beim PMJ und 
 * MPMJ gegeneinander zu testen. Dazu zeigt sie zwei Graphen aus double-Paaren
 * an, von denen der erste üblicherweise die Ergebnisproduktion und der zweite
 * den Schätzer in Abhängigkeit von der Zeit darstellt. Solche Graphenpaare
 * können zum Vergleich gespeichert und geladen sowie nach tex exportiert werden.
 */
public class UserOutput extends Frame implements WindowListener, ActionListener, ItemListener {

	public static boolean NON_INCREASING_VALUES = false;
	
	protected static void prepareSave(String directory, String fileName, String fileExtension) throws IOException {
		if (! fileName.endsWith("."+extension))
			fileName += "."+extension;
		File f = new File(directory);
		if (! f.exists())
			f.mkdir();
		File f2 = new File(directory+fileName);			
		if (! f2.exists())
			f2.createNewFile();
	}
	
	public static void saveRawData(String directory, String fileName, List [] lists) {		
		try {
			prepareSave(directory, fileName, extension);
			FileOutputStream ostream = new FileOutputStream(directory+fileName);
			ObjectOutputStream p = new ObjectOutputStream(ostream);
			p.writeObject(lists);
			p.flush();
			ostream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void saveTex(String directory, String fileName, List [] lists, String[] descriptions) {
		try {
			prepareSave(directory, fileName, "tex");
			FileOutputStream ostream = new FileOutputStream(directory+fileName);
			PrintStream print = new PrintStream(ostream);
			
			print.println("\\begin{figure}[tb]");
	        print.println("  \\begin{center}");
	        print.println("    \\begin{pspicture}(-3,-1)(10,7)");
	        print.println("      \\put(0.0,-1.5){\\makebox(10,1){Zahl}}");
	        print.println("      \\rput[b]{*90}(-2,3){Anteil}");
	        print.println("      \\psset{xunit=0.5cm,yunit=1pt}");       
			for (int j=0; j<lists.length; j++) {
				print.println("      \\savedata{\\mydata_"+descriptions[j]+"}[{");
				for (int i=0; i<lists[j].size(); i++) {
					double [] p = (double[]) lists[j].get(i);
					double x = p[0];
					double y = p[1];
					print.print("        {");
					print.print(Double.toString(x));
					print.print(",");
					print.print(Double.toString(y));
					print.println("},");
				}
				print.println("      }]");
			}
			print.println("      \\dataplot[plotstyle=dots,showpoints=true,dotstyle=*]{\\...}");
	        print.println("      \\psaxes[axesstyle=frame,Dx=1,Dy=10000]{->}(20,1000000)");
	        print.println("      \\psline[linestyle=dotted,dotsep=2pt](0,1000)(20,1000)");
	        print.println("    \\end{pspicture}");
	        print.println("    \\rule{0mm}{8mm}");
	        print.println("    \\caption{}");
	        print.println("    \\label{fi:}");
	        print.println("  \\end{center}");
	        print.println("\\end{figure}");
			print.flush();
			ostream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void saveGNUPlot(String directory, String fileName, List [] lists) {
		try {
			prepareSave(directory, fileName, "dat");
			FileOutputStream ostream = new FileOutputStream(directory+fileName);
			PrintStream print = new PrintStream(ostream);
			int maxListSize = lists[0].size();
			for (int i=0; i<lists.length;i++)
			    if (maxListSize < lists[i].size())
			        maxListSize = lists[i].size();
			for (int i=0; i<maxListSize; i++) {
			    for (int j=0; j<lists.length; j++) {
			        double [] p = (double[]) lists[j].get(i);
					double x = p[0];
					double y = p[1];
					print.print(Double.toString(x));
					print.print("\t");
					print.print(Double.toString(y));
					print.print("\t");
				}
			    print.print("\n");
			}				
			print.flush();
			ostream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	Menu fileMenu;
	Menu optionsMenu;
	Menu curveMenu;
	Menu analyzeMenu;
	CheckboxMenuItem averageItem;
	CheckboxMenuItem exactItem;
	
	List [] listOfLists;
	String [] filenames;
	boolean[] showCurves;
	boolean increasingValues;
	double maxtime, maxcount;
	connectionTypes connectionType;

	private static Color[] colors = { Color.red, Color.green, Color.blue,
            Color.yellow, Color.black, Color.cyan, Color.orange, Color.magenta };
	
	private static enum connectionTypes { no, average, exact };
	
	private static String extension = "xxllog";
	private static String FILE = "File";
	private static String OPTIONS = "Options";
	private static String ANALYZE = "Analyze";
	
	private static String SAVE = "Save";
	private static String SAVE_SINGLE = "Save single graph";
	private static String ADD = "Add";
	private static String REMOVE = "Remove";
	private static String REMOVE_ALL = "Remove all";
	private static String CLOSE = "Close";
	private static String EXPORT_TEX = "Export Tex";
	private static String EXPORT_GNUPLOT = "Export GnuPlot";
	private static String CONNECT_POINTS_AVERAGE = "Connect points (average)";
	private static String CONNECT_POINTS_EXACT = "Connect points (exact)";
	private static String NO_CHANGE_ERROR = "Value not changed as no new value was provided.";
	private static String MAX_X = "Set x range";
	private static String MAX_Y = "Set y range";
	private static String TOGGLE_GRAPH = "Show/hide graph";
	private static String AGG_OVER_GRAPH = "Aggregation over graph";
	private static String AGG_OVER_GRAPHS=  "Aggregation over every graph";
	private static String CHOOSE_FILE = "Choose file";
	private static String NO_GRAPH_AVAILABLE = "No graph available";
	
	/**
	 * Creates a new frame that show results of experiments stored in the given list. 
	 * @param title the title of the frame.
	 * @param listOfLists an array of lists, containing the values of some experiments that are illustrated as curve.
	 * @param descriptions the description of each curve. 
	 * @param increasingValues determines if all the values are increasing.
	 * @param connectPoints determines if the points should be connected. Attention only the points are saved!
	 */
	public UserOutput (String title, List [] listOfLists, String [] descriptions, boolean increasingValues, boolean connectPoints) {
		super(title);
			
		addWindowListener(this);
		setSize(800, 600);

		MenuBar menubar = new MenuBar();
		
		fileMenu = new Menu(FILE);
		fileMenu.add(new MenuItem (SAVE, new MenuShortcut(KeyEvent.VK_S)));
		fileMenu.add(new MenuItem (SAVE_SINGLE, new MenuShortcut(KeyEvent.VK_W)));
		fileMenu.add(new MenuItem (EXPORT_TEX,new MenuShortcut(KeyEvent.VK_T)));		
		fileMenu.add(new MenuItem (EXPORT_GNUPLOT,new MenuShortcut(KeyEvent.VK_G)));
		fileMenu.addSeparator();
		fileMenu.add(new MenuItem (ADD,new MenuShortcut(KeyEvent.VK_A)));
		fileMenu.add(new MenuItem (REMOVE,new MenuShortcut(KeyEvent.VK_R)));
		fileMenu.add(new MenuItem (REMOVE_ALL,new MenuShortcut(KeyEvent.VK_E)));
		fileMenu.addSeparator();
		fileMenu.add(CLOSE);
		fileMenu.addActionListener(this);
		menubar.add(fileMenu);
		
		optionsMenu = new Menu(OPTIONS);
		optionsMenu.add(averageItem = new CheckboxMenuItem(CONNECT_POINTS_AVERAGE));
		optionsMenu.add(exactItem = new CheckboxMenuItem(CONNECT_POINTS_EXACT));
		optionsMenu.add(new MenuItem (MAX_X));
		optionsMenu.add(new MenuItem (MAX_Y));
		averageItem.addItemListener(this);
		exactItem.addItemListener(this);
		curveMenu = new Menu(TOGGLE_GRAPH);
		optionsMenu.addActionListener(this);
		menubar.add(optionsMenu);
		
		analyzeMenu = new Menu(ANALYZE);		
		analyzeMenu.add(new MenuItem(AGG_OVER_GRAPH));
		analyzeMenu.add(new MenuItem(AGG_OVER_GRAPHS));		
		analyzeMenu.addActionListener(this);
		menubar.add(analyzeMenu);
		
		this.increasingValues = increasingValues;
		this.connectionType = connectPoints ? connectionTypes.average : connectionTypes.no;
		
		if (listOfLists==null) {
			load(true);
		}
		else {
			this.listOfLists = listOfLists;
			this.filenames = descriptions;
		}
		
		if (listOfLists != null &&(showCurves == null || showCurves.length != listOfLists.length)) {
			showCurves = new boolean[listOfLists.length];
			for (int i=0; i<showCurves.length; i++)
				showCurves[i] = true;
		}

		updateMenu(true);
		setMenuBar(menubar);
		setVisible(true);
		validate();		
	}
	
	/**
	 * Creates a new frame that show results of experiments stored in the given list. 
	 * @param title the title of the frame.
	 * @param listOfLists an array of lists, containing the values of some experiments that are illustrated as curve.
	 * @param descriptions the description of each curve. 
	 * @param increasingValues determines if all the values are increasing.  
	 */
	public UserOutput (String title, List [] listOfLists, String [] descriptions, boolean increasingValues) {
		this(title, listOfLists, descriptions, increasingValues, false);
	}
	/**
	 * Creates a new frame that show results of experiments stored in the given list. To determine the scale of 
	 * the axis, it is assumed, that the greatest value is at the end of each list. 
	 * @param title the title of the frame.
	 * @param listOfLists an array of lists, containing the values of some experiments that are illustrated as curve.
	 * @param descriptions the description of each curve.
	 */
	public UserOutput (String title, List [] listOfLists, String [] descriptions) {
		this(title, listOfLists, descriptions, false);
	}

	protected void updateMenu(boolean setTrue) {		
		if (curveMenu != null)
			curveMenu.removeAll();
		optionsMenu.remove(curveMenu);
		if (listOfLists != null) {
			CheckboxMenuItem[] curves = new CheckboxMenuItem[listOfLists.length];
			for (int i=0; i<listOfLists.length; i++) {
				curves[i] = new CheckboxMenuItem(filenames[i]);
				if (setTrue)
					curves[i].setState(true);
				else
					curves[i].setState(showCurves[i]);			
				curves[i].addItemListener(this);
				curveMenu.add(curves[i]);
			}
		}
		optionsMenu.add(curveMenu);
		this.repaint();
	}
	
	protected void removeCurve(int position) {
		List [] catList = new List[listOfLists.length-1];
		String [] catFilenames = new String[listOfLists.length-1];
		for (int i=0,index=0; i<catList.length; i++) {
			if (i == position)
				index++;
			catList[i] = listOfLists[index];
			catFilenames[i] = filenames[index];
			index++;			
		}
		listOfLists=catList;
		filenames=catFilenames;
		
		if (listOfLists != null) {
			if (showCurves == null) {
				showCurves = new boolean[listOfLists.length];
				for (int i=0; i<showCurves.length; i++)
					showCurves[i] = true;					
			}
			else {
				boolean[] newShowCurves = new boolean[listOfLists.length];
				for (int i=0,index=0; i<newShowCurves.length; i++) { 
					if (i == position)
						index++;
					newShowCurves[i] = showCurves[index];
					index++;
				}
				showCurves = newShowCurves; 	
			}
		}
		updateMenu(false);
	}
	
	private void load (boolean dirToTitle) {		
		String fn="";
		try {
			FileDialog fd = new FileDialog(this,CHOOSE_FILE,FileDialog.LOAD);
			fd.setFile("*."+extension);
			fd.setVisible(true);
			fn=fd.getFile();
			if (fd.getFile() == null)
				return;
			if (dirToTitle) {				
				this.setTitle(filterString(fd.getDirectory(),1));
			}			
			FileInputStream istream = new FileInputStream(fd.getDirectory()+fd.getFile());
			ObjectInputStream p = new ObjectInputStream(istream);
			List [] readListOfLists = (List [])p.readObject();
			istream.close();
			int llength = listOfLists==null ? 0 : listOfLists.length;
			int lengthsum = llength + readListOfLists.length;
			List [] catList = new List[lengthsum];
			String [] catFilenames = new String[lengthsum];
			for (int i=0; i<llength; i++) {
				catList[i]=listOfLists[i];
				catFilenames[i]=filenames[i];
			}
			for (int i=llength; i<lengthsum; i++) {
				catList[i]=readListOfLists[i-llength];
				catFilenames[i]=fn+" "+Integer.toString(i-llength);
			}
			synchronized(this){
				listOfLists=catList;
				filenames=catFilenames;
				
				if (listOfLists != null) {
					if (showCurves == null) {
						showCurves = new boolean[listOfLists.length];
						for (int i=0; i<showCurves.length; i++)
							showCurves[i] = true;					
					}
					else {
						boolean[] newShowCurves = new boolean[listOfLists.length];
						for (int i=0; i<showCurves.length; i++)
							newShowCurves[i] = showCurves[i];
						for (int i=showCurves.length; i<newShowCurves.length; i++)
							newShowCurves[i] = true;
						showCurves = newShowCurves; 	
					}
				}
			}
			updateMenu(false);
		}
		catch (Exception e) { System.out.println(e); }
	}
	
	private void loadLists(List [] readListOfLists, String[] names, boolean dirToTitle) {
		try {
			int llength = listOfLists==null ? 0 : listOfLists.length;
			int lengthsum = llength + readListOfLists.length;
			List [] catList = new List[lengthsum];
			String [] catFilenames = new String[lengthsum];
			for (int i=0; i<llength; i++) {
				catList[i]=listOfLists[i];
				catFilenames[i]=filenames[i];
			}
			for (int i=llength; i<lengthsum; i++) {
				catList[i]=readListOfLists[i-llength];
				catFilenames[i]=names[i-llength];
			}
			synchronized(this){
				listOfLists=catList;
				filenames=catFilenames;
				
				if (listOfLists != null) {
					if (showCurves == null) {
						showCurves = new boolean[listOfLists.length];
						for (int i=0; i<showCurves.length; i++)
							showCurves[i] = true;					
					}
					else {
						boolean[] newShowCurves = new boolean[listOfLists.length];
						for (int i=0; i<showCurves.length; i++)
							newShowCurves[i] = showCurves[i];
						for (int i=showCurves.length; i<newShowCurves.length; i++)
							newShowCurves[i] = true;
						showCurves = newShowCurves; 	
					}
				}
			}
			updateMenu(false);
		}
		catch (Exception exception) {
			exception.printStackTrace();
		}
	}
	
	private void loadList(List list, String name, boolean dirToTitle) {
		loadLists(new List[]{list}, new String[]{name}, dirToTitle);
	}
		
	protected String filterString(String s, int numberOfSep) {		
		int j=numberOfSep+1;
		for (int i=s.length()-2; i>=0; i--)
			if (new Character(s.charAt(i)).compareTo(new Character(File.separatorChar))==0){
				j--;
				if (j==0)
					return s.substring(i+1, s.length()-1);
			}				
		return null;
	}

	public void paint(Graphics g) {
		if (listOfLists==null) return;
		g.setColor(Color.darkGray);
		g.drawLine(100,100,100,510);
		g.drawLine(90,500,700,500);
		g.drawLine(90,100,100,100);
		g.drawLine(700,500,700,510);
		
		for (int i=0; i<listOfLists.length; i++) {
			if (i<showCurves.length && !showCurves[i])	continue;
			
			if (increasingValues) {
				double [] end = (double []) listOfLists[i].get(listOfLists[i].size()-1);
				if (end[0]>maxtime) maxtime=end[0];
				if (end[1]>maxcount) maxcount=end[1];			
			}
			if (maxtime ==Double.NaN || maxcount==Double.NaN)
				increasingValues = false;
			if (! increasingValues){				
				for (int j=0; j<listOfLists[i].size();j++) {
					if ( ! new Double(((double[])listOfLists[i].get(j))[0]).isNaN())
						maxtime = Math.max(maxtime, ((double[])listOfLists[i].get(j))[0]);				
					if (! new Double(((double[])listOfLists[i].get(j))[1]).isNaN())
						maxcount = Math.max(maxcount, ((double[])listOfLists[i].get(j))[1]);
				}				
			}
		}
		
		if (maxtime == 0)
			maxtime = 1;
		if (maxcount == 0)
			maxcount = 1;
		
		g.setColor(Color.black);
		NumberFormat format = DecimalFormat.getInstance(Locale.ENGLISH);
		format.setMaximumFractionDigits(3);
		String mt = format.format(maxtime);
		String mc = format.format(maxcount);
		g.drawString(mc,20,100);
		g.drawString(mt,700,525);

		for (int j=0; j<listOfLists.length; j++) {
			g.setColor(colors[j%8]);
			if (j<showCurves.length && !showCurves[j])	{
				g.drawString(filenames[j]+" (not shown)",100+300*(j/4),550+15*(j%4));
				continue;
			}			
			g.drawString(filenames[j],100+300*(j/4),550+15*(j%4));
			int oldX=-1;
			int oldY=-1;
			for (int i=0; i<listOfLists[j].size(); i++) {
				double [] p = (double[]) listOfLists[j].get(i);
				int x=0;
				int y=0;
				if (new Double(p[1]).isNaN() || p[1] < 0) {
					x = (int) (100 + java.lang.Math.round(600*p[0]/maxtime));
					y = 500;
					g.drawOval(x,y,5,5);
				}
				else {
					x = (int) (100 + java.lang.Math.round(600*p[0]/maxtime));
					y = (int) (500 - java.lang.Math.round(400*p[1]/maxcount));
					g.drawLine(x-2,y-2,x+2,y+2);
					g.drawLine(x-2,y+2,x+2,y-2);
				}
				if (connectionType == connectionTypes.average) {
					if (oldX != -1 && oldY !=-1)
						g.drawLine(oldX,oldY,x,y);
					oldX = x;
					oldY = y;
				}
				else if (connectionType == connectionTypes.exact) {
					if (oldX != -1 && oldY !=-1){
						g.drawLine(oldX,oldY,x,oldY);
						g.drawLine(x,oldY,x,y);
					}
					oldX = x;
					oldY = y;
				}

			}
		}
	}

	public static void main (String [] args) {
		new UserOutput("",null,null);
	}

	public void windowOpened(WindowEvent e) {}
	public void windowClosing(WindowEvent e) {
		dispose();
	}
	public void windowClosed(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand()==SAVE) {
			if (filenames == null || filenames.length ==0) {
				JOptionPane.showMessageDialog(this, NO_GRAPH_AVAILABLE, "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			FileDialog fd = new FileDialog(this,CHOOSE_FILE,FileDialog.SAVE);
			if (filenames.length == 1) {  // preset export name 
				int j = filenames[0].lastIndexOf(".");
				fd.setFile(j>0 ? filenames[0].substring(0, j)+"."+extension : filenames[0]+"."+extension);
			}
			else
				fd.setFile("*."+extension);
			fd.setVisible(true);
			if (fd.getDirectory() != null && fd.getFile() != null)
				saveRawData(fd.getDirectory(), fd.getFile(), listOfLists);
		}		
		if (e.getActionCommand()==SAVE_SINGLE) {			
			if (filenames == null || filenames.length ==0) {
				JOptionPane.showMessageDialog(this, NO_GRAPH_AVAILABLE, "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			int index = -1;
			String s = (String)JOptionPane.showInputDialog(
                    this,
                    SAVE,
                    SAVE,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    filenames,
                    null);
			if (s != null)
				for (int i=0; i<filenames.length; i++)
					if (s.equals(filenames[i]))
						index =i;
			if (index >=0) {
				FileDialog fd = new FileDialog(this,CHOOSE_FILE,FileDialog.SAVE);
				if (filenames.length == 1) {  // preset export name 
					int j = filenames[0].lastIndexOf(".");
					fd.setFile(j>0 ? filenames[0].substring(0, j)+"."+extension : filenames[0]+"."+extension);
				}
				else
					fd.setFile("*."+extension);
				fd.setVisible(true);
				if (fd.getDirectory() != null && fd.getFile() != null)
					saveRawData(fd.getDirectory(), fd.getFile(), new List[]{listOfLists[index]});				
			}
		}

		if (e.getActionCommand()==EXPORT_TEX) {
			if (filenames == null || filenames.length ==0) {
				JOptionPane.showMessageDialog(this, NO_GRAPH_AVAILABLE, "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			FileDialog fd = new FileDialog(this,CHOOSE_FILE,FileDialog.SAVE);
			if (filenames.length == 1) {  // preset export name 
				int j = filenames[0].lastIndexOf(".");
				fd.setFile(j>0 ? filenames[0].substring(0, j)+".tex" : filenames[0]+".tex");
			}
			else
				fd.setFile("*.tex");
			fd.setVisible(true);
			if (fd.getDirectory() != null && fd.getFile() != null)
				saveTex(fd.getDirectory(), fd.getFile(), listOfLists, filenames);
		}
		if (e.getActionCommand()==EXPORT_GNUPLOT) {
			if (filenames == null || filenames.length ==0) {
				JOptionPane.showMessageDialog(this, NO_GRAPH_AVAILABLE, "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			FileDialog fd = new FileDialog(this,CHOOSE_FILE,FileDialog.SAVE);
			if (filenames.length == 1){ // preset export name  
				int j = filenames[0].lastIndexOf(".");
				fd.setFile(j>0 ? filenames[0].substring(0, j)+".dat" : filenames[0]+".dat");
			}
			else
				fd.setFile("*.dat");
			fd.setVisible(true);
			if (fd.getDirectory() != null && fd.getFile() != null)
				saveGNUPlot(fd.getDirectory(), fd.getFile(), listOfLists);
		}		
		if (e.getActionCommand()==ADD) {
			load(true);			
			repaint();
		}
		
		if (e.getActionCommand()==REMOVE) {
			if (filenames == null || filenames.length ==0) {
				JOptionPane.showMessageDialog(this, NO_GRAPH_AVAILABLE, "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (filenames.length == 1)
				removeCurve(0);				
			else {
				String s = (String)JOptionPane.showInputDialog(
	                    this,
	                    REMOVE,
	                    REMOVE,
	                    JOptionPane.PLAIN_MESSAGE,
	                    null,
	                    filenames,
	                    null);
				if (s != null)
					for (int i=0; i<filenames.length; i++)
						if (s.equals(filenames[i]))
							removeCurve(i);
			}
			repaint();
		}
		
		if (e.getActionCommand()==REMOVE_ALL) {
			if (filenames == null || filenames.length ==0) {
				JOptionPane.showMessageDialog(this, NO_GRAPH_AVAILABLE, "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			for (int i=0, j=listOfLists.length; i<j; i++)
				removeCurve(0);			
			repaint();
		}
		
		if (e.getActionCommand()==MAX_X) {
			String s = (String)JOptionPane.showInputDialog(
                    this,
                    MAX_X,
                    MAX_X,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    ""+maxtime);
			try {
				maxtime = Double.parseDouble(s);
			}
			catch (Exception exc) {
				JOptionPane.showMessageDialog(this, NO_CHANGE_ERROR, "Error", JOptionPane.ERROR_MESSAGE);
			}
			repaint();
		}
		
		if (e.getActionCommand()==MAX_Y) {
			String s = (String)JOptionPane.showInputDialog(
                    this,
                    MAX_Y,
                    MAX_Y,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    ""+maxcount);
			try {
				maxcount = Double.parseDouble(s);
			}
			catch (Exception exc) {
				JOptionPane.showMessageDialog(this, NO_CHANGE_ERROR, "Error", JOptionPane.ERROR_MESSAGE);
			}
			repaint();
		}
		
		if (e.getActionCommand()==AGG_OVER_GRAPH) {
			int selGraph=0;
			if (filenames == null || filenames.length ==0) {
				JOptionPane.showMessageDialog(this, NO_GRAPH_AVAILABLE, "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}				
			if (filenames.length > 1) {
				String s = (String)JOptionPane.showInputDialog(
	                    this,
	                    AGG_OVER_GRAPH,
	                    "Choose graph",
	                    JOptionPane.PLAIN_MESSAGE,
	                    null,
	                    filenames,
	                    null);
				if (s == null)
					return;
				for (int i=0; i<filenames.length; i++)
					if (s.equals(filenames[i]))
						selGraph = i;
			}					
			String s = (String)JOptionPane.showInputDialog(
                    this,
                    AGG_OVER_GRAPH,
                    "Choose aggregate",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    new String[]{"Average", "Variance", "Min", "Max"},
                    null);
			if (s == null)
				return;
			AggregationFunction agg = null;
			if (s.equals("Average"))
				agg = new Average();
			if (s.equals("Variance"))
				agg = new Variance();
			if (s.equals("Min"))
				agg = new Minimum();
			if (s.equals("Max"))
				agg = new Maximum();
			if (agg != null) {							
				Double d = null;;
				for (int j=0; j<listOfLists[selGraph].size(); j++)
					d = (Double)agg.invoke(d, ((double[])listOfLists[selGraph].get(j))[1]);							
				LinkedList newList = new LinkedList();
				for (int j=0; j<listOfLists[selGraph].size(); j++)
					newList.add(new double[]{((double[])listOfLists[selGraph].get(j))[0], d.doubleValue()});
				loadList(newList, s+" over "+filenames[selGraph],false);
			}							
		}
		
		if (e.getActionCommand()==AGG_OVER_GRAPHS) {
			if (filenames == null || filenames.length ==0) {
				JOptionPane.showMessageDialog(this, NO_GRAPH_AVAILABLE, "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}				
			String s = (String)JOptionPane.showInputDialog(
                    this,
                    AGG_OVER_GRAPH,
                    "Choose aggregate",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    new String[]{"Average", "Variance", "Min", "Max"},
                    null);
			if (s == null)
				return;
			AggregationFunction agg = null;
			if (s.equals("Average"))
				agg = new Average();
			if (s.equals("Variance"))
				agg = new Variance();
			if (s.equals("Min"))
				agg = new Minimum();
			if (s.equals("Max"))
				agg = new Maximum();
			if (agg != null) {
				Double d = null;
				int curLength = listOfLists.length;
				for (int i = 0; i < curLength; i++) {
					for (int j=0; j<listOfLists[i].size(); j++)
						d = (Double)agg.invoke(d, ((double[])listOfLists[i].get(j))[1]);
					LinkedList newList = new LinkedList();
					for (int j=0; j<listOfLists[i].size(); j++)
						newList.add(new double[]{((double[])listOfLists[i].get(j))[0], d.doubleValue()});
					loadList(newList, s+" over "+filenames[i],false);
				}
			}							
		}

		if (e.getActionCommand()==CLOSE) 
			windowClosing(null);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e) {
		boolean update = false;
		if (e.getItem()==CONNECT_POINTS_AVERAGE) {
			if (connectionType != connectionTypes.average) {
				connectionType = connectionTypes.average;
				averageItem.setState(true);
				exactItem.setState(false);
			}
			else {
				connectionType = connectionTypes.no;
				averageItem.setState(false);
				exactItem.setState(false);				
			}
			update=true;
		}
		if (e.getItem()==CONNECT_POINTS_EXACT) {
			if (connectionType != connectionTypes.exact) {
				connectionType = connectionTypes.exact;
				averageItem.setState(false);
				exactItem.setState(true);
			}
			else {
				connectionType = connectionTypes.no;
				averageItem.setState(false);
				exactItem.setState(false);								
			}
			update=true;
		}
		if (filenames != null) {
			for (int i=0; i<filenames.length; i++) {
				if (e.getItem().equals(filenames[i])) {
					showCurves[i] = ((CheckboxMenuItem)e.getSource()).getState();
					update=true;
				}
			}
		}
		if (update)
			repaint();
	}
	
}