package com.example.myapplication.service;


import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import static com.example.myapplication.data.GlobalConstants.KEY_SONG_INDEX;
import static com.example.myapplication.data.GlobalConstants.KEY_SONG_LIST;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.myapplication.R;
import com.example.myapplication.data.GlobalConstants;
import com.example.myapplication.data.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import listener.MyPlayListener;
import util.PlayModeHelper;

public class MyMusicService extends Service {

    private static final String CHANNEL_ID = "song_play_channel";
    private MediaPlayer mMediaPlayer;
    private ArrayList<Song> mSongArrayList;

    private int curSongIndex;
    private int curPlayMode;
    private MyPlayListener mMyPlayListener;
    public static final int FOREGROUND_ID=1;
    private RemoteViews remoteView;
    private boolean haveNotification;
    private Notification notification;
    private NotificationManager notificationManager;
    private BroadcastReceiver mReciver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case GlobalConstants.ACTION_CLOSE_MUSIC:
                    stopForeground(true);
                    stopSelf();
                    break;
                case GlobalConstants.ACTION_PRE_MUSIC:
                    previous();
                    break;
                case GlobalConstants.ACTION_NEXT_MUSIC:
                    next();
                    break;
                case GlobalConstants.ACTION_PLAY_PAUSE_MUSIC:
                    if(isPlaying()){
                        pause();
                    }else {
                        play();
                    }
                    break;
                case GlobalConstants.ACTION_START_PLAY_ACTIVITY:
                    startSongPlayActivity();
                    break;
            }
        }
    };

    private void startSongPlayActivity() {
        Intent startSongPlayIntent = new Intent(this, MyMusicService.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(KEY_SONG_LIST,mSongArrayList);
        bundle.putInt(KEY_SONG_INDEX,curSongIndex);
        startSongPlayIntent.putExtras(bundle);
        startSongPlayIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startSongPlayIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(startSongPlayIntent);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                next();
            }
        });
        mSongArrayList = new ArrayList<>();

        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(GlobalConstants.ACTION_CLOSE_MUSIC);
        intentFilter.addAction(GlobalConstants.ACTION_NEXT_MUSIC);
        intentFilter.addAction(GlobalConstants.ACTION_PRE_MUSIC);
        intentFilter.addAction(GlobalConstants.ACTION_PLAY_PAUSE_MUSIC);
        intentFilter.addAction(GlobalConstants.ACTION_START_PLAY_ACTIVITY);
        registerReceiver(mReciver,intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotification();
        return super.onStartCommand(intent, flags, startId);
    }
    private  void createNotification(){
        if(haveNotification){
            return;
        }
         notificationManager=(NotificationManager)getSystemService(Service.NOTIFICATION_SERVICE);
        if(android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.O){
            NotificationChannel channel=new NotificationChannel(CHANNEL_ID,"Junqu Lu Music",NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        remoteView = new RemoteViews(getPackageName(), R.layout.notification_music_layout);
        Song song=getCurSong();
        if(song!=null){
            remoteView.setTextViewText(R.id.tv_notification_title, song.getSongName());
                }




        Intent nextIntent=new Intent(GlobalConstants.ACTION_NEXT_MUSIC);
        PendingIntent nextPendIntent=PendingIntent.getBroadcast(this,0,nextIntent, PendingIntent.FLAG_MUTABLE);
        remoteView.setOnClickPendingIntent(R.id.iv_next,nextPendIntent);

        Intent preIntent=new Intent(GlobalConstants.ACTION_PRE_MUSIC);
        PendingIntent prePendIntent=PendingIntent.getBroadcast(this,0,preIntent, PendingIntent.FLAG_MUTABLE);
        remoteView.setOnClickPendingIntent(R.id.iv_previous,prePendIntent);

        Intent playpauseIntent=new Intent(GlobalConstants.ACTION_PLAY_PAUSE_MUSIC);
        PendingIntent playpausePendIntent=PendingIntent.getBroadcast(this,0,playpauseIntent, PendingIntent.FLAG_MUTABLE);
        remoteView.setOnClickPendingIntent(R.id.iv_play_pause,playpausePendIntent);

        Intent closeIntent=new Intent(GlobalConstants.ACTION_CLOSE_MUSIC);
        PendingIntent closePendIntent=PendingIntent.getBroadcast(this,0,closeIntent, PendingIntent.FLAG_MUTABLE);
        remoteView.setOnClickPendingIntent(R.id.iv_close,closePendIntent);

        Intent startSongPlayIntent=new Intent(GlobalConstants.ACTION_START_PLAY_ACTIVITY);
        PendingIntent startSongPlayPendIntent=PendingIntent.getBroadcast(this,0,startSongPlayIntent, PendingIntent.FLAG_MUTABLE);
        remoteView.setOnClickPendingIntent(R.id.iv_close,startSongPlayPendIntent);

        notification = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setContentText("This music content")
                .setContentTitle("This music name")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setCustomContentView(remoteView)
                //.setContentIntent(startSongPlayPendIntent)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_media_pause))
                .build();
        startForeground(FOREGROUND_ID,notification);
        haveNotification=true;

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyMusicBind(this);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null){
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer=null;
        }
       unregisterReceiver(mReciver);

    }
    private void updateMusicList(ArrayList<Song> songArrayList) {
        this.mSongArrayList = songArrayList;

    }
    public void updateCurrentMusicIndex(int index) {
          if(index <0 ||index >= mSongArrayList.size()){
              return;
          }
        this.curSongIndex= index;
          //play this song
        Song song= mSongArrayList.get(curSongIndex);
        String songName = song.getSongName();

        AssetManager assetManger = getAssets();
        try{
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            AssetFileDescriptor fileDescriptor = assetManger.openFd(songName);
            mMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(),
                    fileDescriptor.getStartOffset(),
                    fileDescriptor.getLength());
            mMediaPlayer.prepare();
            mMediaPlayer.start();

        }catch (IOException e){
            e.printStackTrace();
        }

        if(remoteView!=null){
            remoteView.setTextViewText(R.id.tv_notification_title, song.getSongName());
        }

        notificationManager.notify(FOREGROUND_ID,notification);

        notification.contentView.setImageViewResource(R.id.iv_play_pause, android.R.drawable.ic_media_pause);
        notificationManager.notify(FOREGROUND_ID,notification);

    }
    public boolean isPlaying(){
         return mMediaPlayer.isPlaying();
    }
    public void pause(){
        if(mMediaPlayer.isPlaying()){
            mMediaPlayer.pause();
        }
        notification.contentView.setImageViewResource(R.id.iv_play_pause, android.R.drawable.ic_media_play);
        notificationManager.notify(FOREGROUND_ID,notification);
    }
    public void play(){
        if(mMediaPlayer.isPlaying()){
            return;
        }
        mMediaPlayer.start();
        notification.contentView.setImageViewResource(R.id.iv_play_pause, android.R.drawable.ic_media_pause);
        notificationManager.notify(FOREGROUND_ID,notification);
    }

    public void previous(){
        if(curPlayMode== PlayModeHelper.PLAY_MODE_CIRCLE){
            updateCurrentMusicIndex(curSongIndex);
        } else if (curPlayMode==PlayModeHelper.PLAY_MODE_RANDOM) {
            int nextRandomIndex= getNextRandomIndex();
            updateCurrentMusicIndex(nextRandomIndex);
        }else {
            int preIndex = curSongIndex - 1;
            if (preIndex < 0) {
                preIndex = mSongArrayList.size() - 1;
            }
            updateCurrentMusicIndex(preIndex);
        }
        if(mMyPlayListener!=null){
            mMyPlayListener.onPre(curSongIndex,getCurSong());
        }
    }
    public void next(){
        if(curPlayMode== PlayModeHelper.PLAY_MODE_CIRCLE){
            updateCurrentMusicIndex(curSongIndex);
        } else if (curPlayMode==PlayModeHelper.PLAY_MODE_RANDOM) {
            int nextRandomIndex= getNextRandomIndex();
            updateCurrentMusicIndex(nextRandomIndex);
        }else {

            int nextIndex = curSongIndex + 1;
            if (nextIndex > mSongArrayList.size() - 1) {
                nextIndex = 0;
            }
            updateCurrentMusicIndex(nextIndex);
        }
        if(mMyPlayListener!=null){
            mMyPlayListener.onNext(curSongIndex,getCurSong());
        }
    }
    private int getNextRandomIndex(){
        mSongArrayList.size();
        Random random= new Random();
        int randomIndex=random.nextInt(mSongArrayList.size());
        return randomIndex;
    }
    public void stop(){
        mMediaPlayer.stop();
        try {
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public Song getCurSong(){
        if(curSongIndex<0||curSongIndex>=mSongArrayList.size()){
            return null;
        }
               return mSongArrayList.get(curSongIndex);
    }
    private int getCurProgress(){
        return mMediaPlayer.getCurrentPosition();
    }
    private int getDuration(){
        return mMediaPlayer.getDuration();
    }
    private void seekTo(int progress) {
        mMediaPlayer.seekTo(progress);
    }
    private void setPlayMode(int mode){
        this.curPlayMode=mode;
    }
    public void setPlayerListener(MyPlayListener playerListener){
        this.mMyPlayListener=playerListener;
    }
    public class MyMusicBind extends Binder {
        private MyMusicService mMusicService;

        public MyMusicBind(MyMusicService musicService) {
            mMusicService = musicService;
        }

        public void startPlay(){


        }
        public void updateMusicList(ArrayList<Song> songArrayList){

            mMusicService.updateMusicList(songArrayList);
        }
        public void updataCurrentMusicIndex(int index){
            mMusicService.updateCurrentMusicIndex(index);
        }
        public boolean isPlaying(){
            return mMusicService.isPlaying();
        }
        public void pause(){
            mMusicService.pause();
        }
        public void play(){
            mMusicService.play();
        }

        public void previous(){
            mMusicService.previous();
        }
        public void next(){
            mMusicService.next();
        }
        public void stop(){
            mMusicService.stop();
        }
        public Song getCurSong(){
            return mMusicService.getCurSong();
        }
        public int getCurProgress(){
            return mMusicService.getCurProgress();
        }
        public int getDuration(){
            return mMusicService.getDuration();
        }

        public void seekTo(int progress) {
            mMusicService.seekTo(progress);
        }
        public void setPlayMode(int mode){
            mMusicService.setPlayMode(mode);
        }
        public void setPlayerListener(MyPlayListener playerListener){
            mMusicService.setPlayerListener(playerListener);
        }

    }

}
