package com.example.chatting_app.views;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.View;

import com.example.chatting_app.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatHomeActivity extends AppCompatActivity {

    @BindView(R.id.tabs)
    TabLayout mTabLayout;
    @BindView(R.id.fab)
    FloatingActionButton mFab;
    @BindView(R.id.viewpager)
    ViewPager mViewPager;

    ViewPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_home);
        ButterKnife.bind(this);     // bindview 들의 어노테이션을 가능케 하는 선언부

        mTabLayout.setupWithViewPager(mViewPager);
        setUpViewPager();

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment currentFragment = mPagerAdapter.getItem(mViewPager.getCurrentItem());
                if (currentFragment instanceof FriendFragment) {
                    ((FriendFragment) currentFragment).togglesearchBar();
                }
            }
        });
    }

    // viewPager setting method 선언부
    private void setUpViewPager() {
        mPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mPagerAdapter.addFragment(new FriendFragment(), "친구");
        mPagerAdapter.addFragment(new ChatFragment(), "채팅");
        mPagerAdapter.addFragment(new SearchFragment(), "검색");
        mViewPager.setAdapter(mPagerAdapter);
    }

    // viewPagerAdapter를 생성하는 부분
    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragmentList = new ArrayList<>();
        private List<String> fragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }
        @NonNull
        @Override
        public Fragment getItem(int position) {     // 아이템 들을 반환해주기 위해
            return fragmentList.get(position);
        }
        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {    // 타이틀을 반환해주기 위한
            return fragmentTitleList.get(position);
        }
        @Override
        public int getCount() {
            return fragmentList.size();
        }
        public void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }
    }
}