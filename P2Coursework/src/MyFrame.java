import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * This class creates the main frame that displays the fractals and the controls for it. It contains an init method to create the whole
 * display and an initControl method that just creates the control panel display. 
 * @author chloeallan
 */
public class MyFrame extends JFrame{

	ImagePanel imagePanel;
	JPanel controlPanel;

	JButton favourite = new JButton("Favourite Julia Set");
	JButton export = new JButton("Export Julia Set");
	JButton zoomOut = new JButton("Resize To Original");

	//These variables are static as they need to be accessed in the ImagePanel class so that variables can be changed when buttons are clicked. 
	public static JTextField minX = new JTextField(5);
	public static JTextField maxX = new JTextField(5);
	public static JTextField minY = new JTextField(5);
	public static JTextField maxY = new JTextField(5);
	public static JSlider iterations = new JSlider();
	public static JTextField itNum = new JTextField(3);

	JComboBox<ComplexNumber> favourites = new JComboBox<ComplexNumber>();
	ArrayList<ComplexNumber> favComplex = new ArrayList<ComplexNumber>();
	JComboBox<String> fractals = new JComboBox<String>();
	JComboBox<Integer> zoomIt = new JComboBox<Integer>();

	Boolean firstTime = true;

	public MyFrame(String title){
		super(title);
	}

