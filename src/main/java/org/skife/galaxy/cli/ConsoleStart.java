package org.skife.galaxy.cli;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import jnr.ffi.Library;
import org.eclipse.jetty.server.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.skife.cli.Command;
import org.skife.cli.Option;
import org.skife.galaxy.console.http.GuiceConsoleListener;
import org.skife.galaxy.http.NotFoundServlet;
import org.skife.gressil.Daemon;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.EnumSet;
import java.util.concurrent.Callable;

@Command(name="start", description = "Start and daemonize a console")
public class ConsoleStart implements Callable<Void>
{
    @Option(name = {"-r", "--root"},
            title = "root",
            description = "Root directory for deployment slots",
            configuration = "console.root")
    public File root;

    @Option(name = {"-P", "--port"},
            title = "port",
            description = "Port for HTTP server, default is 36525",
            configuration = "console.port")
    public int port = 36525;

    @Option(name = {"-H", "--host"},
            title = "host",
            description = "IP address to bind to, defaults to 0.0.0.0",
            configuration = "console.host")
    public String host = "0.0.0.0";


    @Option(name = {"-p", "--pidfile"},
            title = "pidfile",
            description = "path to pidfile", configuration = "console.pidfile")
    public File pidfile = new File("sculptor-console.pid");

    @Option(name = {"-l", "--log"},
            title = "log-file",
            description = "Log file",
            configuration = "console.log")
    public File logfile = new File("/dev/null");

    public Void call() throws Exception
    {
        if (pidfile.exists()) {
            // pidfile exists, check to see if an agent is already running
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


        Server server = new Server(InetSocketAddress.createUnresolved(host, port));
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        handler.addEventListener(new GuiceConsoleListener(root, false, host, port));
        handler.addFilter(com.google.inject.servlet.GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
        handler.addServlet(NotFoundServlet.class, "/*");
        server.setHandler(handler);

        server.start();
        server.join();

        return null;
    }


}
