package org.skife.galaxy.cli;

import com.google.common.base.Preconditions;
import org.eclipse.jetty.server.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.iq80.cli.Command;
import org.iq80.cli.Option;
import org.skife.galaxy.http.GuiceServletConfig;
import org.skife.galaxy.http.NotFoundServlet;

import java.io.File;
import java.util.EnumSet;

@Command(name = "agent")
public class AgentCommand implements SculptorCommand
{

    @Option(options = {"-r", "--root"}, required = true)
    public File root;

    @Option(options={"-p", "--port"})
    public int port = 25365;

    public void execute() throws Exception
    {
        if (!root.exists() && root.isDirectory()) {
            Preconditions.checkState(root.mkdirs(), "unable to create agent root directory %s", root.getAbsolutePath());
        }

        Server server = new Server(port);
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        handler.addEventListener(new GuiceServletConfig(root));
        handler.addFilter(com.google.inject.servlet.GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
        handler.addServlet(NotFoundServlet.class, "/*");
        server.setHandler(handler);

        server.start();
        server.join();
    }
}
