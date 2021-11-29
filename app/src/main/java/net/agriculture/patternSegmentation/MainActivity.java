package net.agriculture.patternSegmentation;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;


import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class MainActivity extends AppCompatActivity {

    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;

    ImageView selectedImage;
    Button cameraBtn;
    String currentPhotoPath;

    // routing
    public static final String HOME_ENDPOINT_URI = "https://mscg-flask-rest-api.herokuapp.com/";
    public static final String BASE_EXT = "/api/v1/mscg";
    public static final String Rx50_EXT = "/rx50";
    public static final String Rx101_EXT = "/rx101";
    public static final String UPLOAD_EXT = "/input?=%";

    // upload endpoints
    public static final String UPLOAD_Rx50_FULL_URI = HOME_ENDPOINT_URI + BASE_EXT + Rx50_EXT + UPLOAD_EXT;
    public static final String UPLOAD_Rx101_FULL_URI = HOME_ENDPOINT_URI + BASE_EXT + Rx101_EXT + UPLOAD_EXT;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectedImage = findViewById(R.id.displayImageView);
        cameraBtn = findViewById(R.id.cameraBtn);
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.CAMERA},
                1);

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call camera and capture image when button is clicked
                dispatchTakePictureIntent();

            }
        });

    }//end onCreate

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERM_CODE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //Toast.makeText(getApplicationContext(), "Access Granted!", Toast.LENGTH_SHORT).show();

                } else {
                    //Toast.makeText(getApplicationContext(), "This Permission is needed for the app to work perfectly!", Toast.LENGTH_SHORT).show();
                }

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == CAMERA_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                File f = new File(currentPhotoPath);
                Log.d("tag", "PhotoPath " + currentPhotoPath);
                Uri contentUri = Uri.fromFile(f);

                // I am using Drawable object below, you might be able to use it or change to a file
                // type if that's easier. The way to use a file type to change the imageView displayed
                // is shown below in the commented lines of code

//              File segmentedImg = uploadImageToEndpoint(f.getName(),contentUri);
//              selectedImage.setImageURI(Uri.fromFile(f));

                Drawable segmentedImg = processImage(f,contentUri);
                selectedImage.setImageDrawable(segmentedImg);

            }
        }
    }




    // might need to change to File type instead of Drawable
    private Drawable processImage(File file, Uri contentUri) {

        //putImageToEndpoint(file) <---- PUT here

        // These two lines of code below will get removed, for now Im setting
        // a hardcoded image to the imageView. It should be replaced with the image
        // we get from the rest API
        Resources res = this.getResources();
        Drawable segmentedImg = ResourcesCompat.getDrawable(res, R.drawable.yesitworks, null);


        //getImageFromEndpoint(file); <---- GET here

        //HERE YOU NEED TO GET IMAGE FROM ENDPOINT AND RETURN IT
        return segmentedImg;

    }

    // will need to change to File type
    //will need to add implementation to gradle --> implementation("com.squareup.okhttp3:okhttp:4.9.3")
    private void putImageToEndpoint(File file, Uri contentUri) {

//        OkHttpClient client = new OkHttpClient();
//
//        Request request = new Request.Builder()
//                .url(UPLOAD_Rx50_FULL_URI)
//                .put(file) // <-- PUT IMAGE HERE
//                .addHeader("Authorization", header)
//                .build();
////        request.
//        makeCall(client, request);

    }

    // might need to change to File type
    private void getImageFromEndpoint(URL url) {
        //Probably won't need this, but same logic as above
        //will need to add implementation to gradle --> implementation("com.squareup.okhttp3:okhttp:4.9.3")
//        OkHttpClient client = new OkHttpClient();
//        Request request = new Request.Builder()
//                .url(URL HERE)
//                .build();
//
//        try (Response response = client.newCall(request).execute()) {
//            return response.body().string();

    }

    /**
     * POST Method
     * TODO: Alternative method for uploading -- note should handle for 2 cases
     * 1. Rx50
     * 2 Rx101
     * @param uploadImageEndpoint
     */
    public void sendPost(String uploadImageEndpoint, File file) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(uploadImageEndpoint);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept","application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    JSONObject jsonParam = new JSONObject();
                    // add image here

                    Log.i("JSON", jsonParam.toString());
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                    os.writeBytes(jsonParam.toString());

                    os.flush();
                    os.close();

                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                    Log.i("MSG" , conn.getResponseMessage());

                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }
    // Helper function for dispatchTakePictureIntent() to store images into file format
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // Create new Intent and captures image then pass to helper func to get File
    // format then sends it back as data
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // throw exception
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "net.agriculture.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }


}
