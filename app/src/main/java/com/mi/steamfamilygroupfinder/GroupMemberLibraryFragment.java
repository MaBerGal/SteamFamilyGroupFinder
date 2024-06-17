package com.mi.steamfamilygroupfinder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class GroupMemberLibraryFragment extends Fragment {

    public GroupMemberLibraryFragment() {
        // Required empty public constructor
    }

    public static GroupMemberLibraryFragment newInstance(String[] memberIds) {
        GroupMemberLibraryFragment fragment = new GroupMemberLibraryFragment();
        Bundle args = new Bundle();
        args.putStringArray("memberIds", memberIds);
        fragment.setArguments(args);
        return fragment;
    }

    public static GroupMemberLibraryFragment newInstance(String memberId) {
        GroupMemberLibraryFragment fragment = new GroupMemberLibraryFragment();
        Bundle args = new Bundle();
        args.putString("memberId", memberId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_group_member_library, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewPager2 viewPager = view.findViewById(R.id.libraryPaginator);

        // Retrieve memberIds or memberId from arguments bundle
        Bundle args = getArguments();
        String[] memberIds = null;
        String memberId = null;

        if (args != null) {
            memberIds = args.getStringArray("memberIds");
            if (memberIds == null) {
                memberId = args.getString("memberId");
            }
        }

        FragmentStateAdapter pagerAdapter = new LibraryPagerAdapter(requireActivity(), memberIds, memberId);
        viewPager.setAdapter(pagerAdapter);

        TabLayout tabLayout = view.findViewById(R.id.libraryTabs);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText(R.string.tabOwnedGames);
                            break;
                        case 1:
                            tab.setText(R.string.tabInterestedGames);
                            break;
                    }
                }).attach();
    }

    private static class LibraryPagerAdapter extends FragmentStateAdapter {

        private String[] memberIds; // Member variable to hold memberIds array
        private String memberId; // Member variable to hold a single memberId

        public LibraryPagerAdapter(FragmentActivity fa, String[] memberIds, String memberId) {
            super(fa);
            this.memberIds = memberIds;
            this.memberId = memberId;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Fragment fragment;
            Bundle args = new Bundle();

            if (memberIds != null) {
                args.putStringArray("memberIds", memberIds); // Pass memberIds array to fragment arguments
            } else if (memberId != null) {
                args.putString("memberId", memberId); // Pass single memberId to fragment arguments
            }

            switch (position) {
                case 0:
                    fragment = new GroupMemberOwnedGamesFragment();
                    fragment.setArguments(args); // Set arguments bundle to fragment
                    break;
                case 1:
                    fragment = new GroupMemberInterestedGamesFragment();
                    fragment.setArguments(args); // Set arguments bundle to fragment
                    break;
                default:
                    fragment = null;
                    break;
            }

            return fragment;
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
