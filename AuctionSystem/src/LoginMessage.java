
/**
 * LoginMessage is an extention of Message and it sends a message to the server when a user wants to login
 * to the auction frame. It takes the ID and password as parameters.
 * @author chloeallan
 *
 */
public class LoginMessage extends Message{
	
	private static final long serialVersionUID = -3973352174875249304L;
	String ID;
	String password;

	public LoginMessage(MessageType newType, String newID, String newPassword) {
		super(newType);
		ID = newID;
		password = newPassword;
	}
	
}
