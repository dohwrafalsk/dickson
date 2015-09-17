


import java.util.LinkedList;
import java.util.List;



public class Device
{
  private final String token;
  private final List<Channel> channels;


  public Device(String token)
  {
    this.token = token;
    channels = new LinkedList<>();
  }


  public String getToken() { 
      return token; 
  }


  public List<Channel> getChannels() {
      return channels; 
  }


  public void addChannel(Channel channel)
  {
    channels.add(channel);
  }
}
