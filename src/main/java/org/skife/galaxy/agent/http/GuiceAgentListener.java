package org.skife.galaxy.agent.http;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.guice.JerseyServletModule;
import org.skife.galaxy.http.ServerArgumentsModule;

import javax.servlet.ServletContextEvent;
import java.io.File;

public class GuiceAgentListener extends GuiceServletContextListener
{
    private final File    agentRoot;
    private final boolean debug;

    public GuiceAgentListener(File agentRoot)
    {
        this(agentRoot, false);
    }

    public GuiceAgentListener(File agentRoot, boolean debug)
    {
        this.agentRoot = agentRoot;
        this.debug = debug;
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent)
    {
        super.contextInitialized(servletContextEvent);
    }

    @Override
    protected Injector getInjector()
    {
        return Guice.createInjector(Stage.PRODUCTION,
                                    new JerseyServletModule(),
                                    new ServerArgumentsModule(agentRoot, debug),
                                    new AgentModule());
    }
}
