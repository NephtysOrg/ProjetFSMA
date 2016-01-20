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
import util.Auction;
import gui.MarketGui;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;

@SuppressWarnings("serial")
/**
 * 
 * @author cfollet & rbary
 *
 */
public class Market extends Agent {
	/** The registered auction. */
	private Map<String, Auction> auctions = new HashMap<String, Auction>();

	/** Associates an auction and its subscribers. */
	private Map<String, Set<AID>> subscribers = new HashMap<String, Set<AID>>();

	/** Associate each auction with it's seller agent. */
	private Map<String, AID> sellers = new HashMap<String, AID>();

	/** Tells whether this agent has ended it's task or not. */
	private boolean isDone = false;

	/** The view for this market agent. */
	private MarketGui myView;

	/** Allows logging. */
	private static final Logger LOGGER = Logger.getLogger(Market.class.getName());

	/** The service offered by this agent (for registration to the DRF). */
	public static final String SERVICE_DESCIPTION = "FISH_MARKET_SERVICE";

	/**
	 * Creates a market agent for a fish market protocol.
	 */
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
		/**
		 * Auction management behaviours are add by SellerManagementBehaviour
		 * sub-behaviour (confirm auction registration behaviour)
		 **/

		// Agent view
		this.myView = new MarketGui(this);
		this.myView.setVisible(true);

