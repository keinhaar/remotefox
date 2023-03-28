package de.exware.remotefox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.exware.remotefox.WatcherActor.WatchableResource;
import de.exware.remotefox.event.ResourceEvent;

public class WindowGlobalActor extends AbstractActor
{
    private List<SourceActor> sourceActors = new ArrayList<>();

    WindowGlobalActor(DebugConnector con, AbstractActor parent, String actor) throws JSONException, IOException
    {
        super(con, parent, actor);
        con.registerActor(actor, this);
    }
    
    @Override
    public void handleMessage(JSONObject message) throws JSONException, IOException
    {
        super.handleMessage(message);
        String type = message.optString("type");
        if("resource-available-form".equals(type))
        {
            JSONArray resources = message.getJSONArray("resources");
            for(int i=0;i<resources.length();i++)
            {
                JSONObject resource = resources.getJSONObject(i);
                String rtype = resource.getString("resourceType");
                if(WatchableResource.SOURCE.toString().equals(rtype))
                {
                    if("scriptElement".equals(resource.optString("introductionType")))
                    {
                        SourceActor sourceActor = new SourceActor(connector, this, resource);
                        sourceActors.add(sourceActor);
                        getTabActor().fireSourceAvailable(new ResourceEvent(getTabActor(), WatchableResource.SOURCE));
                    }
                }
                if(WatchableResource.DOCUMENT_EVENT.toString().equals(rtype))
                {
                    String name = resource.getString("name");
                    if("dom-loading".equals(name))
                    {
                        getTabActor().fireDocumentDomLoading(new ResourceEvent(getTabActor(), WatchableResource.DOCUMENT_EVENT));
                    }
                    else if("dom-interactive".equals(name))
                    {
                        getTabActor().fireDocumentInteractiveEvent(new ResourceEvent(getTabActor(), WatchableResource.DOCUMENT_EVENT));
                    }
                }
            }
        }
    }

    private TabActor getTabActor()
    {
        return getParent(TabActor.class);
    }
    
    public void navigateTo(String url) throws JSONException, IOException
    {
        JSONObject request = new JSONObject();
        request.put("to", actor);
        request.put("type", "navigateTo");
        request.put("url", url);
        connector.send(request, null);
    }
    
    public void goBack() throws JSONException, IOException
    {
        JSONObject request = new JSONObject();
        request.put("to", actor);
        request.put("type", "goBack");
        connector.send(request, null);
    }
    
    public void goForward() throws JSONException, IOException
    {
        JSONObject request = new JSONObject();
        request.put("to", actor);
        request.put("type", "goForward");
        connector.send(request, null);
    }

    public List<SourceActor> getSourceActors()
    {
        return sourceActors;
    }
}
