package gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

import agent.seller.Seller;

public class SellerGui extends JFrame implements ActionListener {
    private Seller myAgent;
    private JSpinner minPriceSpinner;
    private JSpinner maxPriceSpinner;
    private JSpinner startingPriceSpinner;
    private JSpinner priceStepSpinner;
    private JSpinner minPriceStepSpinner;
    private JSpinner waitingBidDurationSpinner;
    private JSpinner subscriberCountSpinner;
    private JTable currentAnnounceTable;
    private JTable announceHistoryTable;
    private JTextField fishSupplyNameTextField;
    private JLabel messageLabel;
    private JButton createButton;
    private JButton startButton;
    private JButton cancelButton;
    public static final int DEFAULT_WIDTH = 600;
    public static final int DEFAULT_HEIGHT = 350;
    private static final String CREATE_BUTTON_ACTION_COMMAND = "CREATE_BUTTON_ACTION_COMMAND";
    private static final String CANCEL_BUTTON_ACTION_COMMAND = "CANCEL_BUTTON_ACTION_COMMAND";
    private static final String START_BUTTON_ACTION_COMMAND = "START_BUTTON_ACTION_COMMAND";
    private static final long serialVersionUID = 5048176398667805254L;

    public SellerGui(Seller myAgent) {
	this.myAgent = myAgent;
	this.instantianteWidgets();
	this.addListeners();
	this.assemble();
	this.setTitle("Agent vendeur : " + this.myAgent.getAID().getLocalName());
	this.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	this.setLocationRelativeTo(null);
	this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
	String command = event.getActionCommand();
	if (command.equals(CREATE_BUTTON_ACTION_COMMAND)) {
	    this.myAgent.setFishSupplyName(this.fishSupplyNameTextField.getText());
	    this.myAgent.notifyCreateCommand();
	} else if (command.equals(START_BUTTON_ACTION_COMMAND)) {
	    this.myAgent.notifyStartCommand();
	} else if (command.equals(CANCEL_BUTTON_ACTION_COMMAND)) {
	    this.myAgent.notifyCancelCommand();
	}
    }

    public void reset(String displayMessage) {
	this.setMinPrice(this.myAgent.getMinPrice());
	this.setMaxPrice(this.myAgent.getMaxPrice());
	this.setStartingPrice(this.myAgent.getCurrentPrice());
	this.setPriceStep(this.myAgent.getPriceStep());
	this.setMinPriceStep(this.myAgent.getMinPriceStep());
	this.setSubscriberCount(this.myAgent.getSubscriberCount());
	((SellerTableModel) this.currentAnnounceTable.getModel()).reset();
	((SellerTableModel) this.announceHistoryTable.getModel()).reset();
	if (displayMessage != null) {
	    this.displayMessage(displayMessage);
	}
    }

    public void reset() {
	this.reset(null);
    }

    public void setMinPrice(float minPrice) {
	this.minPriceSpinner.setValue((double) minPrice);
    }

    public void setMaxPrice(float maxPrice) {
	this.maxPriceSpinner.setValue((double) maxPrice);
    }

    public void setStartingPrice(float price) {
	this.startingPriceSpinner.setValue((double) price);
    }

    public void setPriceStep(float priceStep) {
	this.priceStepSpinner.setValue((double) priceStep);
    }

    public void setMinPriceStep(float minPriceStep) {
	this.minPriceSpinner.setValue((double) minPriceStep);
    }

    public void setWaitBidDuration(long millis) {
	this.waitingBidDurationSpinner.setValue((int) millis);
    }

    public void setSubscriberCount(int subscriberCount) {
	this.subscriberCountSpinner.setValue(subscriberCount);
    }

    public void notifyNewSubscriber() {
	int subscriberCount = (Integer) this.subscriberCountSpinner.getValue();
	if (subscriberCount == 0) {
	    this.startButton.setEnabled(true);
	}
	this.subscriberCountSpinner.setValue(++subscriberCount);
	this.messageLabel.setText("Nouvel abonné !");
    }

    public void notifyNewAnnounce(float newPrice) {
	SellerTableModel historyModel = (SellerTableModel) this.announceHistoryTable.getModel();
	SellerTableModel currentAnnounceModel = (SellerTableModel) this.currentAnnounceTable.getModel();
	if (currentAnnounceModel.getRowCount() == 1) {
	    Float currentPrice = (Float) currentAnnounceModel.getValueAt(0, 0);
	    Integer currentBidCount = (Integer) currentAnnounceModel.getValueAt(0, 1);
	    historyModel.addValue(currentPrice, currentBidCount);
	    currentAnnounceModel.setValueAt(newPrice, 0, SellerTableModel.PRICE_COLUMN);
	    currentAnnounceModel.setValueAt(0, 0, SellerTableModel.BID_COUNT_COLUMN);
	    historyModel.fireTableDataChanged();
	} else {
	    currentAnnounceModel.addValue(newPrice);
	}
	currentAnnounceModel.fireTableDataChanged();
    }

