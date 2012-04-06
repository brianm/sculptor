package org.skife.galaxy.http;

import com.google.inject.Binder;
import com.google.inject.Module;
import org.skife.galaxy.ServerRoot;

import java.io.File;
import java.net.SocketException;

public class ServerArgumentsModule implements Module
{
    private final File    agentRoot;
    private final boolean debug;
    private final String  host;
    private final int port;

    public ServerArgumentsModule(@ServerRoot File root,
                                 @Debug boolean debug,
                                 @Host String host,
                                 @Port int port) throws SocketException
    {
        this.host = host;
        this.port = port;
        this.agentRoot = root;
        this.debug = debug;
    }

    public void configure(Binder binder)
    {
        binder.bind(boolean.class).annotatedWith(Debug.class).toInstance(debug);
        binder.bind(File.class).annotatedWith(ServerRoot.class).toInstance(agentRoot);
        binder.bind(int.class).annotatedWith(Port.class).toInstance(port);
        binder.bind(Integer.class).annotatedWith(Port.class).toInstance(port);
        binder.bind(String.class).annotatedWith(Host.class).toInstance(host);
    }
}
