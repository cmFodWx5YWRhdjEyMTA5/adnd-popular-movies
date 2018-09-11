package com.udacity.popularmovies.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.squareup.picasso.Picasso;
import com.udacity.popularmovies.R;
import com.udacity.popularmovies.activities.VideoPlayerActivity;
import com.udacity.popularmovies.config.ApiConfig;
import com.udacity.popularmovies.models.MovieVideo;

import java.util.ArrayList;

/**
 * Created by jesse on 01/09/18.
 * This is a part of the project adnd-popular-movies.
 */
public class MovieVideosAdapter extends RecyclerView.Adapter<MovieVideosAdapter.MovieVideoViewHolder> {

    private static final String LOG_TAG = MovieVideosAdapter.class.getSimpleName();

    private final ArrayList<MovieVideo> movieVideoList;
    private final Context mContext;

    public MovieVideosAdapter(Context mContext, ArrayList<MovieVideo> movieList) {
        this.movieVideoList = movieList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MovieVideoViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_video, viewGroup, false);
        return new MovieVideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MovieVideoViewHolder holder, final int position) {
        int adapterPosition = holder.getAdapterPosition();

        holder.videoTitle.setText(movieVideoList.get(adapterPosition).getVideoName());
        holder.videoType.setText(movieVideoList.get(adapterPosition).getVideoType());
        holder.videoSite.setText(movieVideoList.get(adapterPosition).getVideoSite());
        holder.videoSize.setText(String.valueOf(movieVideoList.get(adapterPosition).getVideoSize()));

        Picasso.get()
                .load(movieVideoList.get(position).getVideoThumbnailUrl())
                .placeholder(R.drawable.video_poster_place_holder)
                .fit().centerInside()
                .error(R.drawable.video_poster_place_holder)
                .into(holder.videoThumbnailUrl);

        final String videoID = movieVideoList.get(adapterPosition).getVideoKey();
        holder.itemView.setTag(videoID);
        final Uri uriWebPage = Uri.parse(ApiConfig.getBaseVideoUrlYoutube() + videoID);

        holder.buttonViewOnWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, uriWebPage);
                if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                    mContext.startActivity(intent);
                }
            }
        });

        holder.iconPlayVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String videoUrl = ApiConfig.getBaseVideoUrlYoutube() + videoID;
                Intent intentVideoPlayer = new Intent(mContext, VideoPlayerActivity.class);
                intentVideoPlayer.putExtra("videoUrl", videoUrl);
                mContext.startActivity(intentVideoPlayer);
                Log.d("VIDEO URL", "===>>MovieVideosAdapter" + " - " + ApiConfig.getBaseVideoUrlYoutube() + videoID);
            }
        });

        holder.buttonShareUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, ApiConfig.getBaseVideoUrlYoutube() + videoID);
                intent.setType("text/plain");
                mContext.startActivity(Intent.createChooser(intent, mContext.getResources().getString(R.string.share_to)));
            }
        });
    }

    @Override
    public int getItemCount() {
        return movieVideoList.size();
    }


    public static class MovieVideoViewHolder extends RecyclerView.ViewHolder {

        private final TextView videoTitle;
        private final TextView videoType;
        private final TextView videoSite;
        private final TextView videoSize;
        private final ImageView videoThumbnailUrl;
        private final Button buttonViewOnWeb;
        private final Button buttonShareUrl;
        private final ImageView iconPlayVideo;


        public MovieVideoViewHolder(View itemView) {
            super(itemView);
            videoTitle = itemView.findViewById(R.id.tv_video_title);
            videoType = itemView.findViewById(R.id.tv_video_type);
            videoSite = itemView.findViewById(R.id.tv_video_site);
            videoSize = itemView.findViewById(R.id.tv_video_size);
            videoThumbnailUrl = itemView.findViewById(R.id.iv_movie_video_poster);

            buttonViewOnWeb = itemView.findViewById(R.id.bt_see_on_youtube);
            buttonShareUrl = itemView.findViewById(R.id.bt_share_youtube_url);
            iconPlayVideo = itemView.findViewById(R.id.iv_movie_video_play_icon);
        }
    }

    private void openWebPage(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        if (intent.resolveActivity(mContext.getPackageManager()) != null) {
            mContext.startActivity(intent);
        }
    }

}
