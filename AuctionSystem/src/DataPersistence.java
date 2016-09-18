import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;

/**
 * This class deals with all the persistence level methods; writing to files and reading from them for both users and items
 * as well as the winning log. I've generally used a buffered reader and filewriter to read and write to the files but have 
 * also used byte streams when dealing with images. 
 * @author chloeallan
 *
 */
public class DataPersistence {

	BufferedReader br = null;
	FileWriter outputwriter = null;
	File file;
	Integer[] bids;
	byte[] imagebyte;

	/**
	 * This method creates a new user file (when a user registers). It firsts checks whether a user file of that userID
	 * already exists and if it does it returns false (the registration has failed) but if it doesnt it goes on
	 * to create a new file with the userID as the filename. The filewriter then writes the user's name, userID,
	 * password and penalty points (they start of with 0) to a new line for each. This makes reading back from the
	 * file a lot easier.
	 */
	public boolean createUserFile(User user) throws IOException{

		File dir = new File("Users");
		if(!dir.exists()){
			dir.mkdirs();
		}

		for(File f : dir.listFiles()){
			if(!(f.isDirectory())){
				if((f.getName().equals(user.userID))){
					return false;
				}
			}
		}

		file = new File("Users/" + user.userID);
		try {
			outputwriter = new FileWriter(file);
			outputwriter.write(user.firstName + " " + user.familyName + "\n");
			outputwriter.write(user.userID + "\n");
			outputwriter.write(user.password + "\n");
			outputwriter.write("Penalty,0\n");
			//each user starts out with 0 penalty points
		} finally{
			if(outputwriter != null){
				outputwriter.close();
			}
		}
		return true;
	}

	/**
	 * Authenticate user is called when a user tries to log in. The userID and password are taken as parameters and then 
	 * the User folder is iterated through to see whether a user file exists for that user. If a file does exist the 
	 * buffered reader reads through the file and sets each line to a local variable newLine. If the line is the second
	 * one (where the userID should be) the local variable thisID is set to the line, and the same for the third line and
	 * the password. The local ID and password variables are then checked against the userID and password's taken in as
	 * the parameter and if they're correct the method returns true (login successful), else it returns false (login failed).
	 */
	public boolean authenticateUser(String userID, String password) throws IOException{
		File IDFile = null;

		int lines = 0;
		String thisID = null;
		String thispass = null;
		String newLine = null;

		File dir = new File("Users");
		if(!dir.exists())
			dir.mkdirs();

		for(File f : dir.listFiles()){
			if(!(f.isDirectory())){
				if(f.getName().equals(userID)){

					IDFile = new File(f.getPath());
					br = new BufferedReader(new FileReader("Users/"+ f.getName()));

					while((newLine = br.readLine()) != null){
						lines++;
						if(lines == 2){
							thisID = newLine; 
						} else if(lines == 3){
							thispass = newLine;
						}
					}
					
					br.close();

				}
			}
		}
		if(thisID == (null) && thispass == (null)){
			return false;
		} else if(thisID.equals(userID) && thispass.equals(password)){
			return true;
		} else{
			return false;
		}
	}

