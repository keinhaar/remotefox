package de.exware.remotefox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

abstract public class AbstractActor
{
    protected DebugConnector connector;
    protected String actor;
    protected JSONObject conf;
    protected AbstractActor parent;
    
    public AbstractActor(DebugConnector con, AbstractActor parent)
    {
        this(con, parent, (String)null);
    }

    public AbstractActor(DebugConnector con, AbstractActor parent, JSONObject conf) throws JSONException
    {
        this(con, parent, conf.getString("actor"));
        this.conf = conf;
    }

    public AbstractActor(DebugConnector con, AbstractActor parent, String actor)
    {
        connector = con;
        this.actor = actor;
        this.parent = parent;
    }

    public void handleMessage(JSONObject message) throws JSONException, IOException
    {
    }
    
    protected void close()
    {
    	connector.unregisterActor(actor);
    }

    /**
     * Close for children
     * @param actor
     */
    protected void close(AbstractActor actor)
    {
    	if(actor != null)
    	{
    		actor.close();
    	}
    }
    
    protected List<String> arrayToString(Object[] array)
    {
    	List<String> strings = new ArrayList();
    	for(int i=0;i<array.length;i++)
    	{
    		strings.add(array[i].toString());
    	}
    	return strings;
    }
    
    public <T extends AbstractActor> T getParent(Class<? extends AbstractActor> type)
    {      
        if(parent == null)
        {
            return null;
        }
        if(parent.getClass().equals(type))
        {
            return (T) parent;
        }
        else
        {
            return parent.getParent(type);
        }
    }

    public String getActorId()
    {
        return actor;
    }
}
