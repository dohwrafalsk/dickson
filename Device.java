import java.util.List;
import java.util.LinkedList;


public class Device
{
  private String token;
  private List<Channel> channels;


  public Device(String token)
  {
    this.token = token;
    channels = new LinkedList<Channel>();
  }


  public String getToken() { return token; }


  public List<Channel> getChannels() { return channels; }


  public void addChannel(Channel channel)
  {
    channels.add(channel);
  }
}
