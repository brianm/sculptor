package org.skife.galaxy.cli;

import org.iq80.cli.Option;

public class GlobalOptions
{
    @Option(name = "debug", options = "--debug")
    public boolean debug = false;


    public GlobalOptions(RunType type)
    {
        this.debug = type == RunType.DEBUG;
    }

    public GlobalOptions()
    {
        // for the cli thing
    }

    public static enum RunType
    {
        DEBUG, NORMAL
    }
}
