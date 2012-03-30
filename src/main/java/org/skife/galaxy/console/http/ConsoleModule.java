package org.skife.galaxy.console.http;

import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.skife.galaxy.console.Console;
import org.skife.galaxy.http.MustacheTemplateProcessor;
import org.skife.galaxy.http.StaticServlet;
import org.skife.galaxy.http.TemplateRoot;

public class ConsoleModule extends ServletModule
{
    @Override
    protected void configureServlets()
    {
        bind(StaticServlet.class).in(Scopes.SINGLETON);
        serve("/static/*").with(StaticServlet.class);
        serve("/*").with(GuiceContainer.class);

        // resources
        bind(ConsoleResource.class);

        // real application model
        bind(Console.class).in(Scopes.SINGLETON);

        // jersey components
        bind(MustacheTemplateProcessor.class).in(Scopes.SINGLETON);
        bind(String.class).annotatedWith(TemplateRoot.class).toInstance("templates");
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
}
