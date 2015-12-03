import java.util.*;
import org.json.simple.*;

/**
 * Responsible for all DicksonOne API JSON requests.
 */
public class Api
{
  private String apiToken;


  /**
   * @param email
   *  DicksonOne account email
   * @param password
   *  DicksonOn account password
   */
  public Api(String email, String password)
  {
    apiToken = getToken(email, password);
  }


  /**
   * @param device
   *  The Device that the returned Channel belongs to
   * @param channelId
   *  The id of the returned Channel
   * @return
   *  A Channel with id channelId and yesterday's DataPoints
   */
  public Channel getChannel(Device device, Long channelId)
  {
    Channel channel = new Channel(channelId);
    String resourcePath = "/devices/" + device.getToken() + "/channels/" + channelId;
    ApiRequest request = getRequest().setParams( getYesterdayParams() );
    JSONObject response = (JSONObject)request.asJson(ApiRequest.HttpMethod.GET, resourcePath);
    JSONArray dataPoints = (JSONArray)response.get("datapoints");

    for(Object obj : dataPoints) {
      JSONObject dataPoint = (JSONObject)obj;
      long at = ((Long)dataPoint.get("at")).longValue();
      
	try
	{
		double value = ((Double)dataPoint.get("value")).doubleValue();
		channel.addDataPoint(value, at);

	}
	catch (java.lang.NullPointerException e)
	{
		System.out.println(e.getMessage());
		continue;
	}

    }
    return channel;
  }


  /**
   * @return
   *  All known Devices for the user
   */

  public List<Device> getDevices()
  {
    List<Device> devices = new LinkedList<Device>();
    JSONArray response = (JSONArray)getRequest().asJson(ApiRequest.HttpMethod.GET, "/devices");

    for(Object obj : response) {
      JSONObject deviceJson = (JSONObject)obj;
      Device device = new Device( deviceJson.get("token").toString() );
      JSONArray channels = (JSONArray)deviceJson.get("channels");

      for(Object channel : channels) {
        JSONObject channelJson = (JSONObject)channel;
        Long channelId = (Long)channelJson.get("channel");
        device.addChannel( getChannel(device, channelId) );
      }
      devices.add(device);
    }

    return devices;
  }


  public List<Locations> getLocations(){
	  List<Locations> locationsList = new LinkedList<Locations>();
	  //JSONArray response = (JSONArray)getRequest().asJson(ApiRequest.HttpMethod.GET, "/locations");
	  JSONArray response = (JSONArray)getRequest().asJson(ApiRequest.HttpMethod.GET, "/locations");
	  for(Object obj : response){
		  JSONObject locationJson = (JSONObject)obj;
		  //Locations location = new Locations(locationJson.get("name").toString());
		  //JSONArray subLocations = (JSONArray) locationJson.get("locations");
		  //System.out.println(location.getName());
		  if(((Long)locationJson.get("num_locations")).intValue() > 0){
			  JSONArray subLocations = (JSONArray) locationJson.get("locations");
			  for(Object obj1 : subLocations) {
				  JSONObject locationJson1 = (JSONObject)obj1;
				  Locations location1 = new Locations(locationJson1.get("name").toString());
				  System.out.println(location1.getName());
				  locationsList.add(location1);
			  }
		  }

		  /*
		  try{
		  location.setId((java.lang.Long)locationJson.get("id"));
		  location.setNum_devices((Integer)locationJson.get("num_devices"));
		  location.setNum_locations((Integer)locationJson.get("num_locations"));
		  }
		  catch(Exception e){
			  System.out.println(++i);
			  continue;
		  }
		   */
		  //locationsList.add(location);
	  }
	return locationsList;
  }

  private ApiRequest getRequest() { return new ApiRequest(apiToken); }


  private String getToken(String email, String password)
  {
    LinkedHashMap<String,String> credentials = new LinkedHashMap<String,String>();
    JSONObject credentialsJson = new JSONObject();
    //HashMap<String,HashMap<String,String>> credentialsJson = new HashMap<String,HashMap<String,String>>();

    credentials.put("email", email);
    credentials.put("password", password);
    credentialsJson.put("credentials", credentials);

    ApiRequest request = getRequest().setJson( credentialsJson.toString() );
    JSONObject response = (JSONObject)request.asJson(ApiRequest.HttpMethod.POST, "/sessions");
    return (String)response.get("token");
  }


  private Map<String, String> getYesterdayParams()
  {
    Calendar calendar = GregorianCalendar.getInstance();
    calendar.add(Calendar.DAY_OF_MONTH, -1);
    calendar.set(Calendar.HOUR, 0);
    calendar.set(Calendar.MINUTE, 0);
    long startTime = calendar.getTimeInMillis();

    calendar.set(Calendar.HOUR, 23);
    calendar.set(Calendar.MINUTE, 59);
    long endTime = calendar.getTimeInMillis();

    Map<String, String> params = new Hashtable<String, String>();
    params.put("start_at", Long.toString(startTime));
    params.put("end_at", Long.toString(endTime));

    return params;
  }
}
