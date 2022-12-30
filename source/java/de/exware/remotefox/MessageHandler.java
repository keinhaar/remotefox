package de.exware.remotefox;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Callback for Actors. Will be called after the server responded with a message.
 */
public interface MessageHandler
{
    void handleMessage(JSONObject message) throws JSONException, IOException;
}
