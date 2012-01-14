package org.skife.galaxy.http;

import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.skife.galaxy.Agent;

public class AgentModule extends ServletModule
{

    @Override
    protected void configureServlets()
    {
        bind(StaticServlet.class).in(Scopes.SINGLETON);
        serve("/static/*").with(StaticServlet.class);
        serve("/*").with(GuiceContainer.class);

        // resources
        bind(AgentResource.class);
        bind(SlotResource.class);

        // real application model
        bind(Agent.class).in(Scopes.SINGLETON);

        // jersey components
        bind(MustacheTemplateProcessor.class).in(Scopes.SINGLETON);
        bind(String.class).annotatedWith(TemplateRoot.class).toInstance("templates");
    }
}
