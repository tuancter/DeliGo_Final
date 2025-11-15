package com.deligo.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.deligo.app.R;
import com.deligo.app.activities.OrderHistoryActivity;

/**
 * Fragment displaying order history
 * Temporary wrapper for OrderHistoryActivity content
 */
public class OrdersFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // For now, launch OrderHistoryActivity as a workaround
        // TODO: Convert OrderHistoryActivity content to Fragment layout
        if (savedInstanceState == null) {
            startActivity(new Intent(getActivity(), OrderHistoryActivity.class));
        }
        
        // Return a simple placeholder view
        View view = new View(getContext());
        view.setBackgroundColor(getResources().getColor(android.R.color.white, null));
        return view;
    }
}
