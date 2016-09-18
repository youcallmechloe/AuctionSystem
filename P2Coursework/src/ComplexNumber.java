import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * This class contains all the necessary code needed for a complex number. The constructor takes in x and y values that are then set
 * to the real and imaginary parts of the complex number respectively. 
 * @author chloeallan
 */
public class ComplexNumber {

	//a complex number is made up of 2 components, a real and imaginary number z = u+v*j where u = real and v = imaginary
	//The instance variables are private and then getters are used to access them for better encapsulation. 
	private double real;
	private double imaginary;

	public ComplexNumber(double x, double y){
		this.real = x;
		this.imaginary = y;
	}

	public double getReal(){
		return real;
	}

	public double getImaginary(){
		return imaginary;
	}

	/**
	 * This method returns the modulus of the complex number, by finding the square root of the two parts squares, so using pythagoras' theorem 
	 * and the math methods to calculate it.
	 */
	public double modulus(){
		return Math.sqrt((real*real) + (imaginary * imaginary));
	}

	public double modulusSquared(){
		return modulus()*modulus();
	}

	/** 
	 * This method find the conjugate of a complex number by simply multiplying the imaginary by -1.
	 */
	public void conjugate(){
		imaginary = (imaginary * -1);

	}

	/**
	 * This method squares a complex number by finding new real and imaginary values for the squared number. It works with the multiplication
	 * (a+bj)(a+bj) = a^2 + 2abj +(bj)^2 where bj^2 = -b^2, so the real = a^2 - b^2 and imaginary = 2ab.
	 */
	public void square(){
		double thisReal = real, thisImaginary = imaginary; 
		real = (thisReal*thisReal)-(thisImaginary*thisImaginary);
		imaginary = (2*thisReal*thisImaginary);
	}

	/**
	 * This returns a string of the full complex number. It rounds the complex number to an appropriate amount of decimal points (4)
	 * and rounds it up, both using DecimalFormat. 
	 */
	public String toString(){
		DecimalFormat decimalFormat = new DecimalFormat("#.####");
		decimalFormat.setRoundingMode(RoundingMode.UP);
		return new String(String.valueOf(decimalFormat.format(getReal())) + " + " + String.valueOf(decimalFormat.format((getImaginary()*-1))) + "j");
	}

	/**
	 * This method adds a complex number d (taken as a parameter) to the complex number being handled, and returns a complex number 
	 * where addition: (x+yj) + (s+tj) = (x+s)+(y+t)j
	 */
	public void add(ComplexNumber d){
		real = real + d.getReal();
		imaginary = imaginary + d.getImaginary();
	}

}
