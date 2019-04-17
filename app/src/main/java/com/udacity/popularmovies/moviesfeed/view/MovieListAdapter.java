package com.udacity.popularmovies.moviesfeed.view;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.udacity.popularmovies.R;
import com.udacity.popularmovies.databinding.ItemListMovieBinding;
import com.udacity.popularmovies.moviedetails.view.MovieDetailsActivity;
import com.udacity.popularmovies.moviesfeed.model.Movie;

import java.util.List;

/**
 * An {@link MovieListAdapter} knows how to create a list item layout for each movie item
 * in the data source (a list of {@link Movie} objects).
 * <p>
 * These list item layouts will be provided to an adapter view like RecyclerView
 * to be displayed to the user.
 * Official Documentation: https://developer.android.com/reference/android/support/v7/widget/RecyclerView.Adapter
 */
public class MovieListAdapter extends RecyclerView.Adapter<MovieListAdapter.MovieViewHolder> {

    @SuppressWarnings("unused")
    private static final String LOG_TAG = MovieListAdapter.class.getSimpleName();

    private List<Movie> movieList;
    private Context context;

    /**
     * Constructs a new {@link MovieListAdapter}.
     *
     * @param context  of the app
     * @param movieList is the list of movies, which is the data source of the adapter
     */
    public MovieListAdapter(Context context, List<Movie> movieList) {
       this.movieList = movieList;
       this.context = context;
    }

    /**
     * Inflates the layout for each movie item and return that view.
     * @param viewType The view type of the new View.
     * @param viewGroup  The ViewGroup into which the new View will be added after it is bound to an adapter position. It's the ViewGroup object used by the Inflater.
     * @return a movie item view object represents the inflated layout filled with
     * data for each item(movie) in the list on the UI
     * Official Documentation: https://developer.android.com/reference/android/support/v7/widget/RecyclerView.Adapter#onBindViewHolder
     */
    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        ItemListMovieBinding itemListMovieBinding = DataBindingUtil.inflate(LayoutInflater.from(
                        viewGroup.getContext()),
                        R.layout.item_list_movie,
                        viewGroup,
                        false);

        return new MovieViewHolder(itemListMovieBinding);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * It replaces the contents of a view (invoked by the layout manager)
     *
     * @param position The position of the item within the adapter's data set.
     * @param holder is the ViewGroup object used by the Inflater. The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     *
     */
    @Override
    public void onBindViewHolder(@NonNull MovieListAdapter.MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        holder.itemListMovieBinding.setMovie(movie);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return movieList.size();
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     * RecyclerView.Adapter implementations should subclass ViewHolder and add fields for caching potentially expensive findViewById(int) results.
     * All UI references in the item list does not require recall all IDs over and over again while the view is recycled.
     */
    public class MovieViewHolder extends RecyclerView.ViewHolder {

        private ItemListMovieBinding itemListMovieBinding;

        private MovieViewHolder(@NonNull ItemListMovieBinding mItemListMovieBinding) {
            super(mItemListMovieBinding.getRoot());

            this.itemListMovieBinding = mItemListMovieBinding;

            itemListMovieBinding.ivMoviePoster.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToDetailsActivities();
                }
            });

            itemListMovieBinding.cardMovieItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToDetailsActivities();
                }
            });
        }

        private void goToDetailsActivities() {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Movie movie = movieList.get(position);
                Intent intent = new Intent(context, MovieDetailsActivity.class);
                    intent.putExtra("movieId",      movie.getId());
                    intent.putExtra("movieTitle",   movie.getTitle());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }
    }
}
