import java.io.Serializable;
import java.util.ArrayList;

/**
 * The user class is serializable so it can be sent between the server and clients. A user takes their first and
 * last name, userID and password as parameters and sets them to instance variables. there's also an array of bids 
 * made by the user and a list of won items, and an instance variable for the user's penalty points. there are 
 * appropriate getters and setters in the class.
 * @author chloeallan
 *
 */
public class User implements Serializable{
	
	private static final long serialVersionUID = 1710497928954504867L;
	String firstName;
	String familyName;
	String userID;
	String password;
	String[] userBids;
	static ArrayList<Item> won = null;
	int penaltypoints = 0;
	
	public User(String gname, String fname, String userID, String password){
		this.firstName = gname;
		this.familyName = fname;
		this.userID = userID;
		this.password = password;
	}
	
	public String getName(){
		return firstName;
	}
	
	public String getFamName(){
		return familyName;
	}
	
	public String getUserId(){
		return userID;
	}
	
	public String getPassword(){
		return password;
	}
	
	public void addPenaltyPoint(){
		penaltypoints++;
	}
	
	public int getPenaltyPoint(){
		return penaltypoints;
	}
	
}
