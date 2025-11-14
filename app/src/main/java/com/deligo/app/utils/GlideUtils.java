package com.deligo.app.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

/**
 * Utility class for image loading with Glide
 */
public class GlideUtils {
    
    /**
     * Loads an image from URL into ImageView with default options
     * @param context Context
     * @param imageUrl Image URL
     * @param imageView Target ImageView
     */
    public static void loadImage(Context context, String imageUrl, ImageView imageView) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
            return;
        }
        
        Glide.with(context)
                .load(imageUrl)
                .apply(getDefaultOptions())
                .into(imageView);
    }
    
    /**
     * Loads an image from URL into ImageView with placeholder
     * @param context Context
     * @param imageUrl Image URL
     * @param imageView Target ImageView
     * @param placeholderResId Placeholder resource ID
     */
    public static void loadImage(Context context, String imageUrl, ImageView imageView, int placeholderResId) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageView.setImageResource(placeholderResId);
            return;
        }
        
        Glide.with(context)
                .load(imageUrl)
                .apply(getDefaultOptions()
                        .placeholder(placeholderResId)
                        .error(placeholderResId))
                .into(imageView);
    }
    
    /**
     * Loads a circular image from URL into ImageView
     * @param context Context
     * @param imageUrl Image URL
     * @param imageView Target ImageView
     */
    public static void loadCircularImage(Context context, String imageUrl, ImageView imageView) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
            return;
        }
        
        Glide.with(context)
                .load(imageUrl)
                .apply(getDefaultOptions().circleCrop())
                .into(imageView);
    }
    
    /**
     * Loads a circular image from URL into ImageView with placeholder
     * @param context Context
     * @param imageUrl Image URL
     * @param imageView Target ImageView
     * @param placeholderResId Placeholder resource ID
     */
    public static void loadCircularImage(Context context, String imageUrl, ImageView imageView, int placeholderResId) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageView.setImageResource(placeholderResId);
            return;
        }
        
        Glide.with(context)
                .load(imageUrl)
                .apply(getDefaultOptions()
                        .circleCrop()
                        .placeholder(placeholderResId)
                        .error(placeholderResId))
                .into(imageView);
    }
    
    /**
     * Loads an image with custom size
     * @param context Context
     * @param imageUrl Image URL
     * @param imageView Target ImageView
     * @param width Target width
     * @param height Target height
     */
    public static void loadImageWithSize(Context context, String imageUrl, ImageView imageView, int width, int height) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
            return;
        }
        
        Glide.with(context)
                .load(imageUrl)
                .apply(getDefaultOptions().override(width, height))
                .into(imageView);
    }
    
    /**
     * Clears image cache for a specific URL
     * @param context Context
     * @param imageUrl Image URL to clear from cache
     */
    public static void clearImageCache(Context context, String imageUrl) {
        Glide.with(context)
                .clear(Glide.with(context).load(imageUrl).into(new ImageView(context)));
    }
    
    /**
     * Clears all Glide memory cache
     * @param context Context
     */
    public static void clearMemoryCache(Context context) {
        Glide.get(context).clearMemory();
    }
    
    /**
     * Gets default RequestOptions for image loading
     * @return RequestOptions with default settings
     */
    private static RequestOptions getDefaultOptions() {
        return new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop();
    }
    
    /**
     * Preloads an image into cache
     * @param context Context
     * @param imageUrl Image URL to preload
     */
    public static void preloadImage(Context context, String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }
        
        Glide.with(context)
                .load(imageUrl)
                .apply(getDefaultOptions())
                .preload();
    }
    
    // Private constructor to prevent instantiation
    private GlideUtils() {
        throw new AssertionError("Cannot instantiate GlideUtils class");
    }
}
