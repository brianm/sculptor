package org.skife.galaxy.cli;

import com.google.common.base.Preconditions;
import org.eclipse.jetty.server.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.skife.cli.Command;
import org.skife.cli.Option;
import org.skife.galaxy.agent.http.GuiceAgentServletModule;
import org.skife.galaxy.http.NotFoundServlet;

import java.io.File;
import java.util.EnumSet;
import java.util.concurrent.Callable;

@Command(name = {"run", "debug"}, description = "Run the agent in debug mode without daemonizing")
public class AgentRun implements Callable<Void>
{
    @Option(name = {"-r", "--root"},
            title = "root",
            required = true,
            description = "Root directory for deployment slots",
            configuration = "agent.root")
    public File root;

    @Option(name = {"-P", "--port"},
            title = "port",
            description = "Port for HTTP server, default is 25365",
            configuration = "agent.port")
    public int port = 25365;

    @Option(name = {"-p", "--pidfile"},
            title = "pidfile",
            description = "path to pidfile", configuration = "agent.pidfile")
    public File pidfile = new File("sculptor-agent.pid");

    public Void call() throws Exception
    {
        if (!root.exists() && root.isDirectory()) {
            Preconditions.checkState(root.mkdirs(), "unable to create agent root directory %s", root.getAbsolutePath());
        }

        Server server = new Server(port);
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        handler.addEventListener(new GuiceAgentServletModule(root, true));
        handler.addFilter(com.google.inject.servlet.GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
        handler.addServlet(NotFoundServlet.class, "/*");
        server.setHandler(handler);

        server.start();
        server.join();

        return null;
    }
}
