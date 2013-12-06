package se.svenne.mp3player;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.List;


public class MediaService extends Service implements MediaPlayer.OnCompletionListener {

    protected MediaPlayer player;
    private boolean isCreated = false;
    private boolean isPlaying = false;
    private boolean songIsPaused = false;
    private NotificationManager notificationMan;
    private Notification notification;
    private PendingIntent pendingNotificationIntent;

    int mStartMode;

    public final IBinder localBinder = new LocalBinder();
    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        notificationMan = (NotificationManager) this.getApplicationContext().getSystemService(this.getApplicationContext().NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(this.getApplicationContext(), MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationIntent.setAction(Intent.ACTION_MAIN);

        pendingNotificationIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return mStartMode;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //when the song is complete, play the next one

        MusicManager musicMan = new MusicManager();
        MusicSong songPlaying = MusicManager.SongPlaying.getSongPlaying();

        List<MusicSong> songs;
        songs = musicMan.listAllMusic(getApplicationContext()); //get all songs

        int songToPlayIndex = 0;
        MusicSong songToPlay;

        if(songs.size() != 0){
            for(MusicSong song : songs){
                //check if the id is the same, (since list is instantiated from different classes)
                if(songPlaying.getId() == song.getId()){
                    //get index of next song
                    songToPlayIndex = (songs.indexOf(song))+ 1;
                    break;
                }
            }

            if(songToPlayIndex < songs.size() - 1){
                songToPlay = songs.get(songToPlayIndex); //song to play
                MusicManager.SongPlaying.setSongPlaying(songToPlay); //set the new song playing
                playSong(this, songToPlay);
            } else {
                //if we have played the last song in list
                pauseSong(this);
                //change background on button
                MainActivity.playButton.setBackground(getResources().getDrawable(R.drawable.play));
                player.seekTo(0);
                MainActivity.songTextView.setText("");
                songIsPaused = false;
            }

        }
    }


    //Declared as a inner class
    //This will allows us to return our service object to our activity
    //so the activity can control methods within the service
    public class LocalBinder extends Binder {
        MediaService getService(){
            return MediaService.this;
        }
    }


    //method used to start the player
    public void playSong(final Context c, final MusicSong musicSong) {

        //convert string to Uri
        Uri song = Uri.parse(musicSong.getPath());

        //if isCreated is false
        if(!isCreated){
            this.player = MediaPlayer.create(c, song);  //create player
            isCreated = true; //set to true
            this.player.setOnCompletionListener(this);
        }
        try{
            //start player
            this.player.reset();
            this.player.setDataSource(c, song);
            this.player.prepare();
            this.player.start();
            isPlaying = true;
            songIsPaused = false;
        } catch(IOException ex) {
            Log.v("Exception: ", ex.getMessage());
        }

        //if the player is started
        if(this.player != null){
            //get duration for song and set the max for seek bar here
            int total = player.getDuration() / 1000;  //get the duration for current song (seconds)
            MainActivity.seekBar.setMax(total);

            mHandler.postDelayed(mRunnable, 1000);
        }

    }


    final Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            int mCurrentPosition = player.getCurrentPosition() / 1000;
            MainActivity.seekBar.setProgress(mCurrentPosition);

            mHandler.postDelayed(this, 500);
        }
    };


    //used to create notification
    public void createNotification(){

        MusicSong songPlaying = MusicManager.SongPlaying.getSongPlaying();

        //if there is a song playing
        if(songPlaying != null){
            notification = new Notification.Builder(this.getApplicationContext())
                    .setContentTitle("Mp3 Player")
                    .setContentText(songPlaying.toString())
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(pendingNotificationIntent)
                    .build();
        } else {
            notification = new Notification.Builder(this.getApplicationContext())
                    .setContentTitle("Mp3 Player")
                    .setContentText("")
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(pendingNotificationIntent)
                    .build();
        }

        if(notification != null){
            notificationMan.notify(1234, notification);
        }

    }

    //method used to pause the player
    public void pauseSong(Context c) {
        this.player.pause();
        isPlaying = false;
        songIsPaused = true;
    }

    //method to resume the song
    public void resume(){
        if(this.player != null){
            this.player.start();
            isPlaying = true;
            songIsPaused = false;
        }
    }

    //returns the state of the media player, if it plays och is paused
    public boolean getState(){
        return isPlaying;
    }


    //returns if the player is paused waiting to be started
    public boolean getPlayerState(){
        return songIsPaused;
    }

    //used to cancel notification
    public void cancelNotification(){
        if(notification != null){
            notificationMan.cancel(1234); //stop notification (with id: 1234)
        }
    }


    //when service is destroyed, stop player
    @Override
    public void onDestroy() {
        super.onDestroy();
        this.player.stop();
        this.player.release();
        isPlaying = false;
        songIsPaused = false;
        MainActivity.playButton.setBackground(getResources().getDrawable(R.drawable.play));
    }

}
