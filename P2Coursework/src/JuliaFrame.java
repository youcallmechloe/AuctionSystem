import java.awt.Container;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * This method extends from the MyFrame, so the methods extend back to there. This creates the new JuliaFrame for the JuliaSet 
 * to be displayed on. The init method is overwritten as there is no control panel needed, only the imagepanel, which in the 
 * constructor is set to a JuliaSet rather than an ImagePanel.
 * @author chloeallan
 *
 */
public class JuliaFrame extends MyFrame{
	
	JuliaSet imagePanel;
	
	/**
	 * takes a title for the new frame and a ComplexNumber to create a new JuliaSet as the parameters. Calls init on 
	 * the frame and then repaint to create the frame.
	 */
	public JuliaFrame(String title, ComplexNumber c){
		super(title);
		imagePanel = new JuliaSet(c);
		init();
		repaint();
	}
	
	/**
	 * This method has all the code necessary to create the frame. 
	 */
	public void init(){
		
		setSize(500, 500);
		Container contain = getContentPane();
		setLocationRelativeTo(null);
				
		contain.add(imagePanel);
		
		toFront();
		setVisible(true);
		
		addWindowListener(new WindowListener(){

			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub
			}

			//When the window is closed, the static varaible thisJuliaFrame in ImagePanel is set to null, so a new one can be created.
			public void windowClosing(WindowEvent e) {
				
				ImagePanel.thisJuliaFrame = null;
			}

			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub
			}
		});
		
	}

}
