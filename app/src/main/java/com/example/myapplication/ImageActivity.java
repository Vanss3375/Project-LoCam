package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_activity);

        ImageView imageView = findViewById(R.id.imageView);
        TextView addressView = findViewById(R.id.address);
        TextView timeStampView = findViewById(R.id.timeStamp);

        String imagePath = getIntent().getStringExtra("imagePath");
        String imageLocation = getIntent().getStringExtra("imageLocation");
        String imageTimestamp = getIntent().getStringExtra("imageTimestamp");

        if (imagePath != null && !imagePath.isEmpty()) {
            Bitmap bitmap = decodeSampledBitmapFromFile(imagePath, 800, 800); // Adjust size as needed
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                Toast.makeText(this, "Unable to load image", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Invalid image path", Toast.LENGTH_SHORT).show();
        }

        if (imageLocation != null && !imageLocation.isEmpty()) {
            addressView.setText(imageLocation);
        } else {
            addressView.setText("Location not available");
        }

        if (imageTimestamp != null && !imageTimestamp.isEmpty()) {
            timeStampView.setText(imageTimestamp);
        } else {
            timeStampView.setText("Timestamp not available");
        }
    }

    private Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
