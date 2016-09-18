import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Once a user has logged in or registered from the initial Client frame, this frame is opened where the user can
 * see everything to do with the auction, create an item, see their account, see their notifications and deal with items.
 * At the bottom of this class there are 3 nested classes; two error frames that are thrown when a user tries to bid on their
 * own item or when the bid made is smaller than the current bid. 
 * The last nested class is a TimerTask for this class that counts down the time of each open item each second. 
 * @author chloeallan
 *
 */
public class AuctionFrame extends JFrame{

	static public String userID = null;
	static ArrayList<Item> items;
	static ArrayList<Item> finished;
	static JPanel scrollingPanel;
	JPanel itemDisplay;
	static JButton itemButton;
	static JPanel panel;
	DataPersistence dp = new DataPersistence();
	static ItemCreateFrame create;
	static AccountFrame accountframe;
	String category = "---";
	String searchCriteria = null;
	String buttonCriteria = null;
	JPanel information = null;
	Item buttonclick = null;
	JPanel bottom;
	static ArrayList<String> messages;
	static NotificationFrame notifyframe;
	static JPanel top;
	BufferedImage image;

	public AuctionFrame(String title, String newUserID){
		super(title);
		userID = newUserID;
		messages = new ArrayList<String>();

		this.addWindowListener(new WindowListener(){

			@Override
			public void windowOpened(WindowEvent e) {
			}

			//when the window is closed, the sever gets a message to say this client window has shut down 
			public void windowClosing(WindowEvent e) {
				Client.comms.sendMessage(new ObjectMessage(MessageType.SHUTDOWNCLIENT, new String(AuctionFrame.userID)));
				System.out.println("Closing client, sending message to server");
				System.exit(0);
			}
			@Override
			public void windowClosed(WindowEvent e) {
			}
			@Override
			public void windowIconified(WindowEvent e) {
			}
			@Override
			public void windowDeiconified(WindowEvent e) {
			}
			@Override
			public void windowActivated(WindowEvent e) {
			}
			@Override
			public void windowDeactivated(WindowEvent e) {
			}
		});

		//each time the auctionframe is opened, the item arraylist is created in datapersistence from the Items folder of open items
		ArrayList<Item> inputItems;
		try {
			inputItems = (ArrayList<Item>) dp.createItemList();
			items = new ArrayList<Item>(new LinkedHashSet<Item>(inputItems));
		} catch (IOException e) {
			e.printStackTrace();
		}

		//the same thing, the finished items is created from the Items/Finished folder 
		ArrayList<Item> finishedinput;
		try {
			finishedinput = (ArrayList<Item>) dp.createFinishedItemList();
			finished = new ArrayList<Item>(new LinkedHashSet<Item>(finishedinput));
		} catch (IOException e) {
			e.printStackTrace();
		}

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		init();
		setLocationRelativeTo(null);

		//a timer is scheduled for every second to run the accounttimer task at the bottom of the class
		Timer t = new Timer();
		TimerTask task = new AccountTimer();
		t.schedule(task, 0, 1000);
	}


	public void init(){

		panel = new JPanel();
		setContentPane(panel);
		setResizable(false);
		setMinimumSize(new Dimension(900,600));
		revalidate(); 

		panel.setLayout(new BorderLayout());

		top = initTop();
		JPanel center = initCenter();
		drawItems();

		panel.add(top, BorderLayout.NORTH);
		panel.add(center, BorderLayout.CENTER);

		setVisible(true);
	}

