package xxl.tests.cursors.visual;

import javax.swing.JPanel;
import javax.swing.WindowConstants;

import xxl.core.cursors.visual.ControllerJPanel;
import xxl.core.cursors.visual.IteratorControllable;
import xxl.core.util.XXLSystem;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ControllerJPanel.
 */
public class TestControllerJPanel {

	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to submit
	 *        parameters when the main method is called.
	 */
	public static void main(String[] args) {
		if (XXLSystem.calledFromMainMaker()) {
			System.out.println("RawExplorer: This class is not started from MainMaker.");
			return;
		}
		javax.swing.JFrame frame = new javax.swing.JFrame("Controller Panel");
		
		JPanel control = new ControllerJPanel(
			new IteratorControllable(
				new xxl.core.cursors.SecureDecoratorCursor(
					xxl.core.cursors.sources.Inductors.naturalNumbers()
				) {
					public Object next() {
						Object next = super.next();
						System.out.println("next = " + next);
						return next;
					}
				}
			)
		);
		
		frame.getContentPane().add(control, null);
		frame.setSize(240, 200);
	   	// ---
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

}
