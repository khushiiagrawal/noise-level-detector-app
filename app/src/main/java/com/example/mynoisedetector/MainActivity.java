package com.example.mynoisedetector;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private TextView dbValueTv, labelTv, thresholdLabel;
    private View coloredCircle;
    private ProgressBar meter;
    private Button startStopBtn;
    private SeekBar thresholdSeekBar;

    private MediaRecorder recorder;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isRecording = false;

    private int thresholdDb = -15;     // dBFS threshold
    private boolean alertShown = false;

    private final int POLL_INTERVAL = 250;
    private File outputFile;

    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbValueTv = findViewById(R.id.dbValueTv);
        labelTv = findViewById(R.id.labelTv);
        coloredCircle = findViewById(R.id.coloredCircle);
        meter = findViewById(R.id.meter);
        startStopBtn = findViewById(R.id.startStopBtn);
        thresholdSeekBar = findViewById(R.id.thresholdSeekBar);
        thresholdLabel = findViewById(R.id.thresholdLabel);

        meter.setMax(100);

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) startRecording();
                    else showSnack("Microphone permission denied");
                }
        );

        startStopBtn.setOnClickListener(v -> {
            if (!isRecording) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
                } else startRecording();
            } else stopRecording();
        });

        thresholdSeekBar.setMax(60);
        thresholdSeekBar.setProgress(45);

        thresholdSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                thresholdDb = -progress;     // -10 to -60 dBFS
                thresholdLabel.setText("Alert threshold: " + thresholdDb + " dBFS");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        try {
            outputFile = File.createTempFile("noise", ".3gp", getCacheDir());
        } catch (Exception e) {
            showSnack("Failed to create file");
        }
    }

    private void startRecording() {
        try {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(outputFile.getAbsolutePath());
            recorder.prepare();
            recorder.start();

            isRecording = true;
            startStopBtn.setText("Stop");
            showSnack("Recording Started");

            handler.post(updateTask);
        } catch (Exception e) {
            showSnack("Error: " + e.getMessage());
        }
    }

    private void stopRecording() {
        try {
            handler.removeCallbacks(updateTask);
            if (recorder != null) {
                recorder.stop();
                recorder.reset();
                recorder.release();
                recorder = null;
            }
        } catch (Exception ignored) {}

        isRecording = false;
        startStopBtn.setText("Start");
        setCircleColor(R.color.green);
        labelTv.setText("Stopped");
        dbValueTv.setText("dB: --");
        meter.setProgress(0);
    }

    private final Runnable updateTask = new Runnable() {
        @Override
        public void run() {
            if (recorder != null && isRecording) {
                int amp = recorder.getMaxAmplitude();
                double db = amplitudeToDb(amp);    // -90 to 0 dBFS

                dbValueTv.setText("dBFS: " + (int) db);

                int scaled = (int) (100 * (db + 90) / 90);
                meter.setProgress(Math.max(0, Math.min(100, scaled)));

                if (db < -40) {
                    labelTv.setText("Low");
                    setCircleColor(R.color.green);
                } else if (db < -15) {
                    labelTv.setText("Medium");
                    setCircleColor(R.color.yellow);
                } else {
                    labelTv.setText("High");
                    setCircleColor(R.color.red);
                }

                if (db >= thresholdDb && !alertShown) {
                    alertShown = true;
                    showAlert("⚠ Noise too high! (" + (int) db + " dBFS)");
                }

                if (db < thresholdDb - 5) {
                    alertShown = false;
                }

                handler.postDelayed(this, POLL_INTERVAL);
            }
        }
    };

    private double amplitudeToDb(int amplitude) {
        if (amplitude <= 0) return -90; // silence
        return 20 * Math.log10((double) amplitude / 32767.0);  // Proper dBFS formula
    }

    private void showAlert(String msg) {
        showSnack(msg);
        vibrate();
    }

    private void showSnack(String msg) {
        Snackbar.make(findViewById(R.id.root), msg, Snackbar.LENGTH_LONG).show();
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            v.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
        else v.vibrate(300);
    }

    private void setCircleColor(int colorRes) {
        int c = ContextCompat.getColor(this, colorRes);
        coloredCircle.getBackground().mutate().setTint(c);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isRecording) stopRecording();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateTask);
        if (outputFile != null && outputFile.exists()) outputFile.delete();
    }
}
