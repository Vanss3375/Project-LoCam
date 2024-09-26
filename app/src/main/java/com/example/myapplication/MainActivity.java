package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int CAMERA_PERM_CODE = 102;
    private static final int CAMERA_AND_LOCATION_PERM_CODE = 103;
    private String currentPhotoPath;
    private List<String> imagePaths = new ArrayList<>();
    private List<String> imageLocations = new ArrayList<>();
    private List<String> imageTimestamps = new ArrayList<>();
    private GridView gridView;
    private ImageAdapter imageAdapter;
    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String IMAGE_PATHS_KEY = "imagePaths";
    private static final String IMAGE_LOCATION_KEY = "imageLocations";
    private static final String IMAGE_TIMESTAMPS_KEY = "imageTimestamps";
    private RecyclerView rvMembers;
    private MemberAdapter memberAdapter;

    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonCapture = findViewById(R.id.button_capture);
        gridView = findViewById(R.id.gridView);
        rvMembers = findViewById(R.id.rvMembers);
        rvMembers.setLayoutManager(new LinearLayoutManager(this));

        // List of members
        List<Member> memberList = new ArrayList<>();
        memberList.add(new Member("Richard Yohanes", "2602139103", "richard.yohanes@binus.ac.id", R.drawable.richard));
        memberList.add(new Member("Evander Octavianus Layardi", "2602075983", "evander.layardi@binus.ac.id", R.drawable.evander));
        memberList.add(new Member("Benedict Zevanno Christabel", "2602056851", "benedict.christabel@binus.ac.id", R.drawable.benedict));
        memberList.add(new Member("Hosanna Megan Putra Wibawa", "2602198900", "hosanna.wibawa@binus.ac.id", R.drawable.hosanna));
        memberList.add(new Member("Gian Guido Hibatulloh", "2502016090", "gian.hibatulloh@binus.ac.id", R.drawable.gian));
        memberList.add(new Member("Nathanael Hansel Yaputra", "2602064153", "nathanael.yaputra@binus.ac.id", R.drawable.hansel));

        MemberAdapter memberAdapter = new MemberAdapter(memberList, this);
        rvMembers.setAdapter(memberAdapter);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        loadImagePaths();
        loadImageLocations();
        loadImageTimestamps();
        cleanUpInvalidFiles(); // Clean up invalid files on startup

        imageAdapter = new ImageAdapter(this, imagePaths, imageLocations, imageTimestamps);
        gridView.setAdapter(imageAdapter);

        buttonCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askCameraAndLocationPermission();
            }
        });
    }

    private void askCameraAndLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION},
                    CAMERA_AND_LOCATION_PERM_CODE);
        } else {
            CaptureImageIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_AND_LOCATION_PERM_CODE) {
            boolean cameraGranted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean locationGranted = grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED;

            if (cameraGranted && locationGranted) {
                CaptureImageIntent();
            } else {
                Toast.makeText(this, "Need Required Permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void CaptureImageIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                getLocation();
            }
        }
    }

    private void getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onSuccess(Location location) {
                            if(location != null){
                                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                                List<Address> addresses = null;
                                try {
                                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    saveLocation("Latitude : " + addresses.get(0).getLatitude() + "\n" +
                                             "Longitude : " + addresses.get(0).getLongitude() + "\n" +
                                                "Address : " + addresses.get(0).getAddressLine(0));

                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    });
        }
    }

    private void saveLocation(String location) {
        imageLocations.add(location);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            File imgFile = new File(currentPhotoPath);
            if (imgFile.exists()) {
                imagePaths.add(currentPhotoPath);
                String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                imageTimestamps.add(timeStamp);
                imageAdapter.notifyDataSetChanged();
                saveImagePaths();
                saveImageLocations();
                saveImageTimestamps();
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void saveImagePaths() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> set = new HashSet<>(imagePaths);
        editor.putStringSet(IMAGE_PATHS_KEY, set);
        editor.apply();
    }

    private void saveImageLocations() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> set = new HashSet<>(imageLocations);
        editor.putStringSet(IMAGE_LOCATION_KEY, set);
        editor.apply();
    }

    private void saveImageTimestamps() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> set = new HashSet<>(imageTimestamps);
        editor.putStringSet(IMAGE_TIMESTAMPS_KEY, set);
        editor.apply();
    }

    private void loadImagePaths() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        Set<String> set = sharedPreferences.getStringSet(IMAGE_PATHS_KEY, new HashSet<>());
        imagePaths.clear();
        imagePaths.addAll(set);
    }

    private void loadImageLocations() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        Set<String> set = sharedPreferences.getStringSet(IMAGE_LOCATION_KEY, new HashSet<>());
        imageLocations.clear();
        imageLocations.addAll(set);
    }

    private void loadImageTimestamps() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        Set<String> set = sharedPreferences.getStringSet(IMAGE_TIMESTAMPS_KEY, new HashSet<>());
        imageTimestamps.clear();
        imageTimestamps.addAll(set);
    }

    private void cleanUpInvalidFiles() {
        List<String> validImagePaths = new ArrayList<>();
        List<String> validImageLocations = new ArrayList<>();
        List<String> validImageTimestamps = new ArrayList<>();

        for (int i = 0; i < imagePaths.size(); i++) {
            File file = new File(imagePaths.get(i));
            if (file.exists()) {
                validImagePaths.add(imagePaths.get(i));
                validImageLocations.add(imageLocations.get(i));
                validImageTimestamps.add(imageTimestamps.get(i));
            }
        }

        imagePaths = validImagePaths;
        imageLocations = validImageLocations;
        imageTimestamps = validImageTimestamps;

        saveImagePaths();
        saveImageLocations();
        saveImageTimestamps();
    }

    public void removeImagePath(int position) {
        imagePaths.remove(position);
        saveImagePaths();
    }

    public void removeImageLocation(int position) {
        imageLocations.remove(position);
        saveImageLocations();
    }

    public void removeImageTimestamp(int position) {
        imageTimestamps.remove(position);
        saveImageTimestamps();
    }

}