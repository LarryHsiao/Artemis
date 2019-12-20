package com.silverhetch.artemis;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.silverhetch.artemis.media.Media;
import com.silverhetch.aura.view.ViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for media listing
 */
public class MediaAdapter extends RecyclerView.Adapter<ViewHolder> {
    /**
     * Listener for media clicked
     */
    public interface OnClickListener {
        /**
         * @param media Clicked Media.
         */
        void onClicked(Media media);
    }

    private final List<Media> data = new ArrayList<>();
    private final OnClickListener listener;

    public MediaAdapter(OnClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        android.R.layout.simple_list_item_1,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        ((TextView) holder.itemView).setText(data.get(position).title());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClicked(data.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void load(List<Media> newData) {
        data.clear();
        data.addAll(newData);
        notifyDataSetChanged();
    }
}
