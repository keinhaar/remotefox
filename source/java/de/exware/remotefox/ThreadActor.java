package de.exware.remotefox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.exware.remotefox.PauseActor.PauseType;
import de.exware.remotefox.event.PauseEvent;

public class ThreadActor extends AbstractActor
{
    private boolean attached = false;
	private PauseActor pauseActor;
    
    public enum ResumeType
    {
        STEP_OVER("next")
        , STEP_INTO("step")
        , STEP_OUT("finish")
        ;
        private String name;

        ResumeType(String string)
        {
            this.name = string;
        }
        
        @Override
        public String toString()
        {
            return name == null ? name().toLowerCase() : name;
        }
    }

    ThreadActor(DebugConnector con, AbstractActor parent, String actor) throws JSONException, IOException
    {
        super(con, parent, actor);
        con.registerActor(actor, this);
    }

    @Override
    public void handleMessage(JSONObject message) throws JSONException, IOException
    {
        super.handleMessage(message);
        String type = message.optString("type");
        if("paused".equals(type))
        {
            pauseActor = new PauseActor(connector, this, message);
            firePausedEvent(pauseActor);
        }
        else if("resumed".equals(type))
        {
            pauseActor.close();
            fireResumedEvent();
            pauseActor = null;
        }
    }

    private void firePausedEvent(PauseActor pauseActor)
    {
        TabActor tab = getParent(TabActor.class);
        PauseEvent evt = new PauseEvent(tab, pauseActor);
        tab.firePausedEvent(evt);
    }

    private void fireResumedEvent()
    {
        TabActor tab = getParent(TabActor.class);
        PauseEvent evt = new PauseEvent(tab, pauseActor, PauseType.RESUME);
        tab.fireResumedEvent(evt);
    }

    public void attach() throws JSONException, IOException
    {
        if(attached == false)
        {
            JSONObject request = new JSONObject();
            request.put("to", actor);
            request.put("type", "attach");
            Map<String, Object> options = new HashMap();
            options.put("pauseOnExceptions", false);
            options.put("ignoreCaughtExeptions", true);
            options.put("shouldShowOverlay", true); //if true, firefox shows that a breakpoint was hit, and allows to resume
            options.put("shouldIncludeSavedFrames", true);
            options.put("shouldIncludeAsyncLiveFrames", false);
            options.put("skipBreakpoints", false);
            options.put("logEventBreakpoints", false);
            options.put("observeAsmJS", false);
            options.put("breakpoints", new JSONObject());
            options.put("eventBreakpoints", new JSONArray());
            request.put("options", options);
            connector.send(request, message ->
            {
                if(message.has("value"))
                {
                    attached = message.getBoolean("value");
                }
            });
        }
    }

    public void interrupt() throws JSONException, IOException
    {
        attach();
        JSONObject request = new JSONObject();
        request.put("to", actor);
        request.put("type", "interrupt");
        request.put("when", "");
        connector.send(request, null);
    }

    public void setBreakpoint(String url, int line, int column) throws JSONException, IOException
    {
        setBreakpoint(url, line, column, null);
    }
    
    public void setBreakpoint(String url, int line, int column, Map<String,String> options) throws JSONException, IOException
    {
        JSONObject request = new JSONObject();
        request.put("to", actor);
        request.put("type", "setBreakpoint");
        JSONObject location = new JSONObject();
        location.put("sourceUrl", url);
        location.put("line", line);
        location.put("column", column);
        request.put("location", location);
        if(options == null)
        {
            options = new HashMap<>();
        }
        request.put("options", options);
        connector.send(request, null);
    }

    public void removeBreakpoint(String url, int line, int column) throws JSONException, IOException
    {
        JSONObject request = new JSONObject();
        request.put("to", actor);
        request.put("type", "removeBreakpoint");
        JSONObject location = new JSONObject();
        location.put("sourceUrl", url);
        location.put("line", line);
        location.put("column", column);
        request.put("location", location);
        Map<String,String> options = new HashMap<>();
        request.put("options", options);
        connector.send(request, null);
    }

    public void resume() throws IOException, JSONException
    {
        JSONObject request = new JSONObject();
        request.put("to", actor);
        request.put("type", "resume");
        connector.send(request, null);
    }

    public void resume(ResumeType type) throws IOException, JSONException
    {
        JSONObject request = new JSONObject();
        request.put("to", actor);
        request.put("type", "resume");
        JSONObject limitType = new JSONObject();
        limitType.put("type", type.toString());
        request.put("resumeLimit", limitType);
        connector.send(request, null);
    }

    public void dumpThread() throws IOException, JSONException
    {
        JSONObject request = new JSONObject();
        request.put("to", actor);
        request.put("type", "dumpThread");
        connector.send(request, null);
    }

    public void isAttached() throws IOException, JSONException
    {
        JSONObject request = new JSONObject();
        request.put("to", actor);
        request.put("type", "isAttached");
        connector.send(request, null);
    }

    public List<String> getActiveEventBreakpoints() throws IOException, JSONException
    {
        JSONObject request = new JSONObject();
        request.put("to", actor);
        request.put("type", "getActiveEventBreakpoints");
        List<String> eventIds = new ArrayList<>();
        connector.send(request, message ->
        {
            JSONArray jids = message.getJSONArray("ids");
            for(int i=0;i< jids.length();i++)
            {
                eventIds.add(jids.getString(i));
            }
        });
        return eventIds;
    }

    public void setActiveEventBreakpoints(List<String> ids) throws IOException, JSONException
    {
        JSONObject request = new JSONObject();
        request.put("to", actor);
        request.put("type", "setActiveEventBreakpoints");
        request.put("ids", ids);
        connector.send(request, null);
    }

    public void addEventBreakpoints(String ... eventTypes) throws IOException, JSONException
    {
        List<String> eventIds = getActiveEventBreakpoints();
        for(int i=0;i<eventTypes.length;i++)
        {
            if(false == eventIds.contains(eventTypes[i]))
            {
                eventIds.add(eventTypes[i]);
            }
        }
        setActiveEventBreakpoints(eventIds);
    }

	public PauseActor getPauseActor() 
	{
		return pauseActor;
	}

}
