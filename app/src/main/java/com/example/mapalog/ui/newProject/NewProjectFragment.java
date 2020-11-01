package com.example.mapalog.ui.newProject;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class NewProjectFragment extends Fragment implements Serializable {

    ImageView imageViewMapPhoto;
    String pathToFile;
    private View view = null;
    Button photographMapButton;
    Button savedPhotoButton;
    private CurrentProjectViewModel model;
    String newProjectNameString;
    EditText newProjectNameInput;
    private final int TAKE_PHOTO = 1;
    private final int LOAD_PHOTO = 2;
    Uri loadedImagePath;
    String loadingPhotoOriginalPath;
    Project newProject;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_new_project, container, false);
        model = new ViewModelProvider(requireActivity()).get(CurrentProjectViewModel.class);

        photographMapButton = (Button) view.findViewById(R.id.photograph_map_button_id);
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        photographMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("CLICK", "Click on button");
                newProjectNameString = newProjectNameInput.getText().toString();
                if (newProjectNameString.length() == 0) {
                    Toast.makeText(getContext(), R.string.User_forgets_project_name_toast, Toast.LENGTH_SHORT).show();
                } else {
                    Map<String, File> tempMap = Project.getSavedProjectList(getActivity());
                    if (tempMap.containsKey(newProjectNameString)) {
                        Toast.makeText(getContext(), R.string.User_should_rename_project, Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            Toast.makeText(getContext(), R.string.Photo_capture_orientation_prompt_toast, Toast.LENGTH_LONG).show();
                            createProject();
                            dispatchPhotoTakerAction();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.d("CALL", "Call dispatch method");
                    }
                }
            }
        });


        imageViewMapPhoto = view.findViewById(R.id.imageViewMapPhoto);

        savedPhotoButton = (Button) view.findViewById(R.id.retrieve_stored_map_button_id);

        savedPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("CLICK", "Click on button");
                newProjectNameString = newProjectNameInput.getText().toString();

                if (newProjectNameString.length() == 0) {
                    Toast.makeText(getContext(), R.string.User_forgets_project_name_toast, Toast.LENGTH_SHORT).show();
                } else {
                    Map<String, File> tempMap = Project.getSavedProjectList(getActivity());

                    if (tempMap.containsKey(newProjectNameString)) {
                        Toast.makeText(getContext(), R.string.User_should_rename_project, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), R.string.Photo_load_orientation_prompt_toast, Toast.LENGTH_LONG).show();
                        createProject();
                        loadSavedPhotoAction();
                        Log.d("CALL", "Call dispatch method");
                    }
                }
            }
        });

        imageViewMapPhoto = view.findViewById(R.id.imageViewMapPhoto);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        newProjectNameInput = (EditText) getView().findViewById(R.id.new_project_name_input);
    }

    //CAMERA METHODS
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //TAKE PHOTO
        if (resultCode == Activity.RESULT_OK && requestCode == TAKE_PHOTO) {

            // removes visibility when photo displayed
            removeVisibleButtons();
            Bitmap bitmap = BitmapFactory.decodeFile(pathToFile); //here pathToFile holds path for Uri
            model.setMapPhoto(bitmap);
            model.setProjectName(newProjectNameString);

            newProject.assignPhotoPath(pathToFile);

            try {
                newProject.saveData(getActivity());
            } catch (IOException e) {
                e.printStackTrace();
            }

            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_nav_new_project_to_nav_map);

        } else if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(getContext(), R.string.User_cancels_photo_toast, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), R.string.Photo_capture_failed, Toast.LENGTH_SHORT).show();
        }

        //LOAD EXISTING PHOTO
        if (resultCode == Activity.RESULT_OK && requestCode == LOAD_PHOTO) {
            loadedImagePath = data.getData(); // getData actually provides intent here, different from when taking photo
            loadingPhotoOriginalPath = getRealPathFromURI(loadedImagePath);
            removeVisibleButtons();
            // CODE TO DISPLAY PHOTO HERE
            Bitmap bitmap = BitmapFactory.decodeFile(loadingPhotoOriginalPath);
            model.setMapPhoto(bitmap);
            model.setProjectName(newProjectNameString);

            this.newProject.assignPhotoPath(loadingPhotoOriginalPath);

            try {
                newProject.saveData(getActivity());
            } catch (IOException e) {
                e.printStackTrace();
            }

            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_nav_new_project_to_nav_map);
        }
    }

    private void removeVisibleButtons() {
        // removes visibility when photo displayed
        view.findViewById(R.id.new_project_name_label).setVisibility(View.GONE);
        view.findViewById(R.id.new_project_name_input).setVisibility(View.GONE);
        view.findViewById(R.id.photograph_map_button_id).setVisibility(View.GONE);
        view.findViewById(R.id.retrieve_stored_map_button_id).setVisibility(View.GONE);
    }

    private void dispatchPhotoTakerAction() throws IOException {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePhotoIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File mapPhotoFile = null;

            try {
                mapPhotoFile = createMapPhotoFile();
            } catch (IOException e) {
                Log.d("myLog", "Exception: " + e.toString());
            }

            if (mapPhotoFile != null) {
                pathToFile = mapPhotoFile.getAbsolutePath();
                Uri mapPhotoURI = FileProvider.getUriForFile(getContext(), "com.example.mapalog.fileprovider", mapPhotoFile);
                Log.d("AFTER URI", "After URI");

                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mapPhotoURI);
                Log.d("AFTER PUTEXTRA", "AFTER PUTEXTRA");
                startActivityForResult(takePhotoIntent, 1);
            }
        }
    }

    private void loadSavedPhotoAction() {
        Intent loadSavedPhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
        loadSavedPhotoIntent.setType("image/*");

        if (loadSavedPhotoIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File mapPhotoFile = null;

            try {
                mapPhotoFile = createMapPhotoFile();
            } catch (IOException e) {
                Log.d("myLog", "Exception: " + e.toString());
            }

            if (mapPhotoFile != null) {
                pathToFile = loadingPhotoOriginalPath;
                startActivityForResult(Intent.createChooser(loadSavedPhotoIntent, "Select photo"), LOAD_PHOTO);
            }
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        //Uri contentUri = Uri.parse(contentURI);

        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = null;
        try {
            if (Build.VERSION.SDK_INT > 19) {
                // Will return "image:x*"
                String wholeID = DocumentsContract.getDocumentId(contentUri);
                // Split at colon, use second item in the array
                String id = wholeID.split(":")[1];
                // where id is equal to
                String sel = MediaStore.Images.Media._ID + "=?";

                cursor = getContext().getContentResolver().query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projection, sel, new String[]{id}, null);
            } else {
                cursor = getContext().getContentResolver().query(contentUri,
                        projection, null, null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String path = null;
        try {
            int column_index = cursor
                    .getColumnIndex(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            path = cursor.getString(column_index).toString();
            cursor.close();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return path;
    }


    private File createMapPhotoFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String mapPhotoFileName = newProjectNameString + "_" + timeStamp + "_";
        File storageDirectory = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(mapPhotoFileName, ".jpg", storageDirectory);
        } catch (IOException exception) {
            Log.d("myLog", "Exception: " + exception.toString());
        }
        return image;
    }

    @Override
    public void onResume() {
        super.onResume();

        Project project = model.getProject().getValue();
        model.setProject(project);
    }

    public void createProject() {
        newProject = new Project();
        newProject.assignProjectName(newProjectNameString);
        model.setProject(newProject);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("call_onRequest_base_class", "onRequestPermissionsResult: ");
    }

}

