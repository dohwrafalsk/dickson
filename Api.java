
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;



/**
 * Responsible for all DicksonOne API JSON requests.
 */
public class Api {

    private final String apiToken;
    private String tempUnits;
    private String unitName;

    /**
     * @param email DicksonOne account email
     * @param password DicksonOn account password
     */
    public Api(String email, String password) {
        apiToken = getToken(email, password);
        System.out.println("apiToken:" + apiToken);
    }

    /**
     * @param device The Device that the returned Channel belongs to
     * @param channelId The id of the returned Channel
     * @return A Channel with id channelId and yesterday's DataPoints
     */
    public Channel getChannel(Device device, Long channelId) {

        Channel channel = new Channel(channelId);
        String resourcePath = "/devices/" + device.getToken() + "/channels/" + channelId;
        ApiRequest request = getRequest().setParams(getYesterdayParams());
        JSONObject response = (JSONObject) request.asJson(ApiRequest.HttpMethod.GET, resourcePath);
        JSONArray dataPoints = (JSONArray) response.get("datapoints");

        for (Object obj : dataPoints) {
            JSONObject dataPoint = (JSONObject) obj;
            long at = ((Long) dataPoint.get("at")).longValue();

            try {
                channel.addDataPoint(((Double) dataPoint.get("value")).doubleValue(), at);
            } catch (NullPointerException npe) {
                System.out.println("Possible probe disconnected");
            }

        }

        return channel;
    }

    public String getTempUnit(String device, Long channelId) {
        String resourcePath = "/devices/" + device + "/channels/" + channelId;
        ApiRequest request = getRequest().setParams(getYesterdayParams());
        JSONObject response = (JSONObject) request.asJson(ApiRequest.HttpMethod.GET, resourcePath);

        tempUnits = (String) response.get("channel_unit");

        return tempUnits;
    }

    public String getUnitName(String device, Long channelId) {
        String resourcePath = "/devices/" + device + "/channels/" + channelId;
        ApiRequest request = getRequest().setParams(getYesterdayParams());
        JSONObject response = (JSONObject) request.asJson(ApiRequest.HttpMethod.GET, resourcePath);

        unitName = (String) response.get("name");

        return unitName;
    }

    public String getTemperatureUnit() {
        return tempUnits;
    }

    public String getDeviceName(String device) {
        String resourcePath = "/devices/" + device;
        ApiRequest request = getRequest().setParams(getYesterdayParams());
        JSONObject response = (JSONObject) request.asJson(ApiRequest.HttpMethod.GET, resourcePath);

        String deviceName = (String) response.get("name");

        return deviceName;
    }

    public String getSerialNumber(String device) {
        String resourcePath = "/devices/" + device;
        ApiRequest request = getRequest().setParams(getYesterdayParams());
        JSONObject response = (JSONObject) request.asJson(ApiRequest.HttpMethod.GET, resourcePath);

        String serialNum = (String) response.get("serial_number");

        return serialNum;
    }

    /**
     * @return All known Devices for the user
     */
    public List<Device> getDevices() {
        List<Device> devices = new LinkedList<Device>();
        JSONArray response = (JSONArray) getRequest().asJson(ApiRequest.HttpMethod.GET, "/devices");

        for (Object obj : response) {
            JSONObject deviceJson = (JSONObject) obj;
            Device device = new Device((deviceJson.get("token").toString()));
            JSONArray channels = (JSONArray) deviceJson.get("channels");

            for (Object channel : channels) {
                JSONObject channelJson = (JSONObject) channel;
                Long channelId = (Long) channelJson.get("channel");
                device.addChannel(getChannel(device, channelId));
            }

            devices.add(device);
        }

        return devices;
    }

    private ApiRequest getRequest() {
        System.out.println("apiToken:" + apiToken);
        return new ApiRequest(apiToken);
    } // Step 8: Returns apiToken during Step 7

    private String getToken(String email, String password) {
//Step 6: Attaining token for given email and password

        JSONObject credentials = new JSONObject();
        JSONObject credentialsJson = new JSONObject();

        credentials.put("email", email);
        credentials.put("password", password);
        credentialsJson.put("credentials", credentials);

        ApiRequest request = getRequest().setJson(credentialsJson.toString());//Step 7,9: Calls the getRequest() above returning the token to a new ApiRequest object
        JSONObject response = (JSONObject) request.asJson(ApiRequest.HttpMethod.POST, "/sessions"); //Step 10: 
        System.out.println("token: " + response.get("token"));
        return (String) response.get("token");
        
    }

    private Map<String, String> getYesterdayParams() {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        long startTime = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR, 23);
        calendar.set(Calendar.MINUTE, 59);
        long endTime = calendar.getTimeInMillis();

        Map<String, String> params;
        params = new Hashtable<String, String>();
        params.put("start_at", Long.toString(startTime));
        params.put("end_at", Long.toString(endTime));

        return params;
    }
}
