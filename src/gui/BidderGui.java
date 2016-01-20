package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import agent.bidder.Bidder;
import util.Auction;

/**
 * 
 * @author cfollet & rbary
 *
 */
public class BidderGui {
    private JPanel panel1;
    private JPanel subscriptionPane;
    private JPanel auctionPane;
    private JLabel subscriptionPaneTitle;
    private JList auctionList;
    private JTable auctionTable;
    private JButton subscribeButton;
    private JButton refreshButton;
    private JButton bidButton;
    private JList bidList;
    private JLabel bidListLabel;
    private JLabel currentAuctionSupply;
    private JLabel currentAuctionReference;
    private JLabel currentAuctionStatus;
    private JCheckBox autoBidCheckBox;
    private JTextField maximumPriceTextField;
    private static String AUTO_BID_MAX_PRICE_NOT_SET = "Please set a maximum price";
    private JFrame currentFrame;
    private DefaultListModel<String> bidListModel;
    private AuctionTableModel auctionTableModel;
    private Bidder _bidderAgent;
    private Map<String, Auction> auctions = new HashMap<String, Auction>();
    /**
     * Almost useless.
     */
    private static int PANE_WIDTH = 175;
    private static int PANE_HEIGHT = 450;
    private static int JSCROLL_PANE_WIDTH = 130;
    private static int JSCROLL_PANE_HEIGHT = 340;

    public BidderGui(Bidder bidderAgent) {
	this._bidderAgent = bidderAgent;
	this.createWindow();
    }

    private void createWindow() {
	this.createSubscriptionPane();
	this.createAuctionPane();
	this.panel1 = new JPanel();
	this.panel1.setLayout(new BoxLayout(this.panel1, BoxLayout.LINE_AXIS));
	this.panel1.add(this.subscriptionPane);
	this.panel1.add(this.auctionPane);
	this.currentFrame = new JFrame("Bidder agent : " + this._bidderAgent.getLocalName());
	this.currentFrame.setContentPane(this.panel1);
	this.currentFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	this.currentFrame.pack();
	this.attachListeners();
	this.findAuctionState();
    }

    public void dispose() {
	this.currentFrame.dispose();
    }

    private void createSubscriptionPane() {
	this.subscriptionPane = new JPanel();
	this.subscriptionPane.setLayout(new BorderLayout(10, 10));
	this.subscriptionPane.setPreferredSize(new Dimension(PANE_WIDTH, PANE_HEIGHT));
	this.subscriptionPaneTitle = new JLabel("Pick an auction");
	this.subscriptionPane.add(this.subscriptionPaneTitle, BorderLayout.PAGE_START);
	this.auctionTableModel = new AuctionTableModel();
	this.auctionTable = new JTable(this.auctionTableModel);
	this.subscriptionPane.add(new JScrollPane(this.auctionTable), BorderLayout.CENTER);
	JPanel buttonsPanel = new JPanel();
	buttonsPanel.setLayout(new GridLayout(2, 1));
	this.subscribeButton = new JButton("Subscribe");
	this.subscribeButton.setEnabled(false);
	buttonsPanel.add(this.subscribeButton);
	this.refreshButton = new JButton("Refresh");
	buttonsPanel.add(this.refreshButton);
	this.subscriptionPane.add(buttonsPanel, BorderLayout.PAGE_END);
    }

    private void createAuctionPane() {
	this.auctionPane = new JPanel();
	this.auctionPane.setLayout(new BoxLayout(this.auctionPane, BoxLayout.PAGE_AXIS));
	this.auctionPane.setPreferredSize(new Dimension(2 * PANE_WIDTH, PANE_HEIGHT));
	this.bidListLabel = new JLabel("Running auction");
	this.auctionPane.add(this.bidListLabel);
	JPanel currentAuctionStatus = new JPanel();
	currentAuctionStatus.setLayout(new FlowLayout(FlowLayout.CENTER));
	this.currentAuctionSupply = new JLabel();
	currentAuctionStatus.add(this.currentAuctionSupply);
	this.currentAuctionReference = new JLabel();
	currentAuctionStatus.add(this.currentAuctionReference);
	this.currentAuctionStatus = new JLabel();
	currentAuctionStatus.add(this.currentAuctionStatus);
	this.auctionPane.add(currentAuctionStatus);
	JScrollPane listScroll = new JScrollPane();
	listScroll.setPreferredSize(new Dimension(2 * JSCROLL_PANE_WIDTH, JSCROLL_PANE_HEIGHT));
	this.bidList = new JList();
	this.bidListModel = new DefaultListModel<String>();
	this.bidList.setModel(this.bidListModel);
	listScroll.setViewportView(this.bidList);
	this.auctionPane.add(listScroll);
	JPanel bidPane = new JPanel();
	bidPane.setLayout(new GridLayout(1, 3));
	this.autoBidCheckBox = new JCheckBox("Auto-bid");
	bidPane.add(this.autoBidCheckBox);
	this.maximumPriceTextField = new JTextField("800");
	bidPane.add(this.maximumPriceTextField);
	this.bidButton = new JButton("Bid");
	bidPane.add(this.bidButton);
	this.auctionPane.add(bidPane);
    }

    @SuppressWarnings("serial")
    private static class AuctionTableModel extends AbstractTableModel {
	private final String[] columnNames = { "Product", "Price" };
	private List<Auction> auctions;

