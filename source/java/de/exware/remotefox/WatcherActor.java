package de.exware.remotefox;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

public class WatcherActor extends AbstractActor
{
    private BreakpointListActor breakpointListActor;
    private TargetConfigurationActor targetConfigurationActor;
    private ThreadActor threadActor;
    private ThreadConfigurationActor threadConfigurationActor;
    private WindowGlobalActor windowActor;
    private ConsoleActor consoleActor;
    private boolean isWatching;
    
    public enum WatchableResource
    {
        CONSOLE_MESSAGE ("console-message")
        , DOCUMENT_EVENT ("document-event")
        , ERROR_MESSAGE ("error-message")
        , NETWORK_EVENT ("network-event")
        , REFLOW( "reflow")
        , SOURCE( "source")
        , THREAD_STATE ("thread-state");

        private String name;

        WatchableResource(String string)
        {
            this.name = string;
        }
        
        @Override
        public String toString()
        {
            return name == null ? name().toLowerCase() : name;
        }
    }

    WatcherActor(DebugConnector con, AbstractActor parent, JSONObject conf) throws JSONException, IOException
    {
        super(con, parent, conf);
        con.registerActor(actor, this);
    }
    
    @Override
    public void handleMessage(JSONObject message) throws JSONException, IOException 
    {
    	super.handleMessage(message);
        String type = message.optString("type");
        if("target-destroyed-form".equals(type))
        {
            resetActors();
            return;
        }
        else if("target-available-form".equals(type))
        {
            JSONObject target = message.getJSONObject("target");
            String actor = target.getString("actor");
            if(windowActor != null && actor.equals(windowActor.getActorId()) == false)
            {
                resetActors();
            }
            threadActor = new ThreadActor(connector, this, target.getString("threadActor"));
            windowActor = new WindowGlobalActor(connector, this, target.getString("actor"));
            consoleActor = new ConsoleActor(connector, this, target.getString("consoleActor"));
            return;
        }
    }
    
    private void resetActors()
    {
        close(threadActor);
        close(windowActor);
        close(consoleActor);
        close(breakpointListActor);
        close(targetConfigurationActor);
        close(threadConfigurationActor);
        threadActor = null;
        windowActor = null;
        consoleActor = null;
        breakpointListActor = null;
        targetConfigurationActor = null;
        threadConfigurationActor = null;
    }
    
    private TabActor getTabActor()
    {
        return getParent(TabActor.class);
    }
    
    
    
    public BreakpointListActor getBreakpointListActor() throws IOException, JSONException
    {
        if(breakpointListActor == null)
        {
            JSONObject request = new JSONObject();
            request.put("to", actor);
            request.put("type", "getBreakpointListActor");
            connector.send(request, message ->
            {
                JSONObject list = message.optJSONObject("breakpointList");
                if(list != null)
                {
                    breakpointListActor = new BreakpointListActor(connector, this, list.getString("actor"));
                }
            });
        }
        return breakpointListActor;
    }
    
    public TargetConfigurationActor getTargetConfigurationActor() throws IOException, JSONException
    {
        if(targetConfigurationActor == null)
        {
            JSONObject request = new JSONObject();
            request.put("to", actor);
            request.put("type", "getTargetConfigurationActor");
            connector.send(request, message ->
            {
                JSONObject config = message.optJSONObject("configuration");
                if(config != null)
                {
                    targetConfigurationActor = new TargetConfigurationActor(connector, this, config);
                }
            });
        }
        return targetConfigurationActor;
    }
    
    private void watchTargets() throws IOException, JSONException
    {
    	isWatching = true;
        JSONObject request = new JSONObject();
        request.put("to", actor);
        request.put("type", "watchTargets");
        request.put("targetType", "frame");
        connector.send(request, null);
    }
    
    public void watchResources(WatchableResource ... resourceTypes) throws IOException, JSONException
    {
        JSONObject request = new JSONObject();
        request.put("to", actor);
        request.put("type", "watchResources");
        request.put("resourceTypes", arrayToString(resourceTypes));
        connector.send(request, null);
    }
    
    public ConsoleActor getConsoleActor()
    {
        return consoleActor;
    }
    
    public ThreadActor getThreadActor() throws IOException, JSONException
    {
        if(isWatching == false)
        {
            watchTargets();
        }
        long start = System.currentTimeMillis();
        while(threadActor == null)
        {
            if(start + 3000 < System.currentTimeMillis())
            {
                throw new IOException("Could not get ThreadActor.");
            }
            try
            {
                Thread.sleep(10);
            }
            catch (InterruptedException e)
            {                
            }
        }
        return threadActor;
    }
    
    public WindowGlobalActor getWindowGlobalActor() throws IOException, JSONException
    {
        if(isWatching == false)
        {
            watchTargets();
        }
        return windowActor;
    }
    
    public ThreadConfigurationActor getThreadConfigurationActor() throws IOException, JSONException
    {
        if(threadConfigurationActor == null)
        {
            JSONObject request = new JSONObject();
            request.put("to", actor);
            request.put("type", "getThreadConfigurationActor");
            connector.send(request, message ->
            {
                JSONObject config = message.optJSONObject("configuration");
                if(config != null)
                {
                    threadConfigurationActor = new ThreadConfigurationActor(connector, this, config);
                }
            });
        }
        return threadConfigurationActor;
    }
}
