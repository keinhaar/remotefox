package de.exware.remotefox;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

public class ConsoleActor extends AbstractActor
{
    enum Listener
    {
        PAGE_ERROR( "PageError")
        ;

        private String name;

    	Listener(String string)
        {
            this.name = string;
        }
        
        @Override
        public String toString()
        {
            return name == null ? name().toLowerCase() : name;
        }
    }

    ConsoleActor(DebugConnector con, AbstractActor parent, String actor) throws JSONException, IOException
    {
        super(con, parent, actor);
        con.registerActor(actor, this);
    }
    
    public void startListeners(Listener ... listeners) throws JSONException, IOException
    {
        JSONObject request = new JSONObject();
        request.put("to", actor);
        request.put("type", "startListeners");
        request.put("listeners", arrayToString(listeners));
        connector.send(request, null);
    }

    @Override
    public void handleMessage(JSONObject message) throws JSONException, IOException
    {
        super.handleMessage(message);
        String type = message.optString("type");
        if("pageError".equals(type))
        {
            System.out.println(message.getJSONObject("pageError").getString("errorMessage"));
        }
    }
}