    public void notifyNewBid() {
	SellerTableModel currentAnnounceModel = (SellerTableModel) this.currentAnnounceTable.getModel();
	Integer currentBidCount = (Integer) currentAnnounceModel.getValueAt(0, SellerTableModel.BID_COUNT_COLUMN);
	currentAnnounceModel.setValueAt(++currentBidCount, 0, SellerTableModel.BID_COUNT_COLUMN);
	currentAnnounceModel.fireTableDataChanged();
    }

    public void notifyAuctionOver(float price) {
	this.notifyAuctionCreated(false); // TODO : reschedule create auction
					  // behaviour.
	this.messageLabel.setText(this.myAgent.getFishSupplyName() + " vendu au prix " + price + " !");
    }

    public float getMinPrice() {
	return ((Double) this.minPriceSpinner.getValue()).floatValue();
    }

    public float getMaxPrice() {
	return ((Double) this.maxPriceSpinner.getValue()).floatValue();
    }

    public float getStartingPrice() {
	return ((Double) this.startingPriceSpinner.getValue()).floatValue();
    }

    public float getPriceStep() {
	return ((Double) this.priceStepSpinner.getValue()).floatValue();
    }

    public float getMinPriceStep() {
	return ((Double) this.minPriceStepSpinner.getValue()).floatValue();
    }

    public long getWaitBidDuration() {
	return ((Integer) this.waitingBidDurationSpinner.getValue()).intValue();
    }

    public void displayMessage(String mess) {
	this.messageLabel.setText(mess);
    }

    public void notifyAuctionCreated(boolean isCreated) {
	this.fishSupplyNameTextField.setEnabled(!isCreated);
	this.createButton.setEnabled(!isCreated);
	this.minPriceSpinner.setEnabled(isCreated);
	this.maxPriceSpinner.setEnabled(isCreated);
	this.startingPriceSpinner.setEnabled(isCreated);
	this.priceStepSpinner.setEnabled(isCreated);
	this.minPriceStepSpinner.setEnabled(isCreated);
	this.waitingBidDurationSpinner.setEnabled(isCreated);
	this.cancelButton.setEnabled(isCreated);
	this.startButton.setEnabled(false); // never before notifyNewSubscriber
	if (isCreated) {
	    this.messageLabel.setText("Enchère crée! En attente d'abonnés...");
	}
    }

    private void instantianteWidgets() {
	this.minPriceSpinner = new JSpinner(new SpinnerNumberModel((double) this.myAgent.getMinPrice(), 0d,
		(double) this.myAgent.getMinPrice() * 10, (double) this.myAgent.getMinPrice() / 10));
	this.maxPriceSpinner = new JSpinner(new SpinnerNumberModel((double) this.myAgent.getMaxPrice(), 0d,
		(double) this.myAgent.getMaxPrice() * 10, (double) this.myAgent.getMaxPrice() / 10));
	this.startingPriceSpinner = new JSpinner(new SpinnerNumberModel((double) this.myAgent.getCurrentPrice(), 0d,
		(double) this.myAgent.getCurrentPrice() * 10, (double) this.myAgent.getCurrentPrice() / 10));
	this.priceStepSpinner = new JSpinner(new SpinnerNumberModel((double) this.myAgent.getPriceStep(), 0d,
		(double) this.myAgent.getPriceStep() * 10, (double) this.myAgent.getPriceStep() / 10));
	this.minPriceStepSpinner = new JSpinner(new SpinnerNumberModel((double) this.myAgent.getMinPriceStep(), 0d,
		(double) this.myAgent.getMinPriceStep() * 10, (double) this.myAgent.getMinPriceStep() / 10));
	this.waitingBidDurationSpinner = new JSpinner(new SpinnerNumberModel((int) this.myAgent.getBidWaitingDuration(),
		0, (int) this.myAgent.getBidWaitingDuration() * 10, (int) this.myAgent.getBidWaitingDuration() / 10));
	this.subscriberCountSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
	this.subscriberCountSpinner.setEnabled(false);
	this.fishSupplyNameTextField = new JTextField("Vente de poisson");
	this.fishSupplyNameTextField.requestFocus(true);
	this.currentAnnounceTable = new JTable(new SellerTableModel());
	this.announceHistoryTable = new JTable(new SellerTableModel());
	this.messageLabel = new JLabel("Pas encore d'enchère créée.");
	this.messageLabel
		.setFont(this.messageLabel.getFont().deriveFont(this.messageLabel.getFont().getStyle() & ~Font.BOLD));
	this.createButton = new JButton("Créer");
	this.cancelButton = new JButton("Annuler");
	this.startButton = new JButton("Démarrer");
	this.notifyAuctionCreated(false);
    }

