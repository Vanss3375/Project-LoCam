package com.example.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.util.List;

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> mImagePaths;
    private List<String> mImageLocations;
    private List<String> mImageTimestamps;

    public ImageAdapter(Context c, List<String> imagePaths, List<String> imageLocations, List<String> imageTimestamps) {
        mContext = c;
        mImagePaths = imagePaths;
        mImageLocations = imageLocations;
        mImageTimestamps = imageTimestamps;
    }

    @Override
    public int getCount() {
        return mImagePaths.size();
    }

    @Override
    public Object getItem(int position) {
        return mImagePaths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View itemView;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            itemView = inflater.inflate(R.layout.grid_item, parent, false);
        } else {
            itemView = convertView;
        }

        ImageView imageView = itemView.findViewById(R.id.imageView);
        Button deleteButton = itemView.findViewById(R.id.deleteButton);

        String imagePath = mImagePaths.get(position);
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        imageView.setImageBitmap(bitmap);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String imagePath = mImagePaths.get(position);
                String imageLocation = mImageLocations.get(position);
                String imageTimestamp = mImageTimestamps.get(position);

                if (imagePath != null && !imagePath.isEmpty() && imageLocation != null && !imageLocation.isEmpty() && imageTimestamp != null && !imageTimestamp.isEmpty()) {
                    Intent intent = new Intent(mContext, ImageActivity.class);
                    intent.putExtra("imagePath", imagePath);
                    intent.putExtra("imageLocation", imageLocation);
                    intent.putExtra("imageTimestamp", imageTimestamp);
                    mContext.startActivity(intent);
                } else {
                    Toast.makeText(mContext, "Failed to load image details", Toast.LENGTH_SHORT).show();
                }
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(mContext)
                        .setTitle("Delete Image")
                        .setMessage("Are you sure you want to delete this image?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Remove item from list and storage
                                ((MainActivity) mContext).removeImagePath(position);
                                ((MainActivity) mContext).removeImageLocation(position);
                                ((MainActivity) mContext).removeImageTimestamp(position);
                                notifyDataSetChanged();

                                Toast.makeText(mContext, "Image deleted", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });

        return itemView;
    }
}
