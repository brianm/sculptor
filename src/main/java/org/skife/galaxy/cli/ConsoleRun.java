package org.skife.galaxy.cli;

import com.google.inject.servlet.GuiceFilter;
import org.eclipse.jetty.server.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.skife.cli.Command;
import org.skife.cli.Option;
import org.skife.galaxy.console.http.GuiceConsoleListener;
import org.skife.galaxy.http.NotFoundServlet;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.EnumSet;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkState;

@Command(name="run")
public class ConsoleRun implements Callable<Void>
{
    @Option(name = {"-r", "--root"},
            title = "root",
            description = "Root directory for console state",
            configuration = "console.root")
    public File root;

    @Option(name = {"-H", "--host"},
            title = "host",
            description = "IP address to bind to, defaults to 0.0.0.0",
            configuration = "console.host")
    public String host = "0.0.0.0";

    @Option(name = {"-P", "--port"},
            title = "port",
            description = "Port for HTTP server, default is 36525",
            configuration = "console.port")
    public int port = 36525;

    public Void call() throws Exception
    {
        if (!root.exists() && root.isDirectory()) {
            checkState(root.mkdirs(), "unable to create agent root directory %s", root.getAbsolutePath());
        }

        Server server = new Server(InetSocketAddress.createUnresolved(host, port));
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        handler.addEventListener(new GuiceConsoleListener(root, true, host, port));
        handler.addFilter(GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
        handler.addServlet(NotFoundServlet.class, "/*");
        server.setHandler(handler);

        server.start();
        server.join();

        return null;
    }

}
