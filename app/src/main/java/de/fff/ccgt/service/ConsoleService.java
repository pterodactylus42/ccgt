package de.fff.ccgt.service;

import java.util.Arrays;

public class ConsoleService {

    private final static int ROWS = 20;
    private final String[] rowHistory;
    private final static String firstLine = "-    .    .    .    *    .    .    .    +\n";
    private double tunerLastCentsValue;

    public ConsoleService() {
        tunerLastCentsValue = 0;
        rowHistory = new String[ROWS];
        initRowHistory();
    }

    public String newConsoleContents(double centsDeviation) {
        addTopRow(centsDeviation);
        return addHeaderLine();
    }

    private void addTopRow(double centsDeviation) {
        putCentsToHistory(getHistoryRow(twoPointMovingAverageFilter(centsDeviation)));
    }

    private String addHeaderLine() {
        StringBuffer output = new StringBuffer();
        output.append(firstLine);
        output.append("\n");
        for (String s : rowHistory) {
            output.append(s);
            output.append("\n");
        }
        return output.toString();
    }



    private void putCentsToHistory(String centsString){
        if (rowHistory.length - 1 >= 0)
            System.arraycopy(rowHistory, 0, rowHistory, 1, rowHistory.length - 1);
        rowHistory[0] = centsString;
    }

    private void initRowHistory(){
        Arrays.fill(rowHistory, "");
    }

    private String getHistoryRow(double cents) {
        int approximateCents = (int) cents;
        StringBuilder tmpstr = new StringBuilder("                                         \n");

        if(cents < -3) {
            if(cents < -40) {
                //value is too big, display it at the bottom (left)
                tmpstr.setCharAt(0, '|');
            } else {
                //from middle, go one char to the left per 3 cents
                tmpstr.setCharAt(20+approximateCents/3, '>');
            }
        } else if (cents > 3) {
            if(cents > 40) {
                //value is too big, display it at the top (right)
                tmpstr.setCharAt(40, '|');
            } else {
                //from middle, go one char to the right per 3 cents
                tmpstr.setCharAt(20+approximateCents/3, '<');
            }

        } else {
            tmpstr.setCharAt(20, 'I');
        }
        return tmpstr.toString();
    }

    private double twoPointMovingAverageFilter(double actualCents) {
        double output = (actualCents + tunerLastCentsValue) / 2;
        tunerLastCentsValue = actualCents;
        return output;
    }


}
