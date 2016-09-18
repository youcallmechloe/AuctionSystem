import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * This frame is created when the user selected to create a new Item. This class holds everything to send a message to
 * the server to create a new item for the auction.
 * At the bottom of the class there's a nested error frame class that creates an error frame when the ITEMFAIL message 
 * is returned as the user has more than 2 penalty points.
 * @author chloeallan
 *
 */
public class ItemCreateFrame extends JFrame {
	
	byte[] imagebyte;

	public ItemCreateFrame(String title){
		super(title);
		init();
	}

	private void init(){
		setSize(400,350);
		setMinimumSize(new Dimension(400,400));
		setLocationRelativeTo(null);
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		add(panel);

		JTextField titleField = new JTextField(20);
		JLabel titleLabel = new JLabel("Item Name: ");
		JPanel titlePanel = new JPanel();
		titlePanel.add(titleLabel);
		titlePanel.add(titleField);

		JTextField descriptionField = new JTextField(30);
		JLabel descriptionLabel = new JLabel("Item Description: ");
		JPanel descriptionPanel = new JPanel();
		descriptionPanel.setPreferredSize(new Dimension(400,50));
		descriptionPanel.add(descriptionLabel);
		descriptionPanel.add(descriptionField);

		JComboBox choosecat = new JComboBox(Item.categories);
		JLabel catLabel = new JLabel("Choose The Item's Category");
		JPanel categoryPanel = new JPanel();
		categoryPanel.add(catLabel);
		categoryPanel.add(choosecat);

		JTextField priceField = new JTextField(10);
		JLabel priceLabel = new JLabel("Reserve Price:");
		JPanel pricePanel = new JPanel();
		pricePanel.add(priceLabel);
		pricePanel.add(priceField);

		JTextField userID = new JTextField(10);
		userID.setText(AuctionFrame.userID);
		userID.setEditable(false);
		JLabel userIDLabel = new JLabel("User ID:");
		JPanel userIDPanel = new JPanel();
		userIDPanel.add(userIDLabel);
		userIDPanel.add(userID);

		String[] years = {"2015", "2016"};
		JComboBox year  = new JComboBox(years);
		String[] months = {"January", "Febuary", "March", "April", "May", "June", "July", "August", "September", 
				"October", "November", "December"};
		JComboBox month  = new JComboBox(months);
		String[] days = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18",
				"19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"};
		JComboBox day  = new JComboBox(days);

		JTextField endTime = new JTextField(5);
		//the format of the time must be in the HH:mm format to be accepted by the date format. 
		JLabel endLabel = new JLabel("End Time (Format HH:mm): ");
		JPanel timeLabelP = new JPanel();
		JPanel timePanel = new JPanel();
		timeLabelP.add(endLabel);
		timePanel.add(endTime);
		timePanel.add(day);
		timePanel.add(month);
		timePanel.add(year);

		JButton imagebutton = new JButton("Choose An Image");
		//This actionlistener uses a filechooser to choose an image file for the item. the image is then read and a 
		//byte array output stram and ImageIO is used to get the byte array for the image to send to the server. 
		imagebutton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				int returnValue = chooser.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File f = chooser.getSelectedFile();
					try {
						BufferedImage originalImage = ImageIO.read(f);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						ImageIO.write(originalImage, "jpg", baos);
						baos.flush();
						imagebyte = baos.toByteArray();
						baos.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}				
				}
			}

		});

		JButton createItem = new JButton("Create Item");
		//when createitem is clicked a new Item variable is created from the details entered into the frame and sent to the server.
		createItem.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
				UUID ID = UUID.randomUUID();
				String title = titleField.getText();
				String description = descriptionField.getText();
				long end = 0;
				Integer monthitem = 0;
				for(int i = 0; i < months.length; i++){
					if(months[i].equals(month.getSelectedItem())){
						monthitem = i+1;
					}
				}

				try {
					end = dateFormat.parse(monthitem + "/" + day.getSelectedItem() + "/" + year.getSelectedItem() + " " +
							endTime.getText()).getTime();
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
				String price = priceField.getText();
				String catergory = choosecat.getSelectedItem().toString();
				Item item = new Item(title, description, catergory, new Date().getTime(), end, 
						AuctionFrame.userID, price, ID);
				item.setImageBytes(imagebyte);
				ObjectMessage message = new ObjectMessage(MessageType.ITEM, item);
				Client.comms.sendMessage(message);
				Message returnedM = Client.comms.listenForMessage();
				//if the returned messagetype is ITEMFAIL there has been a problem with the item and the error page is shown.
				if(returnedM.type == MessageType.ITEMFAIL){
					EFrame frame = new EFrame("Item Error");
				}
			}

		});
		JPanel button = new JPanel();
		button.add(imagebutton);
		button.add(createItem);
		
		JPanel message = new JPanel();
		JLabel label = new JLabel("<html><center> To withdraw this item go to your account page.");
		message.add(label);

		panel.add(titlePanel);
		panel.add(descriptionPanel);
		panel.add(pricePanel);
		panel.add(categoryPanel);
		panel.add(userIDPanel);
		panel.add(timeLabelP);
		panel.add(timePanel);
		panel.add(button);
		panel.add(message);

		setVisible(true);
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

			setSize(400, 100);
			JPanel e = new JPanel();
			e.setLayout(new GridBagLayout());
			JLabel exceptionlabel = new JLabel();
			String exception ="<html><center>You cannot make an item as you have more than 2 <br> penalty points";

			exceptionlabel.setText(exception);

			setLocationRelativeTo(null);
			e.add(exceptionlabel);
			add(e);

			setVisible(true);
		}

	}
}
