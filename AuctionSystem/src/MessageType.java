/**
 * this enum class contains all the messagetypes that are sent between the client and server comms.
 * @author chloeallan
 *
 */
public enum MessageType {
	
	REGISTER,
	LOGIN, 
	USER,
	ITEM,
	AUTHENTICATED,
	FAILED,
	USERSUCCESS,
	USERFAIL,
	UPDATEITEMS,
	ITEMFAIL,
	UPDATEBIDS,
	NEWBID,
	ITEMEND,
	NOTIFYBID,
	NOTIFYWIN,
	NOTIFYFAIL,
	PENALTY,
	ITEMCLOSE,
	SHUTDOWNCLIENT,
	CLOSESERVER
}
