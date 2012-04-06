package org.skife.galaxy.agent.http;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.guice.JerseyServletModule;
import org.joda.time.Duration;
import org.skife.galaxy.base.Start;
import org.skife.galaxy.http.ServerArgumentsModule;

import javax.servlet.ServletContextEvent;
import java.io.File;
import java.net.SocketException;
import java.net.URI;
import java.util.Set;

public class GuiceAgentListener extends GuiceServletContextListener
{
    private final Injector guice;

    public GuiceAgentListener(String host, int port, File agentRoot, boolean debug, Set<URI> consoles, Duration consoleAnnounceDuration) throws SocketException
    {
        this.guice = Guice.createInjector(Stage.PRODUCTION,
                                          new JerseyServletModule(),
                                          new AgentModule(host, port, consoles, agentRoot, debug, consoleAnnounceDuration));
    }


    @Override
    protected Injector getInjector()
    {
        return guice;
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent)
    {
        super.contextInitialized(servletContextEvent);
        guice.getInstance(EventBus.class).post(new Start());
    }

}
