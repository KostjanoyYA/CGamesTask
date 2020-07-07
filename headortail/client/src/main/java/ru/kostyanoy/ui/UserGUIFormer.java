package ru.kostyanoy.ui;

import ru.kostyanoy.data.exchange.ClientExchanger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import static javax.swing.JOptionPane.showMessageDialog;

/**
 * Creates GUI for single player mode game
 */
public class UserGUIFormer {

    private JFrame frame;
    private final ClientExchanger exchanger;
    private JLabel accountLabel;
    private JLabel roundResultValue;
    private JComboBox comboBox;
    private static final Font FONT = new Font("Tahoma", Font.PLAIN, 14);

    public UserGUIFormer(ClientExchanger exchanger) {
        this.exchanger = exchanger;
    }

    /**
     * Gets access to UI element that needs to be updated during data exchange
     * @return  {@link JLabel} that needs to be updated
     */
    public JLabel getAccountLabel() {
        return accountLabel;
    }

    /**
     * Gets access to UI element that needs to be updated during data exchange
     * @return  {@link JLabel} that needs to be updated
     */
    public JLabel getRoundResultValue() {
        return roundResultValue;
    }

    /**
     * Gets access to UI element that needs to be updated during data exchange
     * @return  {@link JComboBox} that needs to be updated
     */
    public JComboBox getComboBox() {
        return comboBox;
    }

    /**
     * Creates the main window
     */
    public void createMainWindow() {

        frame = new JFrame("Heads and tails client");
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setMinimumSize(new Dimension(320, 240));
        frame.setLocationByPlatform(true);
        frame.addWindowListener(new WindowListener() {
            public void windowActivated(WindowEvent event) {}

            public void windowClosed(WindowEvent event) {}

            public void windowDeactivated(WindowEvent event) {}

            public void windowDeiconified(WindowEvent event) {}

            public void windowIconified(WindowEvent event) {}

            public void windowOpened(WindowEvent event) {}

            public void windowClosing(WindowEvent event) {
                Object[] options = {"Exit", "Cancel!"};
                int rc = JOptionPane.showOptionDialog(
                        event.getWindow(), "Exit now?",
                        "Exit confirmation", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null, options, options[0]);
                if (rc == 0) {
                    event.getWindow().setVisible(false);
                    exchanger.stopExchange();
                    event.getWindow().dispose();
                    System.exit(0);
                }
            }
        });

        //Right panel
        JPanel inputPanel = new JPanel();
        frame.add(inputPanel, BorderLayout.EAST);
        inputPanel.setLayout(new GridLayout(4, 1, 5, 5));

        JTextField betField = new JTextField(String.valueOf(Integer.MAX_VALUE).length());
        betField.setText("1");
        betField.setFont(FONT);
        inputPanel.add(betField);

        comboBox = new JComboBox(exchanger.getPossibleOptions());
        inputPanel.add(comboBox);

        roundResultValue = new JLabel(exchanger.getPreviousRoundResult());
        roundResultValue.setFont(FONT);
        inputPanel.add(roundResultValue);

        accountLabel = createLabelOnPanel("0", inputPanel);

        //Left panel
        JPanel labelPanel = new JPanel();
        frame.add(labelPanel, BorderLayout.WEST);
        labelPanel.setLayout(new GridLayout(4, 1, 5, 5));


        createLabelOnPanel("Enter your bet here:", labelPanel);
        createLabelOnPanel("Your choice:", labelPanel);
        createLabelOnPanel("Previous round result:", labelPanel);
        createLabelOnPanel("Your account:", labelPanel);

        //Button panel
        JPanel buttonPanel = new JPanel();
        frame.add(buttonPanel, BorderLayout.SOUTH);
        buttonPanel.setLayout(new BorderLayout());

        JButton makeBetButton = new JButton("Make a bet");
        makeBetButton.setMinimumSize(new Dimension(30, 100));
        makeBetButton.setFocusable(false);
        makeBetButton.setFont(FONT);
        makeBetButton.addActionListener(e ->
                exchanger.sendStake(Math.abs(Long.parseLong(betField.getText())), (String) comboBox.getSelectedItem()));
        buttonPanel.add(makeBetButton, BorderLayout.CENTER);

        //Window
        frame.setResizable(false);
        frame.setFont(FONT);
        frame.pack();
        frame.setVisible(true);
    }

    private JLabel createLabelOnPanel(String text, JPanel panel) {
        JLabel label = new JLabel(text);
        label.setFont(FONT);
        panel.add(label);
        return label;
    }

    //PopUps
    /**
     * Creates pop up asking nickname for registration on the server
     * @return  string result of user input
     */
    public String askNickName() {
        return JOptionPane.showInputDialog(
                frame,
                "Enter your Nickname:",
                "Game nickname",
                JOptionPane.QUESTION_MESSAGE);
    }

    /**
     * Creates pop up asking exit of application
     * @return  int result of user choice
     */
    public int askExit(String message) {
        int userChoise = JOptionPane.showConfirmDialog(
                frame,
                message,
                "Exit", JOptionPane.YES_NO_OPTION);
        if (userChoise == JOptionPane.NO_OPTION) {
            exchanger.stopExchange();
            System.exit(0);
        }
        return userChoise;
    }

    /**
     * Creates the pop up with the given text
     * @param message the shown text
     */
    public void showMessage(String message) {
        showMessageDialog(frame, message);
    }
}
