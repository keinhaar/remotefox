package de.exware.remotefox;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

public class SourceActor extends AbstractActor
{
    SourceActor(DebugConnector con, AbstractActor parent, JSONObject conf) throws JSONException, IOException
    {
        super(con, parent, conf);
    }
    
    public String getURL()
    {
        return conf.optString("url");
    }
    
    public String getSourceMapURL()
    {
        return conf.optString("sourceMapURL");
    }
    
    public String getSourceMapBaseURL()
    {
        return conf.optString("sourceMapBaseURL");
    }
    
    public boolean isBlackBoxed()
    {
        return conf.getBoolean("isBlackBoxed");
    }
    
    public boolean isInlineSource()
    {
        return conf.getBoolean("isInlineSource");
    }
    
    public String getIntroductionType()
    {
        return conf.getString("introductionType");
    }
}
