package org.skife.galaxy.http;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.guice.JerseyServletModule;
import org.skife.galaxy.cli.GlobalOptions;

import javax.servlet.ServletContextEvent;
import java.io.File;

public class GuiceAgentServletModule extends GuiceServletContextListener
{
    private final File agentRoot;
    private final GlobalOptions global;

    public GuiceAgentServletModule(File agentRoot, GlobalOptions global)
    {
        this.agentRoot = agentRoot;
        this.global = global;
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
                                    new ArgumentsModule(agentRoot, global),
                                    new AgentModule());
    }
}
