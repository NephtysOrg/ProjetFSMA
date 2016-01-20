package agent.bidder;

import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import agent.bidder.behaviours.BidderFSMBehaviourAuctionProgress;
import agent.bidder.behaviours.BidderFSMBehaviourHandshake;
import agent.market.Market;
import gui.BidderGui;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import util.Auction;
/**
 * 
 * @author cfollet & rbary
 *
 */
@SuppressWarnings("serial")
public class Bidder extends Agent {
	protected Logger logger = Logger.getLogger(Bidder.class.getName());

	private boolean _bidsAutomatically = false;

	private Auction _selectedAuction;

	private FSMBehaviour _handshakeFSM = null;
	private FSMBehaviour _auctionProgressFSM = null; 

	private BidderGui _bidderView;

	private float _maxPrice;

	private static final String NO_MARKET_AVAILABLE = "Market agent couldn't be located.";

	private float _biddingPrice;

	private boolean _withinBiddingTimeFrame = false;
	private boolean _answerBid = false;
	private boolean _auctionSelected = false;

	@Override
	protected void setup() {
		// Look up market
		super.setup();

		// find new auction
		this._bidderView = new BidderGui(this);
		this._bidderView.display();
	}

	@Override
	protected void takeDown() {
		this._bidderView.hide();

		this._bidderView.dispose();

		// Cancel auction
		super.takeDown();
	}

	/**
	 * Called at the end of the auction subscription process.
	 *
	 * Creates a FSM that manages the bidding process.
	 */
	public void createBidderFSM() {
		this._bidderView.initBidList(this.getSubscribedAuction());

		this.removeAllFSMBehaviours();

		//this.runningAuctionFSM = new RunningAuctionBidderFSMBehaviour(this);
		this._auctionProgressFSM = new BidderFSMBehaviourAuctionProgress(this);

		this.addBehaviour(this._auctionProgressFSM);

		// Disable find auction buttons
		this._bidderView.auctionState();
	}

	/**
	 * Creates a FSM that manages auction subscription for bidders.
	 */
	public void createAuctionFinderFSM() {
		this.removeAllFSMBehaviours();

		this._handshakeFSM = new BidderFSMBehaviourHandshake(this);
		// Register behaviour
		this.addBehaviour(this._handshakeFSM);

	}

	/**
	 * Removes currently attached behaviours.
	 */
	private void removeAllFSMBehaviours() {

		if (this._handshakeFSM != null) {
			this.removeBehaviour(this._handshakeFSM);
		}

		if (this._auctionProgressFSM != null) {
			this.removeBehaviour(this._auctionProgressFSM);
		}

	}

	/**
	 * Called by RunningAuctionBidderFSMBehaviour end states.
	 */
	public void auctionOver() {
		this._bidderView.findAuctionState();
	}

	public void displayAlert(String message) {
		this._bidderView.alert(message);
	}

	public void displayAuctionInformation(String message) {
		this._bidderView.addBidInformation(message);
	}

	/**
	 * Called by BidderView when refresh button is used.
	 */
	public void refreshAuctionList() {
		this.createAuctionFinderFSM();
	}

	/**
	 * Called by PickAuctionBehaviour.
	 * 
	 * @param auctionList
	 */
	public void displayAuctionList(HashSet<Auction> auctionList) {
		this._bidderView.displayAuctionList(auctionList);
	}

	public void setBidsAutomatically(boolean bidsAutomatically) {
		this._bidsAutomatically = bidsAutomatically;
	}

	public boolean bidsAutomatically() {
		return this._bidsAutomatically;
	}

	public void setAuctionSelected(boolean auctionSelected) {
		this._auctionSelected = auctionSelected;
	}

	public boolean hasAuctionSelected() {
		return _auctionSelected;
	}

	/**
	 * Called by BidderView when an auction is selected and the suscribe button
	 * is used.
	 *
	 * @param selectedAuction
	 */
	public void subscribeToAuction(Auction selectedAuction) {
		this._selectedAuction = selectedAuction;
		this._auctionSelected = true;

		// //notify behaviour by sending a message
		// ACLMessage unblockMessage = new
		// ACLMessage(FishMarket.Performatives.TO_ANNOUNCE);
		// unblockMessage.addReceiver(SubscribeToAuctionMarketFSMBehaviour.MESSAGE_TOPIC);
		// unblockMessage.addReceiver(this.getAID());
		// this.send(unblockMessage);
	}

	public Auction getSubscribedAuction() {
		return this._selectedAuction;
	}

	/**
	 * Called by BidderView when the bid button is clicked.
	 */
	public void takeUserBidIntoAccount() {
		this._answerBid = true;
	}

	public boolean isWithinBiddingTimeFrame() {
		return _withinBiddingTimeFrame;
	}

	public void setWithinBiddingTimeFrame(boolean withinBiddingTimeFrame) {
		this._withinBiddingTimeFrame = withinBiddingTimeFrame;
	}

	public boolean answerBid() {
		return _answerBid;
	}

	public void setAnswerBid(boolean answerBid) {
		this._answerBid = answerBid;
	}

	/**
	 * Called when a new bid is received.
	 *
	 * @param information
	 */
	public void displayBidInformation(String information) {
		this._bidderView.addBidInformation(information);
	}

	/**
	 * Displays an alert signaling that market is not available.
	 */
	public void alertNoMarket() {
		this._bidderView.alert(Bidder.NO_MARKET_AVAILABLE);
	}

	public float getMaxPrice() {
		return _maxPrice;
	}

	public void setMaxPrice(float maxPrice) {
		this._maxPrice = maxPrice;
	}

	/**
	 * Returns the price which came with the last to_announce.
	 *
	 * @return
	 */
	public float getBiddingPrice() {
		return this._biddingPrice;
	}

	/**
	 * Called by behaviours when a new announce is received.
	 */
	public void handleAnnounce(float price) {
		this._biddingPrice = price;

		this._bidderView.addBidInformation("New price announce " + String.valueOf(price));

		if (this.bidsAutomatically()) {
			// disable bid button
			this._bidderView.disableBidButton();
		} else {
			// enable bid button
			this._bidderView.enableBidButton();
		}
	}

	public void updateAuctionStatus(String status) {
		this._bidderView.updateRunningAuctionStatus(status);
	}

	public AID getMarketAgentAID() {
		AID marketAgent = null;

		DFAgentDescription marketTemplate = new DFAgentDescription();
		ServiceDescription marketSd = new ServiceDescription();

		// Template for searching the market agent
		marketSd.setType(Market.SERVICE_DESCIPTION);
		marketTemplate.addServices(marketSd);

		try {
			DFAgentDescription[] marketResult = DFService.search(this, marketTemplate);

			if (marketResult.length == 1) {
				marketAgent = marketResult[0].getName();
			} else {
				logger.log(Level.SEVERE, null, "Logic error: multiple market agents found !");
			}
		} catch (FIPAException fe) {
			logger.log(Level.SEVERE, null, fe);
		}

		return marketAgent;
	}

}
