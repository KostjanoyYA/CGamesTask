package ru.kostyanoy.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kostyanoy.dataexchange.TestExchanger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.concurrent.atomic.AtomicInteger;

public class TestGUIFormer {

    private JFrame frame;
    private final String name = "Heads and tails Test client";
    private TestExchanger exchanger;
    private static final int REFRESH_TIMEOUT = 1000;
    private static final Font FONT = new Font("Tahoma", Font.PLAIN, 14);
    private static final Logger log = LoggerFactory.getLogger(TestGUIFormer.class);

    public TestGUIFormer(TestExchanger exchanger) {
        this.exchanger = exchanger;
    }

    public void createMainWindow() throws ClassNotFoundException,
            UnsupportedLookAndFeelException,
            InstantiationException,
            IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        frame = new JFrame(name);
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
                    exchanger.stopTest();
                    System.exit(0);
                }
            }
        });

        //Menu
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> frame.getWindowListeners()[0].windowClosing(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)));

        JMenu jMenu = new JMenu("File");
        jMenu.add(exitItem);

        JMenuBar jMenuBar = new JMenuBar();
        jMenuBar.add(jMenu);

        frame.setJMenuBar(jMenuBar);

        //Right panel
        JPanel inputPanel = new JPanel();
        frame.add(inputPanel, BorderLayout.EAST);
        inputPanel.setLayout(new GridLayout(3, 1, 5, 5));

        JTextField clientCountField = new JTextField(String.valueOf(Integer.MAX_VALUE).length());
        clientCountField.setText("1");
        clientCountField.setFont(FONT);
        inputPanel.add(clientCountField);

        JTextField requestIntervalField = new JTextField(String.valueOf(Integer.MAX_VALUE).length());
        requestIntervalField.setText("1000");
        requestIntervalField.setFont(FONT);
        inputPanel.add(requestIntervalField);

        JTextField requestCountField = new JTextField(String.valueOf(Integer.MAX_VALUE).length());
        requestCountField.setText("5");
        requestCountField.setFont(FONT);
        inputPanel.add(requestCountField);


        //Left panel
        JPanel labelPanel = new JPanel();
        frame.add(labelPanel, BorderLayout.WEST);
        labelPanel.setLayout(new GridLayout(3, 1, 5, 5));

        JLabel clientCountLabel = new JLabel("Client count:");
        clientCountLabel.setFont(FONT);
        labelPanel.add(clientCountLabel);

        JLabel requestIntervalLabel = new JLabel("Request interval (ms):");
        requestIntervalLabel.setFont(FONT);
        labelPanel.add(requestIntervalLabel);

        JLabel requestCount = new JLabel("Request count:");
        requestCount.setFont(FONT);
        labelPanel.add(requestCount);


        //Button panel
        JPanel buttonPanel = new JPanel();
        frame.add(buttonPanel, BorderLayout.SOUTH);
        buttonPanel.setLayout(new BorderLayout());

        JButton startTestButton = new JButton("Start test");
        startTestButton.setMinimumSize(new Dimension(30, 100));
        startTestButton.setFocusable(false);
        startTestButton.setFont(FONT);

        AtomicInteger testHasDone = new AtomicInteger(0);
        startTestButton.addActionListener(e -> {
            if (startTestButton.isEnabled()) {
                startTestButton.setEnabled(false);
                testHasDone.set(exchanger.startTest(
                        Integer.parseInt(clientCountField.getText()),
                        Integer.parseInt(requestIntervalField.getText()),
                        Integer.parseInt(requestCountField.getText())));
            }
        });
        buttonPanel.add(startTestButton, BorderLayout.CENTER);

        //Window
        frame.setResizable(false);
        frame.setFont(FONT);
        frame.pack();
        frame.setVisible(true);

        while (!(testHasDone.get() == 0)) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                log.warn(e.getMessage(), e);
            }
            startTestButton.setEnabled(true);
        }
    }
}
