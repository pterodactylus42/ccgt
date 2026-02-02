package de.fff.ccgt.view;

import java.util.Arrays;

public class ConsoleBuffer {

    private final static int ROWS = 20;
    private final String[] rowHistory;
    private final static String firstLine = "-    .    .    .    *    .    .    .    +\n";

    public ConsoleBuffer() {
        rowHistory = new String[ROWS];
        Arrays.fill(rowHistory, "");
    }

    public String push(double centsDeviation) {
        if (rowHistory.length - 1 >= 0)
            System.arraycopy(rowHistory, 0, rowHistory, 1, rowHistory.length - 1);
        rowHistory[0] = asRow(centsDeviation);
        return withHeader();
    }

    private String withHeader() {
        StringBuffer output = new StringBuffer();
        output.append(firstLine);
        output.append("\n");
        for (String s : rowHistory) {
            output.append(s);
            output.append("\n");
        }
        return output.toString();
    }

    private String asRow(double cents) {
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

}
