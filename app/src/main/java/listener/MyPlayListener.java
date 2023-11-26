package listener;

import com.example.myapplication.data.Song;

public interface MyPlayListener {
    void onComplete(int songIndex, Song song);
    void onNext(int songIndex, Song song);
    void onPre(int songIndex, Song song);
    void onPause(int songIndex, Song song);

    void onPlay(int songIndex, Song song);


}
