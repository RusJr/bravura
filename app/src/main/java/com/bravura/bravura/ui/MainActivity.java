package com.bravura.bravura.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View.OnKeyListener;
import android.view.View;
import android.view.KeyEvent;
import android.widget.Toast;

import com.bravura.bravura.R;
import com.bravura.bravura.adapters.AudioTrackAdapter;
import com.bravura.bravura.entities.AudioTrack;
import com.bravura.bravura.slider.SliderService;
import com.bravura.bravura.slider.SliderServiceGenerator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {

    private RecyclerView searchResult;
    private EditText searchInput;
    private SliderService slider;
    private List<AudioTrack> trackList;
    private AudioTrackAdapter adapter;
    private MediaPlayer player;
    private View.OnClickListener onItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //TODO: Step 4 of 4: Finally call getTag() on the view.
            // This viewHolder will have all required values.
            RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) view.getTag();
            int position = viewHolder.getAdapterPosition();
            // viewHolder.getItemId();
            // viewHolder.getItemViewType();
            // viewHolder.itemView;
            AudioTrack track = trackList.get(position);
            Toast.makeText(MainActivity.this, "You Clicked: " + track.title, Toast.LENGTH_SHORT).show();

            Call<ResponseBody> call = slider.download(track.id,
                                                      track.duration,
                                                      track.url,
                                                      track.title,
                                                      track.extra);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {  // TODO call - Not annotated parameter
                    if (response.isSuccessful()) {
                        boolean writtenToDisk = writeResponseBodyToDisk(response.body(), track.title + ".mp3");
                        if (writtenToDisk) {
                            Toast.makeText(MainActivity.this, "fileSaved", Toast.LENGTH_SHORT).show();
                            try {
                                String fpath = getExternalFilesDir(null) + File.separator + track.title + ".mp3";
                                player.setDataSource(fpath);
                                player.prepare();
                                player.start();
                            } catch (IOException e) {
                                Toast.makeText(MainActivity.this, "playerError", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(MainActivity.this, "saveError", Toast.LENGTH_SHORT).show();
                        }
                        // tasks available
                    } else {
                        Log.d("Error", Integer.toString(response.code()));
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    // something went completely south (like no internet connection)
                    Log.d("Error", t.getMessage());
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.player = new MediaPlayer();
        this.searchResult = findViewById(R.id.searchResult);
        this.searchInput = findViewById(R.id.searchInput);
        this.slider = SliderServiceGenerator.createService();
        this.trackList = new ArrayList<AudioTrack>();
        this.adapter = new AudioTrackAdapter(this, this.trackList);
        //TODO: Step 1 of 4: Create and set OnItemClickListener to the adapter.
        this.adapter.setOnItemClickListener(onItemClickListener);
        this.searchResult.setAdapter(this.adapter);
        this.searchResult.setLayoutManager(new LinearLayoutManager(this));

        this.searchInput.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                //If the keyevent is a key-down event on the "enter" button
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    onSearch(view);
                    return true;
                }
                return false;
            }
        });

    }

    public void onSearch(View v) {
        String query = this.searchInput.getText().toString();
        if (query.isEmpty()) return;

        Call<ResponseBody> call = this.slider.search(query);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {  // TODO call - Not annotated parameter
                if (response.isSuccessful()) {
                    String content = null;
                    try {
                        assert response.body() != null;
                        content = response.body().string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (content!= null) {
                        try {
                            JSONObject data = new JSONObject(content);
                            JSONObject audios = data.optJSONObject("audios");
                            String theRandomKey = audios.keys().next();
                            JSONArray actually_audios = audios.getJSONArray(theRandomKey);
                            String audios_json = actually_audios.toString();

                            Gson gson = new Gson();
                            Type listType = new TypeToken<List<AudioTrack>>() {}.getType();
                            List<AudioTrack> resultTracks = gson.fromJson(audios_json, listType);
                            trackList.clear();
                            trackList.addAll(resultTracks);
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("JSON PARSE Error", "");
                        }
                    }
                    // tasks available
                } else {
                    Log.d("Error", Integer.toString(response.code()));
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // something went completely south (like no internet connection)
                Log.d("Error", t.getMessage());
            }
        });
    }

    private boolean writeResponseBodyToDisk(ResponseBody body, String fileName) {
        try {
            // todo change the file location/name according to your needs
            File futureStudioIconFile = new File(getExternalFilesDir(null) + File.separator + fileName);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    Log.d("TAG", "file download: " + fileSizeDownloaded + " of " + fileSize);
                }

                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

}
