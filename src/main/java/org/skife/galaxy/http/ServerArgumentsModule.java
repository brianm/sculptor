package org.skife.galaxy.http;

import com.google.inject.Binder;
import com.google.inject.Module;
import org.skife.galaxy.ServerRoot;

import java.io.File;

public class ServerArgumentsModule implements Module
{
    private final File agentRoot;
    private final boolean debug;

    public ServerArgumentsModule(File agentRoot, boolean debug) {
        this.agentRoot = agentRoot;
        this.debug = debug;
    }

    public void configure(Binder binder)
    {
        binder.bind(boolean.class).annotatedWith(Debug.class).toInstance(debug);
        binder.bind(File.class).annotatedWith(ServerRoot.class).toInstance(agentRoot);
    }
}
