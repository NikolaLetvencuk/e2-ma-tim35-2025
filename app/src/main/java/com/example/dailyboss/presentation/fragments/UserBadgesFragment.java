package com.example.dailyboss.presentation.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyboss.R;
import com.example.dailyboss.data.dao.UserBadgeDao;
import com.example.dailyboss.data.repository.BadgeRepository;
import com.example.dailyboss.data.repository.UserBadgeRepository;
import com.example.dailyboss.domain.model.Badge;
import com.example.dailyboss.domain.model.UserBadge;
import com.example.dailyboss.presentation.adapters.BadgeAdapter;

import java.util.ArrayList;
import java.util.List;

public class UserBadgesFragment extends Fragment {

    private RecyclerView rvBadges;
    private TextView tvTotalBadges;
    private BadgeAdapter badgeAdapter;
    private List<Badge> badgeList = new ArrayList<>();

    private String userId;

    private BadgeRepository badgeRepository;
    private UserBadgeRepository userBadgeRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_badges, container, false);

        rvBadges = view.findViewById(R.id.rvBadges);

        rvBadges.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        badgeAdapter = new BadgeAdapter(getContext(), badgeList);

        userBadgeRepository = new UserBadgeRepository(requireContext());
        badgeRepository = new BadgeRepository(requireContext());

        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            loadUserBadges();
        }
        rvBadges.setAdapter(badgeAdapter);

        return view;
    }

    private void loadUserBadges() {
        if (userId == null) {
            Toast.makeText(getContext(), "User ID nije prosleđen", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Badge> userBadges = userBadgeRepository.getBadgesForUser(userId);
        Log.d("TAG", "loadUserBadges: " + userBadges.size());
        badgeList.clear();
        for (Badge ub : userBadges) {
            Badge badge = badgeRepository.getBadge(ub.getId());
            if (badge != null) badgeList.add(badge);
        }

        badgeAdapter.notifyDataSetChanged();
    }
}