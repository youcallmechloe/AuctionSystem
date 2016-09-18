import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


/**
 * This class creates the first Client GUI that includes the login/register page. You can either login or register 
 * a new user, both of whom will be accessed through the User folder created when the first User is created. There 
 * are two nested classes in this class which are both error frames. One will be opened when you enter an incorrect 
 * userID/password and the other will be opened if you try to create a user with a username thats already taken, to stop 
 * User files from being overwritten. 
 * @author chloeallan
 *
 */
public class Client extends JFrame {

	static JFrame mainFrame;
	static JPanel mainPanel;
	static ClientComms comms;
	
	public static void main(String[] args) {
		new Client("Auction System");
	}

	public Client(String title){
		super(title);
		init();
		comms = new ClientComms();
	}
	
	public void init(){
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(300, 350);
		setMinimumSize(new Dimension(300,350));
		setLocationRelativeTo(null);
		mainFrame = this;

		mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(9,0));
		
		JPanel useridPanel = new JPanel();
		JPanel passwordPanel = new JPanel();
		JPanel loginPanel = new JPanel();

		JTextField useridfield = new JTextField(10);
		JLabel useridlabel = new JLabel("User ID:");
		JPasswordField passwordfield = new JPasswordField(10);
		JLabel passwordlabel = new JLabel("Password:");
		JButton login = new JButton("Log In");
		login.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				String userID = useridfield.getText();
				char[] passwords = passwordfield.getPassword();
				String password = String.copyValueOf(passwords);
				LoginMessage message = new LoginMessage(MessageType.LOGIN, userID, password);
				comms.sendMessage(message);
				Message returnedM = comms.listenForMessage();
				//waits for a message from the server to say whether the username or password is correct
				if(returnedM.type == MessageType.AUTHENTICATED){
					Client.mainFrame.dispose();
					Client.mainFrame = new AuctionFrame(userID + "'s Auction Frame", userID);
				} else if(returnedM.type == MessageType.FAILED){
					new EFrame("Incorrect User ID/Password");
				}
			}

		});
		
		//i used a password field for the login password to give a sense of security when a user logs in 
		passwordfield.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				login.doClick();
			}
			
		});

		useridPanel.add(useridlabel);
		useridPanel.add(useridfield);
		passwordPanel.add(passwordlabel);
		passwordPanel.add(passwordfield);
		loginPanel.add(login);

		JTextField firstnamefield = new JTextField(10);
		JLabel firstnamelabel = new JLabel("First Name: ");
		JTextField lastnamefield = new JTextField(10);
		JLabel lastnamelabel = new JLabel("Last Name: ");
		JTextField registeruserid = new JTextField(10);
		JLabel registeruseridlabel = new JLabel("User ID:");
		JTextField registerpassword = new JTextField(10);
		JLabel registerpasswordlabel = new JLabel("Password:");

		JButton register = new JButton("Register");
		register.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				String firstname = firstnamefield.getText();
				String lastname = lastnamefield.getText();
				String userID = registeruserid.getText();
				String password = registerpassword.getText();
				ObjectMessage message = new ObjectMessage(MessageType.USER, new User(firstname, lastname, userID, password));
				comms.sendMessage(message);
				Message returnedM = comms.listenForMessage();
				//waits for a message from the server to confirm whether the registration was a success
				if(returnedM.type == MessageType.USERSUCCESS){
					Client.mainFrame.dispose();
					Client.mainFrame = new AuctionFrame(userID + "'s Auction Frame", userID);
				} else if(returnedM.type == MessageType.USERFAIL){
					new REFrame("Username already taken");
				}
			}

		});

		JPanel firstnamePanel = new JPanel();
		JPanel lastnamePanel = new JPanel();
		JPanel registeridPanel = new JPanel();
		JPanel registerpassPanel = new JPanel();
		JPanel registerPanel = new JPanel();

		firstnamePanel.add(firstnamelabel);
		firstnamePanel.add(firstnamefield);
		lastnamePanel.add(lastnamelabel);
		lastnamePanel.add(lastnamefield);
		registeridPanel.add(registeruseridlabel);
		registeridPanel.add(registeruserid);
		registerpassPanel.add(registerpasswordlabel);
		registerpassPanel.add(registerpassword);
		registerPanel.add(register);

		mainPanel.add(useridPanel);
		mainPanel.add(passwordPanel);
		mainPanel.add(loginPanel);
		mainPanel.add(new JPanel());
		mainPanel.add(firstnamePanel);
		mainPanel.add(lastnamePanel);
		mainPanel.add(registeridPanel);
		mainPanel.add(registerpassPanel);
		mainPanel.add(registerPanel);
		add(mainPanel);

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
			String exception ="<html><center>Incorrect User ID/Password. Please Re-Enter.";

			exceptionlabel.setText(exception);
			
		    setLocationRelativeTo(null);
			e.add(exceptionlabel);
			add(e);

			setVisible(true);
		}

	}
	
	public class REFrame extends JFrame{

		public REFrame(String title){
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
			String exception ="<html><center>Username already taken. Please try again.";

			exceptionlabel.setText(exception);
			
		    setLocationRelativeTo(null);
			e.add(exceptionlabel);
			add(e);

			setVisible(true);
		}

	}

}
