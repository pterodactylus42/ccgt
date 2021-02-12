package de.fff.ccgt;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;



import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;


/*
 * ccgt -  concole curses guitar tuner
 *                _
 *   ___ ___ __ _| |_
 *  / __/ __/ _` | __|
 * | (_| (_| (_| | |_
 *  \___\___\__, |\__|
 *          |___/
 *
 * just tune, don't mess with gui's :-)
 *
 *
 * big thanks to joren six, olmo cornelis and marc leman
 * now using yin implementation and some other pitch
 * tracking algorithms from tarsos dsp
 *
 *
 * JorenSix/TarsosDSP is licensed under the
 * GNU General Public License v3.0
 * Permissions of this strong copyleft license
 * are conditioned on making available complete
 * source code of licensed works and modifications,
 * which include larger works using a licensed work,
 * under the same license. Copyright and license
 * notices must be preserved. Contributors provide
 * an express grant of patent rights.
 *
 */


public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private AudioDispatcher dispatcher;
    private PitchDetectionHandler pitchDetectionHandler;
    private AudioProcessor audioProcessor;
    private float pitchInHz = 0;
    private double centsDeviation = 0;

    private final static String PITCHCLASS[] = { "C", "C#/Db", "D", "D#/Eb", "E", "F", "F#/Gb", "G", "G#/Ab", "A", "A#/Bb", "B",  };

    //audio config values
    private final static double REFERENCE_FREQ = 440.0;
    private final static int SAMPLERATE = 8000;
    private final static int BUFFERSIZE = 2048;
    private final static int OVERLAP = (BUFFERSIZE/4)*3;

    private TextView console, note;
    private Handler displayHandler = new Handler();
    private Thread displayUpdateThread = null;

    private final static int UPDATE_TUNER_VALUES = 1;
    private final static int ROWS = 20;
    private final static int COLS = 41;

    private String[] rowHistory = new String[ROWS];
    private final static String firstLine = "-                   *                   +\n";

    private double tunerLastCentsValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getValidSampleRates();

        initRowHistory();

        startAudio(PitchProcessor.PitchEstimationAlgorithm.YIN);

        startDisplay();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ccgt_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.yin:
                stopAudio();
                startAudio(PitchProcessor.PitchEstimationAlgorithm.YIN);
                Toast.makeText(this, "Yin selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.yin_fft:
                stopAudio();
                startAudio(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN);
                Toast.makeText(this, "Yin FFT selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.amdf:
                stopAudio();
                startAudio(PitchProcessor.PitchEstimationAlgorithm.AMDF);
                Toast.makeText(this, "AMDF selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.mpm:
                stopAudio();
                startAudio(PitchProcessor.PitchEstimationAlgorithm.MPM);
                Toast.makeText(this, "MPM selected", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void getValidSampleRates() {
        for(int rate : new int[] {8000, 11025, 16000, 22050, 44100, 48000, 96000}) {
            //Returns: ERROR_BAD_VALUE if the recording parameters are not supported by the hardware, [...]
            int bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                if(bufferSize > 0) {
                    Log.d(TAG, "getValidSampleRates: rate " + rate + " supported");
                }
        }
    }

    private void startAudio(Object pitchAlgorithmObject) {
        if(dispatcher == null) {
            dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(SAMPLERATE, BUFFERSIZE, OVERLAP);
            Log.d(TAG, "startAudio: dispatcher " + dispatcher.toString());

            pitchDetectionHandler = new PitchDetectionHandler() {
                @Override
                public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
                    pitchInHz = pitchDetectionResult.getPitch();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView text = (TextView) findViewById(R.id.note);
                            text.setText(getNearestPitchClass(pitchInHz));
                            //todo put these two in one textview, add freq textview
                            TextView oct = (TextView) findViewById(R.id.octave);
                            oct.setText(getOctave(pitchInHz));
                            TextView freq = (TextView) findViewById(R.id.freq);
                            freq.setText(Float.toString(pitchInHz));
                        }
                    });
                }
            };

            synchronized (this) {
                audioProcessor = new PitchProcessor((PitchProcessor.PitchEstimationAlgorithm) pitchAlgorithmObject, SAMPLERATE, BUFFERSIZE, pitchDetectionHandler);
                dispatcher.addAudioProcessor(audioProcessor);
                new Thread(dispatcher, "Audio Dispatcher").start();
            }
        }
    }

    private void stopAudio() {
        if(dispatcher != null) {
            dispatcher.stop();
            dispatcher = null;
            Log.d(TAG, "stopAudio: dispatcher is null");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void startDisplay() {

        displayUpdateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                            displayHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    console = findViewById(R.id.console);
                                    String centsString = getHistoryRow(twoPointMovingAverageFilter(centsDeviation));
                                    putCentsToHistory(centsString);
                                    StringBuffer output = new StringBuffer();
                                    output.append(firstLine);
                                    output.append("\n");
                                    for(int i = 0; i < rowHistory.length; i++) {
                                        output.append(rowHistory[i]);
                                        output.append("\n");
                                    }
                                    console.setText(output);
                                }
                            });
                        Thread.sleep(250);
                        //todo: set scrolling speed
                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                    }
                }
            }
        });
        displayUpdateThread.start();
    }


    private void putCentsToHistory(String centsString){
        for(int i = rowHistory.length-1; i > 0; i--) {
            rowHistory[i] = rowHistory[i-1];
        }
        rowHistory[0] = centsString;
    }

    private void initRowHistory(){
        for(int i = 0; i < rowHistory.length; i++) {
            rowHistory[i] = "";
        }
    }

    private String getHistoryRow(double cents) {
		int approximateCents = (int) cents;
		StringBuffer tmpstr = new StringBuffer(".................... ....................\n");

		if(cents < -3) {
			if(cents < -40) {
				//value is too big, display it at the bottom (left)
				tmpstr.setCharAt(0, '|');
			} else {
				//from middle, go one char to the left per 3 cents
                tmpstr.setCharAt(20+approximateCents/3, '|');
			}
		} else if (cents > 3) {
			if(cents > 40) {
				//value is too big, display it at the top (right)
                tmpstr.setCharAt(40, '|');
			} else {
				//from middle, go one char to the right per 3 cents
                tmpstr.setCharAt(20+approximateCents/3, '|');
			}

		} else {
            tmpstr.setCharAt(20, '|');
        }
		return tmpstr.toString();
    }

    private double twoPointMovingAverageFilter(double actualCents) {
        double output = (actualCents + tunerLastCentsValue) / 2;
        tunerLastCentsValue = actualCents;
        return output;
    }

    private String getOctave(double freq) {
        double distance;
        distance = 9 + (12 * (log2(freq/REFERENCE_FREQ)  ) );
        return Integer.toString((int) ((distance / 12) + 4));
    }

    private String getNearestPitchClass(double freq) {
        /*
            calculate semitone distance from middle c
            which has approx. 261.6256 Hz
            distance = 9 + (12 log2 (freq / referenceFreq))

            zero means, the note is middle c :-)

            referenceFreq is 440 hz

            distance will be a double, where the part behind the
            decimal point (period) represents distance from the pitch class
         */

        double distance;
        distance = 9 + (12 * (log2(freq/REFERENCE_FREQ)  ) );

        int integerDistance = (int) distance;

        double realDistanceError = distance - integerDistance;
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
                centsDeviation = (distanceError-1) * 100;
            } else {
                centsDeviation = distanceError * 100;
            }
        } else {
            if(distanceError > 0.5 || distanceError == 0.5) {
                integerDistance--;
                centsDeviation = (1-distanceError) * 100;
            } else {
                centsDeviation = -(distanceError * 100);
            }
            if(integerDistance<12) {
                integerDistance = (integerDistance%12)+12;
            }
        }

        return PITCHCLASS[integerDistance%12];
    }

    protected double log2(double value) {
        return Math.log( value ) / Math.log( 2.0 );
    }

    @Override
    protected void onPause() { super.onPause(); }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        displayUpdateThread.interrupt();
        dispatcher.stop();
    }

}