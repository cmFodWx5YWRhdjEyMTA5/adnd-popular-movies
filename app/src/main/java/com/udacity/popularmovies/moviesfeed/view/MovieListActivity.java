package com.udacity.popularmovies.moviesfeed.view;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.MenuItemCompat;
import androidx.core.view.ViewCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.udacity.popularmovies.R;
import com.udacity.popularmovies.config.ApiConfig.UrlParamValue;
import com.udacity.popularmovies.config.SettingsActivity;
import com.udacity.popularmovies.databinding.ActivityMovieListBinding;
import com.udacity.popularmovies.moviesbookmarks.BookmarkActivity;
import com.udacity.popularmovies.moviesfeed.model.Movie;
import com.udacity.popularmovies.moviesfeed.viewmodel.MovieListActivityViewModel;
import com.udacity.popularmovies.shared.AdaptiveGridLayout;
import com.udacity.popularmovies.shared.BottomNavigationBehavior;
import com.udacity.popularmovies.shared.LanguageUtils;
import com.udacity.popularmovies.shared.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

public class MovieListActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = MovieListActivity.class.getSimpleName();

    private List<Movie> movieList = new ArrayList<>();

    private final String loadApiLanguage = UrlParamValue.LANGUAGE_DEFAULT;
    private boolean isDoingSearch = false;
    private String queryTerm = "";
    private int page = 1;
    private String sortBy = UrlParamValue.POPULAR;
    private Toast toast;
    private static final int HIDE = View.GONE;
    private static final int SHOW = View.VISIBLE;
    private MovieListAdapter movieListAdapter;
    private boolean showAdultContent;
    private ActivityMovieListBinding binding;

    private MovieListActivityViewModel movieListActivityViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);
        Log.d(LOG_TAG, ">>>>> onCreate called!");

        movieListActivityViewModel = ViewModelProviders.of(this).get(MovieListActivityViewModel.class);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_movie_list);
        LanguageUtils.checkSystemLanguage(loadApiLanguage);

        setToolbar();
        setupSharedPreferences();
        setRecyclerViewAdapter();

        // Set and handle actions on BottomNavigation
        //BottomNavigationViewHelper.disableShiftMode(binding.bottomNavigationView);
        binding.bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.button_nav_most_popular:
                        if (!NetworkUtils.isDeviceConnected(getApplicationContext())) {
                            doToast(getResources().getString(R.string.warning_check_internet_connection));
                        } else {
                            sortBy = UrlParamValue.POPULAR;
                            movieList.clear();
                            getPopularMovies();
                        }
                        break;
                    case R.id.button_nav_most_rated:
                        if (!NetworkUtils.isDeviceConnected(getApplicationContext())) {
                            doToast(getResources().getString(R.string.warning_check_internet_connection));
                        } else {
                            sortBy = UrlParamValue.TOP_RATED;
                            movieList.clear();
                            getPopularMovies();
                        }
                        break;
                    case R.id.button_bookmarks:
                        Intent intent = new Intent(getApplicationContext(), BookmarkActivity.class);
                        startActivity(intent);
                }
                return true;
            }
        });

        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) binding.bottomNavigationView.getLayoutParams();
        layoutParams.setBehavior(new BottomNavigationBehavior());

        binding.loadingIndicator.setIndeterminate(true);

        checkConnectionAndLoadContent();

    } // Closes onCreate


    private void checkConnectionAndLoadContent() {
        // Check internet connection before start the loader
        if (!NetworkUtils.isDeviceConnected(this)) {
            // If device is not connected show connections warnings on the screen.
            progressBarStatus(HIDE);
            noResultsWarning(HIDE);
            connectionWarning(SHOW);
        } else {
            connectionWarning(HIDE);
            noResultsWarning(HIDE);
            progressBarStatus(SHOW);
            getPopularMovies();
        }
    }

    private void setRecyclerViewAdapter() {
        movieListAdapter = new MovieListAdapter(getApplicationContext(), movieList);
        binding.rvMovies.setAdapter(movieListAdapter);
        ViewCompat.setNestedScrollingEnabled(binding.rvMovies, false);
        int numberOfColumns = AdaptiveGridLayout.calculateNoOfColumns(getApplicationContext());
        binding.rvMovies.setLayoutManager(new GridLayoutManager(getApplicationContext(), numberOfColumns));
        binding.rvMovies.setItemAnimator(new DefaultItemAnimator());
        binding.rvMovies.setHasFixedSize(true);
        movieListAdapter.notifyDataSetChanged();
    }

    private void getPopularMovies() {

        movieListActivityViewModel.getAllMovies(sortBy).observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(List<Movie> moviesFromLiveData) {
                movieList.clear();
                movieList.addAll(moviesFromLiveData);
                movieListAdapter.notifyDataSetChanged();

                if (movieList.size() > 0) {
                    progressBarStatus(HIDE);
                    noResultsWarning(HIDE);
                    connectionWarning(HIDE);
                }

                for(Movie movie:  movieList) {
                    Log.d(">>> getPopularMovies()", " Movie Name: " + movie.getOriginalTitle());
                }
            }
        });

    }

    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        showAdultContent = sharedPreferences.getBoolean(
                getString(R.string.pref_show_adult_key),
                getResources().getBoolean(R.bool.pref_show_adult_default)
        );
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_show_adult_key))) {
            showAdultContent = sharedPreferences.getBoolean(
                    key,
                    getResources().getBoolean(R.bool.pref_show_adult_default)
            );
            getPopularMovies();
        }else {
            showAdultContent = sharedPreferences.getBoolean(
                    key,
                    getResources().getBoolean(R.bool.pref_show_adult)
            );
            getPopularMovies();
        }
    }

    private void setToolbar() {
        binding.toolbarMovieList.setTitle(R.string.app_name);
        binding.toolbarMovieList.setSubtitle(R.string.the_movie_database);
        setSupportActionBar(binding.toolbarMovieList);
    }

    /* === HELPER METHODS === */

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

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.movie_list_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem); // Cast is required

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                doToast(query);
                searchView.clearFocus();
                isDoingSearch = true;
                movieList.clear();
                queryTerm = query;
