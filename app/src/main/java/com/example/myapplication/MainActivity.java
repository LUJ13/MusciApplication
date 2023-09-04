package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.MySongListAdapter;
import com.example.myapplication.data.GlobalConstants;
import com.example.myapplication.data.Song;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRCVSongList;
    private MySongListAdapter mSongListAdapter;
    private ArrayList<Song> mSongArrayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();
        initSongList();
    }

    private void initData() {
        mSongArrayList = new ArrayList<>();
        mSongArrayList.add(new Song("Faded.mp3"));
        mSongArrayList.add(new Song("Baby.mp3"));
        mSongArrayList.add(new Song("会不会.mp3"));
        mSongArrayList.add(new Song("奢香夫人.mp3"));
        mSongArrayList.add(new Song("山河图.mp3"));
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

    }
}