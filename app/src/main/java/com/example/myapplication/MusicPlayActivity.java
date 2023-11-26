package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.myapplication.data.GlobalConstants;
import com.example.myapplication.data.Song;
import com.example.myapplication.service.MyMusicService;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import listener.MyPlayListener;
import util.PlayModeHelper;
import util.Timeutil;

public class MusicPlayActivity extends AppCompatActivity {
    private ImageView ivPlayOrPause, ivNext, ivPre;
    private TextView tvTitle, tvCurtime, tvDuration,tvPlayMode;
    private SeekBar mSeekBar;
    private Timer timer;

    private ArrayList<Song> mSongArrayList;
    private int curSongIndex;
    private Song mCurSong;
    private MyMusicService.MyMusicBind mMusicBind;
    private boolean isSeekbarDragging;
    private int currentPlayMode = PlayModeHelper.PLAY_MODE_ORDER;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //The service has been established, passing the information
            mMusicBind = (MyMusicService.MyMusicBind) iBinder;
            mMusicBind.updateMusicList(mSongArrayList);
            mMusicBind.updataCurrentMusicIndex(curSongIndex);
            mMusicBind.setPlayMode(currentPlayMode);
            mMusicBind.setPlayerListener(new MyPlayListener() {
                @Override
                public void onComplete(int songIndex, Song song) {

                }

                @Override
                public void onNext(int songIndex, Song song) {
                    curSongIndex =songIndex;
                    mCurSong = song;
                    upDateTitle();

                }

                @Override
                public void onPre(int songIndex, Song song) {

                }

                @Override
                public void onPause(int songIndex, Song song) {

                }

                @Override
                public void onPlay(int songIndex, Song song) {

                }
            });
            updateUI();
        }

        public void onServiceDisconnected(ComponentName componentName) {

        }
    };


    private void updateUI() {
        //current time update
        int curProgress = mMusicBind.getCurProgress();
        tvCurtime.setText(Timeutil.millToTimeFormat(curProgress));
        //whole time update
        int duration = mMusicBind.getDuration();
        tvDuration.setText(Timeutil.millToTimeFormat(duration));
        //update seek bar
        mSeekBar.setMax(duration);
        mSeekBar.setProgress(curProgress);
        if (timer != null) {
            return;
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override

            public void run() {
                int curProgress = mMusicBind.getCurProgress();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isSeekbarDragging && mMusicBind.isPlaying()) {
                            mSeekBar.setProgress(curProgress);
                        }
                    }

                });
            }
        }, 0, 200);

    }

    private void updateSeekbar() {
        if (timer != null) {
            return;
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override

            public void run() {
                int curProgress = mMusicBind.getCurProgress();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isSeekbarDragging && mMusicBind.isPlaying()) {
                            mSeekBar.setProgress(curProgress);
                        }
                    }

                });
            }
        }, 0, 200);
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play);
        initView();

        ivPlayOrPause = findViewById(R.id.iv_play_pause);
        ivNext = findViewById(R.id.iv_next);
        ivPre = findViewById(R.id.iv_previous);
        Intent intent = getIntent();
        curSongIndex = intent.getIntExtra("key_song_index",0);
        //mSongArrayList = (ArrayList<Song>) intent.getSerializableExtra(GlobalConstants.KEY_SONG_LIST);
        mSongArrayList = intent.getParcelableArrayListExtra(GlobalConstants.KEY_SONG_LIST);
        mCurSong =mSongArrayList.get(curSongIndex);
        Log.d("tag","now playing"+curSongIndex);
        if(mSongArrayList != null) {
            Log.d("tag","now Music list"+mSongArrayList.toString());

        }
        upDateTitle();
        startMusicService();

    }
    private void upDateTitle(){
        tvTitle.setText(mCurSong.getSongName());
    }
    private void updateCurtimeText(int progress){
        tvCurtime.setText(Timeutil.millToTimeFormat(progress));

    }
    private  void initView(){
        ivPlayOrPause = findViewById(R.id.iv_play_pause);
        ivNext = findViewById(R.id.iv_next);
        ivPre = findViewById(R.id.iv_previous);
        tvTitle = findViewById(R.id.tv_music_title);
        tvCurtime=findViewById(R.id.tv_cur_time);
        tvDuration=findViewById(R.id.tv_duration);
        mSeekBar=findViewById(R.id.seek_bar);
        tvPlayMode=findViewById(R.id.tv_play_mode);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                updateCurtimeText(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
             isSeekbarDragging=true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
             isSeekbarDragging=false;
             int progress=seekBar.getProgress();
                mMusicBind.seekTo(progress);
            }
        });


    }

    private void startMusicService() {
        //starting the service with bind
        Intent intent = new Intent(this, MyMusicService.class);
        //Starting the service with start
        startService(intent);
        bindService(intent,conn,BIND_AUTO_CREATE);

    }

    public void playOrPause(View view) {

      if (mMusicBind.isPlaying()){
          //pause the music
          mMusicBind.pause();
          //change the item for state(will play)
          ivPlayOrPause.setImageResource(android.R.drawable.ic_media_play);
      }
      else {
          //play the music
          mMusicBind.play();
          //change the item for state  (will pause)
          ivPlayOrPause.setImageResource(android.R.drawable.ic_media_pause);
      }
    }

    public void preMusic(View view) {
        mMusicBind.previous();
       mCurSong= mMusicBind.getCurSong();
       upDateTitle();
    }

    public void nextMusic(View view) {
        mMusicBind.next();
        mCurSong= mMusicBind.getCurSong();
        upDateTitle();
    }

    public void stopMusic(View view) {

        mMusicBind.stop();
        ivPlayOrPause.setImageResource(android.R.drawable.ic_media_play);
        updateCurtimeText(0);
        mSeekBar.setProgress(0);
    }
    public void onDestroy(){
        super.onDestroy();
        if(timer!=null){
            timer.cancel();
            timer=null;
        }
    }

    public void switchPlayMode(View view) {
        int playMode=PlayModeHelper.changePlayMode(currentPlayMode);
        currentPlayMode = playMode;
        String strPlayMode=PlayModeHelper.strPlayMode(currentPlayMode);
        tvPlayMode.setText(strPlayMode);
        mMusicBind.setPlayMode(currentPlayMode);

    }
    public void onBackPressed(){
        moveTaskToBack(true);
    }

    public void back (View view){
        this.finish();
    }
}