package de.exware.remotefox;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An Actor for Objects that may be on Stack or an Argument to a method call.
 *
 */
public class ObjectActor extends AbstractActor
{
    private Map<String, Object> properties;
    private PropertyIteratorActor propActor;
    
    public ObjectActor(DebugConnector con, AbstractActor parent, JSONObject conf) throws JSONException
    {
        super(con, parent, conf);
    }
    
    public String getType()
    {
        return conf.getString("type");
    }
    
    public String getClassName()
    {
        return conf.optString("class");
    }
    
    public PropertyIteratorActor getPropertyIteratorActor() throws JSONException, IOException
    {
        if(propActor == null)
        {
            JSONObject request = new JSONObject();
            request.put("to", actor);
            request.put("type", "enumProperties");
            Map<String, Object> options = new HashMap();
            request.put("options", options);
            connector.send(request, message ->
            {
                JSONObject iterator = message.optJSONObject("iterator");
                if(iterator != null)
                {
                    propActor = new PropertyIteratorActor(connector, this, iterator);
                }
            });
        }
        return propActor;
    }
    
    public Map<String, Object> getProperties() throws JSONException, IOException
    {
        if(properties == null)
        {
            PropertyIteratorActor propActor = getPropertyIteratorActor();
            properties = propActor.getProperties();
        }
        return properties;
    }
}
