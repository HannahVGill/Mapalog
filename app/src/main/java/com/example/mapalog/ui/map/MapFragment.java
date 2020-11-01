package com.example.mapalog.ui.map;
import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mapalog.MainActivity;
import com.example.mapalog.Project;
import com.example.mapalog.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;


public class MapFragment extends Fragment implements Serializable {

    Bitmap canvasBitmap;
    Canvas canvas;
    View view;

    private CurrentProjectViewModel model;
    int x;
    int y;
    int canvasX;
    int canvasY;

    Project currentProject;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_map, container, false);

        model = new ViewModelProvider(requireActivity()).get(CurrentProjectViewModel.class);

        Log.d("tag", "onCreateView: about to request permissions in MapFragment");

        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        CharSequence projectName = model.getProjectName().getValue();
        if (projectName != null) {
            ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
            CharSequence currentFragmentActionBarTitle = actionBar.getTitle();
            actionBar.setTitle(currentFragmentActionBarTitle + ": " + projectName);
        } else {
            ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
            CharSequence currentFragmentActionBarTitle = actionBar.getTitle();
            actionBar.setTitle(currentFragmentActionBarTitle + ": no project map loaded yet");
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            draw();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reinitialiseCanvasBitmapWithPhoto() throws Exception {
        final ImageView finalImageViewMapPhoto = (ImageView) view.findViewById(R.id.imageViewMapPhoto);

        Bitmap bitmap = model.getMapPhoto().getValue();
        //check bitmap is not null and if it is raise an exception
        if (bitmap == null) {
            throw new Exception();
        }

        canvasBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());

        canvas = new Canvas(canvasBitmap);

        Bitmap mapPhotoBitmap = bitmap;

        Uri bitmapTempUri = getImageUri(getContext(), mapPhotoBitmap);
        Log.d("bitmapTempUri", ": " + bitmapTempUri);

        File photoFile = new File(getRealPathFromURI(bitmapTempUri));
        Log.d("photoFile", ": " + photoFile);

        Rect photoRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        canvas.drawBitmap(mapPhotoBitmap, null, photoRect, null);

        finalImageViewMapPhoto.setImageBitmap(canvasBitmap);

    }

    public Uri getImageUri(Context inputContext, Bitmap inputImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inputImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inputContext.getContentResolver(), inputImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    public void convertViewXyToCanvasXy(View v) {
        int vWidth = v.getWidth();
        int vHeight = v.getHeight();
        int cBmWidth = canvasBitmap.getWidth();
        int cBmHeight = canvasBitmap.getHeight();
        canvasX = ((x * cBmWidth) / vWidth);
        canvasY = ((y * cBmHeight) / vHeight);
    }

    public void draw() throws Exception {

        reinitialiseCanvasBitmapWithPhoto();

        currentProject = model.getProject().getValue();

        final ImageView finalImageViewMapPhoto = (ImageView) view.findViewById(R.id.imageViewMapPhoto);
        finalImageViewMapPhoto.setImageBitmap(canvasBitmap);
    }

    public Rect initialiseMarkerRect(int X, int Y) {
        Rect markerRect;
        int reqMarkerWidth = 250;
        int reqMarkerHeight = 250;

        int rectLeft = X - (reqMarkerWidth / 2);
        int rectTop = Y - reqMarkerHeight;
        int rectRight = rectLeft + reqMarkerWidth;
        int rectBottom = rectTop + reqMarkerHeight;
        markerRect = new Rect(rectLeft, rectTop, rectRight, rectBottom);
        return markerRect;
    }
}