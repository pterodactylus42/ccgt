package de.fff.ccgt.service;

import java.util.Arrays;

public class ConsoleBuffer {

    private final static int ROWS = 20;
    private final String[] rowHistory;
    private final static String firstLine = "-    .    .    .    *    .    .    .    +\n";
    private double tunerLastCentsValue;

    public ConsoleBuffer() {
        tunerLastCentsValue = 0;
        rowHistory = new String[ROWS];
        initHistory();
    }

    public String getNewContents(double centsDeviation) {
        pushRow(centsDeviation);
        return addHeader();
    }

    private void pushRow(double centsDeviation) {
        putToHistory(getHistoryRow(twoPointMovingAverageFilter(centsDeviation)));
    }

    private String addHeader() {
        StringBuffer output = new StringBuffer();
        output.append(firstLine);
        output.append("\n");
        for (String s : rowHistory) {
            output.append(s);
            output.append("\n");
        }
        return output.toString();
    }

    private void putToHistory(String centsString){
        if (rowHistory.length - 1 >= 0)
            System.arraycopy(rowHistory, 0, rowHistory, 1, rowHistory.length - 1);
        rowHistory[0] = centsString;
    }

    private void initHistory(){
        Arrays.fill(rowHistory, "");
    }

    private String getHistoryRow(double cents) {

        int approximateCents = (int) cents;
        StringBuilder tmpstr = new StringBuilder("                                         \n");

        if(Double.isNaN(cents)){
            tmpstr.setCharAt(20, 'I');
            return tmpstr.toString();
        }

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
            tmpstr.setCharAt(20, '|');
        }
        return tmpstr.toString();
    }

    // TODO: 01.11.24 make this filter configurable in prefs
    private double twoPointMovingAverageFilter(double actualCents) {
        double output = (actualCents + tunerLastCentsValue) / 2;
        tunerLastCentsValue = actualCents;
        return output;
    }

}
