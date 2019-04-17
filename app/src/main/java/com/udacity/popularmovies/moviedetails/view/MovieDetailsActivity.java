package com.udacity.popularmovies.moviedetails.view;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;
import com.udacity.popularmovies.BuildConfig;
import com.udacity.popularmovies.R;
import com.udacity.popularmovies.config.ApiConfig;
import com.udacity.popularmovies.databinding.ActivityMovieDetailsBinding;
import com.udacity.popularmovies.localdatabase.BookmarkContract.BookmarkEntry;
import com.udacity.popularmovies.moviedetails.model.MovieDetailsResponse;
import com.udacity.popularmovies.moviedetails.model.ProductionCompany;
import com.udacity.popularmovies.moviedetails.model.Review;
import com.udacity.popularmovies.moviedetails.model.Video;
import com.udacity.popularmovies.moviesfeed.model.Movie;
import com.udacity.popularmovies.service.MovieDataService;
import com.udacity.popularmovies.service.RetrofitInstance;
import com.udacity.popularmovies.shared.LanguageUtils;
import com.udacity.popularmovies.shared.NetworkUtils;
import com.udacity.popularmovies.shared.WebViewActivity;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings("ALL")
public class MovieDetailsActivity extends AppCompatActivity {

    private static final String API_KEY = BuildConfig.API_KEY;
    private static final String LOG_TAG = MovieDetailsActivity.class.getSimpleName();

    private static final int HIDE = View.GONE;
    private static final int SHOW = View.VISIBLE;
    private static final int INVISIBLE = View.INVISIBLE;

    private String loadApiLanguage = ApiConfig.UrlParamValue.LANGUAGE_DEFAULT;
    private int page = 1;

    private Movie movie;
    private MovieDetailsResponse movieDetailsResponse;
    private int movieId;
    private String movieHomepageUrl;
    private String movieOriginalTitle = "";

    private final ArrayList<ProductionCompany> productionCompaniesList = new ArrayList<>();
    private CompanyListAdapter companyListAdapter;

    private final ArrayList<Video> movieVideosList = new ArrayList<>();
    private MovieVideosAdapter movieVideosAdapter;

    private final ArrayList<Review> movieReviewsList = new ArrayList<>();
    private MovieReviewsAdapter movieReviewsAdapter;

    private Toast toast;
    private boolean isBookmarkOnDatabase;
    private boolean isMovieWatched;

    private int deviceWidth;
    private ActivityMovieDetailsBinding activityMovieDetailsBinding;

    FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        Log.d(LOG_TAG,"===>>> onCreate called");

        activityMovieDetailsBinding = DataBindingUtil.setContentView(this, R.layout.activity_movie_details);

        getIncomingIntent();
        progressBarStatus(SHOW);
        initViews();
        getMovieDetails();

        Toolbar toolbar = findViewById(R.id.toolbar_movie_details);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        // Setup CollapsingToolbar with movie name
        activityMovieDetailsBinding.collapsingToolbar.setTitle(movieOriginalTitle);

        LanguageUtils.checkSystemLanguage(loadApiLanguage);
        // Hide the Tagline TextView due to API to provide tagline to english language only or the
        // movie occasionally may have a empty tagline value. Then the space will be shown.
        if (loadApiLanguage.equals("")) activityMovieDetailsBinding.tvTagline.setVisibility(View.GONE);
        else activityMovieDetailsBinding.tvTagline.setVisibility(View.VISIBLE);

    } // Close onCreate

    // TODO: FIX IT!!!
