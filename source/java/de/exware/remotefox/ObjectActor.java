package de.exware.remotefox;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An Actor for Objects that may be on Stack or an Argument to a method call.
 *
 */
public class ObjectActor extends AbstractActor
{
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
}
