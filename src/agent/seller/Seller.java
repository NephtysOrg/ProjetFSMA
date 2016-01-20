package agent.seller;

import java.util.logging.Level;
import java.util.logging.Logger;

import agent.market.Market;
import agent.seller.behaviours.SellerFSMBehaviourAuctionCreation;
import gui.SellerGui;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

@SuppressWarnings("serial")
/**
 * 
 * @author cfollet & rbary
 *
 */
public class Seller extends Agent {
	/** The AID of the market agent. */
	private AID marketAgentID = null;

	/** A user friendly name for the auction. */
	private String fishSupplyName;

	/** The minimal value for the price. */
	private float minPrice;

	/** The maximal value for the price. */
	private float maxPrice;

	/** The current price of the fish supply. */
	private float currentPrice;

	/** How much the price can currently be decreased or increased. */
	private float priceStep;

	/** How much the price can currently be decreased or increased. */
	private float minPriceStep;

	/** The number of subscriber for the auction of this seller. */
	private int subscriberCount;

	/** The default value for the bid waiting duration. */
	public static final long DEFAULT_BID_WAITING_DURATION = 10 * 1000l; // 10
																		// sec

	/** The amount of time to wait for bids after an announce. */
	private long bidWaitingDuration;

	/** Tells whether the user decided to start the auction or not. */
	private boolean createCommandReceived;

	/** Tells whether the user decided to start the auction or not. */
	private boolean startCommandReceived;

	/** Tells whether the user decided to start the auction or not. */
	private boolean cancelCommandReceived;

	/** The view for this seller agent. */
	private SellerGui myView;

	/** Allows logging. */
	private static final Logger LOGGER = Logger.getLogger(Seller.class.getName());

	/**
	 * Creates a seller agent of the fish market protocol.
	 */
	public Seller() {

	}

	/**
	 * Sets the agent to its initial state.
	 */
	public void reset() {
		this.bidWaitingDuration = DEFAULT_BID_WAITING_DURATION;
		this.cancelCommandReceived = false;
		this.createCommandReceived = false;
		this.startCommandReceived = false;
		this.subscriberCount = 0;
		this.fishSupplyName = "";

		this.maxPrice = 1000f;
		this.minPrice = 200f;
		this.currentPrice = minPrice;
		this.priceStep = (maxPrice - minPrice) / 2f;
		this.minPriceStep = this.priceStep / 10f;

		if (this.myView != null) {
			this.myView.reset();
		}
	}

	@Override
	protected void setup() {
		this.reset();

		this.addBehaviour(new SellerFSMBehaviourAuctionCreation(this));
		// RunningAuctionSellerFSMBehaviour is to be added when the creation
		// terminates.
		
		this.myView = new SellerGui(this);
		this.myView.setVisible(true);
	}

	@Override
	protected void takeDown() {
		this.myView.dispose();

		super.takeDown();
	}

	/**
	 * Retrieves the AID of the market agent, by asking the DF agent.
	 * 
	 * @return true if the AID has been found, false otherwise.
	 */
	public boolean lookupMarketAgent() {
		if (this.marketAgentID == null) {
			DFAgentDescription marketTemplate = new DFAgentDescription();
			ServiceDescription marketSd = new ServiceDescription();

			// Template for searching the market agent
			marketSd.setType(Market.SERVICE_DESCIPTION);
			marketTemplate.addServices(marketSd);

			try {
				DFAgentDescription[] marketResult = DFService.search(this, marketTemplate);

				if (marketResult.length == 1) {
					this.marketAgentID = marketResult[0].getName();
				} else {
					Seller.LOGGER.log(Level.SEVERE, null, "Logic error: multiple market agents found !");
				}
			} catch (FIPAException fe) {
				Seller.LOGGER.log(Level.INFO, null, fe);
			}
		}

		return this.marketAgentID != null;
	}

	/**
	 * 
	 * @return the AID of the market agent.
	 */
	public AID getMarketAgent() {
		return marketAgentID;
	}

	/**
	 * 
	 * @return the minimal price for the fish supply.
	 */
	public float getMinPrice() {
		return minPrice;
	}

	/**
	 * 
	 * @return the maximal price for the fish supply (for termination issue).
	 */
	public float getMaxPrice() {
		return maxPrice;
	}

	/**
	 * 
	 * @return how much the price can currently be decreased or increased.
	 */
	public float getPriceStep() {
		return priceStep;
	}

	/**
	 * 
	 * @return the current price of the fish supply.
	 */
	public float getCurrentPrice() {
		return currentPrice;
	}

	/**
	 * 
	 * @return the minimal value under which the price step must not go.
	 */
	public float getMinPriceStep() {
		return minPriceStep;
	}

	/**
	 * 
	 * @param minPriceStep
	 *            the minimal value under which the price step must not go.
	 */
	public void setMinPriceStep(float minPriceStep) {
		this.minPriceStep = Math.abs(minPriceStep);

		if (this.minPrice > this.priceStep) {
			Seller.LOGGER.log(Level.WARNING, "Setting the min price step (" + this.minPrice
					+ ") to a value lower than the price step(" + this.priceStep + ").");
		}
	}

	/**
	 * 
	 * @param currentPrice
	 *            the current price of the fish supply.
	 */
	public void setCurrentPrice(float currentPrice) {
		this.currentPrice = Math.max(this.minPrice, Math.min(this.maxPrice, currentPrice));
	}

