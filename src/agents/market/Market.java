package agents.market;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import agents.market.behaviors.auctionCreation.MarketFSMBehaviour;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import util.Auction;

public class Market extends Agent {
	/** Logging **/
	static final Logger MARKET_LOGGER = Logger.getLogger(Market.class.getName());
	
	/** registered auction **/
	private Map<String, Auction> _auctions = new HashMap<String, Auction>();
	
	/** Auction and its subscribers linking **/
	private Map<String, Set<AID>> _subscribers = new HashMap<String, Set<AID>>();
	
	/** Auction and its seller linking **/
	private Map<String, AID> _sellers = new HashMap<String, AID>();
	
	/** view of this agent **/
	//todo
	
	/** Service supplied by this agent (for DRF yellowPage) **/
	public static final String SERVICE_DESCRIPTION = "FISH_MARKET_SERVICE";
	
	/** constructor **/
	public Market(){}
	
	/** registration to the DF **/
	private void registrationToDF() throws FIPAException{
		ServiceDescription sd = new ServiceDescription();
		sd.setType(Market.SERVICE_DESCRIPTION);
		sd.setName(this.getAID().getName());
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(this.getAID());
		dfd.addServices(sd);
		DFService.register(this, dfd);
	}

	@Override
	protected void setup() {
		try{
			registrationToDF();
		}catch (FIPAException ex){
			Market.MARKET_LOGGER.log(Level.SEVERE,null, ex);
		}
		
		//adding behaviours
		this.addBehaviour(new MarketFSMBehaviour(this));
		
	}

	@Override
	protected void takeDown() {
		//unregistration of the market servce from DF
		try{
			DFService.deregister(this);
		}catch (FIPAException fe){
			Market.MARKET_LOGGER.log(Level.SEVERE, null, fe);
		}
		/*
		 * Market agent view dispose
		 */
		super.takeDown();
	}
	/************************************************************************************/
	/**
	 * An auction is already registered in this market agent or not
	 */
	public boolean isRegisteredAuction(String auctionID){
		return this._auctions.containsKey(auctionID);
	}
	
	/**
	 * Auction registration
	 */
	public void auctionRegistration(Auction auction, AID seller){
		if(auction != null && seller != null){
			String auctionID = auction.get_auctionID();
			this._auctions.put(auctionID, auction);
			this._sellers.put(auctionID, seller);
			this._subscribers.put(auctionID, new HashSet<AID>());
			/**
			 * market agent view refresh
			 */
		}else{
			Market.MARKET_LOGGER.log(Level.SEVERE,"null auction or null seller registration attempt");
		}
	}
	
	/**
	 * get the list of registered auctions
	 */
	public Set<Auction> getRegisteredAuctions(){
		return new HashSet<Auction>(this._auctions.values());
	}
	
	/**
	 * retrieve a specific auction relating to auctionID
	 */
	public Auction getSpecificAuction(String auctionID){
		return this._auctions.get(auctionID);
	}
	
	/**
	 * retrieve a specific seller relating to auctionID
	 */
	public AID getSpecificSeller(String auctionID){
		return this._sellers.get(auctionID);
	}
	
	/**
	 * add a bidder agent for a auction
	 */
	public void addSubscribers(String auctionID, AID bidderAID){
		if(this._subscribers.get(auctionID) != null){
			this._subscribers.get(auctionID).add(bidderAID);				
		}else{
			Market.MARKET_LOGGER.log(Level.WARNING,"the subscriber "+bidderAID+" cannot be add");
		}
	}
	
	/**
	 * return true whether a bidder agent is a subscriber of an auction
	 */
	public boolean isSubscriber(String auctionID, AID bidderAID){
		Set<AID> subscribers = this._subscribers.get(auctionID);
		if(subscribers != null){
			return subscribers.contains(bidderAID);
		}else{
			Market.MARKET_LOGGER.log(Level.WARNING,"this subscriber "+bidderAID+" not found amoung all subscribers");
			return false;
		}
	}
	
	/**
	 * supply the subscribers of an auction
	 */
	public Set<AID> getSubscribers(String auctionID){
		return this._subscribers.get(auctionID);
	}
	
	/**
	 * Delete all subscribers of the given auction
	 */
	public void deleteSubscribers(String auctionID){
		this._subscribers.get(auctionID).clear();
	}
	
	/**
	 * setting auction price
	 */
	public void setAuctionPrice(String auctionID, float price){
		Auction auction = this.getSpecificAuction(auctionID);
		if(auction != null){
			auction.set_currentPrice(price);
		}else{
			Market.MARKET_LOGGER.log(Level.WARNING,"the auction "+auctionID+" not found => auction price not set");
		}
	}
	
	/**
	 * delete a specific auction
	 */
	public void deleteAuction(String auctionID){
		this._auctions.remove(auctionID);
		this._sellers.remove(auctionID);
		this._subscribers.remove(auctionID);
		/**
		 * refresh the view
		 */
	}
	
	/**
	 * refresh the view of this agent
	 */
	
}