	/**
	 * This method runs the code to create everything inside the main Fractal frame. It contains 2 JPanels, one for the 
	 * image and one for the controls. To create the image panel I've used the ImagePanel class, but for the control panel
	 * I've just used a new JPanel so a new class wasn't needed. 
	 * <br>This method contains the 4 ActionsListeners for the parameter boxes that show the bounds of the ImagePanel. It works by
	 * getting the text from the textfield's and setting it to the bound parameters in ImagePanel, and also works in the 
	 * opposite way so when the bound parameter in ImagePanel is changed, the value of the textfield is as well. 
	 * <br>The method also contains the Change and ActionListeners for the iteration slider and the textfield next to it. This 
	 * changes the number of iterations the Calculate method in ImagePanel will go through to create the image. The slider
	 * contains a ChangeListener so that when the slider is moved, it sets the value in the textfield, and the iteration number in 
	 * ImagePanel to the value on the slider. Visa Versa, when the value in the textfield or the iteration number is changed, the value 
	 * of the slider is changed as well. 
	 */
	public void init(){

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(700, 750);
		Container contain = getContentPane();
		contain.setLayout(new BorderLayout());

		imagePanel = new ImagePanel();
		controlPanel = new JPanel();

		controlPanel.setLayout(new GridLayout(4,0));

		fractals.addItem("Mandelbrot Set");
		fractals.addItem("Burning Ship Fractal");
		fractals.addItem("Tricorn Fractal");

		zoomIt.addItem(1);
		zoomIt.addItem(10);
		zoomIt.addItem(25);
		zoomIt.addItem(40);
		zoomIt.addItem(50);

		JLabel zoom = new JLabel("Zoom Iterations:");

		JPanel complexNum = new JPanel();
		complexNum.add(ImagePanel.userSelectedPoint);

		JPanel juliaButtons = new JPanel();
		juliaButtons.add(favourite);
		juliaButtons.add(favourites);
		juliaButtons.add(export);
		juliaButtons.add(zoom);
		juliaButtons.add(zoomIt);

		JPanel sizeButtons = new JPanel();

		JLabel sizeBL = new JLabel("Bounds:");

		minX.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				double newMinX = Double.parseDouble(minX.getText());
				imagePanel.realMin = newMinX;
				repaint();
			}

		});

		maxX.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				double newMaxX = Double.parseDouble(maxX.getText());
				imagePanel.realMax = newMaxX;
				repaint();
			}

		});

		minY.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				double newMinY = Double.parseDouble(minY.getText());
				imagePanel.imagMin = newMinY;
				repaint();
			}

		});

		maxY.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				double newMaxY = Double.parseDouble(maxY.getText());
				imagePanel.imagMax = newMaxY;
				repaint();
			}

		});

		minX.setText(imagePanel.realMin + "");
		maxX.setText(imagePanel.realMax + "");
		minY.setText(imagePanel.imagMin + "");
		maxY.setText(imagePanel.imagMax + "");

		sizeButtons.add(sizeBL);
		sizeButtons.add(minX);
		sizeButtons.add(maxX);
		sizeButtons.add(minY);
		sizeButtons.add(maxY);
		sizeButtons.add(zoomOut);

		JPanel iterationSlider = new JPanel();

		iterations.setMaximum(1000);
		iterations.setMinimum(0);

		JLabel sliderLabel = new JLabel("Iterations:", JLabel.CENTER);
		iterations.setValue(100);

		iterations.addChangeListener(new ChangeListener(){

			public void stateChanged(ChangeEvent e) { 				
				itNum.setText(String.valueOf(iterations.getValue()));
				imagePanel.iterationSlider = iterations.getValue();
				repaint();
			}

		});

		itNum.setText(100 + "");

		itNum.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				int newItNum = Integer.parseInt(itNum.getText());
				itNum.setText(String.valueOf(iterations.getValue()));
				iterations.setValue(newItNum);
				imagePanel.iterationSlider = newItNum;
				repaint();

			}

		});

		iterationSlider.add(fractals);
		iterationSlider.add(sliderLabel);
		iterationSlider.add(iterations);
		iterationSlider.add(itNum);

		controlPanel.add(complexNum);
		controlPanel.add(juliaButtons);
		controlPanel.add(iterationSlider);
		controlPanel.add(sizeButtons);

		initControl();

		contain.add(imagePanel, BorderLayout.CENTER);
		contain.add(controlPanel, BorderLayout.SOUTH);

		setVisible(true);

	}

	/**
	 * This method contains the code to create the 6 ActionListeners for the buttons and comboboxes on the control panel.
	 */
	public void initControl(){

		/* 
		 * This checks whether the arraylist for the favourites contains the julia set that's trying to be favourited, and if
		 * it doesnt then it adds the complex number relating to the julia frame to the combobox and the arraylist. It's surrounded
		 * by a try-catch so that if the complex number trying to be added is already in the favourites arraylist then it doesnt try 
		 * and add it. 
		 */
		favourite.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

				try{
					if(!favComplex.contains(imagePanel.getThisJuliaFrame().imagePanel.getComplex())){
						favComplex.add(imagePanel.getThisJuliaFrame().imagePanel.getComplex());	
						favourites.addItem(imagePanel.getThisJuliaFrame().imagePanel.getComplex());
					} 
				}
				catch(NullPointerException e2){
				}
			}		
		});

		/*
		 * The export button tries to export a Julia Set related to one of the complex numbers in the favourites list. It cannot
		 * export a Julia Set without the complex number first being favourited, so it's in the favourites arraylist and 
		 * combobox. The ActionListener checks if the arraylist is empty, meaning theres no favourite Julia Sets so an error window
		 * is created asking the user to add/choose a favourite. If there is a favourite chosen, the new Julia Frame is created 
		 * and the image (bufferedimage) of the Julia Set is saved in the project folder as a png. 
		 */
		export.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

				ComplexNumber selected;

				selected = (ComplexNumber) favourites.getSelectedItem();

				if(favComplex.isEmpty()){
					EFrame eFrame = new EFrame("Error");
				} else{
					JuliaFrame favJuliaFrame = new JuliaFrame(selected.toString(), selected);
					try {
						BufferedImage juliaSave = (BufferedImage) favJuliaFrame.imagePanel.getImage();
						File outputfile = new File(selected + ".png");
						ImageIO.write(juliaSave, "png", outputfile);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		
		//Resets the bounds of the fractal display back to -1.6/1.6 and -2/2, as they were originally. 
		zoomOut.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				imagePanel.resizeOriginal();
			}

		});

		/*
		 * This uses the firstTime variable to check whether theres something in the combobox, after the first time there will
		 * always be something there so it's set to false. The combobox then uses the selected complex number to create a new
		 * Julia Frame.
		 */
		favourites.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				if(!firstTime){
					ComplexNumber selected = (ComplexNumber) favourites.getSelectedItem();
					JuliaFrame favJuliaFrame = new JuliaFrame(selected.toString(), selected);
				}
				firstTime = false;
			}
		});

		//Sets the setChooser variable in ImagePanel to the Fractal chosen in this combobox which then changes the fractal image.
		fractals.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				ImagePanel.setChooser = fractals.getSelectedItem().toString();
				repaint();
			}
		});

		//Changes the zoomIterations variable in ImagePanel to the number selected in this combobox so the zoom calculations changes. 
		zoomIt.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e){
				ImagePanel.zoomIterations = (int) zoomIt.getSelectedItem();
				repaint();
			}
		});
	}

	/**
	 * This class creates a new Error frame when there's no complex numbers in the favourite complex number arraylist, so therefore no
	 * Julia Sets can be exported. It's only ever created when the export button is clicked when it shouldnt be, and shows an error message
	 * for the user.
	 * @author chloeallan
	 */
	public class EFrame extends JFrame{

		public EFrame(String title){
			super(title);
			init();
		}

		/**
		 * This methods sets a JLabel showing an error message, as well as the size of the frame. 
		 */
		public void init(){

			setSize(600, 50);
			JPanel e = new JPanel();
			JLabel exceptionlabel = new JLabel();
			String exception ="No image found. Please select a Julia Set from the favourites list, or create a new favourite.";

			exceptionlabel.setText(exception);
			
		    setLocationRelativeTo(null);
			e.add(exceptionlabel);
			add(e);

			setVisible(true);
		}

	}

}
