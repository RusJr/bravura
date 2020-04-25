package com.bravura.bravura.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.bravura.bravura.R;
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
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {

    private TextView result;
    private EditText searchInput;
    private SliderService slider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.result = findViewById(R.id.result);
        this.searchInput = findViewById(R.id.searchInput);
        this.slider = SliderServiceGenerator.createService();
    }

    public void onSearch(View v) throws Exception {
        result.setText("");
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
                            List<AudioTrack> tracks = gson.fromJson(audios_json, listType);

                            for (AudioTrack track: tracks){
                                result.append(track.title + "\n");
                            };

                            Log.d("JSON PARSE Error", Integer.toString(response.code()));
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
