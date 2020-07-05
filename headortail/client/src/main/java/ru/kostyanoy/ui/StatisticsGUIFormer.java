package ru.kostyanoy.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kostyanoy.data.exchange.ClientExchanger;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

import static javax.swing.JOptionPane.showMessageDialog;

public class StatisticsGUIFormer {

    JFrame frame;
    private static final Font FONT = new Font("Tahoma", Font.PLAIN, 14);

    //TODO убрать log
    private static final Logger log = LoggerFactory.getLogger(StatisticsGUIFormer.class);

    public void createTableFrame(List<ClientExchanger.ClientStatistics> statistics) {
        frame = new JFrame("Heads and Tails Test Statistics");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        Object[] columnsHeader = new String[]{"Пользователь", "Успешные запросы", "Неуспешные запросы", "Среднее время запроса, мс"};
        Object[][] cellTexts = new String[statistics.size()][columnsHeader.length];

        log.debug("statistics = {}", statistics.toString());

        int rowNumber = 0;

        for (ClientExchanger.ClientStatistics element : statistics) {
            cellTexts[rowNumber][0] = element.getUsername();
            cellTexts[rowNumber][1] = String.valueOf(element.getSuccessfulRequestCount());
            cellTexts[rowNumber][2] = String.valueOf(element.getExpiredRequests());
            cellTexts[rowNumber][3] = String.valueOf(element.getAverageRequestTime());
            rowNumber++;
        }
        log.debug("cellTexts = {}", Arrays.deepToString(cellTexts));

        JPanel outputPanel = new JPanel();
        frame.add(outputPanel, BorderLayout.CENTER);
        outputPanel.setLayout(new BorderLayout());

        JTable table = new JTable(cellTexts, columnsHeader);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setAutoCreateColumnsFromModel(true);
        table.setCellSelectionEnabled(true);
        table.setShowGrid(true);
        JScrollPane outputTextScrolls = new JScrollPane(table);

        outputPanel.add(outputTextScrolls);
        outputTextScrolls.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        outputTextScrolls.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        outputTextScrolls.createHorizontalScrollBar();
        outputTextScrolls.createVerticalScrollBar();

        frame.setResizable(true);
        frame.setFont(FONT);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void showMessage(String text) {
        showMessageDialog(frame, text);
    }
}
