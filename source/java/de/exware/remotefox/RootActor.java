package de.exware.remotefox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RootActor extends AbstractActor
{
    private List<TabActor> tabs = new ArrayList<>();
    private TabActor lastGetTab;
    
    RootActor(DebugConnector con)
    {
        super(con, null);
    }
    
    public List<TabActor> listTabs() throws JSONException, IOException
    {
        JSONObject request = new JSONObject();
        request.put("to", "root");
        request.put("type", "listTabs");
        connector.send(request, message ->
        {
            JSONArray array = message.optJSONArray("tabs");
            if(array != null)
            {
                tabs.clear();
                for(int i=0;i<array.length();i++)
                {
                    JSONObject obj = array.getJSONObject(i);
                    TabActor actor = new TabActor(connector, this, obj);
                    tabs.add(actor);
                }
            }
        });
        return tabs;
    }
    
    public TabActor getTab(int browserId) throws JSONException, IOException
    {
        JSONObject request = new JSONObject();
        request.put("to", "root");
        request.put("type", "getTab");
        request.put("browserId", browserId);
        connector.send(request, message ->
        {
            JSONObject tab = message.getJSONObject("tab");
            if(tab != null)
            {
                lastGetTab = new TabActor(connector, this, tab);
            }
        });
        return lastGetTab;
    }

    public void getRoot() throws JSONException, IOException
    {
        JSONObject request = new JSONObject();
        request.put("to", "root");
        request.put("type", "getRoot");
        connector.send(request, null);
    }
}
