package de.exware.remotefox;

public class SourceLocation
{
    private String sourceActor;
    private int line;
    private int column;

    public SourceLocation(String sourceActor, int line, int column)
    {
        this.sourceActor = sourceActor;
        this.line = line;
        this.column = column;
    }

    public String getSourceActor()
    {
        return sourceActor;
    }

    public int getLine()
    {
        return line;
    }

    public int getColumn()
    {
        return column;
    }
}
