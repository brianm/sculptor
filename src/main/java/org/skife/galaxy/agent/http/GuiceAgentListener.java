package org.skife.galaxy.agent.http;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.guice.JerseyServletModule;
import org.joda.time.Duration;

import javax.servlet.ServletContextEvent;
import java.io.File;
import java.net.URI;
import java.util.Set;

public class GuiceAgentListener extends GuiceServletContextListener
{
    private final File      agentRoot;
    private final boolean   debug;
    private final Duration consoleAnnounceDuration;
    private final Set<URI> consoles;

    public GuiceAgentListener(File agentRoot, boolean debug, Set<URI> consoles, Duration consoleAnnounceDuration)
    {
        this.agentRoot = agentRoot;
        this.debug = debug;
        this.consoleAnnounceDuration = consoleAnnounceDuration;
        this.consoles = ImmutableSet.copyOf(consoles);
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
                                    new AgentModule(consoles, agentRoot, debug, consoleAnnounceDuration));
    }
}