	/**
	 * To create an Item the item and userID are taken as parameters. This method first gets the penalty points for the user
	 * who's creating the item so it can check whether the user's points are less than 2. If the points are greater than two the 
	 * method returns false so the item cannot be created.
	 * The method uses a byte array input stream and bufferedimage to create an image file from the one selected by the user
	 * when they created the item. This bufferedimage is set to a file which is stored in the Items folder under the same name
	 * as the txt file for the item (the Item's generated ID).
	 * To create the Item file the filewriter is used to write each part of the item to a new line of the file. The last section
	 * of the file will be for the bids so a generic "Bids:" line is added to the file for when the bids are added later on. The method
	 * then returns true once the txt file is created so the item has been successful.
	 */
	public boolean createItemFile(Item item, String userID) throws IOException{

		int point = getPenalty(userID);

		File image = new File("Items/" + item.getID() + ".jpg");

		if(item.getImageBytes() != null){
			InputStream in = new ByteArrayInputStream(item.getImageBytes());
			BufferedImage BIimage = ImageIO.read(in);
			ImageIO.write(BIimage, "jpg", image);
			item.setImagePath(image.getPath());
		}

		if(point < 2){

			file = new File("Items/" + item.getID() + ".txt");
			try {
				outputwriter = new FileWriter(file);
				outputwriter.write("Item\n");
				outputwriter.write(item.title + "\n");
				outputwriter.write(item.description + "\n");
				outputwriter.write(item.categoryKey + "\n");
				outputwriter.write(item.start + "\n");
				outputwriter.write(item.finish + "\n");
				outputwriter.write(item.userID + "\n");
				outputwriter.write(item.rPrice + "\n");
				outputwriter.write(item.getID() + "\n");
				if(item.getImagePath() != null)
					outputwriter.write(image.getPath() + "\n");
				else
					outputwriter.write("\n");
				outputwriter.write("Bids:");
			} finally{
				if(outputwriter != null){
					outputwriter.close();
				}
			}

			return true;
		} else{
			return false;
		}
	}

	/**
	 * This method returns a List of items created from the items in the Items folder. The method iterates through the folder 
	 * checking that the files end with .txt (to get rid of the .jpg files). It then calls the getItem method which returns 
	 * an item from a file and adds that new item to the list (first checking the item doesn't already contain it).
	 */
	public List<Item> createItemList() throws IOException{
		List<Item> items = new ArrayList<Item>();

		File dir = new File("Items");
		if(!dir.exists())
			dir.mkdirs();


		for(File f : dir.listFiles()){
			if(!(f.isDirectory()) && f.getName().endsWith(".txt")){
				Item newItem = getItem(f);
				if(!items.contains(newItem)){
					items.add(newItem);
				}
			}
		}
		return items;
	}

	/**
	 * This method again returns a list of items, instead going through the Items/Finished folder (for items that have been 
	 * finished, either winning or failing, or been withdrawn). 
	 */
	public List<Item> createFinishedItemList() throws IOException{
		List<Item> items = new ArrayList<Item>();

		File dir = new File("Items/Finished");
		if(!dir.exists())
			dir.mkdirs();

		for(File f : dir.listFiles()){
			if(!(f.isDirectory()) && f.getName().endsWith(".txt")){
				Item newItem = getFinishedItem(f);
				if(!items.contains(newItem)){
					items.add(newItem);
				}
			}
		}
		return items;
	}


	/**
	 * This method is called each time a new bid is added to an Item. It takes the bid as a parameter (which holds the bid amount,
	 * item and userID of the bidder) and returns the item with the bid added. The method iterates through the Items folder to find 
	 * the file which contains the item and gets the file associated with that path. the filewriter then writes the new bid amount
	 * and userID to the end of the file (parameter true in the filewriter means the writer will append the text already there). Once
	 * the bid has been added the writer is closed, the current bid on the item set to that bid and the Item is returned using getItem().
	 */
	public Item addBid(Bid bid) throws IOException{
		File dir = new File("Items");
		if(!dir.exists())
			dir.mkdirs();

		File file;
		for(File f : dir.listFiles()){
			if(!(f.isDirectory())){
				if(f.getName().equals(String.valueOf(bid.getItem().getID()+".txt"))){

					file = new File(f.getPath());
					br = new BufferedReader(new FileReader("Items/"+ f.getName()));
					outputwriter = new FileWriter(file, true);

					try {
						outputwriter.write("\n" + String.valueOf(bid.getBid() +","+ bid.getID()));
					} finally{
						if(outputwriter != null){
							outputwriter.close();
						}
					}
					bid.getItem().setCurrentBid(bid);
					br.close();
					return getItem(f);
				}
			}
		}
		return null;
	}

