package de.exware.remotefox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PauseActor extends AbstractActor
{
	private List<StackFrameActor> stackFrames;
	
	public enum PauseType
	{
        UNKNOWN
        , INTERRUPTED
        , BREAKPOINT
        , DEBUGGERSTATEMENT
	    , RESUME;
	    
	    static PauseType getType(String name)
	    {
            PauseType t = UNKNOWN;
            for(int i=0;i<values().length;i++)
            {
                if(values()[i].name().equalsIgnoreCase(name))
                {
                    t = values()[i];
                }
            }
            return t;
	    }
	}
	
    PauseActor(DebugConnector con, AbstractActor parent, JSONObject conf) throws JSONException, IOException
    {
        super(con, parent, conf);
    }
    
    /**
     * Get Variables from top most stackframe.
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public Map<String, Object> getVariables() throws JSONException, IOException
    {
        StackFrameActor actor = new StackFrameActor(connector, this, conf.getJSONObject("/frame"));
    	return actor.getEnvironmentActor().getVariables();
    }
    
    @Override
    public void handleMessage(JSONObject message) throws JSONException, IOException
    {
        super.handleMessage(message);
    }

    public PauseType getPauseType()
    {
        return PauseType.getType((String) conf.optQuery("/why/type"));
    }

    public SourceLocation getLocation()
    {
        if(conf.optQuery("/frame/where") != null)
        {
            int line = (Integer) conf.query("/frame/where/line");
            int column = (Integer) conf.query("/frame/where/column");
            String sourceActor = (String) conf.query("/frame/where/actor");
            SourceLocation location = new SourceLocation(sourceActor, line, column);
            return location;
        }
        return null;
    }
        
    public List<StackFrameActor> getStackFrames() throws IOException, JSONException
    {
        if(stackFrames == null)
        {
            stackFrames = new ArrayList<>();
            JSONObject request = new JSONObject();
            request.put("to", getParent(ThreadActor.class).getActorId());
            request.put("type", "frames");
            request.put("start", 0);
            request.put("count", 100);
            request.put("options", new String[0]);
            connector.send(request, message ->
            {
                JSONArray array = message.getJSONArray("frames");
                for(int i=0;i<array.length();i++)
                {
                    JSONObject jsonFrame = array.getJSONObject(i);
                    StackFrameActor actor = new StackFrameActor(connector, this, jsonFrame);
                    stackFrames.add(actor);
                }
            }
            );
        }
        return stackFrames;
    }
}
