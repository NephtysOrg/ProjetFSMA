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
		
		
		if(message != null){
			_marketAgent.MARKET_LOGGER.log(Level.SEVERE,"Reception d'un message de la part d'un agent");
			try{
				/** Retrieving message content **/
				agentSender = message.getSender(); /* Can be a buyer or a seller */
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
					
					/** Receiving message from a buyer case **/
					if(message_content_str.equals(FishMarketProtocol.BUYER)){
						/** Answer subscription for the buyer who attempt to make the subscription**/
						FishMarketProtocol buyer_subscription = new FishMarketProtocol();
						buyer_subscription.setPerformative(FishMarketProtocol.answer_subscribe);
						buyer_subscription.addReceiver(agentSender);
						
						if(!_marketAgent.isSubscriber(auction.get_auctionID(), agentSender)){
							_marketAgent.addSubscribers(auction.get_auctionID(), agentSender);
							_marketAgent.MARKET_LOGGER.log(Level.INFO,"L'abonnement d'un agent buyer à une offre vient d'être réalisé");
							
							/** answer sending //OK **/
							buyer_subscription.setContent("Abonnement OK pour l'enchère "+ auction.toString());
							_marketAgent.send(buyer_subscription);
							
						}else{
							_marketAgent.MARKET_LOGGER.log(Level.WARNING,"L'agent buyer est deja abonné à cette offre");
							/** answer sending //NOK **/
							buyer_subscription.setContent("Abonnement NOK pour l'enchère "+ auction.toString());
							_marketAgent.send(buyer_subscription);
						}
					}
					
					/** Receiving message from a seller case **/
					if(message_content_str.equals(FishMarketProtocol.SELLER)){
						try{
							if(!_marketAgent.isRegisteredAuction(auction.get_auctionID())){
								_marketAgent.auctionRegistration(auction,agentSender);
								_marketAgent.MARKET_LOGGER.log(Level.INFO,"L'agent seller "+agentSender.getName()+" vient de publier l'enchère "+auction.toString());
								
							}else{
								_marketAgent.MARKET_LOGGER.log(Level.WARNING,"Un agent seller "+agentSender.getName()+" à tenté de publier une enchère déjà existante");
							}
						}catch(Exception e){
							e.printStackTrace();
						}	
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

