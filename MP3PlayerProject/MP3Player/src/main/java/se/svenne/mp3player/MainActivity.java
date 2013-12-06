package se.svenne.mp3player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.app.Activity;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener, SeekBar.OnSeekBarChangeListener {

    protected static Button playButton;
    protected static Button nextButton;
    private Button previousButton;
    private Button forwardButton;
    private Button backwardButton;
    protected static SeekBar seekBar;
    private ListView songListView;
    protected static TextView songTextView;

    private MediaService mediaService;
    private boolean songIsPlaying;
    private MusicManager musicManager;
    private ArrayAdapter<MusicSong> adapter;
    private List<MusicSong> musicList;

    //connecting to the service with a ServiceConnection object
    private ServiceConnection mediaServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder binder) {
            mediaService = ((MediaService.LocalBinder) binder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }

    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //start service
        startService(new Intent(this, MediaService.class));
        //bind to service by creating a new connectionIntent
        Intent connectionIntent = new Intent(this, MediaService.class);
        bindService(connectionIntent, mediaServiceConnection,
                Context.BIND_AUTO_CREATE);

        playButton = (Button)findViewById(R.id.btnPlay);
        nextButton = (Button)findViewById(R.id.btnNext);
        songTextView = (TextView)findViewById(R.id.textView);

        backwardButton = (Button)findViewById(R.id.btnBackward);
        previousButton = (Button)findViewById(R.id.btnPrevious);
        forwardButton = (Button)findViewById(R.id.btnForward);
        songListView = (ListView)findViewById(R.id.list_view);
        seekBar = (SeekBar)findViewById(R.id.seek_bar);

        playButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);

        backwardButton.setOnClickListener(this);
        previousButton.setOnClickListener(this);
        forwardButton.setOnClickListener(this);

        songListView.setClickable(true);
        songListView.setOnItemClickListener(this);

        seekBar.setOnSeekBarChangeListener(this);

        //new Music Manager
        musicManager = new MusicManager();
        musicList = new ArrayList<MusicSong>();

        //add list to the adapter
        adapter = new ArrayAdapter<MusicSong>(this, android.R.layout.simple_list_item_1, musicList);

        //set adapter
        songListView.setAdapter(adapter);
        playButton.setBackground(getResources().getDrawable(R.drawable.play));

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        //whenever our activity gets destroyed, unbind from the service
        unbindService(this.mediaServiceConnection);
        super.onDestroy();
    }

    @Override
    public void onResume(){
        super.onResume();

        if(mediaService != null){
            //when activity is resumed cancel notification (if there is a service and a notification)
            mediaService.cancelNotification();

            if(mediaService.player != null){
                //set the current pos to seekBar
                int currentPlayerPos = mediaService.player.getCurrentPosition();
                seekBar.setProgress(currentPlayerPos);
            }
        }


        //read boolean from sharedPrefs and set songIsPlaying to the correct value
        songIsPlaying = getSharedPreferences("isPlayingKey", MODE_PRIVATE).getBoolean("isTheSongPlaying", false);

        if(songIsPlaying != false){
            //song is playing
            playButton.setBackground(getResources().getDrawable(R.drawable.pause));
        } else {
            //song is not playing
            playButton.setBackground(getResources().getDrawable(R.drawable.play));
        }

        //clear list and adapter
        musicList.clear();
        adapter.clear();

        //get all songs
        musicList = musicManager.listAllMusic(this);

        if(!musicList.isEmpty()){
            //if the list is not empty
            for(MusicSong musicSong : musicList){
                adapter.add(musicSong); // add song to adapter
            }
        }

        //if no songs are found
        if(musicList.size() == 0){
            songTextView.setText("No music found!");
            playButton.setBackground(getResources().getDrawable(R.drawable.play));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mediaService != null){
            //when activity is paused create the notification
            mediaService.createNotification();
        }

        //save the boolean to sharedPrefs (true if the song is playing and false if it's not playing)
        getSharedPreferences("isPlayingKey", MODE_PRIVATE).edit().putBoolean("isTheSongPlaying", songIsPlaying).commit();


    }

    @Override
    public void onClick(View v) {
        int seekTime = 10000; //used to seek forwards/backwards in song

        if(v == playButton && musicList.size() != 0){

            if(mediaService.getState() != true && mediaService.getPlayerState() != true){
                //if the player is paused/stopped (not witing to be resumed)
                MusicSong musicSong = musicList.get(new Random().nextInt(musicList.size())); //get random song from playlist
                playSong(musicSong); //play random song from playlist
                MusicManager.SongPlaying.setSongPlaying(musicSong); //set the song playing
            } else if(mediaService.getState() != true && mediaService.getPlayerState() != false){
                //player is in paused state, waiting to be resumed
                mediaService.resume();
                playButton.setBackground(getResources().getDrawable(R.drawable.pause));
                songIsPlaying = true;
            } else{
                //the player is playing, pause the song
                mediaService.pauseSong(getBaseContext());
                playButton.setBackground(getResources().getDrawable(R.drawable.play));
                songIsPlaying = false;
            }

        } else if(v == backwardButton && musicList.size() != 0){
            //move back in song

            //currentPos in ms
            int currentPos = mediaService.player.getCurrentPosition();

            //check if time the seek time is greater than 0
            if((currentPos - seekTime) >= 0){
                mediaService.player.seekTo(currentPos - seekTime);
            } else {
                //back to start
                mediaService.player.seekTo(0);
            }

        } else if(v == forwardButton && musicList.size() != 0){
            //move forward in song

            //currentPos in seconds
            int currentPos = mediaService.player.getCurrentPosition();

            //check if our seekForward is less than song duration
            if((currentPos + seekTime) <= mediaService.player.getDuration()){
                mediaService.player.seekTo(currentPos + seekTime);
            } else {
                //forward to end pos
                mediaService.player.seekTo(mediaService.player.getDuration());
            }

        } else if (v == nextButton || v == previousButton){
            if(musicList.size() != 0){
                MusicSong songToPlay;  //the song to play
                int songToPlayIndex = 0;
                MusicSong songPlaying = MusicManager.SongPlaying.getSongPlaying();

                for(MusicSong musicSong : musicList){
                    //if we find the song playing and we pressed nextButton or previous
                    if(songPlaying.getId() == musicSong.getId() && v == nextButton){
                        //get index of next song
                        songToPlayIndex = (musicList.indexOf(musicSong)+ 1);
                        break;
                    } else if (songPlaying.getId() == musicSong.getId() && v == previousButton){
                        //get index of previous song
                        songToPlayIndex = (musicList.indexOf(musicSong)- 1);
                        break;
                    }
                }

                if(songToPlayIndex > musicList.size() - 1){
                    //if we try to use a index greater or smaller than the list size
                    songToPlay = musicList.get(0); // get the first song in list
                } else if (songToPlayIndex < 0){
                    songToPlay = musicList.get(musicList.size() - 1); //get the last song in list
                } else {
                    songToPlay = musicList.get(songToPlayIndex); //song to play
                }

                playSong(songToPlay); // play the next song
            }

        }
    }

    //method to play song
    private void playSong(MusicSong musicSong){
        mediaService.playSong(getBaseContext(), musicSong);

        //change background to pause button
        playButton.setBackground(getResources().getDrawable(R.drawable.pause));
        MusicManager.SongPlaying.setSongPlaying(musicSong); //set the song playing
        songIsPlaying = true;
    }

    //when we press a item in the listView
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MusicSong selectedSong = (MusicSong) songListView.getItemAtPosition(position);
        if(selectedSong != null){
            playSong(selectedSong); //play the song
        }
    }


    // fromUser indicates that the change is from user
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser && mediaService.player != null) {
            mediaService.player.seekTo(progress * 1000); //set the media layer to where we place the seek bar (seconds)
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}

