package util;

import java.io.Serializable;

/**
 * @author rbary
 *
 */
public class Auction implements Serializable {
	
	/** auction ID **/
	private String _auctionID;
	
	/** current price of the auction **/
	private float _currentPrice = 0;
	
	/** auction name **/
	private String _auctionName = null;
	
	/** name of the auction's winner */
	private String _winnerName = null;
	
	/** status of the auction **/
	private int _status;
	
	/** code status relating to a auction creation **/
	public static final int AUCTION_CREATED = 0;
	
	/** status code relating to a auction running **/
	public static final int AUCTION_RUNNING = 1;
	
	/** status code relating to a auction ending **/
	public static final int AUCTION_ENDED = 2;
	
	/** status code relating to a auction canceling **/
	public static final int AUCTION_CANCELLED = 3;
	
	/** constructor **/
	public Auction(String _auctionID) {
		this._auctionID = _auctionID;
		this._status = AUCTION_CREATED;
	}
	
	/** default constructor **/
	public Auction(){
		this(null);
	}
	
	/** status printing **/
	public static String printStatus(int status){
		String status_string=null;
		
		switch (status) {
		case Auction.AUCTION_CREATED:
			status_string = "CREATED";
			break;
		case Auction.AUCTION_RUNNING:
			status_string = "RUNNING";
			break;
		case Auction.AUCTION_CANCELLED:
			status_string = "CANCELLED";
			break;
		case Auction.AUCTION_ENDED:
			status_string = "ENDED";
		default:
			status_string = "UNDEFINED";
			break;
		}
		return status_string;
	}

	@Override
	public boolean equals(Object obj) {
		boolean equals = false;
		if(obj != null){
			if(obj instanceof Auction){
				Auction other = (Auction)obj;
				equals = this._auctionID.equals(other.get_auctionID());
			}
		}
		return equals;
	}

	@Override
	public String toString() {
		return "Auction {ID: "+
				this._auctionID +
				"; status: " +
				Auction.printStatus(this._status) +
				"}";
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	/** getters and setters **/
	public String get_auctionID() {
		return _auctionID;
	}

	public void set_auctionID(String _auctionID) {
		this._auctionID = _auctionID;
	}

	public float get_currentPrice() {
		return _currentPrice;
	}

	public void set_currentPrice(float _currentPrice) {
		this._currentPrice = _currentPrice;
	}

	public String get_auctionName() {
		return _auctionName;
	}

	public void set_auctionName(String _auctionName) {
		this._auctionName = _auctionName;
	}

	public String get_winnerName() {
		return _winnerName;
	}

	public void set_winnerName(String _winnerName) {
		this._winnerName = _winnerName;
	}

	public int get_status() {
		return _status;
	}

	public void set_status(int _status) {
		this._status = _status;
	}
}
