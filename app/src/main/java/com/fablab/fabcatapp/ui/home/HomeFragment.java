package com.fablab.fabcatapp.ui.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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

        Button visitWebsiteButton = root.findViewById(R.id.visitWebsiteButton);
        visitWebsiteButton.setOnClickListener((v) -> goToUrl("http://makerstation.it"));

        Button visitGithubRepositoryButton = root.findViewById(R.id.visitGithubRepositoryButton);
        visitGithubRepositoryButton.setOnClickListener((v) -> goToUrl("https://github.com/Fedefreez/FabCatApp.git"));

        return root;
    }

    private void goToUrl (String url) {
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }
}
