package org.skife.galaxy.cli;

import com.google.common.base.Preconditions;
import org.eclipse.jetty.server.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.iq80.cli.Command;
import org.iq80.cli.Option;
import org.iq80.cli.Options;
import org.iq80.cli.OptionsType;
import org.skife.galaxy.agent.http.GuiceAgentServletModule;
import org.skife.galaxy.http.NotFoundServlet;

import java.io.File;
import java.util.EnumSet;

@Command(name = "agent", description = "Run the agent")
public class AgentCommand implements SculptorCommand
{
    @Option(options = {"-r", "--root"}, required = true, description = "Root directory for deployment slots")
    public File root;

    @Option(options={"-p", "--port"}, description = "Port for HTTP server, default is 25365")
    public int port = 25365;

    @Options(OptionsType.GLOBAL)
    public GlobalOptions global;

    public void execute() throws Exception
    {
        if (!root.exists() && root.isDirectory()) {
            Preconditions.checkState(root.mkdirs(), "unable to create agent root directory %s", root.getAbsolutePath());
        }

        Server server = new Server(port);
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        handler.addEventListener(new GuiceAgentServletModule(root, global));
        handler.addFilter(com.google.inject.servlet.GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
        handler.addServlet(NotFoundServlet.class, "/*");
        server.setHandler(handler);

        server.start();
        server.join();
    }
}
