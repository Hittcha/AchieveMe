package com.Bureau.Achivki;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageAdapter extends BaseAdapter {
    private final Context context;
    private final List<String> imageUrls;

    public ImageAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    @Override
    public int getCount() {
        return imageUrls.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);

                /*LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        MATCH_PARENT, 500
                );*/

            imageView.setLayoutParams(new GridView.LayoutParams(MATCH_PARENT, 500));
            //imageView.setLayoutParams(new GridView.LayoutParams(500, 500));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(1, 1, 1, 1);
        } else {
            imageView = (ImageView) convertView;
        }

        // Загрузка изображения по URL-адресу с помощью библиотеки Picasso
        Picasso.get()
                .load(imageUrls.get(position))
                .into(imageView);

        return imageView;
    }
}
