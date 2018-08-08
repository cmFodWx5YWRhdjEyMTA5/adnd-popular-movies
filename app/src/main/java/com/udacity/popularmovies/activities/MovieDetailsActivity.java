package com.udacity.popularmovies.activities;

import android.app.LoaderManager;
import android.content.Loader;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.udacity.popularmovies.R;
import com.udacity.popularmovies.config.ApiConfig;
import com.udacity.popularmovies.config.ApiKey;
import com.udacity.popularmovies.loaders.MovieLoader;
import com.udacity.popularmovies.models.Movie;
import com.udacity.popularmovies.utils.DateUtils;
import com.udacity.popularmovies.utils.NetworkUtils;

import java.util.Arrays;
import java.util.Objects;

public class MovieDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Movie> {

    private int movieId;
    private String movieOriginalTitle = "";

    private static final String LOG_TAG = MovieDetailsActivity.class.getSimpleName();
    private static final int MOVIE_DETAILS_LOADER_ID = 2;
    private String loadApiLanguage = ApiConfig.UrlParamValue.LANGUAGE_DEFAULT;

    private Toast toast;

    private ImageView imageViewMoviePoster, imageViewMovieBackdrop;
    private TextView textViewOverview, textViewReleaseDate, textViewVoteAverage, textViewRuntime;
    private TextView textViewNetworkStatus, textViewNoMovieDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        getIncomingIntent();

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(movieOriginalTitle);

        // Images layout references
        imageViewMoviePoster = findViewById(R.id.iv_movie_poster);
        imageViewMovieBackdrop = findViewById(R.id.iv_movie_backdrop);

        textViewOverview = findViewById(R.id.tv_overview);
        textViewReleaseDate = findViewById(R.id.tv_release_date);
        textViewVoteAverage = findViewById(R.id.tv_vote_average);
        textViewRuntime = findViewById(R.id.tv_runtime);

        // Warnings layout
        textViewNetworkStatus = findViewById(R.id.tv_network_status);
        textViewNoMovieDetails = findViewById(R.id.tv_no_movie_details);


        if (!NetworkUtils.isDeviceConnected(this)) {
            doToast("You are not connected!");
//            linearLayoutDetails.setVisibility(View.GONE);
            textViewNetworkStatus.setVisibility(View.VISIBLE);
        } else {
            // Shows loading indicator and Kick off the loader
            Log.d(LOG_TAG, "Loader Kick off...");
            android.app.LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(MOVIE_DETAILS_LOADER_ID, null, this);
        }

        String[] apiLanguagesList = ApiConfig.LANGUAGES;
        // Verify is the api supports the system language. If not API data will be load in english as default
        if (Arrays.asList(apiLanguagesList).contains(Resources.getSystem().getConfiguration().locale.getLanguage())) {
            loadApiLanguage = Resources.getSystem().getConfiguration().locale.getLanguage();
        } else {
            doToast(getResources().getString(R.string.warning_language));
        }

    }

    private void getIncomingIntent() {
        if (getIntent().hasExtra(ApiConfig.JsonKey.ID) && getIntent().hasExtra(ApiConfig.JsonKey.ORIGINAL_TITLE)) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                movieId = bundle.getInt(ApiConfig.JsonKey.ID);
                movieOriginalTitle = bundle.getString(ApiConfig.JsonKey.ORIGINAL_TITLE);
            }
        }
    }

    @Override
    public Loader<Movie> onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG, "onCreateLoader Started...");

        Uri baseUrl = Uri.parse(ApiConfig.getBaseMovieDetailsUrl() + String.valueOf(movieId));
        Uri.Builder uriBuilder = baseUrl.buildUpon();
        uriBuilder.appendQueryParameter(ApiConfig.UrlParamKey.API_KEY, ApiKey.getApiKey());
        uriBuilder.appendQueryParameter(ApiConfig.UrlParamKey.LANGUAGE, loadApiLanguage);

        Log.v("REQUEST URL ==> ", uriBuilder.toString());

        return new MovieLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<Movie> loader, Movie movie) {
        Log.d(LOG_TAG, "onLoadFinished Started. Outputting data...");
        if (!isMovieValid(movie)) {
            textViewNetworkStatus.setVisibility(View.GONE);
            updateUI(movie);
        } else {
            textViewNoMovieDetails.setVisibility(View.VISIBLE);
        }
    }

    private boolean isMovieValid(Movie movie) {
        return movie == null;
    }

    @Override
    public void onLoaderReset(Loader<Movie> loader) {}

    /**
     * This method makes the reuse of toast object to avoid toasts queue
     *
     * @param toastThisText is the text you want to show in the toast.
     */
    private void doToast(String toastThisText) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, toastThisText, Toast.LENGTH_LONG);
        toast.show();
    }

    private void updateUI(Movie movie) {

        textViewOverview.setText(movie.getMovieOverview());
        textViewVoteAverage.setText(String.valueOf(movie.getMovieVoteAverage() + getString(R.string.slash_10)));
        textViewRuntime.setText(String.valueOf(movie.getMovieRunTime() + getString(R.string.min)));

        String formatedDate = DateUtils.simpleDateFormat(movie.getMovieReleaseDate());
        textViewReleaseDate.setText(formatedDate);
//
        Picasso.get()
                .load(movie.getMoviePosterPath())
//                .placeholder(R.drawable.poster_image_place_holder)
                .fit().centerInside()
//                .error(R.drawable.poster_image_place_holder)
                .into(imageViewMoviePoster);
//
        Picasso.get()
                .load(movie.getMovieBackdropPath())
//                .placeholder(R.drawable.poster_image_place_holder)
                .fit().centerInside()
//                .error(R.drawable.poster_image_place_holder)
                .into(imageViewMovieBackdrop);
    }
}