package de.fff.ccgt;

import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;



import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.filters.HighPass;
import be.tarsos.dsp.filters.LowPassFS;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.util.fft.FFT;


/*
 * ccgt -  concole curses guitar tuner
 *                _
 *   ___ ___ __ _| |_
 *  / __/ __/ _` | __|
 * | (_| (_| (_| | |_
 *  \___\___\__, |\__|
 *          |___/
 *
 * just get in tune, don't buy premium
 *
 *
 * big thanks to joren six, olmo cornelis and marc leman ...
 * and the community! this project uses the mighty pitch
 * tracking algorithms from tarsos dsp and its spectrogram
 * capability.
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
    private final static int[] REFERENCE_FREQUENCIES = { 437, 438, 439, 440, 441, 442, 443 };

    //audio config values
    // todo: put this in separate settings class
    private double referenceFrequency = 440.0;
    private final static int SAMPLERATE = 8000;
    private final static int BUFFERSIZE = 2048;
    private final static int OVERLAP = (BUFFERSIZE/4)*3;

    private TextView console, note;
    private SpectrogramView spectrogramView;
    private AudioProcessor fftProcessor;
    private TextView text, oct, freq;
    private Spinner spinner;
    private SeekBar seekBar;

    private Handler displayHandler = new Handler();
    private Thread displayUpdateThread = null;

    private final static int UPDATE_TUNER_VALUES = 1;
    private final static int ROWS = 20;
    private final static int COLS = 41;

    private String[] rowHistory = new String[ROWS];
    private final static String firstLine = "-    .    .    .    *    .    .    .    +\n";

    private double tunerLastCentsValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle(" :~$ ccgt");

        spectrogramView = findViewById(R.id.spectrogram);
        text = (TextView) findViewById(R.id.note);
        oct = (TextView) findViewById(R.id.octave);
        freq = (TextView) findViewById(R.id.freq);
        spinner = (Spinner) findViewById(R.id.spinner);
        seekBar = (SeekBar) findViewById(R.id.calibrationSeekBar);
        // set initial values
        spinner.setSelection(3);
        seekBar.setProgress(3);

        seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int progress = 0;

                    @Override
                    public void onProgressChanged(SeekBar seekBar,
                                                  int progressValue, boolean fromUser) {
                        progress = progressValue;
                        spinner.setSelection(progress);
                        referenceFrequency = REFERENCE_FREQUENCIES[progress];
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

        spinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    int selection = 0;

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selection = position;
                        seekBar.setProgress(selection);
                        referenceFrequency = REFERENCE_FREQUENCIES[selection];
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                }
        );

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
            case R.id.mpm:
                stopAudio();
                startAudio(PitchProcessor.PitchEstimationAlgorithm.MPM);
                Toast.makeText(this, "MPM selected", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // call this in OnCreate if you want to test:
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

            pitchDetectionHandler = new PitchDetectionHandler() {
                @Override
                public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
                    pitchInHz = pitchDetectionResult.getPitch();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(pitchDetectionResult.isPitched()) {
                                //make it fade from deep red to green
                                text.setTextColor(Color.rgb(distanceError(pitchInHz), 250 - distanceError(pitchInHz), distanceError(pitchInHz)));
                                text.setText(getNearestPitchClass(pitchInHz));
                            } else {
                                getNearestPitchClass(pitchInHz);
                                text.setTextColor(Color.WHITE);
                                text.setText("-");
                            }
                            oct.setText(getOctave(pitchInHz));
                            freq.setText(String.format("%.02f", pitchInHz));
                        }
                    });
                }
            };

            fftProcessor = new AudioProcessor() {

                FFT fft = new FFT(BUFFERSIZE);
                float[] amplitudes = new float[BUFFERSIZE * 2];

                @Override
                public boolean process(AudioEvent audioEvent) {
                    float[] audioFloatBuffer = audioEvent.getFloatBuffer();
                    float[] transformBuffer = new float[BUFFERSIZE * 4];
                    System.arraycopy(audioFloatBuffer, 0 , transformBuffer, 0, audioFloatBuffer.length);
                    fft.forwardTransform(transformBuffer);
                    //modulus ... absolute value of complex fourier coefficient ... aka magnitude:
                    fft.modulus(transformBuffer, amplitudes);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            spectrogramView.feedSpectrogramView(pitchInHz, amplitudes);
                            spectrogramView.invalidate();
                        }
                    });
                    return true;
                }

                @Override
                public void processingFinished() {
                    Log.d(TAG, "processingFinished: fftProcessor");
                }
            };

            synchronized (this) {
                dispatcher.addAudioProcessor(new LowPassFS(3000, SAMPLERATE));
                dispatcher.addAudioProcessor(new HighPass(70, SAMPLERATE));
                audioProcessor = new PitchProcessor((PitchProcessor.PitchEstimationAlgorithm) pitchAlgorithmObject, SAMPLERATE, BUFFERSIZE, pitchDetectionHandler);
                dispatcher.addAudioProcessor(audioProcessor);
                dispatcher.addAudioProcessor(fftProcessor);
                new Thread(dispatcher, "Audio Dispatcher").start();
            }
        }
    }

    private void stopAudio() {
        if(dispatcher != null) {
            dispatcher.stop();
            dispatcher = null;
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
                        Thread.sleep(255);
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
		StringBuffer tmpstr = new StringBuffer("                                         \n");

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

    private String getOctave(double freq) {
        double distance;
        distance = 9 + (12 * (log2(freq/referenceFrequency)  ) );
        return Integer.toString((int) ((distance / 12) + 4));
    }

    private int distanceError(double freq) {
        // returns semitone distance error to be used as argument for color
        // caution, in the process of tuning you get values just below 1...
        // and values just above 0 !
        double distance;
        distance = 9 + (12 * (log2(freq/referenceFrequency)  ) );
        // now we have the semitone distance from middle c ...
        // split into whole and broken semitones
        int integerDistance = (int) distance;
        double distanceError = Math.abs(distance - integerDistance);
        // return value big enough for color generation
        if(distanceError < 0.5) {
            return (int) (250 * distanceError);
        } else {
            return (int) (250 * (1 - distanceError));
        }
    }

    private String getNearestPitchClass(double freq) {
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

        double distance;
        distance = 9 + (12 * (log2(freq/referenceFrequency)  ) );

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