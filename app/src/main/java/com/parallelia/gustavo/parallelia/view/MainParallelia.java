package com.parallelia.gustavo.parallelia.view;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import com.parallelia.gustavo.parallelia.R;
import com.parallelia.gustavo.parallelia.view.Fragment.ParallelPagerAdapter;
import com.parallelia.gustavo.parallelia.view.Fragment.ScreenSlidePageFragment;
import com.parallelia.gustavo.parallelia.view.Transformer.DepthPageTransformer;
import com.viewpagerindicator.TitlePageIndicator;

public class MainParallelia extends FragmentActivity {

    /**
     * The pager widget, which handles animation and allows swiping horizontally
     * to access previous and next pages.
     */
    ViewPager pager = null;

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

        adapter.addFragment(ScreenSlidePageFragment.newInstance(getResources()
                .getColor(R.color.blue), 0));
        adapter.addFragment(ScreenSlidePageFragment.newInstance(getResources()
                .getColor(R.color.purple), 1));
        adapter.addFragment(ScreenSlidePageFragment.newInstance(getResources()
                .getColor(R.color.green), 2));

        this.pager.setAdapter(adapter);
        PagerAdapter pa = this.pager.getAdapter();
        System.out.println(pa.toString());

        TitlePageIndicator titleIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
        titleIndicator.setViewPager(this.pager);
    }

    @Override
    public void onBackPressed() {

        // Return to previous page when we press back button
        if (this.pager.getCurrentItem() == 0)
            super.onBackPressed();
        else
            this.pager.setCurrentItem(this.pager.getCurrentItem() - 1);

    }
}
