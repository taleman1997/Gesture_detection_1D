package com.example.multithread;
/**
 * @File:      LLAP
 * @Author:    Jianing Li (S1997612)
 * @Date:      18032021
 * @Description:    This is the file of LLAP. The function is to calculate
 *                  the distance based on the acoustic signal recorded by the
 *                  mic on the smart phone
 *                  The code is tested on the smart phone.
 * Test equipment:  1. HUAWEI MAIMANG 6       Android Version: 8.0.0
 *                  2. OPPO R11               Android Version: 8.1.0
 */

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    Button BtnStart;
    Button BtnStop;
    TextView TxtView_content;
    public Handler mHandler_1;
    public boolean isRecording = false;
    public boolean isPlaying = false;
    public int num_of_data;


    // tool class for distance calculation
    public int batch_size = 3000;
    int levd_len = batch_size / 15;

    QI_modulation qi_modulation = new QI_modulation(batch_size,16000,44100);

    CIC cic_I = new CIC(15,1,18,batch_size);
    CIC cic_Q = new CIC(15,1,18,batch_size);

    /**Different equipments have have different threshold value.*/
    LEVD levd_I = new LEVD(levd_len,4,-6.8489E4,8.9034E3);
    LEVD levd_Q = new LEVD(levd_len,4,7.5613E4,1.0281E4);

    Distance distance = new Distance(levd_len);



    private class MHandler extends Handler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            // 根据不用线程返回消息，进行不同的UI 操作
            //根据Message对象的what属性，标识不同的消息
            switch (msg.what){
                case 1:
                    TxtView_content.setText(msg.getData().getString("sending_distance"));
                    break;
            }
        }
    }


    /**
     * This method define the button function and initial the UI
     * element to the specific ID.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        mHandler_1 = new MHandler();

        BtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Record_Thread mt1 = new Record_Thread();
                Calculate_Thread mt2 = new Calculate_Thread();
                Emit_Thread mt3 = new Emit_Thread();
                mt3.start();
                mt1.start();
                mt2.start();
            }
        });

        BtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecording = false;
                isPlaying = false;
            }
        });

    }



    void init(){
        TxtView_content = findViewById(R.id.textView);
        BtnStart = findViewById(R.id.buttonStart);
        BtnStop = findViewById(R.id.buttonStop);
    }

    /**
     * @Description:    This is the tool class for stereo recorder.
     * @relyOn:         Thread, AudioRecord, File
     * @See:            Thread: https://developer.android.com/reference/java/lang/Thread
     *                  AudioRecord: https://developer.android.com/reference/android/media/AudioRecord?hl=en
     *                  File: https://developer.android.com/reference/java/io/File
     */
    private class Record_Thread extends Thread{
        /**thread construct method*/
        public Record_Thread(){}

        /**Define the file for PCM*/
        static final String TAG = "PCMSample";
        File file;

        /**44.1K sample rate*/
        int frequency = 44100;

        /**set the channel, here set to the channel*/
        int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;

        /**encode pattern 16bit*/
        int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

        /**It has the best performance when the buffer-size is 2 times.*/
        int bufferSize = 2 * AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);

        /**nstantiate the AudioRecord by the previous variables*/
        AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, bufferSize);

        // when this thread run start the recording
        @Override
        public void run(){
            /**
             * This part is to create the file in the certain location.
             * Ensure the folder is pre-defined in the storage of the phone.
             * @exception: IOException on file generate
             */
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

            /**if the file create successfully, then run the code below*/
            try {
                //output stream
                OutputStream os = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(os);
                DataOutputStream dos = new DataOutputStream(bos);

                /** while recording, write the buffer data to the file*/
                short[] buffer = new short[bufferSize];
                audioRecord.startRecording();
                Log.i(TAG, "start record");
                isRecording = true;
                while (isRecording) {
                    int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
                    for (int i = 0; i < bufferReadResult; i++) {
                        dos.writeShort(buffer[i]);
                        num_of_data++;
                    }
                }

                /**while the recording stop, close the file*/
                audioRecord.stop();
                dos.close();
            } catch (Throwable t) {
                Log.e(TAG, "record fail");
            }

        }
    }

    /**
     * @Description:    This is the tool class for distance calculation.
     * @relyOn:         Thread, File, CIC, QI_modulation, LEVD, Distance.
     * @See:            Thread: https://developer.android.com/reference/java/lang/Thread
     *                  File: https://developer.android.com/reference/java/io/File
     */
    private class Calculate_Thread extends Thread{

        /**thread construct method*/
        public Calculate_Thread(){}

        /**
         * Define the buffer for data load. In stream reading, the data is read by byte
         * To convert the byte data to int16(short) a two byte buffer is needed.
         * The detailed solution for data convert see line ???
         */
        byte[] readin_buffer = new byte[2];
        double[] input_data = new double[batch_size];

        /**
         * Define the location for PCM. We read the data from PCM
         * Defien the stream for file reading
         */
        String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/123folder/mono_version.pcm";
        File file;
        FileInputStream in;

        /**
         * Defien the variable for distance calculation
         */
        int num_of_batch = 0;
        double output_distance = 0;
        String sending_distance =  "0 mm";

        /**
         * This method starts the calculation thread
         */
        @Override
        public void run(){

            /**
             * Sleep the thread for 100 millis, then PCM is generated and has
             * the data we want
             */
            try {
                Thread.sleep(100);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            /**
             * Read the PCM file
             */
            file = new File(file_path);
            try {
                in = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }


            /**
             * Do the data loading dan calculation.
             */
            while(isRecording){
                long startTime=System.currentTimeMillis();   //获取开始时间

                /**
                 * Load the data if the PCM file has the amount of data we need
                 */
                if(num_of_data - (3050*(num_of_batch+1)) >= 0){
                    Log.d("file length", String.valueOf(num_of_data));

                    /**
                     * Read the data into the 2-byte buffer
                     */
                    for (int i = 0; i < batch_size; i ++){
                        try {
                            in.read(readin_buffer,0,2);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        /**convert to int16*/
                        short input_number = (short) (((readin_buffer[0] << 8) | readin_buffer[1] & 0xff));

                        input_data[i] = input_number;
                    }

                    /**
                     * Starts the calculation
                     * QI -> CIC -> LEVD -> DISTANCE
                     */
                    qi_modulation.reset();
                    qi_modulation.modulation(input_data);

                    cic_I.reset();
                    cic_Q.reset();

                    cic_I.filter(qi_modulation.I_buffer);
                    cic_Q.filter(qi_modulation.Q_buffer);

                    levd_I.reset();
                    levd_Q.reset();

                    levd_I.calculate_dynamic_vec(cic_I.comb_out);
                    levd_Q.calculate_dynamic_vec(cic_Q.comb_out);

                    /**Only get the distance when the data is stable. First few batch is not stable.*/
                    if(num_of_batch > 10){
                        distance.reset();
                        distance.calculate_distance(levd_Q.dynamic_vec, levd_I.dynamic_vec);
                        //Log.d("distance is", String.valueOf(distance.total_distance[200]));
                    }

                    num_of_batch++;

                    /**
                     * Send the distance message to the handler and display on the UI
                     */
                    output_distance = distance.total_distance[distance.total_distance.length - 1];
                    Log.d("the distance is"+ num_of_batch, String.valueOf(output_distance));
                    BigDecimal   b   =   new   BigDecimal(output_distance * 1000.0);
                    double   f1   =   b.setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue();
                    sending_distance = f1 + "mm";
                    Message message = new Message();
                    message.what = 1;
                    Bundle bundle = new Bundle();
                    bundle.putString("sending_distance",sending_distance);
                    message.setData(bundle);
                    mHandler_1.sendMessage(message);
                    long endTime=System.currentTimeMillis();
                    Log.d("the time is",endTime-startTime+"ms");
                }
            }
        }
    }

    /**
     * @Description:    This is the tool class for oscillator.
     * @relyOn:         Thread, AudioTrack
     * @See:            Thread: https://developer.android.com/reference/java/lang/Thread
     *                  AudioTrack: https://developer.android.com/reference/android/media/AudioTrack?hl=en
     */
    private class Emit_Thread extends Thread{

        /**thread construct method*/
        public Emit_Thread(){}

        /**Interface to adjust the emit frequency and amplitude*/
        private int mOutputFreq = 16000;
        private int mOutputAmp = 32767;

        /** The most widely support sample rate*/
        private static final int SOURCE_FREQ = 48000; // most widely supported(sample rate)

        private AudioTrack mAudioTrack;

        /**
         * @Description: This method starts the sound emit
         * @Returns: None
         * @Called: onClick
         *
         */
        @Override
        public void run() {
            Log.i("emiter","start playing sound");

            /**
             * Define the buffer size by method AudioTrack.getMinBufferSize
             */
            int buffSize = AudioTrack.getMinBufferSize(SOURCE_FREQ, AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);

            /**
             * instantiate the AudioTrack
             */
            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    SOURCE_FREQ, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                    buffSize, AudioTrack.MODE_STREAM);

            /**Define the variables for signal generation*/
            short samples[] = new short[buffSize];
            final double twopi =2*Math.PI;
            double ph = 0.0;

            /**
             * Sound play part. Write the signal to the buffer, and put the buffer
             * data to AudioTrack when mIsPlaying is true.
             */
            mAudioTrack.play();
            isPlaying = true;
            while (isPlaying){
                for (int i=0; i<buffSize; i++){
                    samples[i] = (short)(mOutputAmp*Math.sin(ph));
                    ph += twopi*mOutputFreq/SOURCE_FREQ;
                }

                mAudioTrack.write(samples, 0, buffSize);
            }

            /**
             * When mIsPlaying is false, stop play and release the resource
             */
            mAudioTrack.stop();
            mAudioTrack.release();
        }
    }

}