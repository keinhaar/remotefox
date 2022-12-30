package de.exware.remotefox.event;

public interface PauseListener
{
    public void paused(PauseEvent event);
    public void resumed(PauseEvent event);
}
