/**
 * File: PAssign08.java
 * @author Brian Abbott
 * Created on: April 2026
 * Description: ATM Machine simulator using JavaFX.
 *     Uses the custom KeyPadPane class (extended as KeyPadCustomPane)
 *     to simulate a real-world ATM with PIN entry, balance inquiry,
 *     withdrawals, and deposits.
 */

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import keypad.KeyPadPane;

import java.util.ArrayList;

public class PAssign08 extends Application {

    // ATM state
    private enum ATMState { IDLE, PIN, MENU, WITHDRAW, DEPOSIT }
    private ATMState currentState = ATMState.IDLE;
    private final String correctPin = "1234";
    private double balance = 1547.63;
    private String inputBuffer = "";
    private int pinAttempts = 0;

    // UI components
    private Label messageLabel;
    private TextField displayField;
    private Button btnWithdraw, btnDeposit, btnBalance, btnExit;

    @Override
    public void start(Stage primaryStage) {
        // Main layout
        BorderPane mainPane = new BorderPane();
        mainPane.setStyle("-fx-background-color: #2C3E50;");

        // --- TOP: Bank header ---
        Label bankLabel = new Label("EAGLE BANK ATM");
        bankLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 28));
        bankLabel.setStyle("-fx-text-fill: #2ECC71;");
        HBox headerBox = new HBox(bankLabel);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(15, 10, 5, 10));
        headerBox.setStyle("-fx-background-color: #1A252F; -fx-border-color: #2ECC71; "
                + "-fx-border-width: 0 0 2 0;");
        mainPane.setTop(headerBox);

        // --- CENTER: Display + Keypad + Menu ---
        VBox centerBox = new VBox(10);
        centerBox.setPadding(new Insets(15));
        centerBox.setAlignment(Pos.CENTER);

        // Display area (screen)
        VBox screenBox = new VBox(5);
        screenBox.setPadding(new Insets(12));
        screenBox.setStyle("-fx-background-color: #0D1F2D; -fx-border-color: #2ECC71; "
                + "-fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5;");

        messageLabel = new Label("Welcome to Eagle Bank");
        messageLabel.setFont(Font.font("Courier New", FontWeight.NORMAL, 14));
        messageLabel.setStyle("-fx-text-fill: #2ECC71;");
        messageLabel.setWrapText(true);
        messageLabel.setPrefWidth(320);

        displayField = new TextField();
        displayField.setEditable(false);
        displayField.setFont(Font.font("Courier New", FontWeight.BOLD, 22));
        displayField.setStyle("-fx-background-color: #0D1F2D; -fx-text-fill: #2ECC71; "
                + "-fx-border-color: #2ECC71; -fx-border-width: 0 0 1 0;");
        displayField.setPrefWidth(320);
        displayField.setAlignment(Pos.CENTER_RIGHT);

        screenBox.getChildren().addAll(messageLabel, displayField);

        // Keypad and side menu layout
        HBox keypadAndMenu = new HBox(15);
        keypadAndMenu.setAlignment(Pos.CENTER);

        // Create the custom keypad
        KeyPadCustomPane keypad = new KeyPadCustomPane();
        keypad.setDisplay(displayField);
        keypad.setATMApp(this);

        // Side menu buttons
        VBox menuBox = new VBox(5);
        menuBox.setAlignment(Pos.CENTER);

        btnWithdraw = createMenuButton("Withdraw");
        btnDeposit = createMenuButton("Deposit");
        btnBalance = createMenuButton("Balance");
        btnExit = createMenuButton("Exit");

        btnWithdraw.setOnAction(e -> handleMenuAction("WITHDRAW"));
        btnDeposit.setOnAction(e -> handleMenuAction("DEPOSIT"));
        btnBalance.setOnAction(e -> handleMenuAction("BALANCE"));
        btnExit.setOnAction(e -> handleMenuAction("EXIT"));

        setMenuButtonsDisabled(true);
        menuBox.getChildren().addAll(btnWithdraw, btnDeposit, btnBalance, btnExit);

        keypadAndMenu.getChildren().addAll(keypad, menuBox);

        // Bottom action buttons
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER);
        actionBox.setPadding(new Insets(8, 0, 0, 0));

        Button btnClear = createActionButton("CLEAR", "#E74C3C");
        Button btnEnter = createActionButton("ENTER", "#2ECC71");
        Button btnCancel = createActionButton("CANCEL", "#F39C12");

        btnClear.setOnAction(e -> handleClear());
        btnEnter.setOnAction(e -> handleEnter());
        btnCancel.setOnAction(e -> handleCancel());

        actionBox.getChildren().addAll(btnClear, btnEnter, btnCancel);

        centerBox.getChildren().addAll(screenBox, keypadAndMenu, actionBox);
        mainPane.setCenter(centerBox);

        // --- BOTTOM: Card slot ---
        Label cardSlotLabel = new Label("[  INSERT CARD  ]");
        cardSlotLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
        cardSlotLabel.setStyle("-fx-text-fill: #95A5A6;");
        HBox bottomBox = new HBox(cardSlotLabel);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(8));
        bottomBox.setStyle("-fx-background-color: #1A252F; -fx-border-color: #2ECC71; "
                + "-fx-border-width: 2 0 0 0;");
        mainPane.setBottom(bottomBox);

        // Set initial state
        setState(ATMState.IDLE);

        // Scene
        Scene scene = new Scene(mainPane, 480, 520);
        primaryStage.setTitle("Eagle Bank ATM");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /** Create a styled side menu button */
    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setPrefSize(100, 40);
        btn.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
        btn.setStyle("-fx-background-color: #34495E; -fx-text-fill: #ECF0F1; "
                + "-fx-border-color: #2ECC71; -fx-border-width: 1; -fx-cursor: hand;");
        btn.setOnMouseEntered(e ->
                btn.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: #1A252F; "
                        + "-fx-border-color: #2ECC71; -fx-border-width: 1; -fx-cursor: hand;"));
        btn.setOnMouseExited(e ->
                btn.setStyle("-fx-background-color: #34495E; -fx-text-fill: #ECF0F1; "
                        + "-fx-border-color: #2ECC71; -fx-border-width: 1; -fx-cursor: hand;"));
        return btn;
    }

    /** Create a styled action button (Clear, Enter, Cancel) */
    private Button createActionButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefSize(100, 35);
        btn.setFont(Font.font("Courier New", FontWeight.BOLD, 13));
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; "
                + "-fx-border-radius: 3; -fx-background-radius: 3; -fx-cursor: hand;");
        return btn;
    }

    /** Enable or disable the side menu buttons */
    private void setMenuButtonsDisabled(boolean disabled) {
        btnWithdraw.setDisable(disabled);
        btnDeposit.setDisable(disabled);
        btnBalance.setDisable(disabled);
        btnExit.setDisable(disabled);
    }

    /** Set ATM state and update display */
    private void setState(ATMState state) {
        currentState = state;
        inputBuffer = "";
        displayField.setText("");

        switch (state) {
            case IDLE:
                messageLabel.setText("Welcome to Eagle Bank\nPress any key to begin");
                setMenuButtonsDisabled(true);
                break;
            case PIN:
                messageLabel.setText("Enter your 4-digit PIN:");
                setMenuButtonsDisabled(true);
                break;
            case MENU:
                messageLabel.setText("Select a transaction:");
                setMenuButtonsDisabled(false);
                break;
            case WITHDRAW:
                messageLabel.setText("Enter withdrawal amount:\nThen press ENTER");
                setMenuButtonsDisabled(true);
                break;
            case DEPOSIT:
                messageLabel.setText("Enter deposit amount:\nThen press ENTER");
                setMenuButtonsDisabled(true);
                break;
        }
    }

    /** Called by KeyPadCustomPane when a key is pressed */
    public void onKeyPressed(String key) {
        System.out.println("Key pressed: " + key);

        switch (currentState) {
            case IDLE:
                setState(ATMState.PIN);
                break;
            case PIN:
                if (inputBuffer.length() < 4) {
                    inputBuffer += key;
                    // Mask PIN with asterisks
                    displayField.setText("*".repeat(inputBuffer.length()));
                }
                break;
            case WITHDRAW:
            case DEPOSIT:
                // Only allow digits and one decimal point
                if (key.equals(".") || key.equals("*")) {
                    if (!inputBuffer.contains(".")) {
                        inputBuffer += ".";
                        displayField.setText("$" + inputBuffer);
                    }
                } else if (inputBuffer.length() < 10) {
                    inputBuffer += key;
                    displayField.setText("$" + inputBuffer);
                }
                break;
            case MENU:
                // Ignore keypad presses in menu mode
                break;
        }
    }

    /** Handle ENTER button */
    private void handleEnter() {
        switch (currentState) {
            case PIN:
                if (inputBuffer.equals(correctPin)) {
                    pinAttempts = 0;
                    System.out.println("PIN accepted. Access granted.");
                    messageLabel.setText("PIN accepted!");
                    displayField.setText("ACCESS GRANTED");
                    // Transition to menu after brief display
                    javafx.animation.PauseTransition pause =
                            new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
                    pause.setOnFinished(e -> setState(ATMState.MENU));
                    pause.play();
                } else {
                    pinAttempts++;
                    System.out.println("Incorrect PIN. Attempt " + pinAttempts + " of 3.");
                    if (pinAttempts >= 3) {
                        messageLabel.setText("Card locked!\nToo many attempts.");
                        displayField.setText("LOCKED");
                        setMenuButtonsDisabled(true);
                    } else {
                        messageLabel.setText("Incorrect PIN. Try again.\n("
                                + (3 - pinAttempts) + " attempts remaining)");
                        inputBuffer = "";
                        displayField.setText("");
                    }
                }
                break;
            case WITHDRAW:
                processWithdrawal();
                break;
            case DEPOSIT:
                processDeposit();
                break;
            default:
                break;
        }
    }

    /** Handle CLEAR button */
    private void handleClear() {
        inputBuffer = "";
        displayField.setText("");
        System.out.println("Display cleared.");
    }

    /** Handle CANCEL button */
    private void handleCancel() {
        System.out.println("Transaction cancelled.");
        if (currentState == ATMState.WITHDRAW || currentState == ATMState.DEPOSIT) {
            setState(ATMState.MENU);
        } else if (currentState == ATMState.PIN) {
            setState(ATMState.IDLE);
        }
    }

    /** Handle side menu actions */
    private void handleMenuAction(String action) {
        switch (action) {
            case "WITHDRAW":
                System.out.println("Selected: Withdraw");
                setState(ATMState.WITHDRAW);
                break;
            case "DEPOSIT":
                System.out.println("Selected: Deposit");
                setState(ATMState.DEPOSIT);
                break;
            case "BALANCE":
                System.out.println("Selected: Balance Inquiry");
                messageLabel.setText("Your current balance:");
                displayField.setText(String.format("$%,.2f", balance));
                System.out.println("Balance: $" + String.format("%,.2f", balance));
                break;
            case "EXIT":
                System.out.println("Session ended. Card ejected.");
                messageLabel.setText("Thank you!\nPlease take your card.");
                displayField.setText("GOODBYE");
                setMenuButtonsDisabled(true);
                javafx.animation.PauseTransition pause =
                        new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
                pause.setOnFinished(e -> {
                    pinAttempts = 0;
                    setState(ATMState.IDLE);
                });
                pause.play();
                break;
        }
    }

    /** Process a withdrawal */
    private void processWithdrawal() {
        try {
            double amount = Double.parseDouble(inputBuffer);
            if (amount <= 0) {
                messageLabel.setText("Invalid amount.\nEnter a positive number.");
                displayField.setText("");
                inputBuffer = "";
            } else if (amount > balance) {
                System.out.println("Withdrawal denied. Insufficient funds.");
                messageLabel.setText("Insufficient funds!\nBalance: $"
                        + String.format("%,.2f", balance));
                displayField.setText("");
                inputBuffer = "";
            } else {
                balance -= amount;
                System.out.println("Withdrew $" + String.format("%,.2f", amount)
                        + ". New balance: $" + String.format("%,.2f", balance));
                messageLabel.setText("Dispensing $" + String.format("%,.2f", amount)
                        + "\nNew balance: $" + String.format("%,.2f", balance));
                displayField.setText("SUCCESS");
                javafx.animation.PauseTransition pause =
                        new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
                pause.setOnFinished(e -> setState(ATMState.MENU));
                pause.play();
            }
        } catch (NumberFormatException ex) {
            messageLabel.setText("Invalid amount.\nPlease try again.");
            displayField.setText("");
            inputBuffer = "";
        }
    }

    /** Process a deposit */
    private void processDeposit() {
        try {
            double amount = Double.parseDouble(inputBuffer);
            if (amount <= 0) {
                messageLabel.setText("Invalid amount.\nEnter a positive number.");
                displayField.setText("");
                inputBuffer = "";
            } else {
                balance += amount;
                System.out.println("Deposited $" + String.format("%,.2f", amount)
                        + ". New balance: $" + String.format("%,.2f", balance));
                messageLabel.setText("Deposited $" + String.format("%,.2f", amount)
                        + "\nNew balance: $" + String.format("%,.2f", balance));
                displayField.setText("SUCCESS");
                javafx.animation.PauseTransition pause =
                        new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
                pause.setOnFinished(e -> setState(ATMState.MENU));
                pause.play();
            }
        } catch (NumberFormatException ex) {
            messageLabel.setText("Invalid amount.\nPlease try again.");
            displayField.setText("");
            inputBuffer = "";
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

/**
 * KeyPadCustomPane - Extended version of KeyPadPane
 * Customizes the keypad styling for an ATM look and overrides
 * registerEventHandlers() to integrate with the ATM display.
 */
class KeyPadCustomPane extends KeyPadPane {
    private TextField display;
    private PAssign08 atmApp;

    /** Create a custom keypad with phone layout */
    public KeyPadCustomPane() {
        super(true); // phone layout with *, 0, #
        customizeStyle();
    }

    /** Set the display TextField that this keypad writes to */
    public void setDisplay(TextField display) {
        this.display = display;
    }

    /** Set reference to the main ATM application for callbacks */
    public void setATMApp(PAssign08 app) {
        this.atmApp = app;
        // Re-register handlers now that we have a reference to the app
        registerEventHandlers();
    }

    /** Apply custom ATM-style appearance to all buttons */
    private void customizeStyle() {
        this.setHgap(5);
        this.setVgap(5);
        this.setPadding(new Insets(5));
        this.setAlignment(Pos.CENTER);

        ArrayList<Button> currList = (copyListButtons != null) ? copyListButtons : listButtons;
        for (Button btn : currList) {
            btn.setPrefSize(65, 50);
            btn.setFont(Font.font("Courier New", FontWeight.BOLD, 18));
            btn.setStyle("-fx-background-color: #34495E; -fx-text-fill: #ECF0F1; "
                    + "-fx-border-color: #7F8C8D; -fx-border-width: 1; "
                    + "-fx-border-radius: 3; -fx-background-radius: 3; -fx-cursor: hand;");

            // Hover effects
            btn.setOnMouseEntered(e ->
                    btn.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: #1A252F; "
                            + "-fx-border-color: #27AE60; -fx-border-width: 1; "
                            + "-fx-border-radius: 3; -fx-background-radius: 3; -fx-cursor: hand;"));
            btn.setOnMouseExited(e ->
                    btn.setStyle("-fx-background-color: #34495E; -fx-text-fill: #ECF0F1; "
                            + "-fx-border-color: #7F8C8D; -fx-border-width: 1; "
                            + "-fx-border-radius: 3; -fx-background-radius: 3; -fx-cursor: hand;"));
        }
    }

    /**
     * Override registerEventHandlers to route key presses
     * to the ATM application instead of just printing to console.
     */
    @Override
    protected void registerEventHandlers() {
        ArrayList<Button> currList = (copyListButtons != null) ? copyListButtons : listButtons;

        for (Button btn : currList) {
            btn.setOnAction(e -> {
                String text = ((Button) e.getSource()).getText().trim();
                if (!text.isEmpty() && atmApp != null) {
                    atmApp.onKeyPressed(text);
                } else {
                    System.out.println("Button was clicked: " + text);
                }
            });
        }
    }
}
