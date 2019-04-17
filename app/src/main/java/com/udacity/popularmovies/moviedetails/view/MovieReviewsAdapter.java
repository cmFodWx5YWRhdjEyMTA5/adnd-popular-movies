package com.udacity.popularmovies.moviedetails.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.udacity.popularmovies.R;
import com.udacity.popularmovies.moviedetails.model.Review;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

/**
 * Created by jesse on 01/09/18.
 * This is a part of the project adnd-popular-movies.
 */
@SuppressWarnings("unused")
public class MovieReviewsAdapter extends RecyclerView.Adapter<MovieReviewsAdapter.MovieReviewViewHolder> {

    @SuppressWarnings("unused")
    private static final String LOG_TAG = MovieReviewsAdapter.class.getSimpleName();
    private static final int COMMENT_MAX_LINES = 999;

    private final List<Review> reviewList;
    private final Context mContext;

    public MovieReviewsAdapter(Context context, List<Review> reviewList) {
        this.reviewList = reviewList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public MovieReviewViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_review, viewGroup, false);
        return new MovieReviewViewHolder(view);
    }

    @SuppressWarnings("unused")
    @Override
    public void onBindViewHolder(@NonNull final MovieReviewViewHolder holder, int position) {
        final int adapterPosition = holder.getAdapterPosition();

        holder.textViewReviewAuthor.setText(reviewList.get(adapterPosition).getAuthor());
        holder.textViewReviewContent.setText(reviewList.get(adapterPosition).getContent());

        final String commentUrl = reviewList.get(adapterPosition).getUrl();
        holder.itemView.setTag(commentUrl);
        holder.textViewSeeReviewOnWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (commentUrl.equals("null") || TextUtils.isEmpty(commentUrl)) {
                    Toast.makeText(mContext, R.string.comment_url_not_available, Toast.LENGTH_SHORT).show();
                } else {
                    Uri uriWebPage = Uri.parse(commentUrl);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uriWebPage);
                    if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                        mContext.startActivity(intent);
                    }
                }
            }
        });

        holder.buttonReadButtonMoreShow.setOnClickListener(new View.OnClickListener() {
            boolean isReadMoreButtonVisible;

            @Override
            public void onClick(View view) {
                TransitionManager.beginDelayedTransition(holder.transitionsContainer);
                if (!isReadMoreButtonVisible) {
                    isReadMoreButtonVisible = true;
                    holder.textViewReviewContent.setMaxLines(COMMENT_MAX_LINES);
                    holder.buttonReadButtonMoreShow.setText(R.string.hide);
                    holder.textViewReviewContent.setVisibility(isReadMoreButtonVisible ? View.VISIBLE : View.GONE);
                } else {
                    isReadMoreButtonVisible = false;
                    holder.textViewReviewContent.setMaxLines(5);
                    holder.buttonReadButtonMoreShow.setText(R.string.read_more);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class MovieReviewViewHolder extends RecyclerView.ViewHolder {

        private final TextView textViewReviewAuthor;
        private final TextView textViewReviewContent;
        private final TextView textViewSeeReviewOnWeb;
        private final Button buttonReadButtonMoreShow;
        private final ViewGroup transitionsContainer;

        public MovieReviewViewHolder(View itemView) {
            super(itemView);
            textViewReviewAuthor = itemView.findViewById(R.id.tv_review_author);
            textViewReviewContent = itemView.findViewById(R.id.tv_review_content);
            textViewSeeReviewOnWeb = itemView.findViewById(R.id.bt_see_review_on_web);

            buttonReadButtonMoreShow = itemView.findViewById(R.id.bt_read_more_show);
            transitionsContainer = itemView.findViewById(R.id.transitions_container);
        }
    }
}