    public void notifyAuctionStarted() {
	this.minPriceSpinner.setEnabled(false);
	this.maxPriceSpinner.setEnabled(false);
	this.cancelButton.setEnabled(false);
	this.startButton.setEnabled(false);
	this.messageLabel.setText("L'enchère a démarré !");
    }

    public void notifyAuctionCancelled() {
	this.notifyAuctionCreated(false); // TODO : reschedule create auction
					  // behaviour.
	this.messageLabel.setText("Enchère annulée !");
    }

    private void addListeners() {
	this.minPriceSpinner.addChangeListener(new ChangeListener() {
	    @Override
	    public void stateChanged(ChangeEvent e) {
		JSpinner spinner = (JSpinner) e.getSource();
		myAgent.setMinPrice(((Double) spinner.getValue()).floatValue());
	    }
	});
	this.maxPriceSpinner.addChangeListener(new ChangeListener() {
	    @Override
	    public void stateChanged(ChangeEvent e) {
		JSpinner spinner = (JSpinner) e.getSource();
		myAgent.setMaxPrice(((Double) spinner.getValue()).floatValue());
	    }
	});
	this.startingPriceSpinner.addChangeListener(new ChangeListener() {
	    @Override
	    public void stateChanged(ChangeEvent e) {
		JSpinner spinner = (JSpinner) e.getSource();
		myAgent.setCurrentPrice(((Double) spinner.getValue()).floatValue());
	    }
	});
	this.priceStepSpinner.addChangeListener(new ChangeListener() {
	    @Override
	    public void stateChanged(ChangeEvent e) {
		JSpinner spinner = (JSpinner) e.getSource();
		myAgent.setPriceStep(((Double) spinner.getValue()).floatValue());
	    }
	});
	this.minPriceStepSpinner.addChangeListener(new ChangeListener() {
	    @Override
	    public void stateChanged(ChangeEvent e) {
		JSpinner spinner = (JSpinner) e.getSource();
		myAgent.setMinPriceStep(((Double) spinner.getValue()).floatValue());
	    }
	});
	this.waitingBidDurationSpinner.addChangeListener(new ChangeListener() {
	    @Override
	    public void stateChanged(ChangeEvent e) {
		JSpinner spinner = (JSpinner) e.getSource();
		myAgent.setBidWaitingDuration(((Integer) spinner.getValue()).longValue());
	    }
	});
	this.fishSupplyNameTextField.addFocusListener(new FocusListener() {
	    @Override
	    public void focusGained(FocusEvent e) {
		fishSupplyNameTextField.select(0, fishSupplyNameTextField.getText().length());
	    }

	    @Override
	    public void focusLost(FocusEvent e) {
		fishSupplyNameTextField.select(0, 0);
	    }
	});
	this.createButton.setActionCommand(CREATE_BUTTON_ACTION_COMMAND);
	this.createButton.addActionListener(this);
	this.startButton.setActionCommand(START_BUTTON_ACTION_COMMAND);
	this.startButton.addActionListener(this);
	this.cancelButton.setActionCommand(CANCEL_BUTTON_ACTION_COMMAND);
	this.cancelButton.addActionListener(this);
    }

    private void assemble() {
	// Left pane
	JPanel leftPane = this.createLeftPane();
	// Right Pane
	JPanel rightPane = this.createRightPane();
	// This frame
	Container windowPane = this.getContentPane();
	windowPane.setLayout(new BorderLayout(10, 0));
	windowPane.add(leftPane, BorderLayout.WEST);
	windowPane.add(rightPane, BorderLayout.CENTER);
    }

