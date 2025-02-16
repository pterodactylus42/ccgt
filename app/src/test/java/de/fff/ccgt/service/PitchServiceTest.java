package de.fff.ccgt.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import junit.framework.TestCase;

import org.junit.Before;

public class PitchServiceTest extends TestCase {

    private PitchService pitchService;
    private double referenceFreq;

    @Before
    public void setUp() {
        pitchService = new PitchService();
        referenceFreq = 440.0;
    }

    public void testGetNearestPitchClass() {

        // given clean conditions
        double inputFreq = referenceFreq;
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("A"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("A#/Bb"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("B"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("C"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("C#/Db"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("D"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("D#/Eb"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("E"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("F"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("F#/Gb"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("G"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("G#/Ab"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("A"));

        assertEquals(referenceFreq*2, inputFreq, 0.0000000000004);

        // a little off
        inputFreq = referenceFreq + 5;
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("A"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("A#/Bb"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("B"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("C"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("C#/Db"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("D"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("D#/Eb"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("E"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("F"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("F#/Gb"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("G"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("G#/Ab"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("A"));

        assertEquals(referenceFreq*2, inputFreq, 10.0000000000004);


        // low frequencies have negative distance
        inputFreq = referenceFreq / 4;
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("A"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("A#/Bb"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("B"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("C"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("C#/Db"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("D"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("D#/Eb"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("E"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("F"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("F#/Gb"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("G"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("G#/Ab"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("A"));

        assertEquals(referenceFreq/2, inputFreq, 0.0000000000001);


        // high frequencies
        inputFreq = referenceFreq * 16;
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("A"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("A#/Bb"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("B"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("C"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("C#/Db"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("D"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("D#/Eb"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("E"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("F"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("F#/Gb"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("G"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("G#/Ab"));

        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getNearestPitchClass(inputFreq, referenceFreq),is("A"));

        assertEquals(referenceFreq*32, inputFreq, 0.000000000006);

    }

    public void testGetCentsDeviation() {
        double inputFreq = referenceFreq;
        assertThat(pitchService.getCentsDeviation(inputFreq, referenceFreq),is(0.0));

        inputFreq = referenceFreq * Math.pow(2.0, 0.1/12.0);
        assertEquals(10.0, pitchService.getCentsDeviation(inputFreq, referenceFreq), 0.0000000000001);
        inputFreq = referenceFreq * Math.pow(2.0, 0.2/12.0);
        assertEquals(20.0, pitchService.getCentsDeviation(inputFreq, referenceFreq), 0.000000000001);

        inputFreq = referenceFreq / 4;
        assertThat(pitchService.getCentsDeviation(inputFreq, referenceFreq),is(-0.0));

        inputFreq = referenceFreq * Math.pow(2.0, 0.1/12.0);
        assertEquals(10.0, pitchService.getCentsDeviation(inputFreq, referenceFreq), 0.0000000000002);
        inputFreq = referenceFreq * Math.pow(2.0, 0.2/12.0);
        assertEquals(20.0, pitchService.getCentsDeviation(inputFreq, referenceFreq), 0.000000000001);
        inputFreq = referenceFreq * Math.pow(2.0, 0.3/12.0);
        assertEquals(30.0, pitchService.getCentsDeviation(inputFreq, referenceFreq), 0.000000000001);
        inputFreq = referenceFreq * Math.pow(2.0, 0.4/12.0);
        assertEquals(40.0, pitchService.getCentsDeviation(inputFreq, referenceFreq), 0.000000000001);
        inputFreq = referenceFreq * Math.pow(2.0, 0.5/12.0);
        assertEquals(-50.0, pitchService.getCentsDeviation(inputFreq, referenceFreq), 0.0000000000002);
        inputFreq = referenceFreq * Math.pow(2.0, 0.6/12.0);
        assertEquals(-40.0, pitchService.getCentsDeviation(inputFreq, referenceFreq), 0.000000000001);
        inputFreq = referenceFreq * Math.pow(2.0, 0.7/12.0);
        assertEquals(-30.0, pitchService.getCentsDeviation(inputFreq, referenceFreq), 0.000000000001);
        inputFreq = referenceFreq * Math.pow(2.0, 0.8/12.0);
        assertEquals(-20.0, pitchService.getCentsDeviation(inputFreq, referenceFreq), 0.000000000001);
        inputFreq = referenceFreq * Math.pow(2.0, 0.9/12.0);
        assertEquals(-10.0, pitchService.getCentsDeviation(inputFreq, referenceFreq), 0.000000000002);
        inputFreq = referenceFreq * Math.pow(2.0, 1.0/12.0);
        assertEquals(-0.0, pitchService.getCentsDeviation(inputFreq, referenceFreq), 0.000000000001);
    }

    public void testGetOctave() {
        double inputFreq = referenceFreq;
        assertThat(pitchService.getOctave(inputFreq, referenceFreq),is("4"));
        assertThat(pitchService.getOctave(inputFreq*2, referenceFreq),is("5"));
        assertThat(pitchService.getOctave(inputFreq*4, referenceFreq),is("6"));
    }

    //        can i get to the next semitone directly please?
    //        10 = 9 + (12 * (log2(inputFreq / referenceFreq) ) );
    //        1/12 = log2(inputFreq / referenceFreq);
    //        2^(1/12) = inputFreq / referenceFreq
    //        2^(1/12) * referenceFreq = inputFreq
    public void testGetDistance() {
        double inputFreq = referenceFreq;

        assertThat(pitchService.getDistance(inputFreq, referenceFreq),is(9.0));
        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getDistance(inputFreq, referenceFreq),is(10.0));
        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getDistance(inputFreq, referenceFreq),is(11.0));

        inputFreq = referenceFreq * 2;
        assertThat(pitchService.getDistance(inputFreq, referenceFreq),is(21.0));
        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getDistance(inputFreq, referenceFreq),is(22.0));
        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getDistance(inputFreq, referenceFreq),is(23.0));

        inputFreq = referenceFreq / 4;
        assertThat(pitchService.getDistance(inputFreq, referenceFreq),is(-15.0));
        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getDistance(inputFreq, referenceFreq),is(-14.0));
        inputFreq = inputFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.getDistance(inputFreq, referenceFreq),is(-13.0));

        inputFreq = referenceFreq;
        assertThat(pitchService.getDistance(inputFreq, referenceFreq),is(9.0));
        inputFreq = referenceFreq * Math.pow(2.0, 0.1/12.0);
        assertThat(pitchService.getDistance(inputFreq, referenceFreq),is(9.1));
        inputFreq = referenceFreq * Math.pow(2.0, 0.2/12.0);
        assertEquals(9.2, pitchService.getDistance(inputFreq, referenceFreq), 0.000000000000002);
        inputFreq = referenceFreq * Math.pow(2.0, 0.3/12.0);
        assertEquals(9.3, pitchService.getDistance(inputFreq, referenceFreq), 0.000000000000002);
        inputFreq = referenceFreq * Math.pow(2.0, 0.4/12.0);
        assertEquals(9.4, pitchService.getDistance(inputFreq, referenceFreq), 0.000000000000002);
        inputFreq = referenceFreq * Math.pow(2.0, 0.5/12.0);
        assertEquals(9.5, pitchService.getDistance(inputFreq, referenceFreq), 0.000000000000002);
        inputFreq = referenceFreq * Math.pow(2.0, 0.6/12.0);
        assertEquals(9.6, pitchService.getDistance(inputFreq, referenceFreq), 0.000000000000002);
        inputFreq = referenceFreq * Math.pow(2.0, 0.7/12.0);
        assertEquals(9.7, pitchService.getDistance(inputFreq, referenceFreq), 0.000000000000002);
        inputFreq = referenceFreq * Math.pow(2.0, 0.8/12.0);
        assertEquals(9.8, pitchService.getDistance(inputFreq, referenceFreq), 0.000000000000002);
        inputFreq = referenceFreq * Math.pow(2.0, 0.9/12.0);
        assertEquals(9.9, pitchService.getDistance(inputFreq, referenceFreq), 0.000000000000002);
    }

    public void testDistanceErrorOctetValue() {
        assertThat(pitchService.distanceErrorOctetValue(referenceFreq, referenceFreq),is(0));

        // next semitone
        double inputFreq = referenceFreq * Math.pow(2.0, 1.0/12.0);
        assertThat(pitchService.distanceErrorOctetValue(inputFreq, referenceFreq),is(0));
        double distance = pitchService.getDistance(inputFreq, referenceFreq);
        assertThat(distance,is(10.0));

        inputFreq = referenceFreq * Math.pow(2.0, 0.25/12.0);
        assertThat(pitchService.distanceErrorOctetValue(inputFreq, referenceFreq),is(62));

        inputFreq = referenceFreq * Math.pow(2.0, 0.5/12.0);
        assertThat(pitchService.distanceErrorOctetValue(inputFreq, referenceFreq),is(125));

        inputFreq = referenceFreq * Math.pow(2.0, 0.75/12.0);
        assertThat(pitchService.distanceErrorOctetValue(inputFreq, referenceFreq),is(62));

        inputFreq = referenceFreq * Math.pow(2.0, 0.9/12.0);
        assertThat(pitchService.distanceErrorOctetValue(inputFreq, referenceFreq),is(25));


        inputFreq = referenceFreq;
        assertThat(pitchService.distanceErrorOctetValue(inputFreq, referenceFreq),is(0));
        inputFreq = referenceFreq * Math.pow(2.0, 0.1/12.0);
        assertThat(pitchService.distanceErrorOctetValue(inputFreq, referenceFreq),is(24));
        inputFreq = referenceFreq * Math.pow(2.0, 0.2/12.0);
        assertThat(pitchService.distanceErrorOctetValue(inputFreq, referenceFreq),is(50));
        inputFreq = referenceFreq * Math.pow(2.0, 0.3/12.0);
        assertThat(pitchService.distanceErrorOctetValue(inputFreq, referenceFreq),is(74));
        inputFreq = referenceFreq * Math.pow(2.0, 0.4/12.0);
        assertThat(pitchService.distanceErrorOctetValue(inputFreq, referenceFreq),is(100));
        inputFreq = referenceFreq * Math.pow(2.0, 0.5/12.0);
        assertThat(pitchService.distanceErrorOctetValue(inputFreq, referenceFreq),is(125));
        inputFreq = referenceFreq * Math.pow(2.0, 0.6/12.0);
        assertThat(pitchService.distanceErrorOctetValue(inputFreq, referenceFreq),is(99));
        inputFreq = referenceFreq * Math.pow(2.0, 0.7/12.0);
        assertThat(pitchService.distanceErrorOctetValue(inputFreq, referenceFreq),is(74));
        inputFreq = referenceFreq * Math.pow(2.0, 0.8/12.0);
        assertThat(pitchService.distanceErrorOctetValue(inputFreq, referenceFreq),is(49));
        inputFreq = referenceFreq * Math.pow(2.0, 0.9/12.0);
        assertThat(pitchService.distanceErrorOctetValue(inputFreq, referenceFreq),is(25));

    }


}