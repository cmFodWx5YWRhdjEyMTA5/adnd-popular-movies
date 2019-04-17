package com.udacity.popularmovies.moviesfeed.model;

import android.app.Application;

import androidx.lifecycle.MutableLiveData;

import com.udacity.popularmovies.BuildConfig;
import com.udacity.popularmovies.config.ApiConfig;
import com.udacity.popularmovies.service.MovieDataService;
import com.udacity.popularmovies.service.RetrofitInstance;
import com.udacity.popularmovies.shared.LanguageUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by JesseFariasdeLima on 4/15/2019.
 * This is a part of the project adnd-popular-movies.
 */
public class MovieRepository {

    private List<Movie> movies = new ArrayList<>();
    private MutableLiveData<List<Movie>> movieListMutableLiveData = new MutableLiveData<>();
    private Application application; // Use it

    public MovieRepository(Application application) {
        this.application = application;
    }

    public MutableLiveData<List<Movie>> getMovieListMutableLiveData(String sortBy) {

        MovieDataService movieDataService = RetrofitInstance.getService();

        Call<MoviesResponse> call = movieDataService.getMovies(
                sortBy,
                LanguageUtils.checkSystemLanguage(ApiConfig.UrlParamValue.LANGUAGE_DEFAULT),
                BuildConfig.API_KEY);

        call.enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                MoviesResponse moviesResponse = response.body();
                if (moviesResponse != null) {
                    movies = moviesResponse.getMovies();
                    if (movies != null) {
                        movieListMutableLiveData.setValue(movies);
                    }
                }
            }

            @Override
            public void onFailure(Call<MoviesResponse> call, Throwable t) { }
        });

        return movieListMutableLiveData;
    }

}
