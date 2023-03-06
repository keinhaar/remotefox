package de.exware.remotefox;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An Actor for Properties from an ObjectActor.
 *
 */
public class PropertyIteratorActor extends AbstractActor
{
    private Map<String, Object> properties;
    
    public PropertyIteratorActor(DebugConnector con, ObjectActor parent, JSONObject conf) throws JSONException
    {
        super(con, parent, conf);
    }
    
    public Map<String, Object> getProperties() throws JSONException, IOException
    {
        Map<String, Object> properties = new HashMap<>();
        JSONObject request = new JSONObject();
        request.put("to", actor);
        request.put("type", "slice");
        request.put("start", "0");
        request.put("count", "1000");
        connector.send(request, message ->
        {
            JSONObject vars = message.optJSONObject("ownProperties");
            varsToMap(connector, this, vars , properties);
        });
        return properties;
    }
    
    static void varsToMap(DebugConnector connector, AbstractActor parentActor, JSONObject vars, Map<String, Object> properties)
    {
        for(String key : vars.keySet())
        {
            if("arguments".equals(key))
            {
                continue;
            }
            JSONObject jsObject = vars.getJSONObject(key);
            Object value = jsObject.opt("value");
            if(value instanceof JSONObject)
            {
                String type = ((JSONObject) value).optString("type");
                if("object".equals(type))
                {
                    value = new ObjectActor(connector, parentActor, (JSONObject) value);
                }
                else
                {
                    value = "";
                }
            }
            properties.put(key, value);
        }
    }
}
