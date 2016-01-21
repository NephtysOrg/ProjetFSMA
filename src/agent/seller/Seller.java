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
public class Seller extends Agent {
    private AID marketAgentID = null;
    private String fishSupplyName;
    private float minPrice;
    private float maxPrice;
    private float currentPrice;
    private float priceStep;
    private float minPriceStep;
    private int subscriberCount;
    public static final long DEFAULT_BID_WAITING_DURATION = 10 * 1000l; // 10
									// sec
    private long bidWaitingDuration;
    private boolean createCommandReceived;
    private boolean startCommandReceived;
    private boolean cancelCommandReceived;
    private SellerGui myView;
    private static final Logger LOGGER = Logger.getLogger(Seller.class.getName());

    public Seller() {
    }

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

    public AID getMarketAgent() {
	return marketAgentID;
    }

    public float getMinPrice() {
	return minPrice;
    }

    public float getMaxPrice() {
	return maxPrice;
    }

    public float getPriceStep() {
	return priceStep;
    }

    public float getCurrentPrice() {
	return currentPrice;
    }

    public float getMinPriceStep() {
	return minPriceStep;
    }

    public void setMinPriceStep(float minPriceStep) {
	this.minPriceStep = Math.abs(minPriceStep);
	if (this.minPrice > this.priceStep) {
	    Seller.LOGGER.log(Level.WARNING, "Setting the min price step (" + this.minPrice
		    + ") to a value lower than the price step(" + this.priceStep + ").");
	}
    }

    public void setCurrentPrice(float currentPrice) {
	this.currentPrice = Math.max(this.minPrice, Math.min(this.maxPrice, currentPrice));
    }

    public void setMinPrice(float minPrice) {
	this.minPrice = Math.min(Math.abs(minPrice), this.maxPrice);
	this.setCurrentPrice(this.currentPrice);
	this.priceStep = (this.maxPrice - this.minPrice) / 2f;
    }

    public void setMaxPrice(float maxPrice) {
	this.maxPrice = Math.max(Math.abs(maxPrice), this.minPrice);
	this.setCurrentPrice(this.currentPrice);
	this.priceStep = (this.maxPrice - this.minPrice) / 2f;
    }

    public void setPriceStep(float priceStep) {
	this.priceStep = Math.abs(priceStep);
    }

    public void increasePriceStep() {
	this.priceStep += this.priceStep / 2f;
    }

    public void decreasePriceStep() {
	this.priceStep -= this.priceStep / 2f;
    }

    public void increasePrice() {
	this.currentPrice += this.priceStep;
    }

    public void decreasePrice() {
	this.currentPrice -= this.priceStep;
    }

    public long getBidWaitingDuration() {
	return bidWaitingDuration;
    }

    public void setBidWaitingDuration(long bidWaitingDuration) {
	this.bidWaitingDuration = Math.abs(bidWaitingDuration);
    }

    public void notifyCreateCommand() {
	this.createCommandReceived = true;
    }

    public void notifyMarketNotFound() {
	this.createCommandReceived = false;
	this.myView.displayMessage("Agent marché introuvable.");
    }

    public void notifyAuctionCreated() {
	this.myView.notifyAuctionCreated(true);
    }

    public boolean isCreateCommandReceived() {
	return this.createCommandReceived;
    }

    public void notifyMessage(String mess) {
	this.myView.displayMessage(mess);
    }

    public void notifyStartCommand() {
	this.startCommandReceived = true;
    }

    public void notifyAuctionStarted() {
	this.myView.notifyAuctionStarted();
    }

    public boolean isStartCommandReceived() {
	return this.startCommandReceived;
    }

    public void notifyCancelCommand() {
	this.cancelCommandReceived = true;
    }

    public void notifyAuctionCancelled() {
	this.myView.notifyAuctionCancelled();
    }

    public boolean isCancelCommandReceived() {
	return this.cancelCommandReceived;
    }

    public void notifyNewAnnounce() {
	this.myView.notifyNewAnnounce(this.currentPrice);
    }

    public void notifyAuctionOver() {
	this.myView.notifyAuctionOver(this.getCurrentPrice());
    }

    public String getFishSupplyName() {
	return fishSupplyName;
    }

    public void setFishSupplyName(String fishSupplyName) {
	this.fishSupplyName = fishSupplyName;
    }

    public void notifyNewSubscriber() {
	++this.subscriberCount;
	this.myView.notifyNewSubscriber();
    }

    public int getSubscriberCount() {
	return this.subscriberCount;
    }

    public void notifyNewBid() {
	this.myView.notifyNewBid();
    }
}
