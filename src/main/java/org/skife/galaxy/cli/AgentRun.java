package org.skife.galaxy.cli;

import com.google.common.base.Preconditions;
import org.eclipse.jetty.server.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.iq80.cli.Command;
import org.iq80.cli.Option;
import org.iq80.cli.OptionType;
import org.skife.galaxy.agent.http.GuiceAgentServletModule;
import org.skife.galaxy.http.NotFoundServlet;

import java.io.File;
import java.util.EnumSet;
import java.util.concurrent.Callable;

@Command(name = "run", description = "Run the agent")
public class AgentRun implements Callable<Void>
{
    @Option(name = {"-r", "--root"}, required = true, description = "Root directory for deployment slots")
    public File root;

    @Option(name = {"-P", "--port"}, description = "Port for HTTP server, default is 25365")
    public int port = 25365;

    @Option(name = "--debug", type = OptionType.GLOBAL)
    public boolean debug;

    @Option(name={"-c", "--config"}, description = "Configuration file", type = OptionType.GLOBAL)
    public File config = new File("/etc/sculptor/agent.conf");

    public Void call() throws Exception
    {
        ConfigFile cf = new ConfigFile(config);
        root = cf.fallbackFrom(root, "root");

        if (!root.exists() && root.isDirectory()) {
            Preconditions.checkState(root.mkdirs(), "unable to create agent root directory %s", root.getAbsolutePath());
        }

        Server server = new Server(port);
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        handler.addEventListener(new GuiceAgentServletModule(root, debug));
        handler.addFilter(com.google.inject.servlet.GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
        handler.addServlet(NotFoundServlet.class, "/*");
        server.setHandler(handler);

        server.start();
        server.join();

        return null;
    }
}
