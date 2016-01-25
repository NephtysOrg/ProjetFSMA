package gui;

import java.util.Iterator;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import agent.market.Market;
import util.Auction;

public class MarketGui extends JFrame {
    private Market myAgent;
    private JTable tableView;
    private MarketTableModel tableViewModel;
    public static final int DEFAULT_WIDTH = 600;
    public static final int DEFAULT_HEIGHT = 400;
    private static final long serialVersionUID = 1837117931407701695L;

    public MarketGui(Market myAgent) {
	this.myAgent = myAgent;
	// The table view
	this.tableViewModel = new MarketTableModel(myAgent);
	this.tableView = new JTable(this.tableViewModel);
	// The scroll pane
	JScrollPane scrollPane = new JScrollPane(this.tableView);
	this.tableView.setFillsViewportHeight(true);
	// This frame
	this.setTitle("Agent Marché : " + this.myAgent.getAID().getLocalName());
	this.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	this.setLocationRelativeTo(null);
	this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	this.getContentPane().add(scrollPane);
    }

    public void refresh() {
	this.tableViewModel.fireTableDataChanged();
    }

    private static class MarketTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -216459483509477696L;
	private Market myAgent;
	private static final String[] COLUMN_NAMES = new String[] { "Vendeur", "Preneurs", "# Abonnés", "Prix courant",
		"Etat", "Gagnant" };

	public MarketTableModel(Market myAgent) {
	    this.myAgent = myAgent;
	}

	@Override
	public int getColumnCount() {
	    return MarketTableModel.COLUMN_NAMES.length;
	}

	@Override
	public int getRowCount() {
	    return this.myAgent.getRegisteredAuctions().size();
	}

	@Override
	public String getColumnName(int column) {
	    return MarketTableModel.COLUMN_NAMES[column];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
	    String value = "";
	    Set<Auction> auctionSet = this.myAgent.getRegisteredAuctions();
	    Iterator<Auction> it = auctionSet.iterator();
	    for (int i = 0; i < rowIndex && it.hasNext(); ++i)
		it.next();
	    switch (columnIndex) {
	    case 0: // Offer
		value = it.next().getAuctionName();
		break;
	    case 1: // Seller
		value = this.myAgent.getSeller(it.next().getID()).getLocalName();
		break;
	    case 2: // # Suscribers
		value = String.valueOf(this.myAgent.getSubscribers(it.next().getID()).size());
		break;
	    case 3: // Current price
		value = String.valueOf(it.next().getCurrentPrice());
		break;
	    case 4: // Status
		value = Auction.printStatus(it.next().getStatus());
		break;
	    case 5: // Winner
		value = it.next().getWinnerName();
		break;
	    }
	    return value;
	}
    }
}
