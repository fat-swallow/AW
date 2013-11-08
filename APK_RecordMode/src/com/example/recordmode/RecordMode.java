
package com.example.recordmode;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class RecordMode extends Activity implements View.OnClickListener {
    
    final String TAG = "RecordMode";
    private Button btn_start_record, btn_stop_record, btn_start_play, btn_stop_play, btn_test;
    private File audioFile;
    private boolean isPlaying = false, isRecording = false;
    private RecordTask recorder;
    private PlayTask player;
    
    private int frequence = 8000;
    private int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    
    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.button1:
                Log.d(TAG, "xx1");
                recorder = new RecordTask();
                recorder.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
            case R.id.button2:
                Log.d(TAG, "xx2");
                isRecording = false;
                break;
            case R.id.button3:
                Log.d(TAG, "xx3");
                player = new PlayTask();
                player.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
            case R.id.button4:
                Log.d(TAG, "xx4");
                isPlaying = false;
                break;
            case R.id.button5:
                Log.d(TAG, "AcousticEchoCanceler available=" + AcousticEchoCanceler.isAvailable());
                Log.d(TAG, "NoiseSuppressor available=" + NoiseSuppressor.isAvailable());
                Log.d(TAG, "AutomaticGainControl  available=" + AutomaticGainControl.isAvailable());
                break;
            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_mode);
        btn_start_record = (Button)findViewById(R.id.button1);
        btn_start_record.setText("start record");
        btn_start_record.setOnClickListener(this);
        btn_stop_record = (Button)findViewById(R.id.button2);
        btn_stop_record.setText("stop record");
        btn_stop_record.setOnClickListener(this);
        btn_start_play = (Button)findViewById(R.id.button3);
        btn_start_play.setText("start play");
        btn_start_play.setOnClickListener(this);
        btn_stop_play = (Button)findViewById(R.id.button4);
        btn_stop_play.setText("stop play");
        btn_stop_play.setOnClickListener(this);
        btn_test = (Button)findViewById(R.id.button5);
        btn_test.setText("test");
        btn_test.setOnClickListener(this);
        
        File fpath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/abao/");
        fpath.mkdirs();//�����ļ���
        try {
            //������ʱ�ļ�,ע������ĸ�ʽΪ.pcm
            audioFile = File.createTempFile("recording", ".pcm", fpath);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.record_mode, menu);
        return true;
    }

    class RecordTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            isRecording = true;
            try {
                //��ͨ�������ָ�����ļ�
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(audioFile)));
                //���ݶ���õļ������ã�����ȡ���ʵĻ����С
                int bufferSize = AudioRecord.getMinBufferSize(frequence, channelConfig, audioEncoding);
                Log.e(TAG, "bufferSize=" + bufferSize);
                //ʵ����AudioRecord
                AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, frequence, channelConfig, audioEncoding, bufferSize);
                Log.e(TAG, "record=" + record);
                
                int sessionId = record.getAudioSessionId();
                AutomaticGainControl agc = AutomaticGainControl.create(sessionId);
                agc.setEnabled(true);
                Log.d(TAG, "agc enabled=" + agc.getEnabled());
                AcousticEchoCanceler aec = AcousticEchoCanceler.create(sessionId);
                //aec.setEnabled(true);
                Log.d(TAG, "aec enabled=" + aec.getEnabled());
                NoiseSuppressor ns = NoiseSuppressor.create(sessionId);
                //ns.setEnabled(true);
                Log.d(TAG, "ns enabled=" + ns.getEnabled());
                //Log.d(TAG, "AGC created");
                
                //���建��
                short[] buffer = new short[bufferSize];
 
                //��ʼ¼��
                record.startRecording();
 
                int r = 0; //�洢¼�ƽ���
                Log.e(TAG, "record start");
                //����ѭ��������isRecording��ֵ���ж��Ƿ����¼��
                while(isRecording){
                    //��bufferSize�ж�ȡ�ֽڣ����ض�ȡ��short����
                    //�������ǳ���buffer overflow����֪����ʲôԭ�����˺ü���ֵ����û�ã�TODO�������
                    int bufferReadResult = record.read(buffer, 0, buffer.length);
                    //ѭ����buffer�е���Ƶ����д�뵽OutputStream��
                    for(int i=0; i<bufferReadResult; i++){
                        dos.writeShort(buffer[i]);
                    }
                    //publishProgress(new Integer(r)); //��UI�̱߳��浱ǰ����
                    r++; //��������ֵ
                }
                //¼�ƽ���
                record.stop();
                Log.v("The DOS available:", "::"+audioFile.length());
                dos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
        }

    }
    
    class PlayTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            isPlaying = true;
            int bufferSize = AudioTrack.getMinBufferSize(frequence, channelConfig, audioEncoding);
            short[] buffer = new short[bufferSize/4];
            try {
                //����������������Ƶд�뵽AudioTrack���У�ʵ�ֲ���
                DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(audioFile)));
                //ʵ��AudioTrack
                AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, frequence, channelConfig, audioEncoding, bufferSize, AudioTrack.MODE_STREAM);
                //��ʼ����
                track.play();
                //����AudioTrack���ŵ����������ԣ�������Ҫһ�߲���һ�߶�ȡ
                Log.e(TAG, "play start");
                while(isPlaying && dis.available()>0){
                    int i = 0;
                    while(dis.available()>0 && i<buffer.length){
                        buffer[i] = dis.readShort();
                        i++;
                    }
                    //Ȼ������д�뵽AudioTrack��
                    track.write(buffer, 0, buffer.length);
 
                }
 
                //���Ž���
                track.stop();
                dis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
        }

    }

}
