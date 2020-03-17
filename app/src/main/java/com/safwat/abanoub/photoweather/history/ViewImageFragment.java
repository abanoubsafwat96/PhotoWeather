package com.safwat.abanoub.photoweather.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.jsibbold.zoomage.ZoomageView;
import com.safwat.abanoub.photoweather.R;

import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Abanoub on 2018-04-27.
 */

public class ViewImageFragment extends Fragment {

    Unbinder unbinder;
    @BindView(R.id.image) ZoomageView imageView;
    String imagePath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_image_fragment, container, false);

        unbinder=ButterKnife.bind(this, view);

        imagePath = getArguments().getString("image_path");

        if (imagePath != null)
            Glide.with(getContext())
                    .load(imagePath)
                    .into(imageView);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        unbinder.unbind(); // unbind the view to free some memory
    }
}
