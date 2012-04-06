package org.skife.galaxy.agent.http;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.joda.time.Duration;
import org.skife.galaxy.ServerRoot;
import org.skife.galaxy.agent.Agent;
import org.skife.galaxy.agent.ConsoleAnnouncement;
import org.skife.galaxy.agent.Consoles;
import org.skife.galaxy.base.GuiceEventBusRegistrar;
import org.skife.galaxy.http.Debug;
import org.skife.galaxy.http.Host;
import org.skife.galaxy.http.MustacheTemplateProcessor;
import org.skife.galaxy.http.Port;
import org.skife.galaxy.http.StaticServlet;
import org.skife.galaxy.http.TemplateRoot;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class AgentModule extends ServletModule
{
    private final EventBus events = new EventBus();
    private final Set<URI> consoles;
    private final File     agentRoot;
    private final boolean  debug;
    private final String   host;
    private final int      port;
    private final Duration consoleAnnounce;

    public AgentModule(String host, int port, Set<URI> consoles, File agentRoot, boolean debug, Duration consoleAnnounce)
    {
        this.port = port;
        this.consoleAnnounce = consoleAnnounce;
        this.consoles = ImmutableSet.copyOf(consoles);
        this.agentRoot = agentRoot;
        this.debug = debug;

        if ("0.0.0.0".equals(host)) {
            // this is a worthless host for URL building, try to infer a useful public address
            InetAddress addy = findPublicIp();
            this.host = addy.getHostAddress();
        }
        else {
            this.host = host;
        }
    }

    @Override
    protected void configureServlets()
    {
        // config
        bind(boolean.class).annotatedWith(Debug.class).toInstance(debug);
        bind(File.class).annotatedWith(ServerRoot.class).toInstance(agentRoot);
        bind(new TypeLiteral<Set<URI>>(){}).annotatedWith(Consoles.class).toInstance(consoles);
        bind(Duration.class).annotatedWith(ConsoleAnnouncement.class).toInstance(consoleAnnounce);
        bind(String.class).annotatedWith(Host.class).toInstance(host);
        bind(int.class).annotatedWith(Port.class).toInstance(port);
        bind(Integer.class).annotatedWith(Port.class).toInstance(port);

        // servlet stuff
        bind(StaticServlet.class).in(Scopes.SINGLETON);
        serve("/static/*").with(StaticServlet.class);
        serve("/*").with(GuiceContainer.class);

        // resources
        bind(AgentResource.class);
        bind(SlotResource.class);
        bind(StagedDeploymentResource.class);

        // real application model
        bind(Agent.class).in(Scopes.SINGLETON);
        bind(ScratchSpace.class).in(Scopes.SINGLETON);
        bind(EventBus.class).toInstance(events);
        bind(ConsoleReporter.class).in(Scopes.SINGLETON);

        // jersey components
        bind(MustacheTemplateProcessor.class).in(Scopes.SINGLETON);
        bind(String.class).annotatedWith(TemplateRoot.class).toInstance("templates");

        bindListener(Matchers.any(), new GuiceEventBusRegistrar(events));
    }

    @Provides
    @Singleton
    public JacksonJsonProvider jacksonProvider()
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        return new JacksonJsonProvider(mapper);
    }




    private static InetAddress findPublicIp()
    {
        // Check if local host address is a good v4 address
        InetAddress localAddress = null;
        try {
            localAddress = InetAddress.getLocalHost();
            if (isGoodV4Address(localAddress)) {
                return localAddress;
            }
        }
        catch (UnknownHostException ignored) {
        }
        if (localAddress == null) {
            try {
                localAddress = InetAddress.getByAddress(new byte[]{127, 0, 0, 1});
            }
            catch (UnknownHostException e) {
                throw new AssertionError("Could not get local ip address");
            }
        }

        // check all up network interfaces for a good v4 address
        for (NetworkInterface networkInterface : getGoodNetworkInterfaces()) {
            for (InetAddress address : Collections.list(networkInterface.getInetAddresses())) {
                if (isGoodV4Address(address)) {
                    return address;
                }
            }
        }
        // check all up network interfaces for a good v6 address
//        for (NetworkInterface networkInterface : getGoodNetworkInterfaces()) {
//            for (InetAddress address : Collections.list(networkInterface.getInetAddresses())) {
//                if (isGoodV6Address(address)) {
//                    return address;
//                }
//            }
//        }
        // just return the local host address
        // it is most likely that this is a disconnected developer machine
        return localAddress;
    }

    private static List<NetworkInterface> getGoodNetworkInterfaces()
    {
        ImmutableList.Builder<NetworkInterface> builder = ImmutableList.builder();
        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                try {
                    if (!networkInterface.isLoopback() && networkInterface.isUp()) {
                        builder.add(networkInterface);
                    }
                }
                catch (Exception ignored) {
                }
            }
        }
        catch (SocketException e) {
        }
        return builder.build();
    }

    private static boolean isGoodV4Address(InetAddress address)
    {
        return address instanceof Inet4Address &&
               !address.isAnyLocalAddress() &&
               !address.isLoopbackAddress() &&
               !address.isMulticastAddress();
    }
}