		// Start seller and bidder, if requested.
		this.handleStartUpParameters();
	}

	/**
	 * Requests the creation of seller and bidders.
	 * 
	 * @param numSellers
	 *            the number of seller agents to be created.
	 * @param numBidders
	 *            the number of bidder agents to be created.
	 */
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

	/**
	 * Defines whether this agent reached a end state.
	 * 
	 * @param isDone
	 *            true to tag this agent as a ended agent (which reached a
	 *            ending state in his FSMBehaviour).
	 */
	public void setIsDone(boolean isDone) {
		this.isDone = isDone;
	}

	/**
	 * Tells whether this agent reached a end state or not.
	 * 
	 * @return true if this agent reached a end state in his FSMBehaviour, false
	 *         otherwise.
	 */
	public boolean isDone() {
		return this.isDone;
	}

	/**
	 * Tells whether an auction is already registered in this market agent or
	 * not.
	 * 
	 * @param auctionID
	 *            the ID of the checked auction.
	 * @return true if the auction is already registered on this market agent,
	 *         false otherwise.
	 */
	public boolean isRegisteredAuction(String auctionID) {
		return this.auctions.containsKey(auctionID);
	}

	/**
	 * Registers a new auction.
	 * 
	 * @param auction
	 *            the auction which is to be registered.
	 * @param seller
	 *            the AID of the seller in this auction.
	 */
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

	/**
	 * 
	 * @return the list registered auction.
	 */
	public Set<Auction> getRegisteredAuctions() {
		return new HashSet<Auction>(this.auctions.values());
	}

	/**
	 * Retrieves a registered auction by the AID the the seller who created it.
	 * 
	 * @param auctionID
	 *            the ID of the auction.
	 * 
	 * @return the looked-up auction if found, null otherwise.
	 */
	public Auction findAuction(String auctionID) {
		return this.auctions.get(auctionID);
	}

	/**
	 * 
	 * @param auctionID
	 *            the ID of an auction.
	 * 
	 * @return the seller in this auction.
	 */
	public AID getSeller(String auctionID) {
		return this.sellers.get(auctionID);
	}

	/**
	 * Adds an agent to the set of registered bidder for an auction.
	 * 
	 * @param auctionID
	 *            the AID of the auction.
	 * @param bidderAID
	 *            the AID of the bidder agent who wants to subscribe to the
	 *            auction.
	 */
	public void addSuscriber(String auctionID, AID bidderAID) {
		Set<AID> subscribers = this.subscribers.get(auctionID);

		if (subscribers != null) {
			subscribers.add(bidderAID);
		} else {
			Market.LOGGER.log(Level.WARNING, "auction with ID " + auctionID + " not found.");
		}
	}

	/**
	 * Tells whether a bidder agent is a subscriber on an auction.
	 * 
	 * @param auctionID
	 *            the ID of the auction.
	 * @param bidderAID
	 *            the AID of the bidder agent which is checked.
	 * 
	 * @return true if the bidder agent is found in the subscriber list of the
	 *         auction represented by it's seller AID.
	 */
	public boolean isSuscriber(String auctionID, AID bidderAID) {
		Set<AID> subscribers = this.subscribers.get(auctionID);

		if (subscribers != null) {
			return subscribers.contains(bidderAID);
		} else {
			Market.LOGGER.log(Level.WARNING, "auction with ID " + auctionID + " not found.");

			return false;
		}
	}

	/**
	 * Provides the subscribers of an auction.
	 * 
	 * @param auctionID
	 *            the AID of the seller who created the auction (uniquely
	 *            identifies the auction).
	 * 
	 * @return the list of subscribers for the given auction.
	 */
	public Set<AID> getSubscribers(String auctionID) {
		return this.subscribers.get(auctionID);
	}

	/**
	 * Clears the subscriber list for the given auction.
	 * 
	 * @param auctionID
	 *            the auction which should have no more subscribers.
	 */
	public void clearSubscribers(String auctionID) {
		this.subscribers.get(auctionID).clear();
	}

	/**
	 * Defines the status of an auction.
	 * 
	 * @param auctionID
	 *            the ID of the auction.
	 * @param status
	 *            the new status of the auction (public static fields of class
	 *            Auction).
	 */
	public void setAuctionStatus(String auctionID, int status) {
		Auction auction = this.findAuction(auctionID);

		if (auction != null) {
			auction.setStatus(status);
		} else {
			Market.LOGGER.log(Level.WARNING, "auction with ID " + auctionID + " not found.");
		}
	}

	/**
	 * Updates the auction status and the view when an auction ends with an
	 * attribution.
	 * 
	 * @param auctionID
	 *            the auction which is over.
	 * @param winnerName
	 *            the name of the winner
	 */
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

	/**
	 * Updates the auction status and the view when an auction has been
	 * cancelled.
	 * 
	 * @param auctionID
	 *            the auction which has been cancelled.
	 */
	public void notifyAuctionCancelled(String auctionID) {
		Auction auction = this.findAuction(auctionID);

		if (auction != null) {
			auction.setStatus(Auction.STATUS_CANCELLED);

			// this.myView.refresh();
		} else {
			Market.LOGGER.log(Level.WARNING, "auction with ID " + auctionID + " not found.");
		}
	}

	/**
	 * Defines the status of an auction.
	 * 
	 * @param auctionID
	 *            the ID of the auction.
	 * @param status
	 *            the new status of the auction (public static fields of class
	 *            Auction).
	 */
	public void setAuctionPrice(String auctionID, float price) {
		Auction auction = this.findAuction(auctionID);

		if (auction != null) {
			auction.setCurrentPrice(price);
		} else {
			Market.LOGGER.log(Level.WARNING, "auction with ID " + auctionID + " not found.");
		}
	}

	/**
	 * 
	 * @param auctionID
	 *            an auction that we want to delete.
	 */
	public void deleteAuction(String auctionID) {
		this.auctions.remove(auctionID);
		this.sellers.remove(auctionID);
		this.subscribers.remove(auctionID);

		this.myView.refresh();
	}

	/**
	 * Update the view associated with this agent.
	 */
	public void refreshView() {
		this.myView.refresh();
	}

	/**
	 * Add a description of this agent's services to the DF.
	 * 
	 * @throws FIPAException
	 *             when an error occurs.
	 */
	private void _register2DF() throws FIPAException {
		ServiceDescription sd = new ServiceDescription();
		sd.setType(Market.SERVICE_DESCIPTION);
		sd.setName(this.getAID().getName());

		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(this.getAID());

		dfd.addServices(sd);
		DFService.register(this, dfd);
	}

	/**
	 * Checks whether or not the some values where passed in order to create
	 * seller and bidder agents.
	 * 
	 * <p>
	 * If parameters are present, creates the requested agents.
	 * </p>
	 */
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