//    @BindingAdapter({"bind:genres"})
//    public static void setGenres(LinearLayout linearLayout, MovieDetailsResponse movieDetailsResponse) {
//        for (Genre genre : movieDetailsResponse.getGenres()) {
//            TextView textView = new TextView(linearLayout.getContext());
//            textView.setText(genre.getName());
//            textView.append(" ");
//            linearLayout.addView(textView);
//        }
//    }

    // It's called inside onCreate method and get the movie ID and Title sent from MovieListActivity.
    private void getIncomingIntent() {
        Intent intent = getIntent();

        if (intent.hasExtra("movieId")) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                movieId = bundle.getInt("movieId");
                movieOriginalTitle = bundle.getString("movieTitle");
                activityMovieDetailsBinding.tvTitle.setText(movieOriginalTitle);

                Log.d(LOG_TAG, ">>> getIncomingIntent - Movie Name: " + movieOriginalTitle + " - Id: " + movieId);
            }
        }
    }


    private void getMovieDetails() {

        MovieDataService movieDataService = RetrofitInstance.getService();
        Call<MovieDetailsResponse> call = movieDataService.getMovieDetails(movieId, loadApiLanguage, BuildConfig.API_KEY);
        call.enqueue(new Callback<MovieDetailsResponse>() {
            @Override
            public void onResponse(Call<MovieDetailsResponse> call, Response<MovieDetailsResponse> response) {
                if (response.body() != null) {
                    movieDetailsResponse = response.body();
                    if (movieDetailsResponse != null) {
                            progressBarStatus(HIDE);
                            activityMovieDetailsBinding.setMovieDetailsResponse(movieDetailsResponse);
                        } else {
                            progressBarStatus(HIDE);
                            warningDetails(SHOW);
                        }
                    }
                }

            @Override
            public void onFailure(Call<MovieDetailsResponse> call, Throwable t) {
            }
        });

    }


    public class MovieDetailsActivityHandlers {

        Context context;

        public MovieDetailsActivityHandlers(Context context) {
            this.context = context;
        }

        private void openWebPage(String urlHomepage) {
            Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                intent.putExtra(ApiConfig.JsonKey.HOMEPAGE, urlHomepage);
                intent.putExtra(ApiConfig.JsonKey.ORIGINAL_TITLE, movieDetailsResponse.getOriginalTitle());
            startActivity(intent);
        }

        private void testMethod(String string) {
            Toast.makeText(context, "Test Method Clicked!", Toast.LENGTH_SHORT).show();
        }

    }

    private void initViews() {

        // TODO: Review the use of this method

    }


    /* === SQLITE DATABASE MANAGEMENT METHODS === */
    private class SaveBookmarkAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            progressBarStatus(SHOW);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            int watched = 0;

            // Implement save method

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (isBookmarkOnDatabase) {
//                activityMovieDetailsBinding.floatSaveBookmark.setImageResource(R.drawable.ic_bookmark_saved);
                Toast.makeText(MovieDetailsActivity.this, getResources().getString(R.string.movie_saved), Toast.LENGTH_SHORT).show();
                // isBookmarkOnDatabase = true;
            } else {
                doToast(getResources().getString(R.string.warning_could_not_be_saved));
//                activityMovieDetailsBinding.floatSaveBookmark.setImageResource(R.drawable.ic_bookmark_unsaved);
                isBookmarkOnDatabase = false;
            }
            progressBarStatus(INVISIBLE);
        }
    }

    private class DeleteBookmarkAsyncTask extends AsyncTask<Integer, Void, Integer> {

        @Override
        protected void onPreExecute() {
            progressBarStatus(SHOW);
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Integer... integers) {

            long id = integers[0];
            String stringId = Long.toString(id);

            return getContentResolver().delete(
                    BookmarkEntry.CONTENT_URI,
                    BookmarkEntry.COLUMN_API_ID + "=?",
                    new String[]{stringId}
            );
        }

        @Override
        protected void onPostExecute(Integer rowsDeleted) {
            if (rowsDeleted > 0) {

                activityMovieDetailsBinding.buttonIsSaved.setIcon(getResources().getDrawable(R.drawable.ic_bookmark_unsaved));
                activityMovieDetailsBinding.buttonIsWatched.setIcon(getResources().getDrawable(R.drawable.ic_watched_not));
                activityMovieDetailsBinding.tvLabelWatched.setVisibility(HIDE);
                doToast(getResources().getString(R.string.movie_removed));
                isBookmarkOnDatabase = false;
                isMovieWatched = false;
            } else {
                doToast(getResources().getString(R.string.warning_could_not_be_removed));
            }
            progressBarStatus(INVISIBLE);
        }
    }

    private class CheckBookmarkAsyncTask extends AsyncTask<Void, Void, Cursor> {

        @Override
        protected void onPreExecute() {
            progressBarStatus(SHOW);
            super.onPreExecute();
        }

        @Override
        protected Cursor doInBackground(Void... voids) {
            String stringId = String.valueOf(movieDetailsResponse.getId());
            Uri uri = BookmarkEntry.CONTENT_URI;
            uri = uri.buildUpon().appendPath(stringId).build();

            Cursor cursor = getContentResolver().query(
                    BookmarkEntry.CONTENT_URI,
                    new String[]{BookmarkEntry.COLUMN_IS_WATCHED},          // projection (colunms).
                    BookmarkEntry.COLUMN_API_ID + "=?",            // selection
                    new String[]{String.valueOf(movieDetailsResponse.getId())},   // selectionArgs
                    null);                                         // sortOrder
            return cursor;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {

//            isBookmarkOnDatabase = cursor.getCount() > 0;
//            if (isBookmarkOnDatabase) {
//                activityMovieDetailsBinding.floatSaveBookmark.setImageResource(R.drawable.ic_bookmark_saved);
//            } else {
//                activityMovieDetailsBinding.floatSaveBookmark.setImageResource(R.drawable.ic_bookmark_unsaved);
//            }
//
//            if (cursor.moveToFirst()) {
//                int WATCHED = cursor.getInt(cursor.getColumnIndex(BookmarkEntry.COLUMN_IS_WATCHED));
//                if (WATCHED == 0) {
//                    isMovieWatched = false;
//                    activityMovieDetailsBinding.floatIsWatched.setImageResource(R.drawable.ic_watched_not);
//                    animateTextFadeOut(activityMovieDetailsBinding.tvLabelWatched);
//                }if (WATCHED == 1){
//                    isMovieWatched = true;
//                    activityMovieDetailsBinding.floatIsWatched.setImageResource(R.drawable.ic_watched_yes);
//                    animateTextFadeIn(activityMovieDetailsBinding.tvLabelWatched);
//                }
//            }
//            progressBarStatus(INVISIBLE);
        }
    }

    private class UpdateBookmarkAsyncTask extends AsyncTask<Integer, Void, Integer> {

        @Override
        protected void onPreExecute() {
            progressBarStatus(SHOW);
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Integer... integers) {

            int NOT_WATCHED = 0;
            int IS_WATCHED = 1;
            // Default value is Not Watched == 0
            int WATCHED_STATUS = 0;

            if (isMovieWatched) WATCHED_STATUS = NOT_WATCHED;
            else  WATCHED_STATUS = IS_WATCHED;

            ContentValues contentValues = new ContentValues();
            contentValues.put(BookmarkEntry.COLUMN_IS_WATCHED,   WATCHED_STATUS);

            long id = integers[0];
            String stringId = Integer.toString(movieDetailsResponse.getId());
            Uri uri = BookmarkEntry.CONTENT_URI;
            uri = uri.buildUpon().appendPath(stringId).build();

            return getContentResolver()
                    .update(
                        uri,
                        contentValues,
                        null,
                        null
            );
        }

        @Override
        protected void onPostExecute(Integer rowsUpdated) {

            if (rowsUpdated > 0) {
                if (isMovieWatched){
//                    activityMovieDetailsBinding.floatIsWatched.setImageResource(R.drawable.ic_watched_not);
                    doToast(getResources().getString(R.string.not_watched));
                    animateTextFadeOut(activityMovieDetailsBinding.tvLabelWatched);
                    isMovieWatched = false;
                }else {
//                    activityMovieDetailsBinding.floatIsWatched.setImageResource(R.drawable.ic_watched_yes);
                    Toast.makeText(MovieDetailsActivity.this, getResources().getString(R.string.watched), Toast.LENGTH_SHORT).show();
                    animateTextFadeIn(activityMovieDetailsBinding.tvLabelWatched);
                    isMovieWatched = true;
                }
            }
            progressBarStatus(INVISIBLE);
        }
    }

    /* === HELPER METHODS === */

    public byte[] convertImageViewToBytes(ImageView imageView) {
        /* Get ImageView to Bitmap from Greg Giacovelli
        https://stackoverflow.com/questions/4715044/android-how-to-convert-whole-imageview-to-bitmap */
        imageView.buildDrawingCache();
        Bitmap imageBitmap = imageView.getDrawingCache();
        /* */

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    private void shareWebUrl() {
        if (movieDetailsResponse.getHomepage().equals("null")) {
            doToast(getString(R.string.warning_homepage_not_available));
        } else {
            String movieInfoToShare = getString(R.string.checkout_this_movie);
            movieInfoToShare += "\n" + movieDetailsResponse.getOriginalLanguage();
            movieInfoToShare += "\n" + movieDetailsResponse.getHomepage();
            Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, movieInfoToShare);
                intent.setType("text/plain");
            startActivity(Intent.createChooser(intent, getResources().getString(R.string.share_to)));
        }
    }

    /**
     * This method makes the reuse of toast object to avoid toasts queue
     *
     * @param toastThisText is the text you want to show in the toast.
     */
    public void doToast(String toastThisText) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, toastThisText, Toast.LENGTH_LONG);
        toast.show();
    }


    /**
     * When called must receive a String url and will open default browser on device or ask to the
     * user about what application he wants to uses to open the URL.
     *
     * @param urlHomepage is the url to be open in the browser.
     */
    private void openWebPage(String urlHomepage) {
        if (urlHomepage == null) {
            doToast(getString(R.string.no_home_page_available));
            return;
        }
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(ApiConfig.JsonKey.HOMEPAGE, urlHomepage);
        intent.putExtra(ApiConfig.JsonKey.ORIGINAL_TITLE, movieOriginalTitle);
        startActivity(intent);
    }

    /**
     * When called do a fade in effect on a TextView.
     * @param textToFadeIn is any TextView object to be animated using fade effect.
     */
    private void animateTextFadeIn(TextView textToFadeIn) {
        textToFadeIn.setVisibility(SHOW);
        textToFadeIn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_animation));
    }
    /**
     * When called do a fade out effect on a TextView.
     * @param textToFadeOut is any TextView object to be animated using fade effect.
     */
    private void animateTextFadeOut(TextView textToFadeOut) {
        textToFadeOut.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out_animation));
        textToFadeOut.setVisibility(HIDE);
    }


    /* === UI HIDE/SHOW WARNING === */

    private void warningConnection(int VISIBILITY) {
        activityMovieDetailsBinding.tvWarningNoData.setVisibility(VISIBILITY);
        activityMovieDetailsBinding.ivWarningNoData.setVisibility(VISIBILITY);
        if (VISIBILITY == SHOW) {
            setContentVisibility(HIDE);
            doToast(getString(R.string.warning_you_are_not_connected));
        } else {
            setContentVisibility(SHOW);
        }
    }

    private void setContentVisibility(int VISIBILITY) {
        activityMovieDetailsBinding.buttonIsSaved.setVisibility(VISIBILITY);
        activityMovieDetailsBinding.buttonShare.setVisibility(VISIBILITY);
        activityMovieDetailsBinding.ivMoviePoster.setVisibility(VISIBILITY);
        activityMovieDetailsBinding.sectionFullContent.setVisibility(VISIBILITY);
    }

    private void warningDetails(int VISIBILITY) {
        activityMovieDetailsBinding.tvWarningNoData.setText(getResources().getText(R.string.warning_no_details));
        activityMovieDetailsBinding.tvWarningNoData.setVisibility(VISIBILITY);
        activityMovieDetailsBinding.ivWarningNoData.setImageResource(R.drawable.ic_details);
        activityMovieDetailsBinding.ivWarningNoData.setVisibility(VISIBILITY);

    }

    private void progressBarStatus(int VISIBILITY) {
        if (VISIBILITY == SHOW) activityMovieDetailsBinding.indeterminateBar.setIndeterminate(true);
        else activityMovieDetailsBinding.indeterminateBar.setIndeterminate(false);
        activityMovieDetailsBinding.indeterminateBar.setVisibility(VISIBILITY);
    }


    /* === Lifecycle methods === */

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("===>>> onStart", " called");
        if (NetworkUtils.isDeviceConnected(this)) {
            //  TODO ->  new CheckBookmarkAsyncTask().execute();
            warningConnection(HIDE);
        } else {
            warningConnection(SHOW);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("===>>> onPause", " called");
        if (NetworkUtils.isDeviceConnected(this)) {
            warningConnection(HIDE);
        } else {
            warningConnection(SHOW);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("===>>> onResume", " called");
        // Check internet connection when activity is resumed.
        if (NetworkUtils.isDeviceConnected(this)) {
            warningConnection(HIDE);
            getMovieDetails();
        } else {
            warningConnection(SHOW);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("===>>> onRestart", " called");
    }
}
