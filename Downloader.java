import java.io.PrintWriter;
import java.io.FileNotFoundException;


/**
 * Downloads all of yesterday's datapoints for all channels
 * on all known devices and writes them to a log file.
 */
public class Downloader
{
  private Api api;
  private String logFile;


  public Downloader(Api api, String logFile)
  {
    this.api = api;
    this.logFile = logFile;
  }


  public void start() throws FileNotFoundException
  {
	  
    PrintWriter log = new PrintWriter(logFile);
    log.print("Device ID,Channel ID,Value,Log Time\n");

    for(Device device : api.getDevices()) {
      String deviceId = device.getToken();

      for(Channel channel : device.getChannels()) {
        Long channelId = channel.getId();

        for(DataPoint dataPoint : channel.getDataPoints())
          log.print( deviceId + ',' + channelId + ',' + dataPoint.getValue() + ',' + dataPoint.getAt() + "\n" );
      }
    }
    /*
    PrintWriter testLog = new PrintWriter("csvdump.csv");
    testLog.print("Location,Location ID\n");
    for(Locations locations : api.getLocations()){
    	testLog.print(locations.getName() + "\n"); //+ ',' + locations.getId() + "\n");
    }
	*/
    //log.close();
    //testLog.close();
  }
}