	/**
	 * 
	 * @param minPrice
	 *            the minimal price at which the fish supply can be sold.
	 */
	public void setMinPrice(float minPrice) {
		this.minPrice = Math.min(Math.abs(minPrice), this.maxPrice);

		this.setCurrentPrice(this.currentPrice);

		this.priceStep = (this.maxPrice - this.minPrice) / 2f;
	}

	/**
	 * 
	 * @param minPrice
	 *            the maximal price at which the fish supply can be sold.
	 */
	public void setMaxPrice(float maxPrice) {
		this.maxPrice = Math.max(Math.abs(maxPrice), this.minPrice);

		this.setCurrentPrice(this.currentPrice);

		this.priceStep = (this.maxPrice - this.minPrice) / 2f;
	}

	/**
	 * 
	 * @param priceStep
	 *            how much the price can currently be decreased or increased.
	 */
	public void setPriceStep(float priceStep) {
		this.priceStep = Math.abs(priceStep);
	}

	/**
	 * Increase the amount by which the price can currently be increased.
	 */
	public void increasePriceStep() {
		this.priceStep += this.priceStep / 2f;
	}

	/**
	 * Increase the amount by which the price can currently be decreased.
	 */
	public void decreasePriceStep() {
		this.priceStep -= this.priceStep / 2f;
	}

	/**
	 * Increases the price by the current price step.
	 */
	public void increasePrice() {
		this.currentPrice += this.priceStep;
	}

	/**
	 * Decreases the price by the current price step.
	 */
	public void decreasePrice() {
		this.currentPrice -= this.priceStep;
	}

	/**
	 * 
	 * @return the amount of time to wait for bids after an announce.
	 */
	public long getBidWaitingDuration() {
		return bidWaitingDuration;
	}

	/**
	 * 
	 * @param bidWaitingDuration
	 *            the amount of time to wait for bids after an announce.
	 */
	public void setBidWaitingDuration(long bidWaitingDuration) {
		this.bidWaitingDuration = Math.abs(bidWaitingDuration);
	}

	/**
	 * Notifies that the user decided to start the auction.
	 */
	public void notifyCreateCommand() {
		this.createCommandReceived = true;
	}

	/**
	 * Notifies that the user decided to start the auction.
	 */
	public void notifyMarketNotFound() {
		this.createCommandReceived = false;

		this.myView.displayMessage("Market agent not found :_(");
	}

	/**
	 * Notifies the view that the auction has been created.
	 */
	public void notifyAuctionCreated() {
		this.myView.notifyAuctionCreated(true);
	}

	/**
	 * 
	 * @return true if a start auction command has been received from the user,
	 *         false otherwise.
	 */
	public boolean isCreateCommandReceived() {
		return this.createCommandReceived;
	}

	/**
	 * Notifies the view of the message.
	 */
	public void notifyMessage(String mess) {
		this.myView.displayMessage(mess);
	}

	/**
	 * Notifies that the user decided to start the auction.
	 */
	public void notifyStartCommand() {
		this.startCommandReceived = true;
	}

	/**
	 * Notifies the view that the auction has been started.
	 */
	public void notifyAuctionStarted() {
		this.myView.notifyAuctionStarted();
	}

	/**
	 * 
	 * @return true if a start auction command has been received from the user,
	 *         false otherwise.
	 */
	public boolean isStartCommandReceived() {
		return this.startCommandReceived;
	}

	/**
	 * Notifies that the user decided to cancel the auction.
	 */
	public void notifyCancelCommand() {
		this.cancelCommandReceived = true;
	}

	/**
	 * Notifies the view that the auction has been cancelled.
	 */
	public void notifyAuctionCancelled() {
		this.myView.notifyAuctionCancelled();
	}

	/**
	 * 
	 * @return true if a cancel auction command has been received from the user,
	 *         false otherwise.
	 */
	public boolean isCancelCommandReceived() {
		return this.cancelCommandReceived;
	}

	/**
	 * Notifies the user that a new announce has been make.
	 */
	public void notifyNewAnnounce() {
		this.myView.notifyNewAnnounce(this.currentPrice);
	}

	/**
	 * Notifies the view that the auction successfully ended.
	 */
	public void notifyAuctionOver() {
		this.myView.notifyAuctionOver(this.getCurrentPrice());
	}

	/**
	 * 
	 * @return a user friendly name for the auction.
	 */
	public String getFishSupplyName() {
		return fishSupplyName;
	}

	/**
	 * 
	 * @param fishSupplyName
	 *            a user friendly name for the auction.
	 */
	public void setFishSupplyName(String fishSupplyName) {
		this.fishSupplyName = fishSupplyName;
	}

	/**
	 * Notifies that a new bidder agent subscribed to the auction created by
	 * this agent.
	 */
	public void notifyNewSubscriber() {
		++this.subscriberCount;

		this.myView.notifyNewSubscriber();
	}

	/**
	 * 
	 * @return the number of subscribers for the auction of this seller.
	 */
	public int getSubscriberCount() {
		return this.subscriberCount;
	}

	/**
	 * Notifies that a new bid for the last announced price.
	 */
	public void notifyNewBid() {
		this.myView.notifyNewBid();
	}
}
