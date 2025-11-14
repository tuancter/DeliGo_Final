package com.deligo.app.utils;

/**
 * Callback interface for repository actions that don't return data.
 * Used for operations like create, update, delete.
 */
public interface ActionCallback {
    /**
     * Called when the action completes successfully
     */
    void onSuccess();
    
    /**
     * Called when the action fails
     * @param message Error message describing the failure
     */
    void onError(String message);
}
