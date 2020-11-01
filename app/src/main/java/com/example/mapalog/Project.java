package com.example.mapalog;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import com.example.mapalog.ui.map.CurrentProjectViewModel;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Project extends Activity implements Serializable {
    private CurrentProjectViewModel model;
    private String name;
    private String photoPath;
    private static Map<String, File> projectListMap;

    public Project() {
        this.name = "";
        this.photoPath = "";
    }

    public Project(String name, String photoPath) {
        this.name = name;
        this.photoPath = photoPath;
    }

    public void assignProjectName(String name) {
        this.name = name;
    }

    public void assignPhotoPath(String path) {
        this.photoPath = path;
    }

    public String getName() {
        return name;
    }


    public String getPhotoPath() {
        return photoPath;
    }

    //Serialize object and store
    public void saveData(Activity activity) throws IOException {
        projectToGsonJson();
    }

    //Deserialize object and retrieve from storage
    public static Project loadData(File fileToLoad) throws IOException {
        return projectFromGsonJson(fileToLoad);
    }

    public void projectToGsonJson() throws IOException {
        Context context = MainActivity.getContext();

        File fileOut = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/" + name + ".where");
        FileOutputStream fileOutputStream = new FileOutputStream(fileOut);

        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));

        Gson gson = new Gson();
        String s1_jsonString = gson.toJson(name);
        bufferedWriter.write(s1_jsonString);
        bufferedWriter.newLine();
        String s3_pathString = ((Project) this).photoPath;
        bufferedWriter.write(s3_pathString);
        bufferedWriter.newLine();
        bufferedWriter.close();
    }

    public static Project projectFromGsonJson(File fileIn) throws IOException {
        String name = new String();
        Uri photoUri = null;
        Project retrievedProject = new Project();
        Context context = MainActivity.getContext();
        String jsonStringLine = null;
        Gson gson = new Gson();
        FileInputStream fileInputStream = new FileInputStream(fileIn);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));

        String jsonName = bufferedReader.readLine();
        name = gson.fromJson(jsonName, String.class);

        String photoPath = bufferedReader.readLine();

        bufferedReader.close();
        fileInputStream.close();

        retrievedProject = new Project(name, photoPath);

        return retrievedProject;
    }

    public static Map<String, File> getSavedProjectList(Activity activity) {
        File storageDirectory = activity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File tempFileList[] = storageDirectory.listFiles();
        projectListMap = new HashMap<String, File>();
        for (int i = 0; i < tempFileList.length; i++) {
            File file = tempFileList[i];
            String fileName = file.getName();
            if (fileName.contains(".where")) {
                String displayName = fileName.substring(0, fileName.lastIndexOf('.'));
                projectListMap.put(displayName, file);
            }
        }
        return projectListMap;
    }
}
