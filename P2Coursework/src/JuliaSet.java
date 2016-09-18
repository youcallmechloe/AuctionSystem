import java.awt.Color;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;

/**
 * This class extends ImagePanel, so all the methods extend up to there. This class contains calculations to create a JuliaSet 
 * instead of a fractal. 
 * @author chloeallan
 *
 */
public class JuliaSet extends ImagePanel{

	private ComplexNumber complexNumber;
	BufferedImage image;

	public JuliaSet(ComplexNumber complex){
		super();
		complexNumber = complex;
	}

	public void addListener(){
		addMouseMotionListener(new MyMotionListener());
	}

	public void setComplex(ComplexNumber c){
		complexNumber = c;
	}

	public ComplexNumber getComplex(){
		return complexNumber;
	}

	//overwritten this method with an empty method because I don't want this to be an accessible method in the JuliaSet
	public void textBoundsChange(){

	}

	/**
	 * Overwritten the calculate method from ImagePanel so that it calculates a JuliaSet rather than one of the fractals. 
	 */
	public Integer calculate(Integer x, Integer y){
		ComplexNumber c = getComplex(x, y);
		ComplexNumber z = new ComplexNumber(c.getReal(), c.getImaginary());
		for(iterations = 0; iterations < 100; iterations++){
			if(z.modulus() < 4){
				z.square();
				z.add(getComplex());
			} else{
				return iterations;
			}
		}
		return iterations;
	}

	/**
	 * This method sets the JuliaSet image to the buffered image on the panel. It's been written here rather than extending from ImagePanel 
	 * so that I can return the image after its been set in the getImage method, that's then used to save the JuliaSets.
	 * <br>This method changes the colour of each pixel depending on the iteration (divergence) number. I've created a buffered image 
	 * to set the image to and used RGB colours to set the colour of the set.
	 */
	public void setImage(){
		image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);

		for(int x = 0; x < getWidth(); x++){
			for(int y = 0; y < getHeight(); y++){
				Integer num = calculate(x, y);
				if(num == 100){
					image.setRGB(x, y, Color.BLACK.getRGB());
				}else{
					int pixelColor = Color.HSBtoRGB((float) num/100, 1, 1);
					image.setRGB(x, y, pixelColor);
				}
			}
		}
	}
	
	public Image getImage(){
		setImage();
		return image;
	}


	class MyMotionListener implements MouseMotionListener{

		JLabel userSelectedPoint = new JLabel();

		/**
		 * Uses the mouse X and Y positions to get the complex number at the specific pixel and then calls toString to 
		 * print out the complex number on the screen as a JLabel (the user selected point). 
		 */
		public void mouseMoved(MouseEvent e) {
			userSelectedPoint.setText(getComplex(e.getX(), e.getY()).toString());
			add(userSelectedPoint);

			repaint();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			// TODO Auto-generated method stub
		}
	}
}
