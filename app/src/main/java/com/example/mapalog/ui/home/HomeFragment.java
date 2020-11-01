package com.example.mapalog.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.mapalog.MainActivity;
import com.example.mapalog.R;

public class HomeFragment extends Fragment implements View.OnClickListener {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.menu_home));

        Button newProjectButton = (Button) view.findViewById(R.id.new_project_button_id);
        newProjectButton.setOnClickListener(this);

        Button savedProjectButton = (Button) view.findViewById(R.id.saved_project_button_id);
        savedProjectButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        NavController navController = NavHostFragment.findNavController(this);
        switch (view.getId()) {
            case R.id.new_project_button_id:
                navController.navigate(R.id.action_nav_home_to_nav_new_project);
                ((MainActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.menu_new_project));
                break;

            case R.id.saved_project_button_id:
                navController.navigate(R.id.action_nav_home_to_nav_saved_project);
                ((MainActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.menu_saved_project));
                break;
        }
    }
}
