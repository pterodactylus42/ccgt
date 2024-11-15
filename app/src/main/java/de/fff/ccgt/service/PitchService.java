package de.fff.ccgt.service;

import android.util.Log;

import de.fff.ccgt.activity.MainActivity;

public class PitchService {

    private final static String TAG = PitchService.class.getSimpleName();
    private final static String[] PITCHCLASSES = { "C", "C#/Db", "D", "D#/Eb", "E", "F", "F#/Gb", "G", "G#/Ab", "A", "A#/Bb", "B",  };

    public String getNearestPitchClass(double freq, double referenceFrequency) {

        double distance = getDistance(freq, referenceFrequency);
        int integerDistance = (int) distance;
        double distanceError = Math.abs(distance - ((int) distance));

//            for positive values of integerDistance
//                deviation up to 0.5 is displayed in positive direction
//                higher deviation is displayed from the next int in negative direction
//            for negative values of integerDistance
//                deviation up to 0.5 is displayed in negative direction
//                higher deviation is displayed from the next int in positive direction

        if(distance > 0) {
            if(distanceError >= 0.5) {
                integerDistance++;
            }
        } else {
            if(distanceError >= 0.5) {
                integerDistance--;
            }
            // invert negative distance
            integerDistance = (integerDistance%12)+12;
        }
        return PITCHCLASSES[integerDistance%12];
    }

    public double getCentsDeviation(double freq, double referenceFrequency) {

        double distance = getDistance(freq, referenceFrequency);
        double distanceError = Math.abs(distance - ((int) distance));

        double centsDeviation;
        if(distance > 0) {
            if(distanceError >= 0.5) {
                centsDeviation = (distanceError-1) * 100;
            } else {
                centsDeviation = distanceError * 100;
            }
        } else {
            if(distanceError >= 0.5) {
                centsDeviation = (1-distanceError) * 100;
            } else {
                centsDeviation = -(distanceError * 100);
            }
        }
        return centsDeviation;
    }

    double getDistance(double freq, double referenceFrequency) {
        return 9 + (12 * (log2(freq / referenceFrequency) ) );
    }

    private double log2(double value) {
        return Math.log( value ) / Math.log( 2.0 );
    }

    public String getOctave(double freq, double referenceFrequency) {
        double distance;
        distance = 9 + (12 * (log2(freq/referenceFrequency)  ) );
        return Integer.toString((int) ((distance / 12) + 4));
    }

    public int distanceErrorOctetValue(double freq, double referenceFrequency) {

        double distance = getDistance(freq, referenceFrequency);
        double distanceError = Math.abs(distance - ((int) distance));

        if(distanceError < 0.5) {
            return (int) (250 * distanceError);
        } else {
            return (int) (250 * (1 - distanceError));
        }

    }

}
