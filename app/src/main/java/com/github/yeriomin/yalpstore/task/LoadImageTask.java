package com.github.yeriomin.yalpstore.task;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.github.yeriomin.yalpstore.BitmapManager;
import com.github.yeriomin.yalpstore.NetworkState;
import com.github.yeriomin.yalpstore.PreferenceActivity;
import com.github.yeriomin.yalpstore.R;
import com.github.yeriomin.yalpstore.model.ImageSource;

public class LoadImageTask extends AsyncTask<ImageSource, Void, Void> {

    protected ImageView imageView;
    private Drawable drawable;
    private String tag;
    private boolean placeholder = true;
    private int fadeInMillis = 0;

    public LoadImageTask() {

    }

    public LoadImageTask(ImageView imageView) {
        setImageView(imageView);
    }

    public LoadImageTask setImageView(ImageView imageView) {
        this.imageView = imageView;
        tag = (String) imageView.getTag();
        return this;
    }

    public LoadImageTask setPlaceholder(boolean placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    public LoadImageTask setFadeInMillis(int fadeInMillis) {
        this.fadeInMillis = fadeInMillis;
        return this;
    }

    @Override
    protected void onPreExecute() {
        if (placeholder) {
            imageView.setImageDrawable(imageView.getContext().getResources().getDrawable(R.drawable.ic_placeholder));
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (null != imageView.getTag() && !imageView.getTag().equals(tag)) {
            return;
        }
        if (null != drawable) {
            if (fadeInMillis > 0) {
                fadeOut();
            }
            imageView.setImageDrawable(drawable);
            if (fadeInMillis > 0) {
                fadeIn();
            }
        }
    }

    @Override
    protected Void doInBackground(ImageSource... params) {
        ImageSource imageSource = params[0];
        if (null != imageSource.getApplicationInfo()) {
            drawable = imageView.getContext().getPackageManager().getApplicationIcon(imageSource.getApplicationInfo());
        } else if (!TextUtils.isEmpty(imageSource.getUrl())) {
            Bitmap bitmap = new BitmapManager(imageView.getContext()).getBitmap(imageSource.getUrl(), imageSource.isFullSize());
            if (null != bitmap || !noImages()) {
                drawable = new BitmapDrawable(bitmap);
            }
        }
        return null;
    }

    private void fadeIn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            imageView.animate().setDuration(fadeInMillis).withLayer().alpha(1.0f);
        } else {
            Animation a = new AlphaAnimation(0.0f, 1.0f);
            a.setDuration(fadeInMillis);
            imageView.startAnimation(a);
        }
    }

    private void fadeOut() {
        if (!placeholder) {
            imageView.setAlpha(0.0f);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            imageView.animate().alpha(0.0f).setDuration(fadeInMillis).withLayer();
        } else {
            Animation a = new AlphaAnimation(1.0f, 0.0f);
            a.setDuration(fadeInMillis);
            imageView.startAnimation(a);
        }
    }

    private boolean noImages() {
        return NetworkState.isMetered(imageView.getContext()) && PreferenceActivity.getBoolean(imageView.getContext(), PreferenceActivity.PREFERENCE_NO_IMAGES);
    }
}
