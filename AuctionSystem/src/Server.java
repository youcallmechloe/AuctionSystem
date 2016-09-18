import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.*;
import javax.swing.border.TitledBorder;

/**
 * This class creates a server GUI that displays a log of messages being sent between the server and the client 
 * as well as producing a 'win' log (LogFrame nested class), so the server can see in table view who has won what 
 * item, from who and how much they won it for. I've also included a 'Stop Server' button that when pressed closes 
 * down the server and any clients that are associated with it. This stops any errors or crashes when the server 
 * closes and clients are still open. 
 * At the bottom there's also a nested class that creates a server timer that's run every second for a list of
 * open items (in ServerComms). Every second it checks whether the finishing time of each item is before current time (so 
 * the item has finished); if not it does nothing but if it is the item is removed from the arraylist and a message is 
 * sent to the clients to update them on the items. The server display also receives a message saying the item has 
 * finished.
 * @author chloeallan
 *
 */
public class Server extends JFrame{

	ServerComms comms;
	static JTextArea display;
	DataPersistence data = new DataPersistence();
	static JTable logging;

	public static void main(String[] args) {
		new Server();
	}

	public Server(){
		comms = new ServerComms();
		Thread t = new Thread(comms);
		t.start();
		init();
		Timer time = new Timer();
		ServerTimer task = new ServerTimer();
		time.schedule(task, 0, 1000);
		try {
			ServerComms.items = (ArrayList<Item>) data.createItemList();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void init(){
		setSize(300, 500);
		setMinimumSize(new Dimension(300,500));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent arg0){
				comms.stopServer();
			}
		});

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		setContentPane(panel);

		JPanel centerPanel = new JPanel();
		display = new JTextArea(25,22);
		display.setMaximumSize(new Dimension(220, 400));
		display.setMinimumSize(new Dimension(220, 400));
		display.setLineWrap(true);
		display.setWrapStyleWord(true);
		display.setEditable(false);

		centerPanel.add(display);
		centerPanel.setBorder(new TitledBorder("Server"));

		JPanel topPanel = new JPanel();
		JButton log = new JButton("Produce Log");
		log.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				LogFrame log = new LogFrame("Win Log");
			}

		});

		JButton stop = new JButton("Stop Server");
		stop.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				display.append("Stopping Server \n");
				comms.closeClients();
				comms.stopServer();
				System.exit(0);
			}

		});

		topPanel.add(log);
		topPanel.add(stop);

		panel.add(centerPanel, BorderLayout.CENTER);
		panel.add(topPanel, BorderLayout.NORTH);

		display.append("Initialising Server Display \n");

		setVisible(true);

	}

	public class LogFrame extends JFrame{
		
		public LogFrame(String title){
			super(title);
			initLog();
		}

		private void initLog(){
			JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout());
			setContentPane(panel);
			setSize(500,300);
			setMinimumSize(new Dimension(500,300));
			
			String[] columnnames = {"Item", "Winner", "Seller", "Amount"};
			String[][] rows = null;
			try {
				rows = data.writeTable();
				//the data for the table is created from the "WinningLog" file created when a user wins. This will update
				//each time there's a new win.
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			logging = new JTable(rows, columnnames);
			JScrollPane scroll = new JScrollPane(logging);

			
			if(rows != null){
				panel.add(logging.getTableHeader(), BorderLayout.NORTH);
				panel.add(scroll, BorderLayout.CENTER);
			}
			
			setVisible(true);
		}

	}

	public class ServerTimer extends TimerTask{


		public synchronized void run() {

			if(ServerComms.items != null){
				for(Item i : ServerComms.items){
					if(new Date(i.finish).before((new Date()))){
						ServerComms.items.remove(i);
						comms.updateItemsClients(i);
						display.append("Item " + i.getTitle() + " has finished. \n");
						break;
					}
				}
			}
		}

	}
}
