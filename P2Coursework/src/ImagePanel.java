import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.swing.Timer;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * This class is the main class for the Image Panel that's displayed on the main frame. It contains all the calculations and colouring
 * needed to display the different fractals on the panel.  
 * @author chloeallan
 */
public class ImagePanel extends JPanel{

	double realMin, realMax;
	double imagMin, imagMax;
	private int firstX, firstY;
	private int lastX, lastY;
	private int finalX, finalY;
	private int zoomHeight, zoomWidth;
	private Boolean mouseclicked = false;
	private Boolean rectangleClicked = false;
	private BufferedImage image;

	public int iterations = 0;
	public int iterationSlider = 100;
	
	//These instance variables are static because they need to be accessed by the MyFrame class to set values to them when buttons are clicked.
	protected static JuliaFrame thisJuliaFrame;
	static JLabel userSelectedPoint = new JLabel();
	public static String setChooser = "Mandelbrot Set";
	public static int zoomIterations = 1;

	public ImagePanel(){

		addListener();
		
		realMin = -1.6;
		realMax = 1.6;
		imagMin = -2;
		imagMax = 2;

		iterationSlider = 100;

	}

	/**
	 * Adds the two listeners (MouseListener and MouseMotionListener) to the panel.
	 */
	public void addListener(){
		addMouseMotionListener(new MyMotionListener());
		addMouseListener(new MyMotionListener());
	}

	/**
	 * paintComponent contains the code to originally create the imagepanel, and then whenever repaint is called, this code is run again.
	 * <br> It creates the image by a new buggered image and then calls setThread on it which does all the calculations and colouring. The 
	 * image is then drawn using Graphics2D. 
	 */
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D g2D = (Graphics2D)g;

		image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
		setThread();
		g2D.drawImage(image, 0, 0, null);

		textBoundsChange();

		int x;
		int y;

