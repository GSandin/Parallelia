package com.parallelocr.gustavo.parallelocr.view.Fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.parallelocr.gustavo.parallelocr.R;

/**
 * Created by gustavo on 28/12/14.
 */
public class ScreenSlideSVMFragment extends Fragment {

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
    private String n_algorithm = "SVM";
    private TextView timer;
    private ViewGroup rootView;
    long timeInMilliseconds = 0L;
    long updatedTime = 0L;
    private long startTime = 0L;
    private Handler customHandler = new Handler();

    /**
     * Instances a new fragment with a background color and an index page.
     *
     * @param color
     *            background color
     * @param index
     *            index page
     * @return a new page
     */
    public static ScreenSlideSVMFragment newInstance(int color, int index) {

        // Instantiate a new fragment
        ScreenSlideSVMFragment fragment = new ScreenSlideSVMFragment();

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

        this.rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_screen_svm_page, container, false);

        //ViewGroup rootView = (ViewGroup) inflater.inflate(
                //R.layout.fragment_screen_knn_page, container, false);

        // Show the current page index in the view
//        TextView tvIndex = (TextView) rootView.findViewById(R.id.tvIndex);
  //      tvIndex.setText(String.valueOf(this.index));

        // Change the background color
        rootView.setBackgroundColor(this.color);

        //Put the name of algorithm
        Button algorithm = (Button) rootView.findViewById(R.id.SVM);
        algorithm.setText(n_algorithm);

        Button algorithm_p = (Button) rootView.findViewById(R.id.SVM_parallel);
        algorithm_p.setText(n_algorithm + " Paralyzed");

        timer=(TextView)this.rootView.findViewById(R.id.timer);

        return rootView;

    }

    public void initTimer(){
        startTime = SystemClock.uptimeMillis();
        customHandler.postDelayed(updateTimerThread, 0);
    }

    public void finishTimer(){
        customHandler.removeCallbacks(updateTimerThread);
    }

    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeInMilliseconds;
            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            int hour = mins / 60;
            secs = secs % 60;
            int milliseconds = (int) (updatedTime % 1000);
            timer.setText("" + hour + ":" + String.format("%02d",mins) + ":"
                    + String.format("%02d", secs) + ":"
                    + String.format("%03d", milliseconds));
            customHandler.postDelayed(this, 0);
        }
    };
}
