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
import com.deligo.app.activities.MenuActivity;

/**
 * Fragment displaying the menu/home screen
 * Temporary wrapper for MenuActivity content
 */
public class MenuFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // For now, launch MenuActivity as a workaround
        // TODO: Convert MenuActivity content to Fragment layout
        if (savedInstanceState == null) {
            startActivity(new Intent(getActivity(), MenuActivity.class));
        }
        
        // Return a simple placeholder view
        View view = new View(getContext());
        view.setBackgroundColor(getResources().getColor(android.R.color.white, null));
        return view;
    }
}
