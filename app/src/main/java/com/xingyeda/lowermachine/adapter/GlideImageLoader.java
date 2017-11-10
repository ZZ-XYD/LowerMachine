package com.xingyeda.lowermachine.adapter;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.youth.banner.loader.ImageLoader;

/**
 * Created by LDL on 2017/11/8.
 */

public class GlideImageLoader extends ImageLoader {
    @Override
    public void displayImage(Context context, Object path, ImageView imageView) {
        //Glide 加载图片简单用法
//        Glide.with(context).load(path).into(imageView);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        Glide.with(context.getApplicationContext())
                .load(path)
                .into(imageView);

//        //Picasso 加载图片简单用法
//        Picasso.with(context).load(path).into(imageView);

        //用fresco加载图片简单用法
//        Uri uri = Uri.parse((String) path);
//        imageView.setImageURI(uri);
    }
    //提供createImageView 方法，如果不用可以不重写这个方法，方便fresco自定义ImageView
//    @Override
//    public ImageView createImageView(Context context) {
//        SimpleDraweeView simpleDraweeView=new SimpleDraweeView(context);
//        return simpleDraweeView;
//    }
}


