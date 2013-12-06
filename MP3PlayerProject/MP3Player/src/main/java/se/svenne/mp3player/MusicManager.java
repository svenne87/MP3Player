package se.svenne.mp3player;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MusicManager {


    public static class SongPlaying{
        //this class is used to keep track of the song currently playing
        private static MusicSong songPlaying;

        //set song playing
        public static void setSongPlaying(MusicSong musicSong){
         songPlaying = musicSong;
            MainActivity.songTextView.setText(musicSong.toString());
        }

        // get song playing
        public static MusicSong getSongPlaying(){
            return songPlaying;
        }
    }

    private List<MusicSong> songs;

    public MusicManager(){
        //empty
    }

    //returns a list of MusicSongObjects (all music on the device)
    public List<MusicSong> listAllMusic(Context context){

        songs = new ArrayList<MusicSong>();

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media._ID
        };

        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null);

        while(cursor.moveToNext()){
            MusicSong song = new MusicSong();
            song.setValues(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5));
            songs.add(song);
        }

        cursor.close();

        /*
        //raw files (for debug mainly)
        Field[] fields = R.raw.class.getFields();
        int uniqueId = 123890455;
        // loop for every file in raw folder
        for(int count=0; count < fields.length; count++){
            // Use that if you just need the file name
            String fileName = fields[count].getName();
            uniqueId = 123890455 + 2345; //a "fake" id for the each of the songs
            MusicSong song = new MusicSong();
            if(fileName != "" || fileName != null){
                song.setValues("android.resource://se.svenne.mp3player/raw/" + fileName, fileName, "", fileName, "", Integer.toString(uniqueId));
                songs.add(song);
            }
        }
        */

        return songs;
    }
}
