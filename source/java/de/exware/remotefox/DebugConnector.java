package de.exware.remotefox;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import de.exware.remotefox.WatcherActor.WatchableResource;

public class DebugConnector 
{
    private String host;
    private int port;
    private Socket socket;
    private BufferedInputStream bin;
    private BufferedOutputStream bout;
    private Map<String, MessageHandler> requests = new HashMap();
    private Map<String, AbstractActor> routingTargets = new HashMap();
    private RootActor root;
    private boolean logWire;
    private boolean running = false;
    
    public DebugConnector(String host, int port)
    {
        this.host = host;
        this.port = port;
    }

    /**
     * Registers the actor for unexpected events. Events that where not a direct response to a client request.
     * @param actorId
     * @param actor
     */
    public void registerActor(String actorId, AbstractActor actor)
    {
        routingTargets.put(actorId, actor);
    }
    
    /**
     * Removes the registration of the actor for unexpected events. 
     * @param actorId
     */
    public void unregisterActor(String actorId)
    {
        routingTargets.remove(actorId);
    }
    
    public void start() throws UnknownHostException, IOException
    {
        running = true;
        connect();
        readMessage();
        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                setName(DebugConnector.class.getSimpleName());
                super.run();
                while(running && socket.isClosed() == false)
                {
                    try
                    {
                        JSONObject message = readMessage();
                        if(message == null)
                        {
                            break;
                        }
                        String from = message.getString("from");
                        if(requests.containsKey(from))
                        {
                            MessageHandler handler = requests.get(from);
                            if(handler != null)
                            {
                                handler.handleMessage(message);
                            }
                            else
                            {
                                AbstractActor actor = routingTargets.get(from);
                                if(actor != null)
                                {
                                    actor.handleMessage(message);                                
                                }
                            }
                        }
                        else
                        {
                            AbstractActor actor = routingTargets.get(from);
                            if(actor != null)
                            {
                                actor.handleMessage(message);                                
                            }
                        }
                        requests.remove(from);
                    }
                    catch (SocketTimeoutException e)
                    {
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();                        
                    }
                }
                running = false;
            }
        };
        t.start();
    }
    
    public void stop()
    {
        running = false;
        try
        {
            socket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public RootActor getRootActor()
    {
        if(root == null)
        {
            root = new RootActor(this);
        }
        return root;
    }
    
    public static void main(String[] args) throws IOException, JSONException, InterruptedException
    {
        DebugConnector con = new DebugConnector("127.0.0.1", 10000);
        con.setLogWire(true);
//        JSONObject welcome = con.readMessage();
//        if(welcome == null)
//        {
//            throw new IOException("No Welcome from Firefox");
//        }
        con.start();
        RootActor actor = con.getRootActor();
        actor.getRoot();
        List<TabActor> tabs = actor.listTabs();
        TabActor tab = tabs.get(0);
//        ConsoleActor console = tab.getConsole();
//        console.startListeners();
        

//        tab.navigateTo("file:///D:/temp/loeschmich/test.html");
//        tab.navigateTo("file:///daten/develop/sdbg_workspace/test.html");
        Thread.sleep(3000);
        tab.interrupt();
//        tab.navigateTo("file:///c:/Temp/fehler.txt");
//        Thread.sleep(3000);
//        tab.goBack();
//        Thread.sleep(3000);
//        tab.goForward();
//        Thread.sleep(3000);
//        tab.navigateTo("file:///c:/Temp/result.txt");
//        Thread.sleep(6000);
        
//        tab.getWatcher().getThreadActor();
        
//        tabs = actor.listTabs();
//        tab = tabs.get(0);
          WatcherActor watcher = tab.getWatcher();
//        watcher.getWindowGlobalActor().navigateTo("http://www.golem.de");
//        Thread.sleep(3000);
//        watcher.getWindowGlobalActor().goBack();
//        Thread.sleep(3000);
//        watcher.getWindowGlobalActor().goForward();
        
        watcher.watchResources(WatchableResource.SOURCE
            , WatchableResource.DOCUMENT_EVENT
            , WatchableResource.THREAD_STATE
            , WatchableResource.REFLOW
            , WatchableResource.CONSOLE_MESSAGE);
//        
//        tab.setBreakpoint("file:///D:/temp/loeschmich/test.js", 5, 8);
//      tab.setBreakpoint("file:///daten/develop/sdbg_workspace/test.js", 5, 8);
        tab.setBreakpoint("http://localhost:8888/tt-0.js", 35903, 2);
//        tab.addEventBreakpoints("event.mouse.click");
        tab.resume();
        
        List<SourceActor> sactors = tab.getSourceActors();
        
        Thread.sleep(6000);
        
//        Map<String, Object> vars = tab.getPauseActor().getVariables();
//        System.out.println(vars);

        Thread.sleep(6000);
        
        //        System.out.println(tab.getActiveEventBreakpoints());
//        tab.dumpThread();

    }
    
    public synchronized boolean connect() throws UnknownHostException, IOException
    {
        boolean success = false;
        if(socket == null || socket.isClosed())
        {
            socket = new Socket(host, port);
            InputStream in = socket.getInputStream();
            socket.setSoTimeout(1000);
            bin = new BufferedInputStream(in);
            bout = new BufferedOutputStream(socket.getOutputStream());
            success = true;
        }
        return success;
    }
    
    public JSONObject readMessage() throws IOException, JSONException
    {
        JSONObject json = null;
        byte[] buf = new byte[10];
        int by = bin.read();
        int pos = 0;
        String length = null;
        while(by != -1 && pos < buf.length)
        {
            byte b = (byte) by;
            if(b == ':')
            {
                length = new String(buf, 0 , pos);
                break;
            }
            buf[pos++] = b;
            by = bin.read();
        }
        if(length != null)
        {
            int len = Integer.parseInt(length);
            buf = new byte[len];
            pos = 0;
            while(pos != len)
            {
                int p = bin.read(buf, pos, len-pos);
                if(p < 0)
                {
                    throw new IOException("Incomplete message.");
                }
                else
                {
                    pos += p;
                }
            }
            json = new JSONObject(new String(buf));
            if(logWire)
            {
                System.out.println("From Firefox: " + new String(buf));
            }
        }
        return json;
    }

    void send(JSONObject data) throws IOException
    {
    	send(data, null);
    }

    void send(JSONObject data, MessageHandler handler) throws IOException, JSONException
    {
        if(running)
        {
            String to = data.getString("to");
            while(requests.containsKey(to) && running)
            {
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {
                }
            }
            requests.put(to, handler);
            String packet = data.toString();
            send(packet);
            while(requests.containsKey(to) && running)
            {
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {
                }
            }
        }
    }

    private void send(String data) throws IOException
    {
        connect();
        String packet = "" + data.length() + ":" + data;
        if(logWire)
        {
            System.out.println(packet);
        }
        bout.write(packet.getBytes());
        bout.flush();
    }

    public boolean isLogWire()
    {
        return logWire;
    }

    public void setLogWire(boolean logWire)
    {
        this.logWire = logWire;
    }
}
