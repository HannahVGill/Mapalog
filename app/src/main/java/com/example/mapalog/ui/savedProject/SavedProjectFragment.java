package com.example.mapalog.ui.savedProject;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.mapalog.Project;
import com.example.mapalog.R;
import com.example.mapalog.ui.map.CurrentProjectViewModel;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.mapalog.R.id;
import static com.example.mapalog.R.layout;

public class SavedProjectFragment extends Fragment implements Serializable {

    View view;
    private CurrentProjectViewModel model;
    String loadedImagePath;
    TextView rowTextView;
    ListView listView;
    Map<String, File> listMap;
    Project savedProject;
    ArrayAdapter adapter;
    ArrayList<String> displayNames;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(layout.fragment_saved_project, container, false);
        model = new ViewModelProvider(requireActivity()).get(CurrentProjectViewModel.class);

        rowTextView = (TextView) view.findViewById(id.row_text);
        listView = (ListView) view.findViewById(id.list_saved_project_files);

        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
        }

        listMap = new HashMap<String, File>();

        displayProjectFileList();

        removeVisibleButton();

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Log.d("Clicked", "Item clicked");
                final String itemName = (String) adapterView.getItemAtPosition(position);
                try {
                    savedProject = Project.loadData(listMap.get(itemName));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                loadedImagePath = savedProject.getPhotoPath();
                Bitmap bitmap = BitmapFactory.decodeFile(loadedImagePath);
                model.setMapPhoto(bitmap);
                model.setProject(savedProject);
                model.setProjectName(itemName);
            }
        });

        ImageButton openSavedProjectButton = (ImageButton) view.findViewById(id.open_saved_project_button_id);
        openSavedProjectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeVisibleButtons();
                NavController navController = NavHostFragment.findNavController(SavedProjectFragment.this);
                navController.navigate(R.id.action_nav_saved_project_to_nav_map);
            }
        });

        ImageButton deleteSavedProjectButton = (ImageButton) view.findViewById(id.delete_saved_project_button_id);
        deleteSavedProjectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                alertDialog.getWindow().setGravity(Gravity.BOTTOM);

                alertDialog.setTitle("PROJECT DELETION");
                alertDialog.setMessage("Permanently delete this project?");
                alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteSavedProject();
                    }
                });
                alertDialog.setButton(Dialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                alertDialog.show();
            }
        });

        ImageButton shareSavedProjectButton = (ImageButton) view.findViewById(id.share_saved_project_button_id);
        shareSavedProjectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.setType("message/rfc822");
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Shared Mapalog app project");

                ArrayList<Uri> uriList = new ArrayList<Uri>();
                File storageDirectory = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                String fileName = model.getProjectName().getValue();
                File projectDocumentFile = new File(storageDirectory, fileName + ".where");
                File projectPictureFile = new File(loadedImagePath);

                Uri projectDocumentFileUri = FileProvider.getUriForFile(getContext(), "com.example.mapalog.fileprovider", projectDocumentFile);
                Uri projectPictureFileUri = FileProvider.getUriForFile(getContext(), "com.example.mapalog.fileprovider", projectPictureFile);
                uriList.add(projectDocumentFileUri);
                uriList.add(projectPictureFileUri);

                emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);

                try {
                    startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                    Toast.makeText(getContext(), "Only share non-copyrighted map images.", Toast.LENGTH_LONG).show();
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getContext(), "There is no email client.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private void removeVisibleButton() {
        // removes visibility when photo displayed
        view.findViewById(R.id.select_saved_project_button_id).setVisibility(View.GONE);
    }

    private void removeVisibleButtons() {
        // removes visibility when photo displayed
        view.findViewById(R.id.text_saved_project).setVisibility(View.GONE);
        view.findViewById(R.id.select_saved_project_button_id).setVisibility(View.GONE);
        view.findViewById(R.id.list_saved_project_files).setVisibility(View.GONE);
        view.findViewById(id.open_saved_project_button_id).setVisibility(View.GONE);
        view.findViewById(id.delete_saved_project_button_id).setVisibility(View.GONE);
        view.findViewById(id.share_saved_project_button_id).setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void displayProjectFileList() {
        Map<String, File> tempMap = Project.getSavedProjectList(getActivity());
        for (String fileName : tempMap.keySet()) {
            File file = tempMap.get(fileName);
            listMap.put(fileName, file);
        }

        displayNames = new ArrayList<String>(listMap.keySet());
        adapter = new ArrayAdapter<String>(getContext(), R.layout.row, displayNames);
        try {
            listView.setAdapter(adapter);
        } catch (Exception e) {
            Log.d("myLog", "Exception: " + e.toString());
        }
    }

    public void deleteSavedProject() {
        File storageDirectory = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        String fileName = model.getProjectName().getValue();
        File projectDocumentFile = new File(storageDirectory, fileName + ".where");
        if (projectDocumentFile != null) {
            projectDocumentFile.delete();
        }

        File projectPictureFile = new File(loadedImagePath);
        if (projectPictureFile != null) {
            projectPictureFile.delete();
        }

        adapter.remove(fileName);
        listMap.remove(fileName);
        adapter.notifyDataSetChanged();
        displayProjectFileList();
    }
}


