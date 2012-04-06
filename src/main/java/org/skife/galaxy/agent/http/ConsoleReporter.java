package org.skife.galaxy.agent.http;

import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.MoreExecutors;
import com.ning.http.client.AsyncHttpClient;
import org.joda.time.Duration;
import org.skife.galaxy.agent.ConsoleAnnouncement;
import org.skife.galaxy.agent.Consoles;
import org.skife.galaxy.base.Start;
import org.skife.galaxy.http.Host;
import org.skife.galaxy.http.Port;
import org.skife.galaxy.rep.Action;
import org.skife.galaxy.rep.ConsoleDescription;
import org.skife.galaxy.rep.Link;
import org.skife.galaxy.rep.ListOfLinks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.collect.Iterables.find;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.skife.galaxy.http.JsonEntityWriter.toJson;
import static org.skife.galaxy.base.MorePredicates.beanPropertyEquals;
import static org.skife.galaxy.http.JsonMappingAsyncHandler.fromJson;

public class ConsoleReporter
{
    private final Set<URI>                 consoles;
    private final Duration                 announce;
    private final ScheduledExecutorService cron;
    private final URI                      self;

    private static final Logger log = LoggerFactory.getLogger(ConsoleReporter.class);

    @Inject
    public ConsoleReporter(@Consoles Set<URI> consoles,
                           @ConsoleAnnouncement Duration frequency,
                           @Host String host,
                           @Port int port)
    {
        this.consoles = ImmutableSet.copyOf(consoles);
        this.announce = frequency;
        this.cron = MoreExecutors.getExitingScheduledExecutorService(new ScheduledThreadPoolExecutor(consoles.size()));
        this.self = URI.create(format("http://%s:%d/", host, port));
    }

    @Subscribe
    public void start(Start _)
    {
        for (final URI console : consoles) {
            cron.scheduleWithFixedDelay(new Runnable()
            {
                @Override
                public void run()
                {

                    final AtomicReference<URI> reg_uri = new AtomicReference<URI>();
                    final AsyncHttpClient http = new AsyncHttpClient();
                    try {
                        try {
                            if (reg_uri.get() == null) {
                                ConsoleDescription cd = http.prepareGet(console.toString())
                                                            .setHeader("Accept", MediaType.APPLICATION_JSON)
                                                            .execute(fromJson(ConsoleDescription.class))
                                                            .get();
                                Action reg = find(cd.getActions(), beanPropertyEquals("rel", "register-agent"));
                                reg_uri.set(reg.getUri());
                            }


                            // register
                            http.preparePost(reg_uri.toString())
                                .setHeader("Content-type", MediaType.APPLICATION_JSON)
                                .setBody(toJson(new ListOfLinks(asList(new Link("agent",
                                                                                self,
                                                                                "Agent URL")))))
                                .execute()
                                .get();
                        }
                        catch (Exception e) {
                            log.warn(String.format("unable to register with console %s because of %s",
                                                   console, e.getMessage()));
                        }
                    }
                    finally {
                        http.close();
                    }

                }
            }, 0, announce.getMillis(), TimeUnit.MILLISECONDS);
        }
    }
}
