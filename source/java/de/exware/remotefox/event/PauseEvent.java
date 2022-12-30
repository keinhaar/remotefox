package de.exware.remotefox.event;

import de.exware.remotefox.PauseActor;
import de.exware.remotefox.PauseActor.PauseType;
import de.exware.remotefox.SourceLocation;
import de.exware.remotefox.TabActor;

public class PauseEvent
{
    private TabActor source;
    private PauseActor pauseActor;
    private PauseType type;
    private SourceLocation location;
    
    public PauseEvent(TabActor source, PauseActor pauseActor)
    {
        this(source, pauseActor, null);
    }

    public PauseEvent(TabActor source, PauseActor pauseActor, PauseType type)
    {
        this.source = source;
        this.type = type;
        this.pauseActor = pauseActor;
        if(pauseActor != null)
        {
            location = pauseActor.getLocation();
        }
    }

    public TabActor getSource()
    {
        return source;
    }

    public PauseType getPauseType()
    {
        if(type != null)
        {
            return type;
        }
        return pauseActor.getPauseType();
    }

    public PauseActor getPauseActor()
    {
        return pauseActor;
    }

    public SourceLocation getLocation()
    {
        return location;
    }
    
}
