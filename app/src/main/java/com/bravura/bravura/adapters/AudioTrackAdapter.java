package com.bravura.bravura.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bravura.bravura.R;
import com.bravura.bravura.entities.AudioTrack;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AudioTrackAdapter extends RecyclerView.Adapter<AudioTrackAdapter.ViewHolder> {

    private Context context;
    private List<AudioTrack> trackList;
    private View.OnClickListener mOnItemClickListener;

    public AudioTrackAdapter (Context context, List<AudioTrack> trackList) {
        this.context = context;
        this.trackList = trackList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(this.context);
        View view = inflater.inflate(R.layout.item_track, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String title = this.trackList.get(position).title;

        Pattern pattern = Pattern.compile("(?<artist>.+) - (?<song>.+)");
        Matcher matcher = pattern.matcher(title);
        String artist = "";
        String song = "";
        if(matcher.matches()) {
            artist = matcher.group(1);
            song = matcher.group(2);
        }

        holder.iArtist.setText(artist);
        holder.iSong.setText(song);
    }

    @Override
    public int getItemCount() {
        return this.trackList.size();
    }

    //TODO: Step 2 of 4: Assign itemClickListener to your local View.OnClickListener variable
    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        mOnItemClickListener = itemClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView iArtist;
        private TextView iSong;


        ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.iArtist = itemView.findViewById(R.id.artist);
            this.iSong = itemView.findViewById(R.id.song);

            //TODO: Step 3 of 4: setTag() as current view holder along with
            // setOnClickListener() as your local View.OnClickListener variable.
            // You can set the same mOnItemClickListener on multiple views if required
            // and later differentiate those clicks using view's id.
            itemView.setTag(this);
            itemView.setOnClickListener(mOnItemClickListener);
        }
    }
}
