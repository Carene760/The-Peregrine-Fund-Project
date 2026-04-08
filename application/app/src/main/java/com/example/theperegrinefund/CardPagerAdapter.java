package com.example.theperegrinefund;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class CardPagerAdapter extends FragmentStateAdapter {

    private Fragment1 fragment1;
    private Fragment2 fragment2;

    public CardPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        fragment1 = new Fragment1();
        fragment2 = new Fragment2();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return fragment1;
            case 1: return fragment2;
            default: return fragment1;
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    // Getters pour accéder aux fragments existants
    public Fragment1 getFragment1() {
        return fragment1;
    }

    public Fragment2 getFragment2() {
        return fragment2;
    }
}
