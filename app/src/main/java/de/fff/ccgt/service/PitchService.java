package de.fff.ccgt.service;

import android.util.Log;

import de.fff.ccgt.activity.MainActivity;

public class PitchService {

    private final static String TAG = PitchService.class.getSimpleName();
    private final static String[] PITCHCLASSES = { "C", "C#/Db", "D", "D#/Eb", "E", "F", "F#/Gb", "G", "G#/Ab", "A", "A#/Bb", "B",  };

    public String getNearestPitchClass(double freq, double referenceFrequency) {
        /*
            calculate semitone distance from middle c
            which has approx. 261.6256 Hz
            distance = 9 + (12 log2 (freq / referenceFreq))

            zero means, the note is middle c :-)

            referenceFreq is 440 hz initially
            configurable via slider

            distance will be a double, where the part behind the
            decimal point (period) represents distance from the pitch class
         */

        double distance = getDistance(freq, referenceFrequency);
        int integerDistance = (int) distance;
        double distanceError = Math.abs(distance - integerDistance);

        /*
            choose the pitch that is nearest and
            return its name
            set deviation from the frequency

            for positive values of integerDistance
                deviation up to 0.5 is displayed in positive direction
                higher deviation is displayed from the next int in negative direction
            for negative values of integerDistance
                deviation up to 0.5 is displayed in negative direction
                higher deviation is displayed from the next int in positive direction
         */

        if(integerDistance > 0) {
            if(distanceError > 0.5 || distanceError == 0.5) {
                integerDistance++;
            }
        } else {
            if(distanceError > 0.5 || distanceError == 0.5) {
                integerDistance--;
            }
            if(integerDistance<12) {
                integerDistance = (integerDistance%12)+12;
            }
        }

        return PITCHCLASSES[integerDistance%12];
    }

    public double getCentsDeviation(double freq, double referenceFrequency) {

        double distance = getDistance(freq, referenceFrequency);
        int integerDistance = (int) distance;
        double distanceError = Math.abs(distance - integerDistance);

        double centsDeviation;

        if(distance > 0) {
            if(distanceError > 0.5 || distanceError == 0.5) {
                centsDeviation = (distanceError-1) * 100;
            } else {
                centsDeviation = distanceError * 100;
            }
        } else {
            if(distanceError > 0.5 || distanceError == 0.5) {
                centsDeviation = (1-distanceError) * 100;
            } else {
                centsDeviation = -(distanceError * 100);
            }
        }
//        if(integerDistance == 0 && !Double.isNaN(distance)) {
//            Log.d(TAG, "getCentsDeviation: distance " + distance + " integerDistance " + integerDistance + " distanceError " + distanceError + " centsDeviation " + centsDeviation);
//        }

        return centsDeviation;
    }

    private double getDistance(double freq, double referenceFrequency) {
        return 9 + (12 * (log2(freq / referenceFrequency)  ) );
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
        // TODO: 04.10.24 could abs() be returned? 
        if(distanceError < 0.5) {
            return (int) (250 * distanceError);
        } else {
            return (int) (250 * (1 - distanceError));
        }
    }

}
