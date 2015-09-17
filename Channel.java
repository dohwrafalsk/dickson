

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;




public class Channel {

    public Long id;
    List<DataPoint> dataPoints;

    public Channel(Long channel) {
        id = channel;
        dataPoints = new LinkedList<DataPoint>();
    }

    public Long getId() {
        return id;
    }

    public void addDataPoint(double value, long at) {
        dataPoints.add(new DataPoint(value, at));
    }

    public List<DataPoint> getDataPoints() {
        Collections.sort(dataPoints, new Comparator<DataPoint>() {
            @Override
            public int compare(DataPoint d1, DataPoint d2) {
                return Long.valueOf(d1.getAt()).compareTo(d2.getAt());
            }
        });

        return dataPoints;
    }
}
