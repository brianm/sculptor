package org.skife.galaxy.http;

import com.google.inject.Binder;
import com.google.inject.Module;
import org.skife.galaxy.agent.AgentRoot;
import org.skife.galaxy.cli.GlobalOptions;

import java.io.File;

public class ArgumentsModule implements Module
{
    private final File agentRoot;
    private final GlobalOptions global;

    public ArgumentsModule(File agentRoot, GlobalOptions global) {
        this.agentRoot = agentRoot;
        this.global = global;
    }

    public void configure(Binder binder)
    {
        binder.bind(GlobalOptions.class).toInstance(global);
        binder.bind(File.class).annotatedWith(AgentRoot.class).toInstance(agentRoot);
    }
}
