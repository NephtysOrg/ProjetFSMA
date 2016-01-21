package util;

import jade.util.leap.Serializable;

@SuppressWarnings("serial")
public class Auction implements Serializable {
    private String auctionID;
    private float currentPrice = 0;
    private int status;
    private String auctionName = "";
    private String winnerName = "";
    public static final int STATUS_CREATED = 0;
    public static final int STATUS_RUNNING = 1;
    public static final int STATUS_OVER = 2;
    public static final int STATUS_CANCELLED = 3;


    public Auction(String auctionID) {
	this.auctionID = auctionID;
	this.status = Auction.STATUS_CREATED;
    }

    public Auction() {
	this(null);
    }

    @Override
    public boolean equals(Object o) {
	boolean equals = false;
	if (o != null) {
	    if (o instanceof Auction) {
		Auction other = (Auction) o;
		equals = this.auctionID.equals(other.getID());
	    }
	}
	return equals;
    }

    @Override
    public String toString() {
	return "Auction {ID: " + this.auctionID + "; status: " + Auction.printStatus(this.status) + "}";
    }

    @Override
    public int hashCode() {
	return this.toString().hashCode();
    }

    public String getID() {
	return this.auctionID;
    }

    public void setID(String auctionID) {
	this.auctionID = auctionID;
    }

    public int getStatus() {
	return status;
    }

    public void setStatus(int status) {
	this.status = status;
    }

    public static String printStatus(int status) {
	String statusString = "Inconnu";
	if (status == Auction.STATUS_CREATED) {
	    statusString = "Créée";
	} else if (status == Auction.STATUS_RUNNING) {
	    statusString = "En cours...";
	} else if (status == Auction.STATUS_OVER) {
	    statusString = "Terminée";
	} else if (status == Auction.STATUS_CANCELLED) {
	    statusString = "Annulée";
	}
	return statusString;
    }

    public float getCurrentPrice() {
	return currentPrice;
    }

    public void setCurrentPrice(float currentPrice) {
	this.currentPrice = currentPrice;
    }

    public String getAuctionName() {
	return auctionName;
    }

    public void setAuctionName(String auctionName) {
	this.auctionName = auctionName;
    }

    public String getWinnerName() {
	return winnerName;
    }

    public void setWinnerName(String winnerName) {
	this.winnerName = winnerName;
    }
}