//                getPopularMovies();
                // Updates the search flag back to false.
                isDoingSearch = false;
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
                startActivity(startSettingsActivity);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * UI HIDE/SHOW WARNING
     * HELPER METHODS FOR UI WARNINGS - SHOW/HIDE WARNINGS FOR LOADING, INTERNET CONNECTION AND
     * NO RESULTS RETURNED FROM THE SERVER.
     */

    private void progressBarStatus(int VISIBILITY) {
        binding.loadingIndicator.setVisibility(VISIBILITY);
    }

    private void noResultsWarning(int VISIBILITY) {
        binding.ivNoMoviesPlaceholder.setVisibility(VISIBILITY);
        binding.tvWarningNoMovies.setVisibility(VISIBILITY);
        if (VISIBILITY == SHOW) progressBarStatus(HIDE);
    }

    private void connectionWarning(int VISIBILITY) {
        binding.tvNetworkStatus.setVisibility(VISIBILITY);
        binding.ivWarningNoConnectionList.setVisibility(VISIBILITY);
        if (VISIBILITY == SHOW) binding.rvMovies.setVisibility(HIDE);
        else binding.rvMovies.setVisibility(SHOW);
    }


    /**
     * LIFECYCLE METHODS
     */

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, ">>>>> onStart called!");
        if (NetworkUtils.isDeviceConnected(this)) {
            connectionWarning(HIDE);
        } else {
            connectionWarning(SHOW);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, ">>>>> onResume called!");
        // Check internet connection when activity is resumed.
        if (NetworkUtils.isDeviceConnected(this)) {
            connectionWarning(HIDE);
        } else {
            connectionWarning(SHOW);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, ">>>>> onPause called!");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, ">>>>> onStop called!");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(LOG_TAG, ">>>>> onRestart called!");
        // Check internet connection when activity is resumed.
        if (NetworkUtils.isDeviceConnected(this)) {
            connectionWarning(HIDE);
            noResultsWarning(HIDE);
        } else {
            connectionWarning(SHOW);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, ">>>>> onDestroy called!");
        // Unregister VisualizerActivity as an OnPreferenceChangedListener to avoid any memory leaks.
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

}
