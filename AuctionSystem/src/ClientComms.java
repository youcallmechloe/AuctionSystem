
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;

/**
 * This class deals with all the messages on the client side of the sockets. It creates a new socket using localhost as 
 * the servername, and the port thats the same as servercomms. A timertask is also attached to the class so the class
 * is effectively constantly listening for messages from the server.
 * In the timertask (nested class at the bottom) there's a switch statement that goes through different message types
 * that are received from the server and deals with them individually. 
 * @author chloeallan
 *
 */
public class ClientComms {

	int port = 1050;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	String serverName = "localhost";
	ArrayList<Message> messages;
	Object object = new Object();
	DataPersistence data = new DataPersistence();

	public ClientComms(){
		messages = new ArrayList<Message>();
		Socket clientSocket;

		try {
			//creates the socket and sets the input and output streams to the sockets streams
			clientSocket = new Socket(serverName, port);
			output = new ObjectOutputStream(clientSocket.getOutputStream());
			output.flush();
			input = new ObjectInputStream(clientSocket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		Timer t = new Timer();
		TimerTask task = new MyTimer();
		t.schedule(task, 0, 50);
	}


	public void sendMessage(Message message){
		try {
			output.writeObject(message);
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public Message recieveMessage(){

		Message message = null;

		try {
			message = (Message) input.readObject();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}

		return message;
	}

	public Message listenForMessage(){
		Message message = null;

		int i = 0;
		while(i < 80){
			synchronized(object){
				if(messages.size() != 0){
					message = messages.get(0);
					messages.remove(message);
					return message;
				}
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			i++;
		}	
		System.out.println("timed out");
		return null;
	}

	public class MyTimer extends TimerTask{

		public void run() {
			Message message = null;

			if((message = recieveMessage()) != null){
				synchronized(object){

					messages.add(message);
				}
			}

			switch(message.type){
			case UPDATEITEMS:
				if(Client.mainFrame instanceof AuctionFrame){
					ObjectMessage newItem = (ObjectMessage) message;
					Item item = (Item) newItem.getObject();

					AuctionFrame.items.add(item);

					try {
						data.createClientImage(item);
					} catch (IOException e) {
						e.printStackTrace();
					}

					if(AuctionFrame.create != null){
						AuctionFrame.create.dispose();
					}

					((AuctionFrame) Client.mainFrame).init();

					if(messages.size() > 0){
						messages.remove(0);
					}
				}
				break;
			case UPDATEBIDS:
				if(Client.mainFrame instanceof AuctionFrame){
					ObjectMessage newItem = (ObjectMessage) message;
					Item item = (Item) newItem.getObject();

					for(Item i : AuctionFrame.items){
						if(i.getTitle().equals(item.getTitle())){
							AuctionFrame.items.remove(i);
							break;
						}
					}

					for(Item i : AuctionFrame.items){
						if(i.equals(item)){
							AuctionFrame.itemButton.doClick();
						}
					}

					AuctionFrame.items.add(item);

					if(AuctionFrame.create != null){
						AuctionFrame.create.dispose();
					}

					((AuctionFrame) Client.mainFrame).init();

					if(messages.size() > 0){
						messages.remove(0);
					}
				}
				break;
			case ITEMEND:
				if(Client.mainFrame instanceof AuctionFrame){
					ObjectMessage newItem = (ObjectMessage) message;
					Item item = (Item) newItem.getObject();

					AuctionFrame.finished.add(item);

					for(Item i : AuctionFrame.items){
						if(i.getTitle().equals(item.getTitle())){
							AuctionFrame.items.remove(i);
							break;
						}
					}

					((AuctionFrame) Client.mainFrame).init();

					if(messages.size() > 0){
						messages.remove(0);
					}
				}
				break;
			case NOTIFYBID:
				if(Client.mainFrame instanceof AuctionFrame){
					NotificationMessage notify = (NotificationMessage) message;
					Item item = (Item) notify.getItem();
					Bid bid = (Bid) notify.getBid();

					if(AuctionFrame.userID.equals(item.getUserID())){
						AuctionFrame.messages.add(bid.getID() + " has made a bid of \u00A3" + bid.getBid() + " on " + item.getTitle());
						if(AuctionFrame.notifyframe != null){
							AuctionFrame.notifyframe.init();
							AuctionFrame.notifyframe.repaint();
						}
					}
					SwingUtilities.invokeLater(new Runnable(){

						@Override
						public void run() {
							AuctionFrame.top.validate();
							AuctionFrame.top.repaint();
						}
						
					});
					
					if(messages.size() > 0){
						messages.remove(0);
					}
				}
				break;
			case NOTIFYWIN:
				if(Client.mainFrame instanceof AuctionFrame){
					NotificationMessage notify = (NotificationMessage) message;
					Item item = (Item) notify.getItem();
					Bid bid = (Bid) notify.getBid();

					if(AuctionFrame.userID.equals(item.getUserID())){
						AuctionFrame.messages.add(bid.getID() + " has won item " + item.getTitle() + " with a bid of " +  bid.getBid());
						if(AuctionFrame.notifyframe != null){
							AuctionFrame.notifyframe.init();
							AuctionFrame.notifyframe.repaint();
						}
					}

					if(AuctionFrame.userID.equals(bid.getID())){
						AuctionFrame.messages.add("You have won " + item.getTitle() + " with a bid of " + bid.getBid());
						if(AuctionFrame.notifyframe != null){
							AuctionFrame.notifyframe.init();
							AuctionFrame.notifyframe.repaint();
						}
					}

					AuctionFrame.finished.add(item);

					for(Item i : AuctionFrame.items){
						if(i.getTitle().equals(item.getTitle())){
							AuctionFrame.items.remove(i);
							break;
						}
					}

					((AuctionFrame) Client.mainFrame).init();

					if(messages.size() > 0){
						messages.remove(0);
					}
				}
				break;
			case NOTIFYFAIL:
				if(Client.mainFrame instanceof AuctionFrame){
					NotificationMessage notify = (NotificationMessage) message;
					Item item = (Item) notify.getItem();
					Bid bid = (Bid) notify.getBid();

					if(AuctionFrame.userID.equals(item.getUserID())){
						AuctionFrame.messages.add(item.getTitle() + " has failed because the reserve price was not met");
						if(AuctionFrame.notifyframe != null){
							AuctionFrame.notifyframe.init();
							AuctionFrame.notifyframe.repaint();
						}
					}

					AuctionFrame.finished.add(item);

					for(Item i : AuctionFrame.items){
						if(i.getTitle().equals(item.getTitle())){
							AuctionFrame.items.remove(i);
							break;
						}
					}

					((AuctionFrame) Client.mainFrame).init();

					if(messages.size() > 0){
						messages.remove(0);
					}
				}
				break;
			case ITEMCLOSE:
				if(Client.mainFrame instanceof AuctionFrame){
					ObjectMessage object = (ObjectMessage) message;
					Item item = (Item) object.getObject();

					AuctionFrame.finished.add(item);

					for(Item i : AuctionFrame.items){
						if(i.getTitle().equals(item.getTitle())){
							AuctionFrame.items.remove(i);
							break;
						}
					}

					((AuctionFrame) Client.mainFrame).init();

					if(messages.size() > 0){
						messages.remove(0);
					}
				}
				break;
			case CLOSESERVER:
				System.exit(0);
			}
		}

	}

}
