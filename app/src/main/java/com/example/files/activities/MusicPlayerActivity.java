package com.example.files.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioAttributes;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.files.R;
import com.example.files.services.AudioPlayerService;

import java.io.IOException;
import java.util.Objects;

public class MusicPlayerActivity extends BaseActivity {

    Uri fileUri = null;
    private AudioPlayerService player;
    boolean play = false, serviceBound = false;

    //Binding this Client to the AudioPlayer Service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            AudioPlayerService.LocalBinder binder = (AudioPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;

            Toast.makeText(MusicPlayerActivity.this, "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        Intent receivedIntent = getIntent();

        if (receivedIntent != null) {
            play = true;
            if (Objects.equals(receivedIntent.getAction(), Intent.ACTION_VIEW)) {
                fileUri = receivedIntent.getData();
            }
        }

        if (!play) return;
        // playAudio("/sdcard/Music/Deezer/דוד בן ארזה - להיות.mp3");

        initLayout();
    }

    private void playAudio(String audio) {
        //Check is service is active
        if (!serviceBound) {
            Intent playerIntent = new Intent(this, AudioPlayerService.class);
            playerIntent.putExtra("audio", audio);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            //Service is active
            //Send media with BroadcastReceiver
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            //service is active
            player.stopSelf();
        }
    }

    private void initLayout() {
        TextView title = findViewById(R.id.song_title);
        TextView album = findViewById(R.id.song_album);
        TextView artist = findViewById(R.id.song_artist);
        TextView format = findViewById(R.id.song_format);
        TextView currentPosition = findViewById(R.id.current_position);
        TextView duration = findViewById(R.id.song_duration);
        ImageView cover = findViewById(R.id.song_cover);
        ImageButton play = findViewById(R.id.btn_play_pause);
        ImageButton previous = findViewById(R.id.btn_previous);
        ImageButton next = findViewById(R.id.btn_next);
        SeekBar seekBar = findViewById(R.id.seekbar);

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(this, fileUri);

        String songTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        //if (songTitle == null || songTitle.equals("")) songTitle = file name
        title.setText(songTitle);
        album.setText(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
        artist.setText(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
        format.setText(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE));

        play.setOnClickListener(view -> {
            if (player.isPlaying()) player.pauseMedia();
            else player.resumeMedia();
        });
    }

    private String getSongTitle() {
        return null;
    }


}