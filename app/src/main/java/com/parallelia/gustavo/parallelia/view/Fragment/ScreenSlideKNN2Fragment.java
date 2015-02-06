package com.parallelia.gustavo.parallelia.view.Fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parallelia.gustavo.parallelia.R;

/**
 * Created by gustavo on 28/12/14.
 */
public class ScreenSlideKNN2Fragment extends Fragment {

    /**
     * Key to insert the background color into the mapping of a Bundle.
     */
    private static final String BACKGROUND_COLOR = "color";

    /**
     * Key to insert the index page into the mapping of a Bundle.
     */
    private static final String INDEX = "index";

    private int color;
    private int index;
    private String algorithm;

    /**
     * Instances a new fragment with a background color and an index page.
     *
     * @param color
     *            background color
     * @param index
     *            index page
     * @return a new page
     */
    public static ScreenSlideKNN2Fragment newInstance(int color, int index) {

        // Instantiate a new fragment
        ScreenSlideKNN2Fragment fragment = new ScreenSlideKNN2Fragment();

        // Save the parameters
        Bundle bundle = new Bundle();
        bundle.putInt(BACKGROUND_COLOR, color);
        bundle.putInt(INDEX, index);
        fragment.setArguments(bundle);
        fragment.setRetainInstance(true);

        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Load parameters when the initial creation of the fragment is done
        this.color = (getArguments() != null) ? getArguments().getInt(
                BACKGROUND_COLOR) : Color.GRAY;
        this.index = (getArguments() != null) ? getArguments().getInt(INDEX)
                : -1;


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_screen_knn_page, container, false);

        // Show the current page index in the view
//        TextView tvIndex = (TextView) rootView.findViewById(R.id.tvIndex);
  //      tvIndex.setText(String.valueOf(this.index));

        // Change the background color
        rootView.setBackgroundColor(this.color);

        return rootView;

    }
}
