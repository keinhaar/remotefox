package de.exware.remotefox;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

public class TargetConfigurationActor extends AbstractActor
{
    TargetConfigurationActor(DebugConnector con, AbstractActor parent, JSONObject config) throws JSONException, IOException
    {
        super(con, parent, config);
    }

    @Override
    public void handleMessage(JSONObject message) throws JSONException, IOException
    {
        super.handleMessage(message);
    }

    public void updateConfiguration(String ... params) throws JSONException, IOException
    {
        JSONObject request = new JSONObject();
        request.put("to", actor);
        request.put("type", "updateConfiguration");
        JSONObject configuration = new JSONObject();
        for(int i=0;i<params.length;i+=2)
        {
            configuration.put(params[i], params[i+1]);
        }
        request.put("configuration", configuration);
        connector.send(request, null);
    }

    public void updateConfiguration(String param, boolean value) throws JSONException, IOException
    {
        JSONObject request = new JSONObject();
        request.put("to", actor);
        request.put("type", "updateConfiguration");
        JSONObject configuration = new JSONObject();
        configuration.put(param, value);
        request.put("configuration", configuration);
        connector.send(request, null);
    }

    public void updateConfigurationToDebug() throws JSONException, IOException
    {
        JSONObject request = new JSONObject();
        request.put("to", actor);
        request.put("type", "updateConfiguration");
        JSONObject configuration = new JSONObject();
        configuration.put("cacheDisabled", false);
        request.put("configuration", configuration);
        connector.send(request, null);
    }
}
