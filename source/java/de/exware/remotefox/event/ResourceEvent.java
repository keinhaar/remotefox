package de.exware.remotefox.event;

import de.exware.remotefox.TabActor;
import de.exware.remotefox.WatcherActor.WatchableResource;

public class ResourceEvent
{
    private TabActor source;
    private WatchableResource type;
    
    public ResourceEvent(TabActor source, WatchableResource type)
    {
        this.source = source;
        this.type = type;
    }

    public TabActor getSource()
    {
        return source;
    }

    public WatchableResource getType()
    {
        return type;
    }
}
