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

import com.example.myapplication.data.GlobalConstants;
import com.example.myapplication.data.Song;
import com.example.myapplication.service.MyMusicService;

import java.util.ArrayList;

public class MusicPlayActivity extends AppCompatActivity {
    private ImageView ivPlayOrPause,ivNext,ivPre;


    private ArrayList<Song> mSongArrayList;
    private int curSongIndex;
    private MyMusicService.MyMusicBind mMusicBind;
    private ServiceConnection conn =new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
              //The service has been established, passing the information
            mMusicBind = (MyMusicService.MyMusicBind) iBinder;
            mMusicBind.updateMusicList(mSongArrayList);
            mMusicBind.updataCurrentMusicIndex(curSongIndex);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
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

        Log.d("tag","now playing"+curSongIndex);
        if(mSongArrayList != null) {
            Log.d("tag","now Music list"+mSongArrayList.toString());

        }
        startMusicService();

    }
    private  void initView(){
        ivPlayOrPause = findViewById(R.id.iv_play_pause);
        ivNext = findViewById(R.id.iv_next);
        ivPre = findViewById(R.id.iv_previous);

    }

    private void startMusicService() {
        //starting the service with bind
        Intent intent = new Intent(this, MyMusicService.class);

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
    }

    public void nextMusic(View view) {
        mMusicBind.next();
    }

    public void stopMusic(View view) {
        mMusicBind.stop();
        ivPlayOrPause.setImageResource(android.R.drawable.ic_media_play);
    }
}