	/**
	 * This returns the JPanel associated with the top of the auction frame. It contains the search bar with attached combo
	 * box to search through the items, as well as the buttons to choose items of a specific category. My search bar has the 
	 * facility to search through the Item's title, seller, ID Code and find new matching keywords in the description. There's
	 * a button that when clicked, opens the notification frame for that current client sessions (this shows bids made on the users
	 * items, if an item has finished who won it etc). There's also a combobox that has the option to open the account frame to see
	 * open and old items, and bids the user has made, as well as open the item creation frame. 
	 */
	private JPanel initTop(){
		JPanel topPanel = new JPanel(new GridLayout(2,0));

		JPanel search = new JPanel();
		JLabel searchlabel = new JLabel("Search:");
		JTextField searchBar = new JTextField(20);
		String[] searches = {"---", "Item Title", "Seller", "ID Code", "Description"};
		JComboBox<String> searchbox = new JComboBox<String>(searches);
		JButton notify = new JButton("Notifications");
		notify.setBorder(null);
		notify.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				notifyframe = new NotificationFrame("Notifications");
			}

		});

		searchBar.getDocument().addDocumentListener(new DocumentListener(){

			public void insertUpdate(DocumentEvent e) {
				category = searchbox.getSelectedItem().toString();
				searchCriteria = searchBar.getText();
				drawItems();
				searchCriteria = null;						
			}

			public void removeUpdate(DocumentEvent e) {
				category = searchbox.getSelectedItem().toString();
				searchCriteria = searchBar.getText();
				drawItems();
				searchCriteria = null;	
			}

			public void changedUpdate(DocumentEvent e) {
			}

		});

		search.add(searchlabel);
		search.add(searchbox);
		search.add(searchBar);

		String[] accountString = {"Home", "My Account", "Create An Item"};
		JComboBox<String> account = new JComboBox(accountString);
		account.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				if(account.getSelectedItem().toString().equalsIgnoreCase("Create An Item")){
					create = new ItemCreateFrame("Create Item");
				}
				if(account.getSelectedItem().toString().equalsIgnoreCase("My Account")){
					accountframe = new AccountFrame("Account");
				}
			}

		});

		search.add(account);
		search.add(notify);

		JPanel catergories = new JPanel();
		JLabel catLabel = new JLabel("Categories: ");
		catergories.add(catLabel);
		JButton all = new JButton("All");
		all.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				category = "---";
				drawItems();
			}

		});
		catergories.add(all);
		JButton fashion = new JButton("Fashion");
		fashion.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				buttonCriteria = "Fashion";
				drawItems();
				buttonCriteria = null;
			}

		});
		catergories.add(fashion);
		JButton electronics = new JButton("Electronics");
		electronics.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				buttonCriteria = "Electronics";
				drawItems();
				buttonCriteria = null;
			}

		});
		catergories.add(electronics);
		JButton beauty = new JButton("Beauty");
		beauty.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				buttonCriteria = "Beauty";
				drawItems();
				buttonCriteria = null;
			}

		});
		catergories.add(beauty);
		JButton home = new JButton("Home");
		home.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				buttonCriteria = "Home";
				drawItems();
				buttonCriteria = null;
			}

		});
		catergories.add(home);
		JButton books = new JButton("Books");
		books.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				buttonCriteria = "Books";
				drawItems();
				buttonCriteria = null;
			}

		});
		catergories.add(books);
		JButton outdoors = new JButton("Outdoors");
		outdoors.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				buttonCriteria = "Outdoors";
				drawItems();
				buttonCriteria = null;
			}

		});
		catergories.add(outdoors);

		topPanel.add(search);
		topPanel.add(catergories);
		return topPanel;
	}

	/**
	 * This method returns the JPanel for the center of the auction frame where the items are displayed. There are two JPanels
	 * in the center panel; scrollingPanel which displays all the items down the left hand side and itemDiplay which, when an 
	 * item button is clicked, shows all the information and bids. Both panel's contents are drawn outside of this method.
	 */
	private JPanel initCenter(){
		scrollingPanel = new JPanel();
		JScrollPane scroll = new JScrollPane(scrollingPanel);
		scrollingPanel.setMinimumSize(new Dimension((this.getWidth()/10)*3, 600));
		scrollingPanel.setPreferredSize(new Dimension((this.getWidth()/10)*3, items.size()*120));
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		itemDisplay = new JPanel();
		itemDisplay.setBorder(BorderFactory.createLoweredBevelBorder());

		JPanel itemPanel = new JPanel(new BorderLayout());
		itemPanel.add(scroll, BorderLayout.WEST);
		itemPanel.add(itemDisplay, BorderLayout.CENTER);

		return itemPanel;
	}

	/**
	 * This method sorts through the list of items using a LinkedHashSet to get rid of duplicate items, and using an Item 
	 * Comparator so it sorts the items alphabetically in the list. The rest of the method iterates through the items and uses
	 * two switch statements that deals with the search bar and the buttons. The statements switch through the search combobox's
	 * value (set to instance variable category) and the button thats clicked (set to buttonClicked). Each one checks the variable in
	 * item that's associated with the search bar or the category of the item for the buttons and only displays the items that 
	 * match the criteria.
	 */
	private synchronized void drawItems(){

		items = new ArrayList<Item>(new LinkedHashSet<Item>(items));
		scrollingPanel.removeAll();
		itemDisplay.removeAll();

		Collections.sort(items, new Comparator<Item>(){

			public int compare(Item o1, Item o2) {
				return o1.getTitle().compareToIgnoreCase(o2.getTitle());
			}

		});

		for(Item i : items){
			switch(category){
			case "---": 
				if(buttonCriteria == null){
					buttonDrawing(i);
				}
				break;
			case "Item Title": 
				if(searchCriteria != null){
					if(i.getTitle().toLowerCase().contains(searchCriteria.toLowerCase())){
						buttonDrawing(i);
					}
				} 
				break;
			case "Seller":
				if(searchCriteria != null){
					if(i.getUserID().toLowerCase().contains(searchCriteria.toLowerCase())){
						buttonDrawing(i);
					}
				} 
				break;
			case "ID Code":
				if(searchCriteria != null){
					if(i.getID().toString().toLowerCase().contains(searchCriteria.toLowerCase())){
						buttonDrawing(i);
					}
				}
				break;
			case "Description":
				if(searchCriteria != null){
					if(i.getDescription().toString().toLowerCase().contains(searchCriteria.toLowerCase())){
						buttonDrawing(i);
					}
				} 
				break;
			}

			if(buttonCriteria != null){
				switch(buttonCriteria){
				case "Fashion":
					if(i.getCategory().equals(buttonCriteria)){
						buttonDrawing(i);
					}
					break;
				case "Electronics":
					if(i.getCategory().equals(buttonCriteria)){
						buttonDrawing(i);
					}
					break;
				case "Beauty":
					if(i.getCategory().equals(buttonCriteria)){
						buttonDrawing(i);
					}
					break;
				case "Home":
					if(i.getCategory().equals(buttonCriteria)){
						buttonDrawing(i);
					}
					break;
				case "Books":
					if(i.getCategory().equals(buttonCriteria)){
						buttonDrawing(i);
					}
					break;
				case "Outdoors":
					if(i.getCategory().equals(buttonCriteria)){
						buttonDrawing(i);
					}
					break;
				}
			}
		}

		scrollingPanel.validate();
		scrollingPanel.repaint();
	}

	/**
	 * This method draws the button for the item taken as a parameter and adds an actionlistener to it so when the button is 
	 * clicked the item panel for that item is displayed.
	 */
	private void buttonDrawing(Item i){
		itemButton = new JButton("<html><center>" + i.getTitle() +"<br>"+ "Created By: " + i.getUserID() + "<br>" + 
				"Reserve Price: \u00A3" + i.rPrice);
		itemButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				buttonclick = i;
				itemDisplay.removeAll();
				itemPanel(buttonclick);
				SwingUtilities.invokeLater(new Runnable(){
					public void run() {
						validate();
						repaint();
					}
				});
			}	
		});

		itemButton.setPreferredSize(new Dimension(((this.getWidth()/10)*3)-30, 100));
		scrollingPanel.add(itemButton);
	}

	/**
	 * This method displays the panel associated with the item taken in as a parameter. The top of the panel shows the title 
	 * description and the image for the item. I put the image creation in a new thread as it takes a second to display the
	 * image, and without the thread it made my itemPanel creation very slow. This way the panel is displayed quickly and
	 * the item comes up a second later, a much more realistic situation. Underneath this, a button to make a bid on the item
	 * is displayed.
	 * The lower half of the panel is split into two sides; one for the item's bids and the other that shows the information, and
	 * both panels are added to a 'bottom' panel so they are shown next to each other. The bid's panel iterates through the 
	 * items bid array and prints out all the bids amounts and who they were made by to jlabels. The information panel is dealt
	 * with in another method.
	 */
	private void itemPanel(Item item){

		JPanel display = new JPanel();
		display.setLayout(new BoxLayout(display, BoxLayout.PAGE_AXIS));

		JPanel description = new JPanel();

		JTextArea descrip = new JTextArea(5,17);
		descrip.setLineWrap(true);
		descrip.setWrapStyleWord(true);
		descrip.setText(item.getDescription());
		descrip.setEditable(false);
		descrip.setBackground(null);

		description.add(descrip);

		//the thread created to display the image
		Thread t = new Thread(new Runnable(){

			public void run(){
				if(item.getImagePath() != null && new File(item.getImagePath()).exists()){
					try {
						image = ImageIO.read(new File(item.getImagePath()));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					
					//the image is taken in and then scaled down to 150x150, so each image is the same size
					Image newimage = image.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
					ImageIcon newicon = new ImageIcon(newimage);

					JLabel piclabel = new JLabel(newicon);
					description.add(piclabel);
				}
			}
		});
		t.start();
		
		int panelwidth = (int) (itemDisplay.getWidth()/2.0);
		int panelheight = (int) (itemDisplay.getHeight()/1.6);

		JLabel title = new JLabel(item.getTitle());
		title.setFont(new Font(title.getFont().getFontName(), Font.BOLD, 14));
		JPanel titlep = new JPanel();
		titlep.add(title);

		JPanel bidding = new JPanel();
		JButton bid = new JButton("Add New Bid");
		JTextField bidprice = new JTextField(5);
		bidding.add(bidprice);
		bidding.add(bid);
		//actionlistener added to the bid button, so a message is sent to the server when a user wants to make a new bid.
		bid.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				Double price = Double.valueOf(bidprice.getText());
				if(item.getCurrentBid() != null){
					//checks whether the bid made is greater than the current bid on the item. An error frame is thrown is it is not.
					if(item.getCurrentBid().getBid() < price){
						if(item.getUserID().equals(userID)){
							//if the user tries to bid on their own item, an error frame is thrown.
							EFrame error = new EFrame("Bidding Error");
						}else{
							Bid bid = new Bid(price, userID, item);
							ObjectMessage message = new ObjectMessage(MessageType.NEWBID, bid);
							Client.comms.sendMessage(message);
						}
					} else{
						BidEFrame frame = new BidEFrame("Bid Price Error");
					}
				}
				//if there is no current bid on the item it simply sends the bid to the server.
				else{
					if(item.getUserID().equals(userID)){
						EFrame error = new EFrame("Bidding Error");
					}else{
						Bid bid = new Bid(price, userID, item);
						ObjectMessage message = new ObjectMessage(MessageType.NEWBID, bid);
						Client.comms.sendMessage(message);
					}
				}
			}

		});


		JPanel bids = new JPanel();
		bids.setLayout(new BoxLayout(bids, BoxLayout.PAGE_AXIS));
		bids.setPreferredSize(new Dimension((int) (itemDisplay.getWidth()/2.8), panelheight));

		JLabel bidlabel = new JLabel("<html><center>" + "Bids:" +"<br>");
		bidlabel.setHorizontalAlignment(JLabel.CENTER);
		bidlabel.setFont(new Font(bidlabel.getFont().getFontName(), Font.BOLD, 12));
		bids.add(bidlabel);

		for(Bid b : item.bids){
			JLabel text = new JLabel();
			text.setText("Bid of: \u00A3" + b.getBid() + ", From User: " + b.getID());
			bids.add(text);
		}

		bottom = new JPanel();
		information = new JPanel();
		bottom.add(bids);
		createInformation(item);

		display.add(titlep);
		display.add(Box.createRigidArea(new Dimension(0,20)));
		display.add(description);
		display.add(bidding);
		display.add(bottom);
		itemDisplay.add(display);
	}

	/**
	 * This method takes the item as a parameter and sets all the item's information to the panel. The panel also
	 * updates every second with the time, so the time counts down automatically on the screen. This uses the timeleft in the
	 * item class and converts it to the time using division. It then displays it as a jlabel.
	 */
	private void createInformation(Item item){
		int panelwidth = (int) (itemDisplay.getWidth()/2.0);
		int panelheight = (int) (itemDisplay.getHeight()/1.6);

		information.setLayout(new BoxLayout(information, BoxLayout.PAGE_AXIS));
		information.setPreferredSize(new Dimension(panelwidth, panelheight));

		JLabel infolabel = new JLabel("<html><center>" + "Item Information:" +"<br>");
		infolabel.setHorizontalAlignment(JLabel.CENTER);
		infolabel.setFont(new Font(infolabel.getFont().getFontName(), Font.BOLD, 12));

		JLabel userlabel = new JLabel("Created By " + item.getUserID());
		JLabel reserve = new JLabel("Reserve Price: \u00A3" + item.rPrice);
		JLabel idcode = new JLabel("Unique ID Code:");
		JLabel idcode2 = new JLabel("  " + item.getID());
		JLabel cater = new JLabel("Category: " + item.getCategory());

		long length = item.getTimeLeftSec();
		long hours;
		String mins, sec;
		String fmins = null, fsec = null; 
		if((hours = (length/3600)) < 0){
			hours = 0;
		}else{ 
			length = length % 3600;
		}
		if(((length/60)) < 0){
			mins = "00";
		}else{
			mins = String.valueOf(length/60);
			fmins = String.format("00", mins);
			length = length % 60;
		}
		sec = String.valueOf(length);
		fsec = String.format("00", sec);

		JLabel endtime = new JLabel("Time Till End: " + hours + ":" + mins+ ":" + sec);

		information.add(infolabel);
		information.add(userlabel);
		information.add(reserve);
		information.add(idcode);
		information.add(idcode2);
		information.add(cater);
		information.add(endtime);

		bottom.add(information);
	}

	public class EFrame extends JFrame{

		public EFrame(String title){
			super(title);
			init();
		}

		/**
		 * This methods sets a JLabel showing an error message, as well as the size of the frame. 
		 */
		public void init(){

			setSize(400, 50);
			JPanel e = new JPanel();
			e.setLayout(new GridBagLayout());
			JLabel exceptionlabel = new JLabel();
			String exception ="<html><center>Error. You may not bid on your own item.";

			exceptionlabel.setText(exception);

			setLocationRelativeTo(null);
			e.add(exceptionlabel);
			add(e);

			setVisible(true);
		}

	}

	public class BidEFrame extends JFrame{

		public BidEFrame(String title){
			super(title);
			init();
		}

		/**
		 * This methods sets a JLabel showing an error message, as well as the size of the frame. 
		 */
		public void init(){

			setSize(450, 50);
			JPanel e = new JPanel();
			e.setLayout(new GridBagLayout());
			JLabel exceptionlabel = new JLabel();
			String exception ="<html><center>Bid entered smaller than current bid, please try again";

			exceptionlabel.setText(exception);

			setLocationRelativeTo(null);
			e.add(exceptionlabel);
			add(e);

			setVisible(true);
		}
	}

	public class AccountTimer extends TimerTask{

		public void run() {
			for(Item i : items){
				i.timeleft -= 1000;
				if(i.equals(buttonclick)){
					if(information != null){
						information.removeAll();
						createInformation(buttonclick);
						SwingUtilities.invokeLater(new Runnable(){
							public void run() {
								validate();
								repaint();
							}
						});
					}
				}
			}
		}
	}

}
