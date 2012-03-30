package org.skife.galaxy.cli;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import org.eclipse.jetty.server.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.joda.time.Duration;
import org.skife.cli.Command;
import org.skife.cli.Option;
import org.skife.galaxy.agent.command.DainDuration;
import org.skife.galaxy.agent.http.GuiceAgentListener;
import org.skife.galaxy.http.NotFoundServlet;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = {"run", "debug"}, description = "Run the agent in debug mode without daemonizing")
public class AgentRun implements Callable<Void>
{

    @Option(name = {"-a", "--announce"},
            title = "announcement-interval",
            description = "how frequently to announce, default is 1m")
    public String announcementInterval = "1m";

    @Option(name = {"-r", "--root"},
            title = "root",
            description = "Root directory for deployment slots",
            configuration = "agent.root")
    public File root;

    @Option(name = {"-P", "--port"},
            title = "port",
            description = "Port for HTTP server, default is 25365",
            configuration = "agent.port")
    public int port = 25365;

    @Option(name = {"-c", "--console"}, title = "console-url", description = "URL for console to report to, multiple okay")
    public List<URI> consoles = Collections.emptyList();

    public Void call() throws Exception
    {
        if (!root.exists() && root.isDirectory()) {
            Preconditions.checkState(root.mkdirs(), "unable to create agent root directory %s", root.getAbsolutePath());
        }

        Server server = new Server(port);
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        handler.addEventListener(new GuiceAgentListener(root,
                                                        true,
                                                        ImmutableSet.copyOf(consoles),
                                                        DainDuration.valueOf(announcementInterval).toJodaDuration()));
        handler.addFilter(com.google.inject.servlet.GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
        handler.addServlet(NotFoundServlet.class, "/*");
        server.setHandler(handler);

        server.start();
        server.join();

        return null;
    }
}
