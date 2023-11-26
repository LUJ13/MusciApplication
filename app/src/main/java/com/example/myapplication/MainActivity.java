package com.example.myapplication;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.MySongListAdapter;
import com.example.myapplication.data.GlobalConstants;
import com.example.myapplication.data.Song;
import com.example.myapplication.service.MyMusicService;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRCVSongList;
    private MySongListAdapter mSongListAdapter;
    private ArrayList<Song> mSongArrayList;
    private ImageView ivMusicIcon;
    private TextView tvMusicTitle;
    private MyMusicService.MyMusicBind mMusicBind;
    private Boolean isplaying=false;
    private Song curSong;
    private ObjectAnimator rotationY;
    private ServiceConnection conn=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentname, IBinder iBinder) {
            mMusicBind=(MyMusicService.MyMusicBind)iBinder;
            isplaying=mMusicBind.isPlaying();
            curSong=mMusicBind.getCurSong();
            updateBottomUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentname) {

        }
    };

    private void updateBottomUI() {
        if(isplaying){
            rotationY=ObjectAnimator.ofFloat(ivMusicIcon,"rotationY",0,360);
            rotationY.setRepeatCount(ValueAnimator.INFINITE);
            rotationY.setDuration(1000);
            rotationY.setInterpolator(new LinearInterpolator());
            rotationY.start();

        }else {
            if(rotationY!=null){
                rotationY.cancel();
                rotationY=null;
            }

        }
        if(curSong!=null){
           String songName= curSong.getSongName();
        tvMusicTitle.setText(songName);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(rotationY!=null){
            rotationY.cancel();
            rotationY=null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();
        initSongList();
        bindMusicService();
    }

    private void bindMusicService() {
        Intent intent=new Intent(this, MyMusicService.class);

        bindService(intent,conn,BIND_AUTO_CREATE);
    }


    private void initData() {
        mSongArrayList = new ArrayList<>();
        mSongArrayList.add(new Song("Faded.mp3"));
        mSongArrayList.add(new Song("Baby.mp3"));
        mSongArrayList.add(new Song("会不会.mp3"));
        mSongArrayList.add(new Song("奢香夫人.mp3"));
        mSongArrayList.add(new Song("山河图.mp3"));
        mSongArrayList.add(new Song("My Heart Will Go On.mp3"));
    }

    private void initSongList() {
       mSongListAdapter = new MySongListAdapter(mSongArrayList,this);
       mSongListAdapter.setmItemClickListener(new MySongListAdapter.OnItemClickListener() {
           @Override
           public void onItemClick(int position) {
               Toast.makeText(MainActivity.this, "Click"+position, Toast.LENGTH_SHORT).show();

               Intent intent = new Intent(MainActivity.this,MusicPlayActivity.class);
               intent.putExtra(GlobalConstants.KEY_SONG_LIST,mSongArrayList);
               intent.putExtra(GlobalConstants.KEY_SONG_INDEX,position);
               intent.putParcelableArrayListExtra(GlobalConstants.KEY_SONG_LIST,mSongArrayList);
               startActivity(intent);
           }
       });
       mRCVSongList.setAdapter(mSongListAdapter);
       mRCVSongList.setLayoutManager(new LinearLayoutManager(this));

    }




    private void initView() {
        mRCVSongList = findViewById(R.id.rcv_song_list);
        tvMusicTitle=findViewById(R.id.tv_bottom_song_name);
        ivMusicIcon=findViewById(R.id.iv_bottom_icon);

    }
}