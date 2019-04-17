
package com.udacity.popularmovies.moviedetails.model;

import java.util.List;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Videos implements Parcelable
{

    @SerializedName("results")
    @Expose
    private List<Video> videos = null;
    public final static Parcelable.Creator<Videos> CREATOR = new Creator<Videos>() {

        @SuppressWarnings({
            "unchecked"
        })
        public Videos createFromParcel(Parcel in) {
            return new Videos(in);
        }

        public Videos[] newArray(int size) {
            return (new Videos[size]);
        }

    }
    ;

    protected Videos(Parcel in) {
        in.readList(this.videos, (Video.class.getClassLoader()));
    }

    public Videos() {
    }

    public List<Video> getVideos() {
        return videos;
    }

    public void setVideos(List<Video> videos) {
        this.videos = videos;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(videos);
    }

    public int describeContents() {
        return  0;
    }

    @Override
    public String toString() {
        return "Videos{" +
                "videos=" + videos +
                '}';
    }
}
