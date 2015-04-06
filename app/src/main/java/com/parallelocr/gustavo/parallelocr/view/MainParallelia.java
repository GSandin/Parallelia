package com.parallelocr.gustavo.parallelocr.view;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.parallelocr.gustavo.parallelocr.R;
import com.parallelocr.gustavo.parallelocr.controller.ClassifierKNN;
import com.parallelocr.gustavo.parallelocr.controller.ClassifierKNNP;
import com.parallelocr.gustavo.parallelocr.view.Fragment.ParallelPagerAdapter;
import com.parallelocr.gustavo.parallelocr.view.Fragment.ScreenSlideSVMFragment;
import com.parallelocr.gustavo.parallelocr.view.Fragment.ScreenSlideKNNFragment;
import com.parallelocr.gustavo.parallelocr.view.Transformer.DepthPageTransformer;
import com.viewpagerindicator.TitlePageIndicator;

public class MainParallelia extends FragmentActivity{

    /**
     * Attributes
     */
    ViewPager pager = null;

    /**
     * Method to create activity
     * @param arg0
     */
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        this.setContentView(R.layout.activity_main_parallelia);

        // Instantiate a ViewPager
        this.pager = (ViewPager) this.findViewById(R.id.pager);

        this.pager.setPageTransformer(true, new DepthPageTransformer());

        // Create an adapter with the fragments we show on the ViewPager
        ParallelPagerAdapter adapter = new ParallelPagerAdapter(
                getSupportFragmentManager());

        adapter.addFragment(ScreenSlideKNNFragment.newInstance(getResources()
                .getColor(R.color.black), 0));
        adapter.addFragment(ScreenSlideSVMFragment.newInstance(getResources()
                .getColor(R.color.black), 1));

        this.pager.setAdapter(adapter);
        PagerAdapter pa = this.pager.getAdapter();

        TitlePageIndicator titleIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
        titleIndicator.setViewPager(this.pager);
    }

    /**
     * Method to come back the previous page
     */
    @Override
    public void onBackPressed() {
        // Return to previous page when we press back button
        if (this.pager.getCurrentItem() == 0)
            super.onBackPressed();
        else
            this.pager.setCurrentItem(this.pager.getCurrentItem() - 1);
    }

    /**
     * Method to run the KNN without paralyze
     * @param v
     */
    public void KNN(View v){
        ParallelPagerAdapter adapter = (ParallelPagerAdapter) pager.getAdapter();
        ScreenSlideKNNFragment fragment = (ScreenSlideKNNFragment)adapter.getItem(0);
        ClassifierKNN c = new ClassifierKNN(fragment);
        c.execute(this);
    }

    /**
     * Method to run the KNN without paralyze
     * @param v
     */
    public void KNN_Parallel(View v){
        ParallelPagerAdapter adapter = (ParallelPagerAdapter) pager.getAdapter();
        ScreenSlideKNNFragment fragment = (ScreenSlideKNNFragment)adapter.getItem(0);
        ClassifierKNNP c = new ClassifierKNNP(fragment);
        c.execute(this);
    }
}
