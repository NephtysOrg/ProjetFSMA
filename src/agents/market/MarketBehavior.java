package agents.market;

import java.util.logging.Level;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.UnreadableException;
import util.Auction;
import util.FishMarketProtocol;

public class MarketBehavior extends CyclicBehaviour {
	
	Market _marketAgent = null;
	
	public MarketBehavior() {
	}
	
	public MarketBehavior(Market m){
		super(m);
		this._marketAgent = m;
	}

	@Override
	public void action() {
		FishMarketProtocol message;
		Auction auction = null;
		String message_content_str = "";
		message = (FishMarketProtocol) this._marketAgent.receive();
		AID agentSender = null;
		boolean subscriber_founded;
		int k;
		
		
		
		if(message != null){
			_marketAgent.MARKET_LOGGER.log(Level.SEVERE,"Reception d'un message de la part d'un agent");
			try{
				/** Retrieving message content **/
				agentSender = message.getSender();
				auction = new Auction();
				message_content_str = message.get_message();
				auction = (Auction)message.getContentObject();
			} catch(UnreadableException e){
				e.printStackTrace();
			}
			
			/** Message processing **/
			switch(message.getPerformative()){
				case FishMarketProtocol.to_subscribe:
					_marketAgent.MARKET_LOGGER.log(Level.SEVERE,"Traitement message to_subscribe");
					
					
					if(message_content_str.equals(FishMarketProtocol.BUYER)){
						if(!_marketAgent.isSubscriber(auction.get_auctionID(), agentSender)){
							_marketAgent.addSubscribers(auction.get_auctionID(), agentSender);
							_marketAgent.MARKET_LOGGER.log(Level.INFO,"L'abonnment d'un agent vient d'être réalisé");
						}else{
							_marketAgent.MARKET_LOGGER.log(Level.INFO,"L'agent buyer est deja abonné");
						}
					}
					
					if(message_content_str.equals(FishMarketProtocol.SELLER)){
						
					}
						
					
					
				
					
				
					
					
					break;
				case FishMarketProtocol.buyer_subscribed:
					_marketAgent.MARKET_LOGGER.log(Level.SEVERE,"Traitement message buyer_subscribed");
					break;
				case FishMarketProtocol.to_announce:
					_marketAgent.MARKET_LOGGER.log(Level.SEVERE,"Traitement message to_announce");
					break;
				case FishMarketProtocol.to_attribute:
					_marketAgent.MARKET_LOGGER.log(Level.SEVERE,"Traitement message to_attribute");
					break;
				case FishMarketProtocol.to_bid:
					_marketAgent.MARKET_LOGGER.log(Level.SEVERE,"Traitement message to_give");
					break;
				case FishMarketProtocol.to_pay:
					_marketAgent.MARKET_LOGGER.log(Level.SEVERE,"Traitement message to_pay");
					break;
				case FishMarketProtocol.get_auction:
					_marketAgent.MARKET_LOGGER.log(Level.SEVERE,"Traitement message get_auction");
					break;
				default:
					_marketAgent.MARKET_LOGGER.log(Level.SEVERE,"Traitement de message pas fait");
					break;
			}
				
		}else{
			_marketAgent.MARKET_LOGGER.log(Level.SEVERE,"Agent Market Termine");	
		}
	}
}

