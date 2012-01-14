package org.skife.galaxy.http;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.guice.JerseyServletModule;

import java.io.File;

public class GuiceServletConfig extends GuiceServletContextListener
{
    private final File     agentRoot;

    public GuiceServletConfig(File agentRoot) {
        this.agentRoot = agentRoot;
    }

    @Override
    protected Injector getInjector()
    {
        return Guice.createInjector(Stage.PRODUCTION,
                                    new JerseyServletModule(),
                                    new CommandLineArgumentsModule(agentRoot),
                                    new AgentModule());
    }
}
