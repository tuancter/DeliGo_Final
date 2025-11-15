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
import com.deligo.app.activities.ProfileActivity;

/**
 * Fragment displaying user profile
 * Temporary wrapper for ProfileActivity content
 */
public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // For now, launch ProfileActivity as a workaround
        // TODO: Convert ProfileActivity content to Fragment layout
        if (savedInstanceState == null) {
            startActivity(new Intent(getActivity(), ProfileActivity.class));
        }
        
        // Return a simple placeholder view
        View view = new View(getContext());
        view.setBackgroundColor(getResources().getColor(android.R.color.white, null));
        return view;
    }
}
