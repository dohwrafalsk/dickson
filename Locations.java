import java.util.LinkedList;
import java.util.List;

public class Locations {

	private long id;
	private String name;
	int num_locations;
	int num_devices;


	public Locations(String name)
	{
		this.name = name;
	}

	public long getId() { return id; }
	
	public void setId(long id) { this.id = id; }
	
	public String getName(){ return name; }
	
	public void setName(String name){ this.name = name; }
	
	public int getNum_locations() { return num_locations; }
	
	public void setNum_locations(int numloc) { this.num_locations = numloc; }
	
	public int getNum_devices() { return num_locations; }
	
	public void setNum_devices(int numdev) { this.num_devices = numdev; }

	/*
    private List<Channel> channels;

    private class locations{
	  int id;
    }
    
	*/
	/*
	public List<Channel> getChannels() { return channels; }
	
	public void add(Channel channel)
	{
		channels.add(channel);
	}
	*/
}
