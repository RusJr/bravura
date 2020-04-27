package com.bravura.bravura.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View.OnKeyListener;
import android.view.View;
import android.view.KeyEvent;

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

import java.io.IOException;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.searchResult = findViewById(R.id.searchResult);
        this.searchInput = findViewById(R.id.searchInput);
        this.slider = SliderServiceGenerator.createService();
        this.trackList = new ArrayList<AudioTrack>();
        this.adapter = new AudioTrackAdapter(this, this.trackList);
        searchResult.setAdapter(adapter);
        searchResult.setLayoutManager(new LinearLayoutManager(this));

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

}
