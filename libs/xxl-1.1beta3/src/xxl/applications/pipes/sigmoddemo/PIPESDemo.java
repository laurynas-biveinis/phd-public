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

package xxl.applications.pipes.sigmoddemo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * This class demostrates the querying streams with pipes. It constructs an
 * gui that allows the illustration of query with a picture in the upper left
 * area. In the upper right area the query can be viewed as cql, query graph or
 * source code. Finally the downer area of the gui can be used to execute and monitor
 * the queries. <br>
 * Other queries can be added, by implementing demo and query classes and adding them
 * in initdemo(). <br>
 * <b>Note<b> that the following things have to be done to run this demo: <br>
 * 1. The system property 'xxlrootpath' must point to the xxl root path (with the -D option) <br>
 * 2. To run queries two and three of the highway example run the class with 150 megabytes for
 * maximum heapsize ( '-Xmx150m') <br>
 * 3. The test data must be stored in 'xxlrootpath\data\traffic\' and 'xxlrootpath\data\nexmark\' <br>  
 * 4. The swt.jar and native libs must be set up<br>
 * Note that this class shows the same demo, as our Web demo that is avaiable under: <br>
 * http://dbs.mathematik.uni-marburg.de/Home/Research/Projects/PIPES/Demo
 */ 
public class PIPESDemo extends Composite {
		
	/**
	 * A constant, signing that the right windows displays currently cql. 
	 */
	public final static int CQL = 0;

	/**
	 * A constant, signing that the right windows displays currently the query graph
	 */
	public final static int GRAPH = 1;
	
	/**
	 * A constant, signing that the right windows displays currently code. 
	 */
	public final static int CODE = 2;		
	
	/**
	 * Shows the about frame.
	 */
	class AboutFrame extends Dialog {
		private Shell dialogShell;

		AboutFrame(Shell parent, int style) {
			super(parent, style);			
			try {		
						
				dialogShell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
				dialogShell.setText("About");
				
				dialogShell.setSize(new org.eclipse.swt.graphics.Point(270,250));
				dialogShell.setBackground(pipeDemobackground);
				Display display = dialogShell.getDisplay();
				dialogShell.open();
				
				final Image img = new Image(Display.getDefault(), getClass().getResourceAsStream("images/aboutlogo.gif"));
				GC gc = new GC(dialogShell);
				gc.drawImage(img, 10, 0);
				gc.dispose();
				dialogShell.addPaintListener(new PaintListener() {
					public void paintControl(PaintEvent e) {
						GC gc = e.gc;
						gc.drawImage(img, 10, 0);
					}
				});
				
				Button okButton = new Button(dialogShell, SWT.PUSH);
				okButton.setText("Ok");	
				okButton.setSize(50, 20);
				okButton.setLocation(105, 180);
				
				okButton.addSelectionListener(new SelectionAdapter(){
					public void widgetSelected(SelectionEvent e) {					
						dialogShell.dispose();
						img.dispose();
					}			
				});			

				while (!dialogShell.isDisposed()) {
					if (!display.readAndDispatch())
						display.sleep();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}		
		}		
	}
	
	// gui elements
	private MenuItem aboutMenuItem;
	private Menu helpMenu;
	private MenuItem helpMenuItem;
	private MenuItem exitMenuItem;
	private Menu demoMenu;
	private MenuItem demoMenuItem;
	private Menu menu;
	private Text cqlOut;
	private Canvas canvas;
	private Composite console;
	private SashForm verSash;
	private SashForm horSash;
	private Composite cqlOutComposite;	
	private final Color pipeDemobackground = new Color(Display.getDefault(),255,255,255);
	static Shell shell;
	// variables
	private String backGroundImage ="images/DBS-Logo.gif" ;
	private int selectedDemo = -1;
	private int selectedQuery = -1;	
	private Demo[] demos ;
	private int cqlMode = CQL;
	private Color cqlColor = new Color(Display.getDefault(), 0, 0, 255);
	private Font cqlFont = new Font(Display.getDefault(), "Courier", 10, SWT.BOLD);

