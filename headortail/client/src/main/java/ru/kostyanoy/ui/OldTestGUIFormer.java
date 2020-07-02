//package ru.kostyanoy.ui;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import ru.kostyanoy.dataexchange.ClientExchanger;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.WindowEvent;
//import java.awt.event.WindowListener;
//import java.util.Arrays;
//
//import static javax.swing.JOptionPane.showMessageDialog;
//
//public class TestGUIFormer implements VisualPresenter {
//
//    private JFrame frame;
//    private JTextArea outputTextArea;
//    private String name = "Heads and tails client";
//    private ClientExchanger exchanger;
//    private static final int REFRESH_TIMEOUT = 2000;
//    private static final Logger log = LoggerFactory.getLogger(TestGUIFormer.class);
//
//    public TestGUIFormer(ClientExchanger exchanger) {
//        this.exchanger = exchanger;
//    }
//
//    @Override
//    public void createMainWindow() throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
//        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//
//        frame = new JFrame(name);
//        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
//        frame.setMinimumSize(new Dimension(640, 480));
//        frame.setLocationByPlatform(true);
//        frame.addWindowListener(new WindowListener() {
//            public void windowActivated(WindowEvent event) {
//            }
//
//            public void windowClosed(WindowEvent event) {
//            }
//
//            public void windowDeactivated(WindowEvent event) {
//            }
//
//            public void windowDeiconified(WindowEvent event) {
//            }
//
//            public void windowIconified(WindowEvent event) {
//            }
//
//            public void windowOpened(WindowEvent event) {
//            }
//
//            public void windowClosing(WindowEvent event) {
//                Object[] options = {"Exit", "Cancel!"};
//                int rc = JOptionPane.showOptionDialog(
//                        event.getWindow(), "Exit now?",
//                        "Exit confirmation", JOptionPane.YES_NO_OPTION,
//                        JOptionPane.QUESTION_MESSAGE,
//                        null, options, options[0]);
//                if (rc == 0) {
//                    event.getWindow().setVisible(false);
//                    exchanger.stopExchange();
//                    System.exit(0);
//                }
//            }
//        });
//
//        //Menu
//        JMenuItem exitItem = new JMenuItem("Exit");
//        exitItem.addActionListener(e -> frame.getWindowListeners()[0].windowClosing(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)));
//
//        JMenu jMenu = new JMenu("File");
//        jMenu.add(exitItem);
//
//        JMenuBar jMenuBar = new JMenuBar();
//        jMenuBar.add(jMenu);
//
//        frame.setJMenuBar(jMenuBar);
//
//        //Output
//        JPanel outputPanel = new JPanel();
//        frame.add(outputPanel, BorderLayout.SOUTH);
//        outputPanel.setLayout(new BorderLayout());
//        outputTextArea = new JTextArea(name + " started\n\nРезультат:\n");
//        outputTextArea.setEditable(false);
//        JScrollPane outputTextScrolls = new JScrollPane(outputTextArea);
//
//        outputPanel.add(outputTextScrolls);
//        outputTextScrolls.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
//        outputTextScrolls.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//        outputTextScrolls.createHorizontalScrollBar();
//        outputTextScrolls.createVerticalScrollBar();
//
//        //Right panel
//        JPanel inputPanel = new JPanel();
//        frame.add(inputPanel, BorderLayout.EAST);
//        inputPanel.setLayout(new BorderLayout());
//
//        JTextField playerCountField = new JTextField(String.valueOf(Integer.MAX_VALUE).length());
//        playerCountField.setText("1");
//        inputPanel.add(playerCountField, BorderLayout.PAGE_START);
//
//        JTextField requestPeriodField = new JTextField(String.valueOf(Integer.MAX_VALUE).length());
//        requestPeriodField.setText("1000");
//        inputPanel.add(requestPeriodField, BorderLayout.LINE_END);
//
//        JTextField requestCountField = new JTextField(String.valueOf(Integer.MAX_VALUE).length());
//        requestCountField.setText("10");
//        inputPanel.add(requestCountField, BorderLayout.LINE_END);
//
//        //Left panel
//        JPanel labelPanel = new JPanel();
//        frame.add(labelPanel, BorderLayout.WEST);
//        labelPanel.setLayout(new BorderLayout());
//
//        JLabel playerCountLabel = new JLabel("Count of players");
//        labelPanel.add(playerCountLabel, BorderLayout.PAGE_START);
//
//        JLabel requestPeriodLabel = new JLabel("Period of request sending");
//        labelPanel.add(requestPeriodLabel, BorderLayout.LINE_END);
//
//        JLabel requestCountLabel = new JLabel("Count of requests");
//        labelPanel.add(requestCountLabel, BorderLayout.LINE_END);
//
//        JLabel statusLabel = new JLabel("Ready for start");
//        labelPanel.add(statusLabel, BorderLayout.LINE_END);
//
//        //Start button
//        JButton startButton = new JButton("Start");
//        startButton.setMinimumSize(new Dimension(30, 60));
//        startButton.setFocusable(false);
//        startButton.addActionListener(e -> {
//            statusLabel.setText("Process is running");
//            while (exchanger.isGameAllowed()) {
//
//                client.getPlayerState();
//            }
//
//            while (exchanger.getStatistics().isEmpty()) {
//
//            }
//
//
//        });
//
//        inputPanel.add(startButton, BorderLayout.NORTH);
//
//        //Window
//        frame.setResizable(false);
//        frame.pack();
//        frame.setVisible(true);
//
//        while (exchanger.getConnection().isConnected()) {
//            newParticipantList = exchanger.getParticipants().clone();
//            Arrays.sort(newParticipantList);
//            listModel.clear();
//            for (String participant : newParticipantList) {
//                listModel.addElement(participant);
//            }
//
//            newMessages = exchanger.getIncomingMessages().clone();
//            for (String message : newMessages) {
//                outputTextArea.append(message + "\n");
//            }
//            exchanger.clearUnansweredMessages();
//
//            try {
//                Thread.sleep(REFRESH_TIMEOUT);
//            } catch (InterruptedException e) {
//                log.warn(e.getMessage(), e);
//            }
//        }
//    }
//
//
//    //PopUps
//    @Override
//    public String askServerIP() {
//        String userString = JOptionPane.showInputDialog(
//                frame,
//                "Enter IP Address of the Server:",
//                "Welcome to the " + name,
//                JOptionPane.QUESTION_MESSAGE);
//        if (userString == null) {
//            //frame.getWindowListeners()[0].windowClosing(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
//            System.exit(0);
//        }
//        return userString;
//
//    }
//
//    @Override
//    public String askNickName() {
//        String userString = JOptionPane.showInputDialog(
//                frame,
//                "Enter your Nickname:",
//                "Chat nickname",
//                JOptionPane.QUESTION_MESSAGE);
//        if (userString == null) {
//            askServerIP();
//        }
//        return userString;
//    }
//
//    @Override
//    public int askExit(String message) {
//        int userChoise = JOptionPane.showConfirmDialog(
//                frame,
//                message,
//                "Exit", JOptionPane.YES_NO_OPTION);
//        if (userChoise == JOptionPane.NO_OPTION) {
//            exchanger.stopExchange();
//            System.exit(0);
//        }
//        return userChoise;
//    }
//
//    @Override
//    public void showMessage(String message) {
//        showMessageDialog(frame, message);
//    }
//}
