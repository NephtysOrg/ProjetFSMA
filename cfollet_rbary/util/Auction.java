package util;
import jade.util.leap.Serializable;

@SuppressWarnings("serial")
/**
 * 
 * @author cfollet & rbary
 *
 */
public class Auction implements Serializable {
	/** The ID of this auction. */
	private String auctionID;

	/** The current price of the auction. */
	private float currentPrice = 0;

	/**
	 * The status of the auction. One of the static fields prefixed with
	 * <i>STATUS_</i>.
	 */
	private int status;

	/** A human friendly name. */
	private String auctionName = "";

	/** The name of the winner of the auction. */
	private String winnerName = "";

	/**
	 * Status code which indicates that an auction has been created, but the
	 * seller has made no announcement yet.
	 */
	public static final int STATUS_CREATED;

	/** Status code which indicates that an auction has ended. */
	public static final int STATUS_RUNNING;

	/** Status code which indicates that an auction has ended. */
	public static final int STATUS_OVER;

	/** Status code which indicates that an auction has ended. */
	public static final int STATUS_CANCELLED;

	static {
		int start = -1;

		STATUS_CREATED = ++start;
		STATUS_RUNNING = ++start;
		STATUS_OVER = ++start;
		STATUS_CANCELLED = ++start;
	}

	/**
	 * Creates a new auction, associated with it seller initiator.
	 * 
	 * @param auctionID
	 *            the ID of this auction.
	 */
	public Auction(String auctionID) {
		this.auctionID = auctionID;

		this.status = Auction.STATUS_CREATED;
	}

	/**
	 * Default constructor.
	 */
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

	/**
	 * 
	 * @return the seller agent who requested the creation of this auction.
	 */
	public String getID() {
		return this.auctionID;
	}

	/**
	 * 
	 * @param auctionID
	 *            the seller agent who requested the creation of this auction.
	 */
	public void setID(String auctionID) {
		this.auctionID = auctionID;
	}

	/**
	 * 
	 * @return the status of the auction. One of the static fields prefixed with
	 *         <i>STATUS_</i>.
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Forces the status of the auction.
	 * 
	 * @param status
	 *            the status of the auction. One of the static fields prefixed
	 *            with <i>STATUS_</i>.
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * Converts an auction status code into a human readable string.
	 * 
	 * @param status
	 *            the auction status code.
	 * 
	 * @return the corresponding string.
	 */
	public static String printStatus(int status) {
		String statusString = "UNKOWN";

		if (status == Auction.STATUS_CREATED) {
			statusString = "CREATED";
		} else if (status == Auction.STATUS_RUNNING) {
			statusString = "RUNNING";

		} else if (status == Auction.STATUS_OVER) {
			statusString = "OVER";

		} else if (status == Auction.STATUS_CANCELLED) {
			statusString = "CANCELLED";

		}

		return statusString;
	}

	/**
	 * 
	 * @return the current price of the auction.
	 */
	public float getCurrentPrice() {
		return currentPrice;
	}

	/**
	 * 
	 * @param currentPrice
	 *            the current price of the auction.
	 */
	public void setCurrentPrice(float currentPrice) {
		this.currentPrice = currentPrice;
	}

	/**
	 * 
	 * @return a human friendly name for the auction.
	 */
	public String getAuctionName() {
		return auctionName;
	}

	/**
	 * 
	 * @param auctionName
	 *            a human friendly name for the auction.
	 */
	public void setAuctionName(String auctionName) {
		this.auctionName = auctionName;
	}

	/**
	 * 
	 * @return the name of the winner of the auction, for an auction which
	 *         successfully ended with an attribution.
	 */
	public String getWinnerName() {
		return winnerName;
	}

	/**
	 * 
	 * @param winnerName
	 *            the name of the winner of the auction, for an auction which
	 *            successfully ended with an attribution.
	 */
	public void setWinnerName(String winnerName) {
		this.winnerName = winnerName;
	}
}

