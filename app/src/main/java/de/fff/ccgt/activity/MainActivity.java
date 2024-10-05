package de.fff.ccgt.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
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

import java.util.Objects;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.util.fft.FFT;
import de.fff.ccgt.R;
import de.fff.ccgt.service.AudioService;
import de.fff.ccgt.service.ConsoleService;
import de.fff.ccgt.service.PitchService;
import de.fff.ccgt.service.PreferencesService;
import de.fff.ccgt.view.SpectrogramView;

/*
 * ccgt -  console curses guitar tuner
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
 */

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private AudioService audioService;
    private PitchService pitchService;
    private ConsoleService consoleService;
    private PreferencesService preferencesService;

    private float pitchInHz = 0;
    private double centsDeviation = 0;

    private final static int[] REFERENCE_FREQUENCIES = { 437, 438, 439, 440, 441, 442, 443 };

    private double referenceFrequency = 440.0;

    private SpectrogramView spectrogramView;
    private TextView pitchNameTV, octTV, freqTV, consoleTV;
    private Spinner calibSpinner;
    private SeekBar calibSeekBar;

    private final Handler displayHandler = new Handler();
    private Thread displayUpdateThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.title_action_bar);

        spectrogramView = findViewById(R.id.spectrogram);
        pitchNameTV = findViewById(R.id.note);
        octTV = findViewById(R.id.octave);
        octTV.setTextColor(Color.WHITE);
        freqTV = findViewById(R.id.freq);
        freqTV.setTextColor(Color.WHITE);
        calibSpinner = findViewById(R.id.spinner);
        calibSeekBar = findViewById(R.id.calibrationSeekBar);
        consoleTV = findViewById(R.id.console);

        initCalibSpinner();

        preferencesService = new PreferencesService(this.getApplicationContext());
        pitchService = new PitchService();
        audioService = new AudioService(this.getApplicationContext());
        consoleService = new ConsoleService();
        audioService.getValidSampleRates();
        audioService.startAudio(getPitchDetectionHandler(), getFftProcessor());

        startDisplay();

    }

    private void initCalibSpinner() {
        calibSpinner.setBackgroundColor(Color.DKGRAY);
        calibSpinner.setSelection(3);
        calibSeekBar.setProgress(3);
        calibSeekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int progress = 0;

                    @Override
                    public void onProgressChanged(SeekBar seekBar,
                                                  int progressValue, boolean fromUser) {
                        progress = progressValue;
                        calibSpinner.setSelection(progress);
                        referenceFrequency = REFERENCE_FREQUENCIES[progress];
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

        calibSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    int selection = 0;

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selection = position;
                        calibSeekBar.setProgress(selection);
                        referenceFrequency = REFERENCE_FREQUENCIES[selection];
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                }
        );
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
                audioService.stopAudio();
                audioService.startAudio(PitchProcessor.PitchEstimationAlgorithm.YIN, getPitchDetectionHandler(), getFftProcessor());
                Toast.makeText(this, "Yin selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.yin_fft:
                audioService.stopAudio();
                audioService.startAudio(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, getPitchDetectionHandler(), getFftProcessor());
                Toast.makeText(this, "Yin FFT selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.mpm:
                audioService.stopAudio();
                audioService.startAudio(PitchProcessor.PitchEstimationAlgorithm.MPM, getPitchDetectionHandler(), getFftProcessor());
                Toast.makeText(this, "MPM selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.settings:
                startActivity(new Intent(this,SettingsActivity.class));
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startDisplay() {
        displayUpdateThread = new Thread(() -> {
            while (true) {
                try {
                    displayHandler.post(getUpdateConsoleRunnable());
                    Thread.sleep(255);
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }
            }
        });
        displayUpdateThread.start();
    }

    @Override
    protected void onPause() {
        audioService.stopAudio();
        super.onPause();
    }

    @Override
    protected void onResume() {
        audioService.startAudio(getPitchDetectionHandler(), getFftProcessor());
        super.onResume();
        preferencesService.isShowSplash();
        preferencesService.getAlgorithm();
        preferencesService.isDisplaySlow();
        preferencesService.getBufferSize();
        preferencesService.getCalibrationFreq();
        preferencesService.getSampleRate();
        preferencesService.isShowOctave();
        preferencesService.isSpectrogramLogarithmic();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(displayUpdateThread != null) {
            displayUpdateThread.interrupt();
        }
        audioService.stopAudio();
    }

    public PitchDetectionHandler getPitchDetectionHandler() {
        @SuppressLint("DefaultLocale") PitchDetectionHandler pitchDetectionHandler = (pitchDetectionResult, audioEvent) -> {
            pitchInHz = pitchDetectionResult.getPitch();
            runOnUiThread(() -> {
                if (pitchDetectionResult.isPitched()) {
                    //make it fade from deep red to green
                    int octetValue = pitchService.distanceErrorOctetValue(pitchInHz, referenceFrequency);
                    pitchNameTV.setTextColor(Color.rgb(octetValue, 250 - octetValue, octetValue));
                    pitchNameTV.setText(pitchService.getNearestPitchClass(pitchInHz, referenceFrequency));
                    centsDeviation = pitchService.getCentsDeviation(pitchInHz, referenceFrequency);
                } else {
                    pitchService.getNearestPitchClass(pitchInHz, referenceFrequency);
                    centsDeviation = pitchService.getCentsDeviation(pitchInHz, referenceFrequency);
                    pitchNameTV.setTextColor(Color.WHITE);
                    pitchNameTV.setText("-");
                }
                octTV.setText(pitchService.getOctave(pitchInHz, referenceFrequency));
                freqTV.setText(String.format("%.02f", pitchInHz));
            });
        };

        return pitchDetectionHandler;
    }

    public AudioProcessor getFftProcessor() {
        AudioProcessor fftProcessor = new AudioProcessor() {
            int buffersize = preferencesService.getBufferSize();
            final FFT fft = new FFT(buffersize);
            final float[] amplitudes = new float[buffersize * 2];

            @Override
            public boolean process(AudioEvent audioEvent) {
                float[] audioFloatBuffer = audioEvent.getFloatBuffer();
                float[] transformBuffer = new float[buffersize * 4];
                System.arraycopy(audioFloatBuffer, 0, transformBuffer, 0, audioFloatBuffer.length);
                fft.forwardTransform(transformBuffer);
                //modulus: absolute value of complex fourier coefficient aka magnitude
                fft.modulus(transformBuffer, amplitudes);
                runOnUiThread(() -> {
                    spectrogramView.feedSpectrogramView(pitchInHz, amplitudes);
                    spectrogramView.invalidate();
                });
                return true;
            }

            @Override
            public void processingFinished() {
                    Log.d(TAG, "processingFinished: fftProcessor");
            }
        };

        return fftProcessor;
    }
    private Runnable getUpdateConsoleRunnable() {
        Runnable updateConsoleRunnable = () -> {
            consoleTV.setText(consoleService.newConsoleContents(centsDeviation));
        };

        return updateConsoleRunnable;
    }

}
