package de.exware.remotefox;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class BreakpointListActor extends AbstractActor
{
    BreakpointListActor(DebugConnector con, AbstractActor parent, String actor) throws JSONException, IOException
    {
        super(con, parent, actor);
    }

    @Override
    public void handleMessage(JSONObject message) throws JSONException, IOException
    {
        super.handleMessage(message);
    }

    public void setBreakpoint(String url, int line, int column) throws JSONException, IOException
    {
        JSONObject request = new JSONObject();
        request.put("to", actor);
        request.put("type", "setBreakpoint");
        JSONObject location = new JSONObject();
        location.put("sourceUrl", url);
        location.put("line", line);
        location.put("column", column);
        location.put("sourceId", "");
        request.put("location", location);
        Map options = new HashMap();
//        options.put("pauseOnExceptions", "true");
        request.put("options", options);
        connector.send(request, null);
    }
}
