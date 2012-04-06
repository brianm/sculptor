package org.skife.galaxy.http;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.Response;
import org.codehaus.jackson.map.ObjectMapper;


public class JsonMappingAsyncHandler<T> extends AsyncCompletionHandler<T>
{

    private static final ObjectMapper mapper = new ObjectMapper();

    private final Class<T> type;

    public static <T> JsonMappingAsyncHandler<T> fromJson(Class<T> type) {
        return new JsonMappingAsyncHandler<T>(type);
    }

    public JsonMappingAsyncHandler(Class<T> type)
    {
        this.type = type;
    }

    @Override
    public T onCompleted(Response response) throws Exception
    {
        try {
            return mapper.readValue(response.getResponseBody(), type);
        }
        catch (Exception e) {
            System.err.println(response.getResponseBody());
            throw e;
        }
    }
}
