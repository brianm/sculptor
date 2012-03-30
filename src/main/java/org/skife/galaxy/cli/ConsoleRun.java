package org.skife.galaxy.cli;

import com.google.common.base.Preconditions;
import com.google.inject.servlet.GuiceFilter;
import org.eclipse.jetty.server.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.skife.cli.Command;
import org.skife.cli.Option;
import org.skife.galaxy.console.http.GuiceConsoleListener;
import org.skife.galaxy.http.NotFoundServlet;

import java.io.File;
import java.util.EnumSet;
import java.util.concurrent.Callable;

@Command(name="run")
public class ConsoleRun implements Callable<Void>
{
    @Option(name = {"-r", "--root"},
            title = "root",
            description = "Root directory for console state",
            configuration = "console.root")
    public File root;

    @Option(name = {"-P", "--port"},
            title = "port",
            description = "Port for HTTP server, default is 25365",
            configuration = "console.port")
    public int port = 36525;

    public Void call() throws Exception
    {
        if (!root.exists() && root.isDirectory()) {
            Preconditions.checkState(root.mkdirs(), "unable to create agent root directory %s", root.getAbsolutePath());
        }

        Server server = new Server(port);
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        handler.addEventListener(new GuiceConsoleListener(root, true));
        handler.addFilter(GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
        handler.addServlet(NotFoundServlet.class, "/*");
        server.setHandler(handler);

        server.start();
        server.join();

        return null;
    }

}
