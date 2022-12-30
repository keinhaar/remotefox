package de.exware.remotefox.event;

public interface ResourceListener
{
    public void sourceAvailable(ResourceEvent event);

    public void reflow(ResourceEvent event);

    public void documentWillLoading(ResourceEvent event);

    public void documentDomLoading(ResourceEvent event);

    public void documentDomInteractive(ResourceEvent event);
}
