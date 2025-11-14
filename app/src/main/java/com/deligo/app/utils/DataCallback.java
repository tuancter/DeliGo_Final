package com.deligo.app.utils;

/**
 * Generic callback interface for repository data operations that return data.
 * @param <T> The type of data returned on success
 */
public interface DataCallback<T> {
    /**
     * Called when the operation completes successfully
     * @param data The data returned from the operation
     */
    void onSuccess(T data);
    
    /**
     * Called when the operation fails
     * @param message Error message describing the failure
     */
    void onError(String message);
}
