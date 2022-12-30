package de.exware.remotefox;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EnvironmentActor extends AbstractActor
{
    EnvironmentActor(DebugConnector con, AbstractActor parent, JSONObject conf) throws JSONException, IOException
    {
        super(con, parent, conf);
    }
    
    public void properties() throws JSONException, IOException
    {
        JSONObject request2 = new JSONObject();
        request2.put("to", conf.query("/bindings/variables/x/value/actor"));
        request2.put("type", "enumProperties");
        Map options = new HashMap();
//        options.put("ignoreIndexedProperties", true);
        request2.put("options", options );
        connector.send(request2);
    }
    
    @Override
    public void handleMessage(JSONObject message) throws JSONException, IOException
    {
        super.handleMessage(message);
    }

    public Map<String, Object> getVariables() 
    {
        Map<String, Object> variables = new HashMap<>();
        JSONObject vars = (JSONObject) conf.optQuery("/bindings/variables");
        for(String key : vars.keySet())
        {
            if("arguments".equals(key))
            {
                continue;
            }
            JSONObject jsObject = vars.getJSONObject(key);
            Object value = jsObject.opt("value");
            value = value instanceof JSONObject ? "Object" : value;
            variables.put(key, value);
        }
        return variables;
    }

    public Map<String, Object> getArguments() 
    {
        Map<String, Object> variables = new HashMap<>();
        JSONArray arr = (JSONArray) conf.optQuery("/bindings/arguments");
        JSONObject vars = arr.getJSONObject(0);
        for(String key : vars.keySet())
        {
            JSONObject jsObject = vars.getJSONObject(key);
            Object value = jsObject.opt("value");
            value = value instanceof JSONObject ? "Object" : value;
            variables.put(key, value);
        }
        return variables;
    }
}
