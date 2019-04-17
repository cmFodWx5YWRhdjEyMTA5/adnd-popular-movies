package com.udacity.popularmovies.moviesfeed.viewmodel;

import android.app.Application;

import com.udacity.popularmovies.moviesfeed.model.Movie;
import com.udacity.popularmovies.moviesfeed.model.MovieRepository;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class MovieListActivityViewModel extends AndroidViewModel {

    private MovieRepository movieRepository;

    public MovieListActivityViewModel(@NonNull Application application) {
        super(application);
        movieRepository = new MovieRepository(application);
    }

    public LiveData<List<Movie>> getAllMovies(String sortBy) {
        return movieRepository.getMovieListMutableLiveData(sortBy);
    }
}
