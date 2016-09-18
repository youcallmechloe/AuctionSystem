
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JTextArea;

/**
 * This class uses a ServerSocket to deal with the server side of the network. At the bottom of this class there is a 
 * ServerThread class which deals with different clients on different threads. In the class there is send, receive and parse
 * message, and receive and parse message are called in the run method for the thread. There is an arraylist of ServerThreads
 * in the ServerComms so the threads open are stored. There are also a number of methods in ServerComms that deal with
 * sending messages to multiple thread (clients).
 * @author chloeallan
 *
 */
public class ServerComms implements Runnable {

	int port = 1050;
	ServerSocket serverSocket;
	ArrayList<ServerThread> threads = new ArrayList<ServerThread>();
	static ArrayList<Item> items = new ArrayList<Item>();
	DataPersistence data = new DataPersistence();

	public void stopServer(){
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public void run() {
		try {
			serverSocket = new ServerSocket(port);
			System.out.println("Waiting for connection\n");
			while(true){
				Socket socket = serverSocket.accept();
				ServerThread thread = new ServerThread(socket);
				threads.add(thread);
				thread.start();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * when the server closes, all the clients are notified and closed with it
	 */
	public void closeClients(){
		for(ServerThread t : threads){
			t.sendMessage(new ObjectMessage(MessageType.CLOSESERVER, new String("Server Closing")));
		}
	}

	/**
	 * When a client closes and leaves, the thread of that client is removed from the arraylist
	 */
	public void terminateClient(ServerThread t, Object o){
		threads.remove(t);
		String s = (String) o;
		Server.display.append(o + " has disconnected from the server");
	}

	/**
	 * when an item needs updating on the screen, this is called to send the UPDATEITEMS message to all
	 * of the clients, where the clientcomms deals with the updating.
	 */
	public void updateClients(Item item){
		for(ServerThread t : threads){
			t.sendMessage(new ObjectMessage(MessageType.UPDATEITEMS, item));
		}
	}

	/**
	 * When an item has finished, the ITEMCLOSE method is sent to all clients to notify them of the closure
	 * and so their auction frames update
	 */
	public void itemCloseClients(Item item){
		for(ServerThread t : threads){
			t.sendMessage(new ObjectMessage(MessageType.ITEMCLOSE, item));
		}
	}

	/**
	 * When an item finishes with bidders, this checks whether the currentbid was greater than the reserve 
	 * price and sends a NOTIFYWIN or NOTIFYFAIL method for each instance. The clients receive these and
	 * deal with them as they need to. Also, for when the item has won (thread = true), datapersistence is
	 * called to move the item to the finished folder and add it to the winning log, and the server display
	 * is appended; the same goes for an item failure (thread = false) except the item is not written to the
	 * winning log.
	 */
	public void updateItemsClients(Item item){

		boolean thread = false;

		for(ServerThread t : threads){

			if(item.getCurrentBid() != null && item.getCurrentBid().getBid() > Double.valueOf(item.rPrice)){
				thread = true;
				t.sendMessage(new NotificationMessage(MessageType.NOTIFYWIN, item.getCurrentBid(), item));
			} else{
				thread = false;
				t.sendMessage(new NotificationMessage(MessageType.NOTIFYFAIL, item.getCurrentBid(), item));
			}
		}

		if(thread){
			try {
				data.moveFinishedItem(item);
				data.writeToWinLog(item);
				Server.display.append("User " + item.getCurrentBid().getID() + " has won Item " + item.getTitle() + ". \n");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		} else{
			data.moveFinishedItem(item);
			Server.display.append("Item " + item.getTitle() + " has failed. \n");
			return;
		}
	}

	/**
	 * when a bid is made on an item it needs to update for all the clients so this method sends an UPDATEBIDS
	 * message to every thread.
	 */
	public void updateClientBids(Item item, Bid bid){
		for(ServerThread t : threads){
			t.sendMessage(new ObjectMessage(MessageType.UPDATEBIDS, item));
		}
	}

	/**
	 * This method notifies the seller of the item that a bid has been made on their item by sending a NOTIFYBID
	 * message to just that client.
	 */
	public void notifyUserBid(ServerThread thread, Bid bid, Item item){
		for(ServerThread t : threads){
			if(!(t.equals(thread)))
				t.sendMessage(new NotificationMessage(MessageType.NOTIFYBID, bid, item));
		}

	}

	public class ServerThread extends Thread{

		ObjectOutputStream output;
		ObjectInputStream input;
		Socket connection;

		BufferedReader br;

		public ServerThread(Socket s){
			connection = s;
		}

		public void run(){
			try {
				output = new ObjectOutputStream(connection.getOutputStream());
				output.flush();
				input = new ObjectInputStream(connection.getInputStream());
			} catch (IOException e) {
			}

			Message message;

			while(true){
				if((message = recieveMessage()) != null){
					parseMessage(message);
				}
			}

		}

		public void sendMessage(Message message){

			try {
				output.writeObject(message);
				output.flush();
			} catch (IOException e) {
			}

		}

		public Message recieveMessage(){

			Message message = null;

			try {
				message = (Message) input.readObject();
			} catch (ClassNotFoundException | IOException e) {
			}

			return message;
		}

		public void parseMessage(Message message){

			switch(message.type){
			case SHUTDOWNCLIENT:
				terminateClient(this, ((ObjectMessage)message).getObject());
				break;
			case LOGIN: 
				LoginMessage loginMessage = (LoginMessage) message;
				try {
					if(data.authenticateUser(loginMessage.ID, loginMessage.password)){
						Server.display.append(loginMessage.ID + " has logged in to the server. \n");
						sendMessage(new ObjectMessage(MessageType.AUTHENTICATED, new String("Login Successful")));
					} else{
						sendMessage(new ObjectMessage(MessageType.FAILED, new String("Login Failed")));
					}

				} catch (IOException e1) {
					e1.printStackTrace();
				}
				return;
			case USER:
				ObjectMessage newUser = (ObjectMessage) message;
				User user = (User) newUser.getObject();
				boolean create = false;
				try {
					create = data.createUserFile(user);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(create){
					Server.display.append("New User " + user.getUserId() + " has been created. \n");
					sendMessage(new ObjectMessage(MessageType.USERSUCCESS, "User registration success"));
				} else{
					Server.display.append("Username " + user.getUserId() + " has already been taken. \n");
					sendMessage(new ObjectMessage(MessageType.USERFAIL, "User registration failed"));
				}
				return;
			case ITEM:
				ObjectMessage newItem = (ObjectMessage) message;
				Item item = (Item) newItem.getObject();
				items.add(item);
				boolean itemb = false;
				try {
					itemb = data.createItemFile(item, item.getUserID());
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(itemb){
					Server.display.append("New Item " + item.getTitle() + " has been created. \n");
					updateClients(item);
				} else{
					Server.display.append("New Item cannot be created as User " + item.getUserID() + " has more than 2 penalty points \n");
					sendMessage(new ObjectMessage(MessageType.ITEMFAIL, item));
				}
				return;
			case NEWBID:
				ObjectMessage newBid = (ObjectMessage) message; 
				Bid bid = (Bid) newBid.getObject();

				Item bidItem = null;
				try {
					bidItem = data.addBid(bid);
				} catch (IOException e) {
					e.printStackTrace();
				}

				try {
					items = (ArrayList<Item>) data.createItemList();
				} catch (IOException e) {
					e.printStackTrace();
				}

				Server.display.append("Item " + bidItem.getTitle() + " has a new bid of \u00A3" + bid.getBid() + 
						" from " + bid.getID() + ". \n");

				updateClientBids(bidItem, bid);
				notifyUserBid(this, bid, bidItem);
				return;
			case PENALTY:
				ObjectMessage thismessage = (ObjectMessage) message;
				Item newitem = (Item) thismessage.getObject();
				String userID = newitem.getUserID();

				int point = 0;

				if(newitem.getCurrentBid() != null){
					if(newitem.getCurrentBid().getBid() > Integer.valueOf(newitem.rPrice)){
						try {
							point = data.addPenalty(userID);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

				if(point < 0){
					System.out.println("error with pp");
				} else{
					items.remove(newitem);
					data.moveFinishedItem(newitem);
					Server.display.append("Item " + newitem.getTitle() + " has been withdrawn by the seller. \n");
					itemCloseClients(newitem);
				}
			}

		}

	}
}