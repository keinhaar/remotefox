package de.exware.remotefox;

import java.io.IOException;

import org.json.JSONArray;
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
    
    /**
     * Get the first possible localtion for a breakpoint in the given line.
     * @param line
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public SourceLocation getBreakpointPosition(int line) throws JSONException, IOException
    {
        final SourceLocation[] loc = new SourceLocation[1];
        JSONObject request = new JSONObject();
        request.put("to", actor);
        request.put("type", "getBreakpointPositionsCompressed");
        JSONObject query = new JSONObject();
        request.put("query", query);
        JSONObject start = new JSONObject();
        query.put("start", start);
        start.put("line", line);
        start.put("column", 0);
        JSONObject end = new JSONObject();
        query.put("end", end);
        end.put("line", line+1);
        end.put("column", 0);
        connector.send(request, message ->
        {
            JSONArray array = (JSONArray) message.optQuery("/positions/" + line);
            if(array != null && array.length() > 0)
            {
                int column = array.getInt(0);
                loc[0] = new SourceLocation(actor, line, column);
            }
        });
        return loc[0];
    }
}
