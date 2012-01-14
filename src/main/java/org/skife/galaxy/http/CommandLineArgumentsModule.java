package org.skife.galaxy.http;

import com.google.inject.Binder;
import com.google.inject.Module;
import org.skife.galaxy.AgentRoot;

import java.io.File;

public class CommandLineArgumentsModule implements Module
{
    private final File agentRoot;

    public CommandLineArgumentsModule(File agentRoot) {
        this.agentRoot = agentRoot;
    }

    public void configure(Binder binder)
    {
        binder.bind(File.class).annotatedWith(AgentRoot.class).toInstance(agentRoot);
    }
}
