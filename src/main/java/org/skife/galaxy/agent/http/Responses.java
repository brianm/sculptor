package org.skife.galaxy.agent.http;

import com.sun.jersey.api.view.Viewable;

import javax.ws.rs.core.Response;
import java.net.URI;

public class Responses
{
    public static Response viewableCreatedWithRedirectTo(URI uri) {
        return Response.created(uri)
            .location(uri)
            .entity(new Viewable("redirect.html", new RedirectTo(uri), RedirectTo.class))
            .build();
    }
}
