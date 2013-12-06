package se.svenne.mp3player;

import java.util.concurrent.TimeUnit;

public class MusicSong {

    private int id;
    private String path;
    private String artist;
    private String title;
    private String displayName;
    private String duration;

    public MusicSong(){
        //empty
    }

    public MusicSong(int id, String path, String artist, String title, String displayName, String duration){
        this.id = id;
        this.path = path;
        this.artist = artist;
        this.title = title;
        this.displayName = displayName;
        this.duration = duration;
    }

    public void setValues(String path, String title, String artist, String displayName, String duration, String idString){
        this.path = path;
        this.title = title;
        this.artist = artist;
        this.displayName = displayName;
        this.duration = duration;
        this.id = Integer.parseInt(idString); //id as int
    }

    //get path
    public String getPath(){
        return path;
    }

    //get duration
    public String getDuration(){
        return duration;
    }

    //set duration
    public void setDuration(String duration){
        this.duration = duration;
    }

    //get artist
    public String getArtist(){
        return artist;
    }

    //return a string with duration in minutes and seconds
    public String getDurationInMinutes(){
        //convert String to Long
        Long durationLong = Long.parseLong(duration);

        String result = String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(durationLong),
                TimeUnit.MILLISECONDS.toSeconds(durationLong) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(durationLong))
        );

        return result;
    }

    //get title
    public String getTitle(){
        return title;
    }

    //get displayName
    public String getDisplayName(){
        return displayName;
    }

    //get the id
    public int getId(){
        return id;
    }

    @Override
    public String toString(){
        return artist + "-" + title;
    }

}
