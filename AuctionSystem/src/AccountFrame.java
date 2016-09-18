import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Date;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * This frame is created from the AuctionFrame when a client wants to view their account. It contains a JComboBox that can access
 * three parts of their account; open items, old items and their bids. The user's penalty points are also stored at the top
 * of this frame.
 * @author chloeallan
 *
 */
public class AccountFrame extends JFrame{
	String choosebox = "Open Items";

	JPanel panel;
	JPanel center;
	JPanel items;
	JPanel olditems;
	int penalty = 0;
	DataPersistence data = new DataPersistence();

	public AccountFrame(String title){
		super(title);
		init();
	}

	public void init(){

		center = new JPanel();
		panel = new JPanel();


		setContentPane(panel);
		panel.setLayout(new BorderLayout());

		setSize(400,350);
		setMinimumSize(new Dimension(500,400));
		setLocationRelativeTo(null);

		center = new JPanel (new BorderLayout());
		center.setMinimumSize(new Dimension(400,350));
		center.setPreferredSize(new Dimension(400,350));

		center.add(openBidsPanel());
		panel.add(center, BorderLayout.CENTER);

		//The item listener attached to the combobox will change the panel on the frame whenever the selected value
		//in the combobox is changed.
		String[] strings = {"Open Items", "Old Items", "My Bids"};
		JComboBox<String> choose = new JComboBox<String>(strings);
		choose.addItemListener(new ItemListener(){


			@Override
			public void itemStateChanged(ItemEvent e) {

				//When the section is chosen, the center panel has everything removed, then the new panel added to it. 
				if(e.getItem().equals("Open Items")){
					choosebox = "Open Items";
					center.removeAll();
					center.add(openBidsPanel());
					SwingUtilities.invokeLater(new Runnable(){
						public void run() {
							validate();
							repaint();
						}
					});
				} else if(e.getItem().equals("Old Items")){
					choosebox = "Old Items";
					center.removeAll();
					center.add(oldBidsPanel());
					SwingUtilities.invokeLater(new Runnable(){
						public void run() {
							validate();
							repaint();
						}
					});
				} else if(e.getItem().equals("My Bids")){
					choosebox = "My Bids";
					center.removeAll();
					JPanel bids = myBidsPanel();
					center.add(bids);
					SwingUtilities.invokeLater(new Runnable(){
						public void run() {
							validate();
							repaint();
						}
					});
				}	
				panel.add(center, BorderLayout.CENTER);
			}

		});

		int point = 0;
		try {
			//this returns the penalty point associated with the AuctionFrame's user
			point = data.getPenalty(AuctionFrame.userID);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		JLabel userpoint = new JLabel("Penalty Points: " + point + "    ");

		JPanel choosepanel = new JPanel();
		choosepanel.setLayout(new BorderLayout());
		choosepanel.add(choose, BorderLayout.LINE_START);
		choosepanel.add(userpoint, BorderLayout.LINE_END);
		panel.add(choosepanel, BorderLayout.NORTH);


		setVisible(true);
	}

	/**
	 * This method returns the scrollpanel associated with the open items for the user. The items are shown as buttons 
	 * that contain the item's title, reserve price, current bid and end time. I've also attached action listeners to each
	 * button so the user can withdraw their items by clicking on them. 
	 */
	private JScrollPane openBidsPanel(){
		items = new JPanel();
		JScrollPane itemscroll = new JScrollPane(items);
		itemscroll.setBorder(null);
		items.setMinimumSize(new Dimension(300, 350));
		items.setLayout(new BoxLayout(items, BoxLayout.PAGE_AXIS));

		JLabel itemtitle = new JLabel("<html><center> My Items: <br>");
		itemtitle.setHorizontalAlignment(JLabel.CENTER);
		itemtitle.setFont(new Font(itemtitle.getFont().getFontName(), Font.BOLD, 14));
		items.add(itemtitle);

		int num = 0;

		for(Item i : AuctionFrame.items){
			if(i.getUserID().equals(AuctionFrame.userID)){
				num++;
				JButton item;
				//The current bid on the button will be set to 0 if there is no current bid for that item.
				if(i.getCurrentBid() != null){
					item = new JButton("<html><center>" + i.getTitle() + "<br>" + "Reserve Price: \u00A3" + i.rPrice + "<br>" + 
							"Current Bid: \u00A3" + i.getCurrentBid().getBid() + "<br>"+ "Ends At: " + new Date(i.finish) + "<br>" + 
							"<b>Click to withdraw</b>");
				} else{
					item = new JButton("<html><center>" + i.getTitle() + "<br>" + "Reserve Price: \u00A3" + i.rPrice + "<br>" + 
							"Current Bid: \u00A30" + "<br>"+ "Ends At: " + new Date(i.finish) + "<br>" + "<b>Click to withdraw</b>");
				}

				item.setBorder(BorderFactory.createRaisedSoftBevelBorder());
				item.setPreferredSize(new Dimension(300,80));

				item.addActionListener(new ActionListener(){

					public void actionPerformed(ActionEvent e) {
						ObjectMessage message = new ObjectMessage(MessageType.PENALTY, i);
						Client.comms.sendMessage(message);
						dispose();
					}

				});

				items.add(item);
			}
		}

		items.setPreferredSize(new Dimension(300, num*100));

		return itemscroll;
	}

	/**
	 * This method does the same as the openBidsPanel(); it creates buttons associated with all the old items from that user
	 * (the ones in the finished list, rather than the items list). No action listeners are set to the buttons as the user
	 * doesn't want to do anything with the old items. 
	 */
	private JScrollPane oldBidsPanel(){
		olditems = new JPanel();
		JScrollPane itemscroll = new JScrollPane(olditems);
		itemscroll.setBorder(null);
		olditems.setMinimumSize(new Dimension(300, 350));
		olditems.setLayout(new BoxLayout(olditems, BoxLayout.PAGE_AXIS));

		JLabel itemtitle = new JLabel("<html><center> My Old Items: <br>");
		itemtitle.setHorizontalAlignment(JLabel.CENTER);
		itemtitle.setFont(new Font(itemtitle.getFont().getFontName(), Font.BOLD, 14));
		olditems.add(itemtitle);

		int num = 0;

		for(Item i : AuctionFrame.finished){
			if(i.getUserID().equals(AuctionFrame.userID)){
				num++;
				JButton item;
				//if the current bid greater than the reserve price, is displays "winning bid", else it just says "last bid"
				if(i.getCurrentBid() != null){
					if(i.getCurrentBid().getBid() > Double.valueOf(i.rPrice)){
						item = new JButton("<html><center>" + i.getTitle() + "<br>" + "Reserve Price: \u00A3" + i.rPrice + "<br>" + "Winning Bid: \u00A3" + 
								i.getCurrentBid().getBid() + ", From: " + i.getCurrentBid().getID() +  "<br>"+ "Ended At: " + new Date(i.finish));
					}
					else{
						item = new JButton("<html><center>" + i.getTitle() + "<br>" + "Reserve Price: \u00A3" + i.rPrice + "<br>" + "Last Bid: \u00A3" + 
								i.getCurrentBid().getBid() + ", From: " + i.getCurrentBid().getID() +  "<br>"+ "Ended At: " + new Date(i.finish));
					}
				} else{
					item = new JButton("<html><center>" + i.getTitle() + "<br>" + "Reserve Price: \u00A3" + i.rPrice + "<br>" + 
							"Last Bid: \u00A3" + "<br>"+ "Ended At: " + new Date(i.finish));
				}
				item.setBorder(BorderFactory.createRaisedSoftBevelBorder());
				item.setPreferredSize(new Dimension(300,80));
				olditems.add(item);
			}
		}

		olditems.setPreferredSize(new Dimension(300, num*100));

		return itemscroll;
	}

	/**
	 * This panel holds JLabels for each bid you have made on an item. To get the bids the method iterates through the items
	 * list in AuctionFrame and checks whether the current bid on those items was made by the user. If they were a new label
	 * is made displaying that bid. 
	 */
	private JPanel myBidsPanel(){
		JPanel bids = new JPanel();
		bids.setLayout(new BoxLayout(bids, BoxLayout.PAGE_AXIS));
		bids.setMinimumSize(new Dimension(200, 400));
		bids.setPreferredSize(new Dimension(200, 400));

		JLabel bidtitle = new JLabel("<html><center>My Bids: <br>");
		bidtitle.setFont(new Font(bidtitle.getFont().getFontName(), Font.BOLD, 14));
		bidtitle.setHorizontalAlignment(JLabel.CENTER);
		bids.add(bidtitle);

		for(Item i : AuctionFrame.items){
			if(i.getCurrentBid() != null){
				if(i.getCurrentBid().getID().equals(AuctionFrame.userID)){
					JLabel bidlabel = new JLabel("Bid on " + i.getTitle() + " for \u00A3" + i.getCurrentBid().getBid());
					JPanel bidpanel = new JPanel();
					bidpanel.add(bidlabel);
					bids.add(bidpanel);
				}
			}
		}

		return bids;
	}
}
