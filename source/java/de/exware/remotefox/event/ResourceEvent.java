package de.exware.remotefox.event;

import de.exware.remotefox.SourceActor;
import de.exware.remotefox.TabActor;
import de.exware.remotefox.WatcherActor.WatchableResource;

public class ResourceEvent
{
    private TabActor source;
    private SourceActor sourceActor;
    private WatchableResource type;
    
    public ResourceEvent(TabActor source, WatchableResource type)
    {
        this(source, null, type);
    }

    public ResourceEvent(TabActor source, SourceActor sourceActor, WatchableResource type)
    {
        this.source = source;
        this.type = type;
        this.sourceActor = sourceActor;
    }

    public TabActor getSource()
    {
        return source;
    }

    public WatchableResource getType()
    {
        return type;
    }

    public SourceActor getSourceActor()
    {
        return sourceActor;
    }
}
