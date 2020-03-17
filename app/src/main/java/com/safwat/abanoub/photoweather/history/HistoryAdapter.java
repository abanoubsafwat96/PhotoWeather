package com.safwat.abanoub.photoweather.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.safwat.abanoub.photoweather.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    Context context;
    ArrayList<String> history_list;
    Communicator communicator;

    public HistoryAdapter(Context context, ArrayList<String> history_list) {
        this.context = context;
        this.history_list = history_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.history_single_item, null);

        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String history_item = history_list.get(position);

        Glide.with(context)
                .load(history_item)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return history_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.imageView)
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            String history_item = history_list.get(position);

            if (history_item == null) {
                return;
            }
            communicator.setItemClicked(history_item);
        }
    }

    public void setCommunicator(Communicator communicator) {
        this.communicator = communicator;
    }
}