	public AuctionTableModel() {
	    this.auctions = new ArrayList<Auction>();
	}

	public void refreshAuctions(HashSet<Auction> auctions) {
	    this.auctions.clear();
	    this.auctions.addAll(auctions);
	    this.fireTableDataChanged();
	}

	public Auction getAuctionAt(int rowIndex) {
	    return this.auctions.get(rowIndex);
	}

	public void removeAuction(Auction a) {
	    this.auctions.remove(a);
	    this.fireTableDataChanged();
	}

	@Override
	public int getColumnCount() {
	    return columnNames.length;
	}

	@Override
	public int getRowCount() {
	    return auctions.size();
	}

	@Override
	public String getColumnName(int columnIndex) {
	    return columnNames[columnIndex];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
	    Object o = null;
	    switch (columnIndex) {
	    case 0:
		o = auctions.get(rowIndex).getAuctionName();
		break;
	    case 1:
		o = auctions.get(rowIndex).getCurrentPrice();
	    }
	    return o;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
	    Class<?> colClass = null;
	    switch (columnIndex) {
	    case 0:
		colClass = String.class;
		break;
	    case 1:
		colClass = Float.class;
	    }
	    return colClass;
	}
    }

    public void prepare() {
	this.currentFrame = new JFrame("AuctionPicker");
	this.currentFrame.setContentPane(this.panel1);
	this.currentFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	this.currentFrame.pack();
	this.attachListeners();
	this.findAuctionState();
    }

    public void findAuctionState() {
	this.subscribeButton.setEnabled(false);
	this.refreshButton.setEnabled(true);
	this.disableBidButton();
    }

    public void auctionState() {
	this.subscribeButton.setEnabled(false);
	this.refreshButton.setEnabled(false);
    }

    public void display() {
	this.currentFrame.setVisible(true);
    }

    public void hide() {
	this.currentFrame.setVisible(false);
    }

    public void attachListeners() {
	this.refreshButton.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent actionEvent) {
		_bidderAgent.refreshAuctionList();
		// ensure subscribe button is disabled
		subscribeButton.setEnabled(false);
	    }
	});
	this.auctionTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
	    public void valueChanged(ListSelectionEvent e) {
		// enable subscribe butt
		subscribeButton.setEnabled(true);
	    }
	});
	this.subscribeButton.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent actionEvent) {
		Auction selectedAuction = auctionTableModel.getAuctionAt(auctionTable.getSelectedRow());
		_bidderAgent.subscribeToAuction(selectedAuction);
		// remove auction from collecitons
		auctions.remove(selectedAuction);
		auctionTableModel.removeAuction(selectedAuction);
		subscribeButton.setEnabled(false);
	    }
	});
	this.bidButton.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent actionEvent) {
		_bidderAgent.takeUserBidIntoAccount();
		bidButton.setEnabled(false);
	    }
	});
	this.autoBidCheckBox.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent actionEvent) {
		if (autoBidCheckBox.isSelected()) {
		    // Auto bid requested
		    // is max price set ?
		    String maxPriceString = maximumPriceTextField.getText().trim();
		    float maxPrice = Float.parseFloat(maxPriceString.equals("") ? "0" : maxPriceString);
		    if (maxPrice <= 0) {
			alert(BidderGui.AUTO_BID_MAX_PRICE_NOT_SET);
			// Uncheck checkbox
			autoBidCheckBox.setSelected(false);
			// Focus max price field
			maximumPriceTextField.requestFocus();
		    } else {
			// max price is set
			_bidderAgent.setMaxPrice(maxPrice);
			_bidderAgent.setBidsAutomatically(true);
			// disable max price field
			maximumPriceTextField.setEnabled(false);
			// Disable bid button
			disableBidButton();
		    }
		} else {
		    // Auto bid cancelled.
		    _bidderAgent.setBidsAutomatically(false);
		    // enable max price field
		    maximumPriceTextField.setEnabled(true);
		}
	    }
	});
    }

    public void displayAuctionList(HashSet<Auction> auctions) {
	// this.auctionListModel.clear();
	this.auctions.clear();
	for (Auction a : auctions) {
	    this.auctions.put(a.getID(), a);
	}
	this.auctionTableModel.refreshAuctions(auctions);
    }

    public void updateRunningAuctionStatus(String status) {
	this.currentAuctionStatus.setText("[" + status + "]");
    }

    public void clearBidList() {
	this.bidListModel.clear();
    }

    /**
     * Entered auction.
     *
     * @param auction
     */
    public void initBidList(Auction auction) {
	this.clearBidList();
	// this.bidListLabel.setText(auction.getAuctionName());
	this.currentAuctionSupply.setText("[" + auction.getAuctionName() + "]");
	this.currentAuctionReference.setText("[" + auction.getID() + "]");
	this.updateRunningAuctionStatus("STARTING");
	// Remove focus from list element
	this.auctionTable.clearSelection();
    }

    public void addBidInformation(String information) {
	this.bidListModel.addElement(information);
    }

    public void enableBidButton() {
	this.bidButton.setEnabled(true);
    }

    public void disableBidButton() {
	this.bidButton.setEnabled(false);
    }

    public void alert(String message) {
	JOptionPane.showMessageDialog(null, message);
    }
}
