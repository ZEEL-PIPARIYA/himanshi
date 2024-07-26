package com.example.musicplayer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    String[] items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        listView = findViewById(R.id.listView);

        permission(this);

    }

    public void permission(Context context) {

        Dexter.withContext(context)
                .withPermissions(
                        Manifest.permission.READ_MEDIA_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        for (PermissionGrantedResponse response : report.getGrantedPermissionResponses()) {
                            Log.d("Permissions", "Granted: " + response.getPermissionName());
                        }
                        for (PermissionDeniedResponse response : report.getDeniedPermissionResponses()) {
                            Log.d("Permissions", "Denied: " + response.getPermissionName());
                        }

                        if (report.areAllPermissionsGranted()) {
                            Log.d("Permissions", "All permissions are granted");
                            displaySong();
                        } else {
                            // Handle the case when permissions are denied
                            Log.d("Permissions", "Not all permissions are granted");
                            Toast.makeText(context, "All permissions need to be granted to display songs.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();

    }

    public ArrayList<File> findsong(File root) {
        ArrayList<File> allSongs = new ArrayList<>();
        File[] files = root.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    allSongs.addAll(findsong(file));
                } else if (file.getName().endsWith(".mp3") || file.getName().endsWith(".wav")) {
                    allSongs.add(file);
                }
            }
        }
        return allSongs;
    }


    public void displaySong() {
        final ArrayList<File> mySong = findsong(Environment.getExternalStorageDirectory());

        if (mySong == null || mySong.isEmpty()) {
            Log.e("displaySong", "No songs found!");
            return;
        }

        Toast.makeText(this, "Songs list size: " + mySong.size(), Toast.LENGTH_SHORT).show();

        items = new String[mySong.size()];
        for (int i = 0; i < mySong.size(); i++) {
            items[i] = mySong.get(i).getName().replace(".mp3", "").replace(".wav", "");
        }

        customAdapter customAdapter = new customAdapter();
        listView.setAdapter(customAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String songName = (String) listView.getItemAtPosition(position);

                startActivity(new Intent(getApplicationContext(), PlayerActivity.class)
                        .putExtra("songs", mySong)
                        .putExtra("songname", songName)
                        .putExtra("pos", position)
                );
            }
        });
    }

    class customAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int position) {
            return items[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            }
            TextView textView = convertView.findViewById(R.id.txtSong);
            textView.setText(items[position]);
            return convertView;
        }
    }

}