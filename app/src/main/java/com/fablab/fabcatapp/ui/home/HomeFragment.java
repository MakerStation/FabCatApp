package com.fablab.fabcatapp.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.fablab.fabcatapp.R;
import com.fablab.fabcatapp.ui.bluetooth.BluetoothFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeFragment extends Fragment {
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        setHasOptionsMenu(true);

        FloatingActionButton fab = root.findViewById(R.id.fab);
        fab.setOnClickListener((view) -> BluetoothFragment.sendCustomCommand(view, getContext()));

        return root;
    }
}
