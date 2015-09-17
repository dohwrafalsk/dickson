public class DataPoint
{
  private long at;
  private double value;


  public DataPoint(double value, long at)
  {
    this.at = at;
    this.value = value;
  }


  public long getAt() { return at; }


  public double getValue() { return value; }
}