		//uses an instance variable thats only set to true once the mouse has been released, so a rectangle is only drawn then.
		if(rectangleClicked){
			if(firstX < lastX){
				x = firstX;
			} else{
				x = lastX;
			}
			if(firstY < lastY){
				y = firstY;
			} else{
				y = lastY;
			}
			g2D.setColor(Color.gray);
			g2D.drawRect(x, y, Math.abs(zoomWidth), Math.abs(zoomHeight));
		}
	}

	/**
	 * This method changes the bounds when the values from the iteration slider and textfield are changed. 
	 */
	public void textBoundsChange(){

		//DecimalFormat is used to round the numbers up to 4 decimal places.
		DecimalFormat decimalFormat = new DecimalFormat("#.####");
		decimalFormat.setRoundingMode(RoundingMode.UP);

		MyFrame.minX.setText(decimalFormat.format(realMin) + "");
		MyFrame.maxX.setText(decimalFormat.format(realMax) + "");
		MyFrame.minY.setText(decimalFormat.format(imagMin) + "");
		MyFrame.maxY.setText(decimalFormat.format(imagMax) + "");
		MyFrame.itNum.setText(iterationSlider + "");

		//calling repaint so the image will change 
		MyFrame.minX.repaint();
		MyFrame.maxX.repaint();
		MyFrame.minY.repaint();
		MyFrame.maxY.repaint();
		MyFrame.itNum.repaint();

	}

	/**
	 * sets the bounds back to what they originally started as.
	 */
	public void resizeOriginal(){
		realMin = -1.6;
		realMax = 1.6;
		imagMin = -2;
		imagMax = 2;

		repaint();
	}

	/**
	 * This method gets the panel position of the complex number. X and y together create a pixel on the screen and you want a complex number for each pixel. 
	 * To find position of the complex number you find the total complex length (realMax-realMin) and divide it by the width of the panel, then times that by 
	 * the x or y value of the complex number and add the real/imaginary minimum length to it to get it to the panel size.
	 */
	public ComplexNumber getComplex(Integer x, Integer y){
		return new ComplexNumber((realMin + x * (realMax-realMin)/(getWidth())), (imagMin + y * (imagMax-imagMin)/(getHeight())));
	}

	/**
	 * This method changes the colour of each pixel depending on the iteration (divergence) number. I've created a buffered image 
	 * to set the image to and used RGB colours to set the colour of the set. 
	 * <br>Synchronized is used so that the threading runs properly when setting the image.
	 */
	public void setImage(int height){

		synchronized(image){
			for(int x = 0; x < getWidth(); x++){
				for(int y = ((getHeight() - height)); y < height; y++){
					Integer num = calculate(x, y);
					if(num == iterationSlider){
						image.setRGB(x, y, Color.BLACK.getRGB());
					}else{
						int pixelColor = Color.HSBtoRGB((float) num/iterationSlider, 1, 1);
						image.setRGB(x, y, pixelColor);
					}
				}
			}
		}
	}

	/**
	 * This method creates new threads for the image to be created with. The amount of threads can be changed through threadNum, and the iterations
	 * change accordingly to that. The threads are all added to an arraylist so that it can be iterated over and check which ones are 'alive' and once 
	 * the amount of threads alive matches threadNum then they're painte.d 
	 */
	public void setThread(){
		ArrayList<Thread> threads = new ArrayList<Thread>();
		int threadNum = 4;
		
		for(int i = 1; i < threadNum + 1; i++){
			final int j = i;
			Thread t = new Thread(){
				public void run(){
					setImage((getHeight()/threadNum) * j);
				}
			};
			threads.add(t);
			t.start();
		}

		boolean escape = false;

		while(!escape){
			int i = 0;

			for(Thread t : threads){
				if(!t.isAlive()){
					i++;
				}
				if(i == threads.size()){
					escape = true;
				}
			}
		}

	}

	/**
	 * This method calculates the divergence number needed for each pixel in the fractal. The method takes parameters
	 * x and y that are used to create the complex number c, then uses that to create another new complex number that's 
	 * then used as z in the fractal equation. The complex number z is then iterated over to find divergence.
	 * <br>A switch statement is originally used with setChooser to decide which calculation needs doing.
	 */
	public Integer calculate(Integer x, Integer y){
		ComplexNumber c = getComplex(x, y);

		switch(setChooser){
		case "Burning Ship Fractal": 
			ComplexNumber z = new ComplexNumber(0, 0);
			for(iterations = 0; iterations < iterationSlider; iterations++){
				ComplexNumber z1 = new ComplexNumber(Math.abs(z.getReal()), Math.abs(z.getImaginary()));
				z = z1;
				if(z.modulusSquared() < 4){
					z.square();
					z.add(c);
				}else{
					return iterations;
				}
			}
			return iterations;

		case "Tricorn Fractal":
			ComplexNumber z2 = new ComplexNumber(c.getReal(), c.getImaginary());
			for(iterations = 0; iterations < iterationSlider; iterations++){
				if(z2.modulusSquared() < 4){
					z2.conjugate();
					z2.square();
					z2.add(c);
				} else{
					return iterations;
				}
			}
			return iterations;

		default:

			ComplexNumber z3 = new ComplexNumber(c.getReal(), c.getImaginary());
			for(iterations = 0; iterations < iterationSlider; iterations++){
				if(z3.modulusSquared() < 4){
					z3.square();
					z3.add(c);
				} else{
					return iterations;
				}
			}
			return iterations;
		}

	}

	public JuliaFrame getThisJuliaFrame() {
		return thisJuliaFrame;
	}

	class MyMotionListener implements MouseMotionListener, MouseListener{

		/**
		 * Uses the mouse X and Y positions to get the complex number at the specific pixel and then calls toString to 
		 * print out the complex number on the screen as a JLabel (the user selected point). 
		 */
		public void mouseMoved(MouseEvent e) {
			userSelectedPoint.setText(getComplex(e.getX(), e.getY()).toString());
			
			//If the variable mouseclicked is true, then the JuliaFrame will automatically update 
			if(mouseclicked){
				JuliaFrameMovement(e);
			}
		}

		/**
		 * When the mouse is clicked on a certain point on the screen, it checks whether there's an existing Julia Frame or not,
		 * if there isnt then a new one is created and if there is then JuliaFrameMovement is called. 
		 * <br>The boolean mouseclicked is set to the opposite of mouseclicked in this method so that the automatic update of the julia
		 * frame when the mouse moved can be turned on and off by a click of the mouse.
		 */
		public void mouseClicked(MouseEvent e) {
			userSelectedPoint.setText(getComplex(e.getX(), e.getY()).toString());

			mouseclicked = !mouseclicked;
			if(thisJuliaFrame == null){
				thisJuliaFrame = new JuliaFrame("Julia Set", getComplex(e.getX(), e.getY()));
			} else{
				JuliaFrameMovement(e);
			}
		}

		/**
		 * If there is an existing julia frame then the julia frame will update every time the x and y values change, so when the mouse
		 * moves. This creates a different julia image because theres a different complex number in each pixel. 
		 */
		public void JuliaFrameMovement(MouseEvent e){
			if(thisJuliaFrame != null){
				thisJuliaFrame.imagePanel.setComplex(getComplex(e.getX(), e.getY()));
				thisJuliaFrame.validate();
				thisJuliaFrame.repaint();
			}
		}

		/**
		 * This method draws a rectangle whenever the mouse is dragged. It changes the values of the lastX and lastY value to whenever the 
		 * mouse is, so the reactangle will change size each time the mouse is dragged. It also calculates the zoomHeight and zoomWidth needed
		 * for the zoom method using the firstX/firstY and lastX/lastY. This sets the boolean rectangleClicked to true so the rectangle is drawn.
		 */
		public void mouseDragged(MouseEvent e) {

			lastX = e.getX();
			lastY = e.getY();

			if(firstX < lastX){
				zoomWidth = (lastX - firstX);
			} else {
				zoomWidth = (firstX - lastX);
			}
			if(firstY < lastY){
				zoomHeight = ((getHeight() - (getHeight() - lastY)) - firstY);
			} else {
				zoomHeight = (firstY - (getHeight() - (getHeight() - lastY)));
			}

			rectangleClicked = true;
			repaint();

		}

		/**
		 * Wherever the mouse is pressed, it sets the firstX and firstY values to those X and Y values, as that would be the start of a 
		 * rectangle or zoom area. 
		 */
		public void mousePressed(MouseEvent e) {
			firstX = e.getX();
			firstY = e.getY();
			repaint();
		}

		/**
		 * When the mouse is released, the image will change/zoom in to the area of the rectangle. As the mouse is released, variables 
		 * finalX and finalY are set to those X and Y values. A check is done to make sure that firstX doesnt equal finalX, so to make 
		 * sure an actual rectangle has been drawn. Then complex numbers are created using the first and last X and Y values so the differnce
		 * between them and the current image min/max's can be found. zoomIterations is used to change the amount of smaller rectangles that 
		 * are created, so to give an animated zoom. A timer is then used for the animated zoom to add/subtract the differences to create 
		 * smaller rectangles (and increments i each time it does), and will stop once i = zoomIterations, so the final image has been created.
		 */
		public void mouseReleased(MouseEvent e) {
			finalX = e.getX();
			finalY = e.getY();

			if(firstX != finalX){

				ComplexNumber firstComplex = getComplex(firstX, firstY);
				ComplexNumber finalComplex = getComplex(lastX, lastY);

				double realMinDiff, imagMinDiff;
				double realMaxDiff, imagMaxDiff;

				if(firstX < lastX){
					realMinDiff = (firstComplex.getReal() - realMin)/(zoomIterations);
					realMaxDiff = (realMax - finalComplex.getReal())/(zoomIterations);
				} else{
					realMinDiff = (finalComplex.getReal() - realMin)/(zoomIterations);
					realMaxDiff = (realMax - firstComplex.getReal())/(zoomIterations);
				}

				if(firstY < lastY){
					imagMinDiff = (firstComplex.getImaginary() - imagMin)/(zoomIterations);
					imagMaxDiff = (imagMax - finalComplex.getImaginary())/(zoomIterations);
				} else{
					imagMinDiff = (finalComplex.getImaginary() - imagMin)/(zoomIterations);
					imagMaxDiff = (imagMax - firstComplex.getImaginary())/(zoomIterations);
				}

				Timer timer = new Timer(20, new ActionListener(){
					int i = 0;

					public void actionPerformed(ActionEvent e) {
						i++;

						realMin += realMinDiff;
						realMax -= realMaxDiff;
						imagMin += imagMinDiff;
						imagMax -= imagMaxDiff;
						repaint();	

						if(i == zoomIterations){
							((Timer) e.getSource()).stop();
						}
					}
				});
				timer.start();
			}
			rectangleClicked = false;
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
		}

	}
}