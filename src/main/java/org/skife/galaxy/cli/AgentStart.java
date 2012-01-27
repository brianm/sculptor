package org.skife.galaxy.cli;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import jnr.ffi.Library;
import org.eclipse.jetty.server.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.iq80.cli.Command;
import org.iq80.cli.Option;
import org.iq80.cli.OptionType;
import org.skife.galaxy.agent.http.GuiceAgentServletModule;
import org.skife.galaxy.http.NotFoundServlet;
import org.skife.gressil.Daemon;

import java.io.File;
import java.util.EnumSet;
import java.util.concurrent.Callable;

@Command(name = "start", description = "Start and daemonize an agent")
public class AgentStart implements Callable<Void>
{
    @Option(name = {"-r", "--root"}, required = true, description = "Root directory for deployment slots")
    public File root;

    @Option(name = {"-P", "--port"}, description = "Port for HTTP server, default is 25365")
    public int port = 25365;

    @Option(name = "--debug", type = OptionType.GLOBAL)
    public boolean debug = false;

    @Option(name = {"-p", "--pidfile"}, description = "Pidfile")
    public File pidfile;

    @Option(name = {"-l", "--log"}, description = "Log file")
    public File logfile;

    @Option(name={"-c", "--config"}, description = "Configuration file", type = OptionType.GLOBAL)
    public File config = new File("/etc/sculptor/agent.conf");

    public Void call() throws Exception
    {
        ConfigFile cf = new ConfigFile(config);
        root = cf.fallbackFrom(root, "root");
        logfile = cf.fallbackFrom(logfile, "log");
        pidfile = cf.fallbackFrom(pidfile, "pidfile");

        if (pidfile.exists()) {
            // pidfile exists, check to see if an agent is alreay running
            int pid = Integer.parseInt(Files.readFirstLine(pidfile, Charsets.US_ASCII));
            int rs = Library.loadLibrary("c", LibC.class).kill(pid, 0);
            if (rs == 0) {
                // already running, LSB says this is a success case
                return null;
            }
        }

        if (!root.exists() && root.isDirectory()) {
            Preconditions.checkState(root.mkdirs(), "unable to create agent root directory %s", root.getAbsolutePath());
        }

        new Daemon().withPidFile(pidfile)
                    .withStdout(logfile)
                    .daemonize();


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
