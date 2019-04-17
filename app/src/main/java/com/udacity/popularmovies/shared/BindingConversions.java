package com.udacity.popularmovies.shared;

import android.view.View;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;

import com.udacity.popularmovies.R;

import java.text.DecimalFormat;


/**
 * Created by JesseFariasdeLima on 4/17/2019.
 * This is a part of the project adnd-popular-movies.
 */
public class BindingConversions {

    private static final String EMPTY_STRING = "";
    private static final String LOG_TAG = BindingConversions.class.getSimpleName();

    /**
     * How to use this got set isVisible property
     *     <TextView
     *         ...
     *         app:isVisible="@{post.hasComments()}" />
     *
     * @param view
     * @param isVisible
     */
    @BindingAdapter("isVisible")
    public static void setIsVisible(View view, boolean isVisible) {
        view.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    }

    @BindingAdapter("checkIfZeroAndFormat")
    public static void checkIfZeroAndFormatValue(TextView textView, int value) {
        if (value == 0) {
            textView.setText(textView.getResources().getString(R.string.not_available));
        } else {
            textView.setText(String.valueOf(formatNumberToString(value)));
        }
    }

    /**
     * When called must receive a number that will be converted to a String with the pattern $###,###,###.##
     *
     * @param numberToBeFormatted is the number (Buget or Revenue) to be formated.
     * @return a the number as String properly formated.
     */
    private static String formatNumberToString(int numberToBeFormatted) {
        DecimalFormat myFormatter = new DecimalFormat("$###,###,###.##");
        return myFormatter.format(numberToBeFormatted);
    }

    private static String formatNumberToString(double numberToBeFormatted) {
        DecimalFormat myFormatter = new DecimalFormat("$###,###,###.##");
        return myFormatter.format(numberToBeFormatted);
    }


}
