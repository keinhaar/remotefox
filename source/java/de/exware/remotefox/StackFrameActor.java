package de.exware.remotefox;

import java.io.IOException;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class StackFrameActor extends AbstractActor
{
    private EnvironmentActor environmentActor;
    private ObjectActor thisActor;
    
    public StackFrameActor(DebugConnector con, AbstractActor parent, JSONObject conf) throws JSONException
    {
        super(con, parent, conf);
    }
    
    public String getType()
    {
        return conf.getString("type");
    }
    
    public String getDisplayName()
    {
        return conf.optString("displayName");
    }

    public Map<String, Object> getArguments() throws JSONException, IOException
    {
        return getEnvironmentActor().getArguments();
    }
    
    public Map<String, Object> getVariables() throws JSONException, IOException
    {
        return getEnvironmentActor().getVariables();
    }
    
    public SourceLocation getLocation()
    {
        if(conf.optQuery("/where") != null)
        {
            int line = (Integer) conf.query("/where/line");
            int column = (Integer) conf.query("/where/column");
            String sourceActor = (String) conf.query("/where/actor");
            SourceLocation location = new SourceLocation(sourceActor, line, column);
            return location;
        }
        return null;
    }

    public EnvironmentActor getEnvironmentActor() throws JSONException, IOException
    {
        if(environmentActor == null)
        {
            JSONObject request = new JSONObject();
            request.put("to", conf.query("/actor"));
            request.put("type", "getEnvironment");
            request.put("options", new String[0]);
            connector.send(request, message ->
            {
                environmentActor = new EnvironmentActor(connector, StackFrameActor.this, message);
            }
            );
        }
        return environmentActor;
    }
    
    public ObjectActor getThis()
    {
        if(thisActor == null)
        {
            thisActor = new ObjectActor(connector, this, conf.getJSONObject("this"));
        }
        return thisActor;
    }
}
