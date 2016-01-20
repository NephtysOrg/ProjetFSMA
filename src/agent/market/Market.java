package agent.market;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import agent.bidder.Bidder;
import agent.market.behaviours.MarketFSMBehaviourAuctionCreation;
import agent.market.behaviours.MarketFSMBehaviourHandshake;
import agent.seller.Seller;
import gui.MarketGui;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;
import util.Auction;

@SuppressWarnings("serial")

public class Market extends Agent {

    private Map<String, Auction> auctions = new HashMap<String, Auction>();

    private Map<String, Set<AID>> subscribers = new HashMap<String, Set<AID>>();

    private Map<String, AID> sellers = new HashMap<String, AID>();

    private boolean isDone = false;

    private MarketGui myView;

    private static final Logger LOGGER = Logger.getLogger(Market.class.getName());

    public static final String SERVICE_DESCIPTION = "FISH_MARKET_SERVICE";

    public Market() {
    }

    @Override
    protected void setup() {
	// Register service to DF
	try {
	    _register2DF();
	} catch (FIPAException ex) {
	    Market.LOGGER.log(Level.SEVERE, null, ex);
	}

	// Add behaviours
	this.addBehaviour(new MarketFSMBehaviourHandshake(this));
	this.addBehaviour(new MarketFSMBehaviourAuctionCreation(this));

	// Agent view
	this.myView = new MarketGui(this);
	this.myView.setVisible(true);

	// Start seller and bidder, if requested.
	this.handleStartUpParameters();
    }

    private void createMarketUsers(int numSellers, int numBidders) {
	AgentContainer container = this.getContainerController();

	for (int i = 0; i < numSellers; i++) {
	    String agentName = "seller" + String.valueOf(i);
	    try {
		container.createNewAgent(agentName, Seller.class.getName(), null).start();
		System.out.println("Created agent " + agentName);
	    } catch (StaleProxyException e) {
		Market.LOGGER.log(Level.SEVERE, null, e);
	    }
	}

	for (int i = 0; i < numBidders; i++) {
	    String agentName = "bidder" + String.valueOf(i);
	    try {
		container.createNewAgent(agentName, Bidder.class.getName(), null).start();
		System.out.println("Created agent " + agentName);
	    } catch (StaleProxyException e) {
		Market.LOGGER.log(Level.SEVERE, null, e);
	    }
	}

    }

    @Override
    protected void takeDown() {
	// Unregister service from DF
	try {
	    DFService.deregister(this);
	} catch (FIPAException fe) {
	    Market.LOGGER.log(Level.SEVERE, null, fe);
	}

	this.myView.setVisible(false);
	this.myView.dispose();

	super.takeDown();
    }

    public void setIsDone(boolean isDone) {
	this.isDone = isDone;
    }

    public boolean isDone() {
	return this.isDone;
    }

    public boolean isRegisteredAuction(String auctionID) {
	return this.auctions.containsKey(auctionID);
    }

    public void registerAuction(Auction auction, AID seller) {
	if (auction != null && seller != null) {
	    String auctionID = auction.getID();

	    this.auctions.put(auctionID, auction);
	    this.sellers.put(auctionID, seller);
	    this.subscribers.put(auctionID, new HashSet<AID>());

	    this.myView.refresh();
	} else {
	    Market.LOGGER.log(Level.SEVERE, "Trying to register null auction or null seller failed.");
	}
    }

    public Set<Auction> getRegisteredAuctions() {
	return new HashSet<Auction>(this.auctions.values());
    }

    public Auction findAuction(String auctionID) {
	return this.auctions.get(auctionID);
    }

    public AID getSeller(String auctionID) {
	return this.sellers.get(auctionID);
    }

    public void addSuscriber(String auctionID, AID bidderAID) {
	Set<AID> subscribers = this.subscribers.get(auctionID);

	if (subscribers != null) {
	    subscribers.add(bidderAID);
	} else {
	    Market.LOGGER.log(Level.WARNING, "auction with ID " + auctionID + " not found.");
	}
    }

    public boolean isSuscriber(String auctionID, AID bidderAID) {
	Set<AID> subscribers = this.subscribers.get(auctionID);

	if (subscribers != null) {
	    return subscribers.contains(bidderAID);
	} else {
	    Market.LOGGER.log(Level.WARNING, "auction with ID " + auctionID + " not found.");

	    return false;
	}
    }

    public Set<AID> getSubscribers(String auctionID) {
	return this.subscribers.get(auctionID);
    }

    public void clearSubscribers(String auctionID) {
	this.subscribers.get(auctionID).clear();
    }

    public void setAuctionStatus(String auctionID, int status) {
	Auction auction = this.findAuction(auctionID);

	if (auction != null) {
	    auction.setStatus(status);
	} else {
	    Market.LOGGER.log(Level.WARNING, "auction with ID " + auctionID + " not found.");
	}
    }

    public void notifyAuctionOver(String auctionID, String winnerName) {
	Auction auction = this.findAuction(auctionID);

	if (auction != null) {
	    auction.setStatus(Auction.STATUS_OVER);
	    auction.setWinnerName(winnerName);

	    // this.myView.refresh();
	} else {
	    Market.LOGGER.log(Level.WARNING, "auction with ID " + auctionID + " not found.");
	}
    }

    public void notifyAuctionCancelled(String auctionID) {
	Auction auction = this.findAuction(auctionID);

	if (auction != null) {
	    auction.setStatus(Auction.STATUS_CANCELLED);

	    // this.myView.refresh();
	} else {
	    Market.LOGGER.log(Level.WARNING, "auction with ID " + auctionID + " not found.");
	}
    }

    public void setAuctionPrice(String auctionID, float price) {
	Auction auction = this.findAuction(auctionID);

	if (auction != null) {
	    auction.setCurrentPrice(price);
	} else {
	    Market.LOGGER.log(Level.WARNING, "auction with ID " + auctionID + " not found.");
	}
    }

    public void deleteAuction(String auctionID) {
	this.auctions.remove(auctionID);
	this.sellers.remove(auctionID);
	this.subscribers.remove(auctionID);

	this.myView.refresh();
    }

    public void refreshView() {
	this.myView.refresh();
    }

    private void _register2DF() throws FIPAException {
	ServiceDescription sd = new ServiceDescription();
	sd.setType(Market.SERVICE_DESCIPTION);
	sd.setName(this.getAID().getName());

	DFAgentDescription dfd = new DFAgentDescription();
	dfd.setName(this.getAID());

	dfd.addServices(sd);
	DFService.register(this, dfd);
    }

    private void handleStartUpParameters() {
	int sellerCount = 0;
	int bidderCount = 0;

	Object[] parameters = this.getArguments();

	if (parameters != null) {
	    if (parameters.length > 0) {
		sellerCount = Integer.valueOf((String) parameters[0]);
	    }

	    if (parameters.length > 1) {
		bidderCount = Integer.valueOf((String) parameters[1]);
	    }
	}

	this.createMarketUsers(sellerCount, bidderCount);
    }
}
