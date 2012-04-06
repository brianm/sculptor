package org.skife.galaxy.console.http;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.guice.JerseyServletModule;
import org.skife.galaxy.base.Start;
import org.skife.galaxy.http.ServerArgumentsModule;

import javax.servlet.ServletContextEvent;
import java.io.File;
import java.net.SocketException;

public class GuiceConsoleListener extends GuiceServletContextListener
{
    private final Injector guice;

    public GuiceConsoleListener(File root, boolean debug, String host, int port) throws SocketException
    {
        guice = Guice.createInjector(Stage.PRODUCTION,
                                     new ServerArgumentsModule(root, debug, host, port),
                                     new JerseyServletModule(),
                                     new ConsoleModule());
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent)
    {
        super.contextInitialized(servletContextEvent);
        guice.getInstance(EventBus.class).post(new Start());
    }

    @Override
    protected Injector getInjector()
    {
        return guice;
    }
}
