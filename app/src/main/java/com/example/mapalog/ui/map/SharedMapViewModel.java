package com.example.mapalog.ui.map;


import android.graphics.Bitmap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mapalog.Project;

public class SharedMapViewModel extends ViewModel {

    private final MutableLiveData<Bitmap> selected = new MutableLiveData<Bitmap>();

    public void setMapPhoto(Bitmap bitmap) {
        selected.setValue(bitmap);
    }

    public LiveData<Bitmap> getMapPhoto() {
        return selected; }


    private final MutableLiveData<String> newProjectName = new MutableLiveData<String>(); //TODO: check if being used

    public void setProjectName(String string) {
        newProjectName.setValue(string);
    }

    public LiveData<String> getProjectName() {
        return newProjectName;
    }


    private final MutableLiveData<Project> project = new MutableLiveData<Project>();

    public void setProject(Project project) { this.project.setValue(project); }

    public LiveData<Project> getProject() {
        return project;
    }
}
