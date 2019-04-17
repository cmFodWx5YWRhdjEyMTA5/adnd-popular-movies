package com.udacity.popularmovies.service;


import com.udacity.popularmovies.config.ApiConfig.UrlParamKey;
import com.udacity.popularmovies.moviedetails.model.MovieDetailsResponse;
import com.udacity.popularmovies.moviesfeed.model.MoviesResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;


/**
 * Created by JesseFariasdeLima on 4/8/2019.
 * This is a part of the project adnd-popular-movies.
 */
public interface MovieDataService {

    @GET("movie/{queryType}")
    Call<MoviesResponse> getMovies(
            @Path("queryType") String queryType,
            @Query(UrlParamKey.LANGUAGE) String language,
            @Query(UrlParamKey.API_KEY) String apiKeyValue
    );

    @GET("movie/{movieId}?append_to_response=images,videos,reviews")
    Call<MovieDetailsResponse> getMovieDetails(
            @Path ("movieId") int movieId,
            @Query(UrlParamKey.LANGUAGE) String language,
            @Query(UrlParamKey.API_KEY) String apiKeyValue
    );

}
