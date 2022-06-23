package com.smartiotdevices.iotbox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class PageAdapter extends FragmentPagerAdapter
{
    private ArrayList<Fragment> fragment_list;
    private ArrayList<String> fragment_title_list;

    @SuppressWarnings("deprecation")
    PageAdapter(FragmentManager fmng)
    {
        super(fmng);
        fragment_list=new ArrayList<>();
        fragment_title_list=new ArrayList<>();
    }

    @NonNull
    @Override
    public Fragment getItem(int position)
    {
        return fragment_list.get(position);
    }

    @Override
    public int getCount()
    {
        return fragment_list.size();
    }

    void addFragment(Fragment fragment, String title)
    {
        fragment_list.add(fragment);
        fragment_title_list.add(title);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position)
    {
        return fragment_title_list.get(position);
    }
}