	/**
	 * Creates a new instance of pipedemo in the specified 
	 * Composite with the given style. 
	 * @param parent the parent composite of this instance.
	 * @param style the style (swt constants).
	 */
	public PIPESDemo(Composite parent, int style) {
		super(parent, style);
		try {		
			initDemo();			
			shell.setImage(new Image(Display.getDefault(), getClass().getResourceAsStream("images/xxl.gif")));
			verSash = new SashForm(this, SWT.VERTICAL | SWT.BORDER);
			Composite imageAndCql = new Composite(verSash, SWT.NULL);
			imageAndCql.setLayout(new FillLayout());
			horSash = new SashForm(imageAndCql, SWT.HORIZONTAL | SWT.BORDER);
			
			final ScrolledComposite sc = new ScrolledComposite(horSash, SWT.H_SCROLL |SWT.V_SCROLL);
			
			// image for illustration
			canvas = new Canvas(sc, SWT.MIN);
			canvas.setSize(500,200);
			sc.setContent(canvas);
			
			// cql / graph / code  - window
			Composite cqlComposite = new Composite(horSash, SWT.NULL);			
			FormLayout cqlCompositeLayout = new FormLayout();			
			cqlComposite.setLayout(cqlCompositeLayout);
			
			Group group = new Group(cqlComposite, SWT.NONE);
	     	FormData groupData = new FormData();
	     	groupData.left = new FormAttachment(0);
	     	groupData.top = new FormAttachment(0);
	     	groupData.right = new FormAttachment(100);
	     	group.setLayoutData(groupData);
	     	group.setLayout(new FillLayout());
			
	     	Button cql = new Button(group, SWT.RADIO | SWT.CENTER);
			cql.setText("CQL");
			Button graph = new Button(group, SWT.RADIO | SWT.CENTER);
			graph.setText("Graph");
			Button code = new Button(group, SWT.RADIO | SWT.CENTER);
			code.setText("Code");
			Button text = new Button(group, SWT.CENTER);
			text.setText("Font...");
			
			cqlOutComposite = new Composite(cqlComposite, SWT.NONE);
	     	FormData cqlOutCompositeData = new FormData();
	     	cqlOutCompositeData.left = new FormAttachment(0);
	     	cqlOutCompositeData.top = new FormAttachment(group);
	     	cqlOutCompositeData.right = new FormAttachment(100);
	     	cqlOutCompositeData.bottom = new FormAttachment(100);
	     	cqlOutComposite.setLayoutData(cqlOutCompositeData);
			cqlOutComposite.setLayout(new FillLayout());
			
			cqlOut = new Text(cqlOutComposite, SWT.MULTI |SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
			cqlOut.setText("PIPES Demo");
			cqlOut.setEditable(false);
			cqlOut.setFont(new Font(Display.getDefault(), "Courier", 20, SWT.BOLD));
			cqlOut.setForeground(cqlColor);

			// console will de used by Demo
			console = new Composite(verSash, SWT.NULL);			
			verSash.setWeights(new int[]{65, 35});
			horSash.setWeights(new int[]{80, 20});
			this.setBackground(pipeDemobackground);
			this.setLayout(new FillLayout());

			menu = new Menu(getShell(),SWT.BAR);
			
			// demomenu
			demoMenuItem = new MenuItem(menu,SWT.CASCADE);
			demoMenu = new Menu(demoMenuItem);
			demoMenuItem.setText("Control");
			demoMenuItem.setMenu(demoMenu);	
			exitMenuItem = new MenuItem(demoMenu,SWT.CASCADE);
			exitMenuItem.setText("Exit");
			
			// menus as defined in Demo
			Menu displayDemos[] = new Menu[demos.length];
			for (int i=0; i<demos.length;i++){
				MenuItem[] queries = new MenuItem[demos[i].getQueries().length];				
				if (demos[i].getQueries().length >1) {
					MenuItem temp = new MenuItem(menu,SWT.CASCADE);
					displayDemos[i] = new Menu(temp);
					temp.setText(demos[i].getName());
					temp.setMenu(displayDemos[i]);
					final int tempi = i;
					for (int j=0; j < demos[i].getQueries().length; j++) {
						queries[j] = new MenuItem(displayDemos[i],SWT.CASCADE);
						queries[j].setText(demos[i].getQueries()[j].getName());
						// events for menuentries						
						final int tempj = j;
						queries[j].addSelectionListener(new SelectionAdapter() {
							public void widgetSelected(SelectionEvent e) {
								startDemo(tempi,tempj);
							}
						});						
					}					
				}
			}
			
			// helpMenu
			helpMenuItem = new MenuItem(menu,SWT.CASCADE);
			helpMenu = new Menu(helpMenuItem);	
			aboutMenuItem = new MenuItem(helpMenu,SWT.CASCADE);			
			helpMenuItem.setText("Help");	
			helpMenuItem.setMenu(helpMenu);	
			aboutMenuItem.setText("About");
			
			getShell().setMenuBar(menu);
			
			canvas.setBackground(pipeDemobackground);
			sc.setBackground(pipeDemobackground);	
			
			// events
			addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					pipeDemobackground.dispose();
					System.exit(0);	
				}
			});			
			
