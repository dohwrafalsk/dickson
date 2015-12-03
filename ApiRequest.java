import java.util.Map;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONValue;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;


/**
 * Responsible for all DicksonOne RESTful API requests
 */
public class ApiRequest
{
  public enum HttpMethod { GET, POST, PUT }

  public static final String BASE_API_URL = "https://www.dicksonone.com/api/v2";

  private static final Client REST = new Client();


  private String json;
  private String apiToken;
  private Map<String, String> params;


  /**
   * @param token
   *  DicksonOne API authentication token
   */
  public ApiRequest(String token)
  {
    json = null;
    params = null;
    apiToken = token;
  }


  /**
   * @param json
   *  A JSON String to be sent with the request
   * @return
   *  this ApiRequest
   */
  public ApiRequest setJson(String json) { this.json = json; return this; }


  /**
   * @param params
   *  A Map of key/value pairs to be sent as query params with the request
   * @return
   *  this ApiRequest
   */
  public ApiRequest setParams(Map<String, String> params) { this.params = params; return this; }


  /**
   * Makes an HTTP request that accepts and sends only JSON
   * @param
   *  The RESTful HTTP method to use in the request
   * @param resourcePath
   *  A path, relative to BASE_API_URL, that points to the
   *  resource we are requesting
   */
  public Object asJson(HttpMethod method, String resourcePath)
  {
    WebResource resource = REST.resource(BASE_API_URL + resourcePath);

    if(params != null) {
      for(String key : params.keySet())
        resource.queryParam(key, params.get(key));
    }

    WebResource.Builder request = resource.accept(MediaType.APPLICATION_JSON_TYPE)
                                          .header("X-API-KEY", apiToken);

    if(json != null)
      request.entity(json, MediaType.APPLICATION_JSON_TYPE);

    return doRequest(method, request);
  }


  private Object doRequest(HttpMethod method, WebResource.Builder request)
  {
    String response = null;

    switch(method) {
      case PUT:
        response = request.put(String.class);
        break;
      case POST:
        response = request.post(String.class);
        break;
      default:
        response = request.get(String.class);
    }

    return JSONValue.parse(response);
  }
}
