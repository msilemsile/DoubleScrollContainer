package com.msile.android.doublescrollcontainer;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Example3Activity extends AppCompatActivity {

    private ViewPager bottomPager;
    private LinearLayout bottomTabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example_3);
        bottomPager = (ViewPager) findViewById(R.id.double_scroll_bottom_view);
        bottomTabs = (LinearLayout) findViewById(R.id.bottom_tabs);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomPager.setAdapter(new MyPageAdapter(getSupportFragmentManager()));
        bottomPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                changeTabStyle(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        for (int i = 0; i < 3; i++) {
            final int pos = i;
            TextView tab = (TextView) bottomTabs.getChildAt(i);
            tab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (bottomPager.getCurrentItem() != pos) {
                        bottomPager.setCurrentItem(pos);
                        changeTabStyle(pos);
                    }
                }
            });
        }
        changeTabStyle(0);
    }

    private void changeTabStyle(int selectPos) {
        for (int i = 0; i < 3; i++) {
            TextView tab = (TextView) bottomTabs.getChildAt(i);
            if (selectPos == i) {
                tab.setTextColor(Color.parseColor("#0000ff"));
            } else {
                tab.setTextColor(Color.BLACK);
            }
        }
    }

    class MyPageAdapter extends FragmentPagerAdapter {

        public MyPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return position % 2 == 0 ? Example3SubFragment1.newInstance() : Example3SubFragment2.newInstance();
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