			canvas.addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent e) {
					GC gc = e.gc;							
					Image img = new Image(Display.getDefault(), getClass().getResourceAsStream(backGroundImage));			
					gc.drawImage(img, 0, 0);
					canvas.setSize(img.getBounds().width, img.getBounds().height);
					img.dispose();
				}
			});
			
			exitMenuItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					pipeDemobackground.dispose();
					shell.dispose();
					System.exit(0);				
				}
			});

			aboutMenuItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					new AboutFrame(new Shell(shell), SWT.NULL);
				}
			});			
			
			cql.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (((Button)e.getSource()).getSelection()){						
						cqlMode = CQL;
						updateCql();
					}
				}
			});
			
			graph.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (((Button)e.getSource()).getSelection()){
						cqlMode = GRAPH;
						updateCql();
					}
				}
			});
			
			code.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (((Button)e.getSource()).getSelection()){						
						cqlMode = CODE;
						updateCql();
					}
				}
			});
			
			text.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					FontDialog dialog = new FontDialog(new Shell(shell));
					dialog.setFontList(cqlFont.getFontData());
					dialog.setRGB(cqlColor.getRGB());
					dialog.open();
					if (dialog.getFontList() != null)
						cqlFont = new Font(Display.getCurrent(), dialog.getFontList());					
					if (dialog.getRGB() != null) 
						cqlColor = new Color(Display.getCurrent(), dialog.getRGB().red, dialog.getRGB().green, dialog.getRGB().blue);					
					updateCql();					
				}
			});						
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.getShell().setMaximized(true);
	}
	/**
	 * @return Returns the selectedDemo.
	 */
	public int getSelectedDemo() {
		return selectedDemo;
	}

	/**
	 * @return Returns the selectedQuery.
	 */
	public int getSelectedQuery() {
		return selectedQuery;
	}

	/**
	 * @return Returns the demoList.
	 */
	public Demo[] getDemos() {
		return demos;
	}

	/**
	 * Starts a demo-query. 
	 * @param demo the selected demo.
	 * @param query the selected query.
	 */
	private void startDemo(int demo, int query) {
		if (selectedDemo != -1 && selectedQuery != -1)
			getDemos()[demo].getQueries()[query].stopQuery();					
		for (int i=0; i< console.getChildren().length; i++){
			console.getChildren()[i].dispose();
		}
		backGroundImage = getDemos()[demo].getQueries()[query].getImage();
		canvas.redraw();
		getDemos()[demo].getQueries()[query].startQuery(console);		
		selectedDemo = demo;
		selectedQuery = query;
		updateCql();		
		updateSash(horSash, getDemos()[demo].getQueries()[query].getHorizontalWeights());
		updateSash(verSash, getDemos()[demo].getQueries()[query].getVerticalWeights());		
	}
	
	/**
	 * This method is called at beginning to init the demos.
	 */
	private void initDemo() {
		demos = new Demo[] {Demo.getTrafficDemo(), Demo.getNEXMarkDemo()};		
	}
	
	/**
	 * updates the sash.
	 * @param sash the sash.
	 * @param values the weigth that specifiy the partitioning.
	 */
	private void updateSash(SashForm sash, int[] values) {
		sash.setWeights(new int[]{values[0]+5, values[1]+5});		
		sash.setWeights(values);		
	}
	
	/**
	 * Extracts some text. 
	 * @param fileName the file that is read.
	 * @return the extracted text.
	 */
	private String getJavaFile(String fileName){
		try {			
			String path = replaceSpaces(getClass().getResource(fileName).getFile().substring(1));
		    BufferedReader br = new BufferedReader(new FileReader(new File(path)));
			StringBuffer buffer = new StringBuffer();
			String s = "";
			for(; s!= null;s =br.readLine())
				buffer.append(s+"\n");
			return buffer.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return "an exception occured while proceeding the file";
		}
	}
	
	/**
	 * Helper method.
	 * @param string
	 * @return string
	 */
	private String replaceSpaces(String string) {
		StringBuffer buffer = new StringBuffer();		
		for (int i=0; i< string.length(); i++) {
			while (string.charAt(i)=='%')
				if (i+1 < string.length() && i+2 < string.length() &&
					string.charAt(i+1) =='2' && string.charAt(i+2)=='0'){					 
					 i += 3;
					 buffer.append(" ");
				}	
			buffer.append(string.charAt(i));
		}
		return buffer.toString();
	}
	
	/**
	 * Changes the illusttration a a query in the upper right window.	 
	 */
	private void updateCql() {
		for (int i=0; i< cqlOutComposite.getChildren().length; i++)
			cqlOutComposite.getChildren()[i].dispose();		
		switch (cqlMode) {
			case CQL:{
				cqlOut = new Text(cqlOutComposite, SWT.MULTI |SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
				cqlOut.setText(selectedDemo>=0&& selectedQuery >=0 ? getDemos()[selectedDemo].getQueries()[selectedQuery].getCQL() : "PIPES Demo");
				cqlOut.setEditable(false);
				cqlOut.setFont(cqlFont);
				cqlOut.setForeground(cqlColor);
				updateSash(horSash, horSash.getWeights());
				break;
			}
			case GRAPH: {
				cqlOutComposite.setLayout(new FillLayout());
				final ScrolledComposite sc = new ScrolledComposite(cqlOutComposite, SWT.H_SCROLL |SWT.V_SCROLL);
				sc.setLayout(new FillLayout());
				sc.setBackground(pipeDemobackground);
				final Canvas cqlPicture = new Canvas(sc, SWT.FILL);				
				sc.setContent(cqlPicture);
				cqlPicture.setSize(200,100);
				cqlPicture.setBackground(pipeDemobackground);								
				cqlPicture.addPaintListener(new PaintListener() {
					public void paintControl(PaintEvent e) {
						if (selectedDemo >=0 && selectedQuery >=0) {
							GC gc =e.gc;
							Image img = new Image(Display.getDefault(), getClass().getResourceAsStream(getDemos()[selectedDemo].getQueries()[selectedQuery].getQueryImage()));
							sc.setSize(cqlOutComposite.getSize());
							cqlPicture.setSize(Math.max(cqlOutComposite.getSize().x, img.getBounds().width), Math.max(cqlOutComposite.getSize().y, img.getBounds().height));
								gc.drawImage(img, cqlOutComposite.getSize().x > img.getBounds().width ? (cqlOutComposite.getSize().x - img.getBounds().width)/2 : 0, 0);
							img.dispose();
						}
						else {
							GC gc = new GC(cqlPicture);
							gc.drawString("select a demo first", 50, 50);
							gc.dispose();
						}
					}
				});
				updateSash(horSash, horSash.getWeights());
				break;
			}
			case CODE:{
				Text cqlOut = new Text(cqlOutComposite, SWT.MULTI |SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
				cqlOut.setText(selectedDemo>=0&& selectedQuery >=0 ? getJavaFile(getDemos()[selectedDemo].getQueries()[selectedQuery].getJavaFile()):"select a demo first");
				cqlOut.setEditable(false);
				cqlOut.setFont(cqlFont);
				cqlOut.setForeground(cqlColor);
				updateSash(horSash, horSash.getWeights());
				break;
			}
			default: throw new IllegalStateException("unknown cql mode");			
		}
	}
	
	/**
	 * Starts pipedemo.
	 * @param args the parameters are ignored. 
	 */
	public static void main(String[] args){
		try {
			Display display = Display.getDefault();
			shell = new Shell(display);
			new PIPESDemo(shell, SWT.NULL);			
			shell.setLayout(new org.eclipse.swt.layout.FillLayout());
			Rectangle shellBounds = shell.computeTrim(0,0,800,600);
			shell.setSize(shellBounds.width, shellBounds.height);
			shell.setText("PIPES Demo");
			shell.open();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
