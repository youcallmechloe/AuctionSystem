import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Item implements serializable as it needs to be sent between the sockets. The class contains getters and setters
 * for different elements of the item to be used when item is being displayed etc. 
 * @author chloeallan
 *
 */
public class Item implements Serializable{

	private static final long serialVersionUID = -641402287337099915L;
	String title;
	String description;
	static public String[] categories = {"Fashion", "Electronics", "Beauty", "Home", "Books", "Outdoors"};
	String categoryKey;
	String userID;
	long start;
	long finish;
	String rPrice;
	ArrayList<Bid> bids = new ArrayList<Bid>();
	Bid currentBid;
	UUID ID;
	long timeleft = 0;
	byte[] image;
	String imagepath;
	
	/**
	 * The constructor takes what is needed to create an item to begin with and sets it to instance variables
	 * in the item.
	 */
	public Item(String title, String description, String categoryKey, long start, long finish, String userID, 
			String rPrice, UUID ID){
		this.title = title;
		this.description = description;
		this.categoryKey = categoryKey;
		this.userID = userID;
		this.start = start;
		this.finish = finish;
		this.rPrice = rPrice;
		this.ID = ID;
		this.timeleft = finish - new Date().getTime();
	}
	
	public String getTitle(){
		return title;
	}
	
	public String getDescription(){
		return description;
	}
	
	public long getTimeLeftSec(){
		return timeleft/1000;
	}
	
	public String getCategory(){
		return categoryKey;
	}
	
	public String getUserID(){
		return userID;
	}
	
	public long getStart(){
		return start;
	}
	
	public long getFinish(){
		return finish;
	}
	
	public UUID getID(){
		return ID;
	}
	
	public void setCurrentBid(Bid bid){
		currentBid = bid;
	}
	
	public Bid getCurrentBid(){
		return currentBid;
	}
	
	public void setImageBytes(byte[] image){
		this.image = image;
	}
	
	public byte[] getImageBytes(){
		return image;
	}
	
	public void setImagePath(String path){
		imagepath = path;
	}
	
	public String getImagePath(){
		return imagepath;
	}

}