	/**
	 * This method returns an item when a file is taken in as a parameter. A buffered reader is used to read in the file. The first line 
	 * in an Item file is always "Item" so there's an if statement to check that the first line is that, to check it is definitely an Item. 
	 * A new Item is then created from the rest of the lines in the file (ordered so the parameters of an Item match up with the Item file) 
	 * and the item's image path is set to the last line before the bids (if there is no image for the item, a blank line is left). 
	 * The method then checks that the image to that path exists and then creates the byte array for that image to be set to the item. 
	 * The last part of this method sets the bids to the item from the file. It checks whether the next line is equal to bids, and then 
	 * the lines from then on are split by a "," so the bid amount and bidder can be accessed separately. That bid is then set to
	 * the current bid and added to an array of bids in the item. This is done for each line to make sure that the most current 
	 * and highest bid is set to the item. 
	 */
	public Item getItem(File f) throws IOException{
		String newLine = null;

		br = new BufferedReader(new FileReader(f));
		if((newLine = br.readLine()) != null){
			if(newLine.equals("Item")){
				Item item = new Item(br.readLine(), br.readLine(), br.readLine(), Long.parseLong(br.readLine()), Long.parseLong(br.readLine()),
						br.readLine(), br.readLine(), UUID.fromString(br.readLine()));
				item.setImagePath(br.readLine());

				if(new File(item.getImagePath()).exists()){
					BufferedImage originalImage = ImageIO.read(new File(
							item.getImagePath()));

					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ImageIO.write(originalImage, "jpg", baos);
					baos.flush();
					imagebyte = baos.toByteArray();
					baos.close();

					item.setImageBytes(imagebyte);
				}

				if(br.readLine().equals("Bids:")){
					while((newLine = br.readLine()) != null){
						String[] parts = newLine.split(",");
						Bid newBid = new Bid(Double.valueOf(parts[0]), parts[1], item);
						item.bids.add(newBid);
						item.setCurrentBid(newBid);
					}
				}
				return item;
			}
			return null;
		}
		return null;
	}

	/**
	 * This method does the same as getItem() but for a finished item. The only difference being the folder it goes through
	 * and it doesn't access any images, as there are no images associated with finished items.
	 */
	public Item getFinishedItem(File f) throws IOException{
		String newLine = null;

		br = new BufferedReader(new FileReader("Items/Finished/"+ f.getName()));
		if((newLine = br.readLine()) != null){
			if(newLine.equals("Item")){
				Item item = new Item(br.readLine(), br.readLine(), br.readLine(), Long.parseLong(br.readLine()), Long.parseLong(br.readLine()),
						br.readLine(), br.readLine(), UUID.fromString(br.readLine()));
				if(br.readLine().equals("Bids:")){
					while((newLine = br.readLine()) != null){
						String[] parts = newLine.split(",");
						Bid newBid = new Bid(Double.valueOf(parts[0]), parts[1], item);
						item.bids.add(newBid);
						item.setCurrentBid(newBid);
					}
				}
				return item;
			}
			return null;
		}
		return null;
	}

