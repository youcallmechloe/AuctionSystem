import java.io.Serializable;

/**
 * Message is a general messagetype class that is serializable so it can be sent between sockets.
 * @author chloeallan
 *
 */
public class Message implements Serializable{
	
	private static final long serialVersionUID = 1463858580651891947L;
	MessageType type;
	
	public Message(MessageType newType){
		type = newType;
	}

}