    private JPanel createLeftPane() {
	JPanel leftPane = new JPanel();
	leftPane.setLayout(new BoxLayout(leftPane, BoxLayout.Y_AXIS));
	// Fish supply pane
	JPanel fishSupplyPanel = new JPanel();
	fishSupplyPanel.setBorder(this.createTitleBorder("Enchère"));
	fishSupplyPanel.setLayout(new GridLayout(0, 2));
	fishSupplyPanel.add(new JLabel("Nom"));
	fishSupplyPanel.add(this.fishSupplyNameTextField);
	fishSupplyPanel.add(new JLabel(""));
	fishSupplyPanel.add(this.createButton);
	// Price configuration
	JPanel priceConfigPanel = new JPanel();
	priceConfigPanel.setBorder(this.createTitleBorder("Prix"));
	priceConfigPanel.setLayout(new GridLayout(0, 2));
	priceConfigPanel.add(new JLabel("Min"));
	priceConfigPanel.add(this.minPriceSpinner);
	priceConfigPanel.add(new JLabel("Max"));
	priceConfigPanel.add(this.maxPriceSpinner);
	priceConfigPanel.add(new JLabel("Pas"));
	priceConfigPanel.add(this.priceStepSpinner);
	priceConfigPanel.add(new JLabel("Pas Min"));
	priceConfigPanel.add(this.minPriceStepSpinner);
	// Price configuration
	JPanel bidWaitingConfigPanel = new JPanel();
	bidWaitingConfigPanel.setBorder(this.createTitleBorder("Attente"));
	bidWaitingConfigPanel.setLayout(new GridLayout(0, 2));
	bidWaitingConfigPanel.add(new JLabel("Temps"));
	bidWaitingConfigPanel.add(this.waitingBidDurationSpinner);
	// Subscription feedback
	JPanel subscriptionsFeedbackPanel = new JPanel();
	subscriptionsFeedbackPanel.setBorder(this.createTitleBorder("Abonnés"));
	subscriptionsFeedbackPanel.setLayout(new GridLayout(0, 2));
	subscriptionsFeedbackPanel.add(new JLabel("Enregistré"));
	subscriptionsFeedbackPanel.add(this.subscriberCountSpinner);
	// Buttons pane
	JPanel buttonsPanel = new JPanel();
	buttonsPanel.setBorder(this.createTitleBorder("Enchère"));
	buttonsPanel.setLayout(new GridLayout(0, 2));
	buttonsPanel.add(this.cancelButton);
	buttonsPanel.add(this.startButton);
	// Assemble
	leftPane.add(fishSupplyPanel);
	leftPane.add(priceConfigPanel);
	leftPane.add(bidWaitingConfigPanel);
	leftPane.add(subscriptionsFeedbackPanel);
	leftPane.add(buttonsPanel);
	return leftPane;
    }

    private JPanel createRightPane() {
	JPanel rightPane = new JPanel();
	rightPane.setLayout(new BoxLayout(rightPane, BoxLayout.Y_AXIS));
	rightPane.setBorder(this.createTitleBorder("Progression de l'enchère"));
	// Messages label
	JPanel messagePanel = new JPanel();
	messagePanel.setBorder(this.createTitleBorder("Info"));
	messagePanel.setLayout(new GridLayout(0, 1));
	messagePanel.add(this.messageLabel);
	// Current announce price panel
	JPanel currentAnnouncePanel = new JPanel();
	this.currentAnnounceTable.setSize(currentAnnouncePanel.getWidth(), 100);
	currentAnnouncePanel.setBorder(this.createTitleBorder("Proposition courante"));
	currentAnnouncePanel.setLayout(new BoxLayout(currentAnnouncePanel, BoxLayout.Y_AXIS));
	currentAnnouncePanel.add(this.currentAnnounceTable.getTableHeader());
	currentAnnouncePanel.add(this.currentAnnounceTable);
	// Current announce price panel
	JPanel announceHistoryPanel = new JPanel();
	announceHistoryPanel.setBorder(this.createTitleBorder("Historique"));
	JScrollPane scrollPane = new JScrollPane(this.announceHistoryTable);
	announceHistoryPanel.setLayout(new BoxLayout(announceHistoryPanel, BoxLayout.Y_AXIS));
	announceHistoryPanel.add(scrollPane);
	// Assemble
	rightPane.add(messagePanel);
	rightPane.add(currentAnnouncePanel);
	rightPane.add(announceHistoryPanel);
	return rightPane;
    }

    private TitledBorder createTitleBorder(String title) {
	return BorderFactory.createTitledBorder(title);
    }

    private static class SellerTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 8031870852112649553L;
	private List<Float> prices = new ArrayList<Float>();
	private List<Integer> bids = new ArrayList<Integer>();
	private static final String[] COLUMN_NAMES = new String[] { "Prix", "Annonces" };
	public static final int PRICE_COLUMN = 0;
	public static final int BID_COUNT_COLUMN = 1;

	@Override
	public int getColumnCount() {
	    return COLUMN_NAMES.length;
	}

	@Override
	public int getRowCount() {
	    return prices.size();
	}

	@Override
	public String getColumnName(int column) {
	    return SellerTableModel.COLUMN_NAMES[column];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
	    if (columnIndex == PRICE_COLUMN) {
		return prices.get(rowIndex);
	    } else {
		return bids.get(rowIndex);
	    }
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	    if (columnIndex == PRICE_COLUMN) {
		prices.set(rowIndex, (Float) aValue);
	    } else {
		bids.set(rowIndex, (Integer) aValue);
	    }
	}

	public void addValue(float price, int bidCount) {
	    prices.add(0, price);
	    bids.add(0, bidCount);
	}

	public void addValue(float price) {
	    this.addValue(price, 0);
	}

	public void reset() {
	    this.prices.clear();
	    this.bids.clear();
	}
    }
}
