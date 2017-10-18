package com.mgenio.jarvisofficedoor.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

/**
 * Created by Austin Nelson on 3/23/2016.
 */
public class PagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<Fragment> fragments;
    private ArrayList<String> titles;

    public PagerAdapter(FragmentManager fm) {
        super(fm);
        fragments = new ArrayList<>();
        titles = new ArrayList<>();
    }

    public void setFragments(ArrayList<Fragment> fragments, ArrayList<String> titles) {
        this.fragments = fragments;
        this.titles = titles;
        notifyDataSetChanged();
    }

    public void add(Fragment fragment, String title) {
        this.fragments.add(fragment);
        this.titles.add(title);
        notifyDataSetChanged();
    }

    public void remove(int i) {
        this.fragments.remove(i);
        this.titles.remove(i);
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        return this.fragments.get(position);
    }

    @Override
    public int getCount() {
        return this.fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position).toUpperCase();
    }
}
