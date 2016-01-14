package util;

import jade.lang.acl.ACLMessage;

public class FishMarketProtocol extends ACLMessage {
	
	public static final int to_announce = ACLMessage.CFP;
	public static final int to_attribute = ACLMessage.ACCEPT_PROPOSAL;
	public static final int to_give = ACLMessage.AGREE;
	public static final int rep_bid = ACLMessage.INFORM;
	public static final int to_bid = ACLMessage.PROPOSE;
	public static final int to_pay = ACLMessage.CONFIRM;
	public static final int to_subscribe = ACLMessage.SUBSCRIBE;
	public static final int get_auction = 200;
	public static final int buyer_subscribed = 201;
	
	public static final String BUYER = "buyer";
	public static final String SELLER = "seller";
	private String _message = null;
	
	public FishMarketProtocol(String message){
		this._message = message;
	}

	public String get_message() {
		return _message;
	}

	public void set_message(String _message) {
		this._message = _message;
	} 
}