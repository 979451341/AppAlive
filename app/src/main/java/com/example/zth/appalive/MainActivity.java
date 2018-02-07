package com.example.zth.appalive;

import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener {

    private final int REQ_PERMISSION_AUDIO = 0x01;
    private Button mPlay;
    private boolean mIsPlaying;
    private int mFrequence = 44100;
    private int mPlayChannelConfig = AudioFormat.CHANNEL_IN_STEREO;
    private int mAudioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    private PlayTask mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
                                                  

        mPlay = (Button) findViewById(R.id.audio_paly);

        mPlay.setText("play");

        mPlay.setOnClickListener(this);

        checkPermission();

    }



    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.audio_paly:
                if (mPlay.getTag() == null) {
                    startAudioPlay();
                } else {
                    stopAudioPlay();
                }
                break;
        }
    }



    private void startAudioPlay() {
        mPlay.setTag(this);
        mPlay.setText("stop");

        mPlayer = new PlayTask();
        mPlayer.execute();

        showToast("Recording Playing");
    }

    private void stopAudioPlay() {

        mIsPlaying = false;

        mPlay.setTag(null);
        mPlay.setText("play");

    }


    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, REQ_PERMISSION_AUDIO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQ_PERMISSION_AUDIO:
                if (grantResults.length > 0) {
                    boolean StoragePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {
                        showToast("Permission Granted");
                    } else {
                        showToast("Permission  Denied");
                    }
                }
                break;
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }




    /**
     * AudioTrack
     */
    class PlayTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            mIsPlaying = true;
            for(;mIsPlaying;){
                int bufferSize = AudioTrack.getMinBufferSize(mFrequence,
                        mPlayChannelConfig, mAudioEncoding);
                short[] buffer = new short[bufferSize ];
                try {
                    // 定义输入流，将音频写入到AudioTrack类中，实现播放
                    DataInputStream dis = new DataInputStream(
                            new BufferedInputStream(getResources().openRawResource(R.raw.a)));
                    // 实例AudioTrack
                    AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC,
                            mFrequence,
                            mPlayChannelConfig, mAudioEncoding, bufferSize,
                            AudioTrack.MODE_STREAM);
                    track.setStereoVolume(0,0);
                    // 开始播放
                    track.play();
                    // 由于AudioTrack播放的是流，所以，我们需要一边播放一边读取
                    while (mIsPlaying && dis.available() > 0) {
                        int i = 0;
                        while (dis.available() > 0 && i < buffer.length) {
                            buffer[i] = dis.readShort();
                            i++;
                        }
                        // 然后将数据写入到AudioTrack中
                        track.write(buffer, 0, buffer.length);
                    }


                    // 播放结束
                    track.stop();
                    dis.close();
                } catch (Exception e) {
                    // TODO: handle exception
                    Log.e("slack","error:" + e.getMessage());
                }
            }


            return null;
        }


        protected void onPostExecute(Void result) {

        }


        protected void onPreExecute() {

        }
    }
}