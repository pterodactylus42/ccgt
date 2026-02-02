package de.fff.ccgt.view;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ConsoleBufferTest {

    @Test
    public void testCharEncoding() {
        char c = (char) 0;
        while(! (c == '|')) {
            c++;
        }
        assertTrue("The int for | should be 124.", c == 124);
    }

    @Test
    public void testMiddleMarkerChar() {
        ConsoleBuffer consoleBuffer = new ConsoleBuffer();

        String consoleContentsPitched = consoleBuffer.push(0);
        assertNotNull(consoleContentsPitched);
        System.out.println(consoleContentsPitched);
        assertFalse("Console should not use a capital I as marker character when pitched", consoleContentsPitched.contains("I"));

        String consoleContentsUnPitched = consoleBuffer.push(Double.NaN);
        assertNotNull(consoleContentsUnPitched);
        System.out.println(consoleContentsUnPitched);
        assertTrue("Console should use a capital I as marker character when unpitched", consoleContentsUnPitched.contains("I"));
    }

}
