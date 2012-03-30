package org.skife.galaxy.console.http;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.guice.JerseyServletModule;
import org.skife.galaxy.http.ServerArgumentsModule;

import java.io.File;

public class GuiceConsoleListener extends GuiceServletContextListener
{

    private final File root;
    private final boolean debug;

    public GuiceConsoleListener(File root, boolean debug)
    {
        this.root = root;
        this.debug = debug;
    }

    @Override
    protected Injector getInjector()
    {
        return Guice.createInjector(Stage.PRODUCTION,
                                    new ServerArgumentsModule(root, debug),
                                    new JerseyServletModule(),
                                    new ConsoleModule());
    }

}
