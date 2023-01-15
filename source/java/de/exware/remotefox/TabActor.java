package de.exware.remotefox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import de.exware.remotefox.ThreadActor.ResumeType;
import de.exware.remotefox.event.PauseEvent;
import de.exware.remotefox.event.PauseListener;
import de.exware.remotefox.event.ResourceEvent;
import de.exware.remotefox.event.ResourceListener;

public class TabActor extends AbstractActor
{
    private ConsoleActor console;
    private JSONObject frame;
    private WatcherActor watcher;
    private List<PauseListener> pauseListeners = new ArrayList<>();
    private List<ResourceListener> resourceListeners = new ArrayList<>();
    
    TabActor(DebugConnector con, AbstractActor parent, JSONObject conf) throws JSONException, IOException
    {
        super(con, parent, conf);
    }
    
    public String getTitle()
    {
        return conf.optString("title");
    }
    
    public String getURL()
    {
        return conf.optString("url");
    }
    
    private void getTarget() throws JSONException, IOException
    {
        if(frame == null)
        {
            JSONObject request = new JSONObject();
            request.put("to", actor);
            request.put("type", "getTarget");
            connector.send(request, message ->
            {
                JSONObject frame = message.optJSONObject("frame");
                if(frame != null)
                {
                    TabActor.this.frame = frame;
                }
            });
        }
    }

    public WatcherActor getWatcher() throws JSONException, IOException
    {
        if(watcher == null)
        {
            JSONObject request = new JSONObject();
            request.put("to", actor);
            request.put("type", "getWatcher");
            request.put("isServerTargetSwitchingEnabled", true);
            request.put("isPopupDebuggingEnabled", false);
            connector.send(request, message ->
            {
                String watcheractor = message.getString("actor");
                if(watcheractor.contains("watcher"))
                {
                    TabActor.this.watcher = new WatcherActor(connector, this, message);
                }
            });
        }
        return watcher;
    }

    public ConsoleActor getConsole() throws JSONException, IOException
    {
        getTarget();
        if(console == null)
        {
            console = new ConsoleActor(connector, this, frame.getString("consoleActor"));
        }
        return console;
    }
    
    private ThreadActor getThreadActor() throws JSONException, IOException
    {
        getWatcher();
        return watcher.getThreadActor();
    }
    
    public PauseActor getPauseActor() throws JSONException, IOException
    {
    	return watcher.getThreadActor().getPauseActor();
    }
    
    public void attach() throws JSONException, IOException
    {
        getThreadActor().attach();
    }
    
    public void interrupt() throws JSONException, IOException
    {
        if(isPaused() == false)
        {
            getThreadActor().interrupt();
        }
    }
    
    public boolean isPaused() throws JSONException, IOException
    {
        return getThreadActor().getPauseActor() != null;
    }
    
    public void removeBreakpoint(String url, int line, int column) throws JSONException, IOException
    {
        getThreadActor().removeBreakpoint(url, line, column);
    }
    
    public void setBreakpoint(String url, int line, int column) throws JSONException, IOException
    {
        getThreadActor().setBreakpoint(url, line, column);
    }
    
    public void setBreakpoint(String url, int line, int column, Map<String, String> options) throws JSONException, IOException
    {
        getThreadActor().setBreakpoint(url, line, column, options);
    }
    
    public void addEventBreakpoints(String eventType) throws JSONException, IOException
    {
        getThreadActor().addEventBreakpoints(eventType);
    }
    
    public List<String> getActiveEventBreakpoints() throws IOException, JSONException
    {
        return getThreadActor().getActiveEventBreakpoints();
    }
    
    public void resume() throws IOException, JSONException
    {
        if(isPaused())
        {
            getThreadActor().resume();
        }
    }

    public void resume(ResumeType type) throws IOException, JSONException
    {
        if(isPaused())
        {
            getThreadActor().resume(type);
        }
    }

    public List<StackFrameActor> getStackFrames() throws JSONException, IOException
    {
        if(isPaused())
        {
            return getPauseActor().getStackFrames();
        }
        return null;
    }
    
    public void dumpThread() throws IOException, JSONException
    {
        getThreadActor().dumpThread();
    }
    
    public void navigateTo(String url) throws IOException, JSONException
    {
        while(getWatcher().getWindowGlobalActor() == null)
        {
        }
        getWatcher().getWindowGlobalActor().navigateTo(url);
    }
    
    public void goForward() throws IOException, JSONException
    {
        getWatcher().getWindowGlobalActor().goForward();
    }
    
    public void goBack() throws IOException, JSONException
    {
        getWatcher().getWindowGlobalActor().goBack();
    }
    

    public List<SourceActor> getSourceActors() throws JSONException, IOException
    {
        return getWatcher().getWindowGlobalActor().getSourceActors();
    }
    
    public void addPauseListener(PauseListener listener)
    {
        if(pauseListeners.contains(listener) == false)
        {
            pauseListeners.add(listener);
        }
    }
    
    public void removePauseListener(PauseListener listener)
    {
        pauseListeners.remove(listener);
    }

    public void addResourceListener(ResourceListener listener)
    {
        if(resourceListeners.contains(listener) == false)
        {
            resourceListeners.add(listener);
        }
    }
    
    public void removeResourceListener(ResourceListener listener)
    {
        resourceListeners.remove(listener);
    }

    void firePausedEvent(PauseEvent evt)
    {
        for(int i=0;i<pauseListeners.size();i++)
        {
            pauseListeners.get(i).paused(evt);
        }
    }

    void fireResumedEvent(PauseEvent evt)
    {
        for(int i=0;i<pauseListeners.size();i++)
        {
            pauseListeners.get(i).resumed(evt);
        }
    }
    
    void fireDocumentWillLoadingEvent(ResourceEvent evt)
    {
        for(int i=0;i<resourceListeners.size();i++)
        {
            resourceListeners.get(i).documentWillLoading(evt);
        }
    }
    
    void fireDocumentInteractiveEvent(ResourceEvent evt)
    {
        for(int i=0;i<resourceListeners.size();i++)
        {
            resourceListeners.get(i).documentDomInteractive(evt);
        }
    }
    
    void fireDocumentDomLoading(ResourceEvent evt)
    {
        for(int i=0;i<resourceListeners.size();i++)
        {
            resourceListeners.get(i).documentDomLoading(evt);
        }
    }
    
    void fireSourceAvailable(ResourceEvent evt)
    {
        for(int i=0;i<resourceListeners.size();i++)
        {
            resourceListeners.get(i).sourceAvailable(evt);
        }
    }
    
    void fireReflow(ResourceEvent evt)
    {
        for(int i=0;i<resourceListeners.size();i++)
        {
            resourceListeners.get(i).reflow(evt);
        }
    }
    
    public boolean isZombie()
    {
        return conf.getBoolean("isZombieTab");
    }
}