	/**
	 * When an item finished this method is called from the Server to move the item from the Items folder to the Finished 
	 * folder. It iterates through the Items folder and find the .txt file associated with the item taken as a parameter. The 
	 * method then uses Files.move() to move the file from that path to the path associated with the finished folder. I've used
	 * StandardCopyOption.REPLACE_EXISTING to make sure only one copy of the file will exist, and the new one will replace the
	 * old one. 
	 */
	public void moveFinishedItem(Item item){
		File dir = new File("Items");
		if(!dir.exists())
			dir.mkdirs();

		for(File f : dir.listFiles()){
			if(!(f.isDirectory())){
				if(f.getName().equals(String.valueOf(item.getID() + ".txt"))){
					try {
						Files.move(f.toPath(), new File("Items/Finished/" + f.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
						f.delete();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else if(f.getName().equals(String.valueOf(item.getID() + ".jpg"))){
					f.delete();
				}
			}
		}
	}

	/**
	 * This method is called when an Item has been won in an auction and the item is added to the winning log. The filewriter
	 * appends the items details (title, bidder, seller and bid amount) to the end of the file to be used in the table.
	 */
	public void writeToWinLog(Item item) throws IOException{
		File log = new File("WinningLog");
		if(!log.exists()){
			try {
				log.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} 

		try {
			outputwriter = new FileWriter(log, true);
			outputwriter.write(item.getTitle() + "," + item.getCurrentBid().getID() + "," + item.getUserID() + "," + item.getCurrentBid().getBid()+"\n");
		} finally{
			if(outputwriter != null){
				outputwriter.close();
			}
		}
	}

	/**
	 * This method returns a 2D string array of the information from the winninglog to be used to write the winnning table
	 * in the sever GUI. A buffered reader goes through each line of the file and splits each line by "," and adds it to the 2D 
	 * array. If nothing exists for the winninglog it returns an empty 2D array so the table will still be displayed with
	 * nothing in it.
	 */
	public String[][] writeTable() throws IOException{

		String[][] rows = new String[200][4];
		String string;
		int line = 0;

		File log = new File("WinningLog");

		if(log.exists()){

			br = new BufferedReader(new FileReader("WinningLog"));

			while((string = br.readLine()) != null){
				String[] info = string.split(",");
				rows[line][0] = info[0];
				rows[line][1] = info[1];
				rows[line][2] = info[2];
				rows[line][3] = info[3];

				line++;
				
			}
			br.close();
			return rows;
		} else{
			return new String[10][4];
		}
	}

	/**
	 * When a user withdraws an item before the end time, and the current bid is higher than the reserve price, this method
	 * is called to add a penalty point to the user. The ID is taken as a parameter to find the file associated to that user.
	 * The User's file is found, and each line is added to a new string, until the Penalty line is found. That line is then
	 * split up and the points (second part of the line) is set to a local variable. replaceAll is then called on the total
	 * string of the file, and this replaces the penalty line with the same line but with 1 more penalty point. A 
	 * fileoutputstream is then used to write the file string back to the file. The point is then returned.
	 */
	public int addPenalty(String userID) throws IOException{
		File dir = new File("Users");
		String file = "";
		String newLine = null;
		String penaltyline = null;
		String[] line = null;
		int point = 0;

		for(File f : dir.listFiles()){
			if(!(f.isDirectory())){
				if(f.getName().equals(userID)){

					br = new BufferedReader(new FileReader("Users/" + userID));
					while((newLine = br.readLine()) != null){
						file += newLine + System.lineSeparator();
						if(newLine.startsWith("Penalty")){
							penaltyline = newLine;
							line = newLine.split(",");
							point = Integer.valueOf(line[1]);
						}
					}

					file = file.replaceAll(penaltyline, "Penalty," + (point+1));

					FileOutputStream os = new FileOutputStream("Users/" + userID);
					os.write(file.getBytes());

					os.close();

					return point;
				}
			}
		}
		return -1;
	}

	/**
	 * This method goes through the file associated with the userID taken as a parameter and returns the penalty point
	 * in that file. It uses a buffered reader to find the line beginning with Penalty and splits it up so the point 
	 * can be returned.
	 */
	public int getPenalty(String userID) throws IOException{

		String newLine = null;
		String[] line = null;
		int point = 0;

		File dir = new File("Users");
		for(File f : dir.listFiles()){
			if(!(f.isDirectory())){
				if(f.getName().equals(userID)){

					br = new BufferedReader(new FileReader("Users/" + userID));
					while((newLine = br.readLine()) != null){
						if(newLine.startsWith("Penalty")){
							line = newLine.split(",");
							point = Integer.valueOf(line[1]);
							return point;
						}
					}
				}

			}
		}
		return -1;
	}

	/**
	 * This method does the same as the image section in createItemFile, but it creates an image that can be stored on the Client's
	 * computer so it can be accessed without going through the Server. It takes the Item as a parameter and creates the .jpg image
	 * associated with the byte array stored in the item. 
	 */
	public void createClientImage(Item item) throws IOException{
		File dir = new File("ClientItems");
		if(!dir.exists())
			dir.mkdirs();

		File image = new File("ClientItems/" + item.getID() + ".jpg");

		if(item.getImageBytes() != null){
			InputStream in = new ByteArrayInputStream(item.getImageBytes());
			BufferedImage BIimage = ImageIO.read(in);
			ImageIO.write(BIimage, "jpg", image);
		}
	}
}
