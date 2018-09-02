package com.udacity.popularmovies.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.udacity.popularmovies.models.MovieReview;
import com.udacity.popularmovies.utils.QueryUtilsMovieReviewList;

import java.util.List;

/**
 * Created by jesse on 01/09/18.
 * This is a part of the project adnd-popular-movies.
 */
public class ReviewListLoader extends AsyncTaskLoader<List<MovieReview>> {

    private static final String LOG_TAG = ReviewListLoader.class.getSimpleName();

    private final String reviewsRequestUrl;

    public ReviewListLoader(Context context, String url) {
        super(context);
        reviewsRequestUrl = url;
    }

    @Override
    public List<MovieReview> loadInBackground() {
        if (reviewsRequestUrl == null) {
            return null;
        }
        return QueryUtilsMovieReviewList.fetchMovieReviewListData(reviewsRequestUrl);
    }
}
