package com.example.multithread;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class RecorderPCMmono {
    public boolean isRecording = false;
    public static final String TAG = "PCMSample";
    public File file;

    private static Thread rThread;
    int j = 0;
    int frequency = 44100; //44.1K sample rate
    int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO; //channel
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;// 16 bit encode
    // It has the best performance when the buffer-size is 2 times.
    int bufferSize = 2 * AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
    AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, bufferSize);

    public void StartRecord() {
        rThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG,"start record");
                file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/123folder/mono_version.pcm");
                Log.i(TAG,"file generate");
                //if the file exists, then delete first
                if (file.exists())
                    file.delete();
                Log.i(TAG,"del file");
                try {
                    file.createNewFile();
                    Log.i(TAG,"file generated");
                } catch (IOException e) {
                    Log.i(TAG,"fail to generate");
                    throw new IllegalStateException("fail to generate" + file.toString());
                }
                try {
                    //output stream
                    OutputStream os = new FileOutputStream(file);
                    BufferedOutputStream bos = new BufferedOutputStream(os);
                    DataOutputStream dos = new DataOutputStream(bos);

                    short[] buffer = new short[bufferSize];
                    audioRecord.startRecording();
                    Log.i(TAG, "start record");
                    isRecording = true;
                    while (isRecording) {
                        int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
                        for (int i = 0; i < bufferReadResult; i++) {
                            dos.writeShort(buffer[i]);
                        }
                    }
                    audioRecord.stop();
                    dos.close();
                } catch (Throwable t) {
                    Log.e(TAG, "record fail");
                }
            }
        });rThread.start();
    }

    public void StopRecord(){
        isRecording = false;
    }

}
