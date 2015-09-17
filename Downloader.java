
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;
import java.text.*;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * Downloads all of yesterday's datapoints for all channels on all known devices
 * and writes them to a log file.
 */
public class Downloader {

    private final Api api;
    private final String logFile;
    static String facCode;
    static double fTemp;
    static double cTemp;

    static TreeSet<String> deviceIdentifiers;
    static TreeMap<String, String> deviceInfo;

    public Downloader(Api api, String logFile) {
        this.api = api;
        this.logFile = logFile;

        deviceInfo = new TreeMap<>();
    }

    public static void readDeviceIdProperties() throws Exception {
        Properties props = new Properties();
        InputStream is;

        File f = new File("deviceId.properties");
        is = new FileInputStream(f);

        props.load(is);

        //deviceIdentifiers = new TreeSet(props.keySet());
        //Iterator facNameIter = deviceIdentifiers.iterator();
        Iterator facNameIter = props.keySet().iterator();

        while (facNameIter.hasNext()) {
            String deviceCode = (String) facNameIter.next();
            System.out.println("The deviceCode is " + deviceCode + " and the facility code is " + props.getProperty(deviceCode));
            deviceInfo.put(deviceCode, props.getProperty(deviceCode));
        }

    }

    public static void sendAlert(String facilityCode) throws Exception {

        String jdbcURL = "jdbc:oracle:thin:@cir-db:1521:doh50";

        String user = "wrafalski";
        String passwd = "kingdom13";

        Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
        Connection conn = DriverManager.getConnection(jdbcURL, user, passwd);

        Properties emailSettings = new Properties();

        emailSettings.setProperty("mail.smtp.host", "app22csmtp");

        Session session = Session.getDefaultInstance(emailSettings, null);
        MimeMessage message = new MimeMessage(session);

        message.setFrom(new InternetAddress("ttaylor2@health.nyc.gov"));

       // TODO: //message.addRecipient(Message.RecipientType.TO, new InternetAddress("mgolende@health.nyc.gov"));
        message.setSubject("ALERT: A new data logger has been installed, update Probe Table! ");
        BodyPart messageBodyPart = new MimeBodyPart();

        String emailContents = "ALERT: A new data logger has been installed at facility " + facilityCode + " but it's information has not be added to the Probe Table in the database.\nPlease update the Probe Table within the CIR database with the probe names and their corresponding storage units.";
        messageBodyPart.setContent(emailContents, "text/html; charset=utf-8");

       //TODO: // Transport.send(message);
        //conn.close();
    }

    public static ResultSet findProbe(String probe) throws ClassNotFoundException, SQLException {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        String URL = "jdbc:oracle:thin:@10.135.71.134:1521:doh50t";
        Connection conn = DriverManager.getConnection(URL, "ttaylor2", "Joshua10475!");

        Statement stmt = conn.createStatement();

        ResultSet rs = stmt.executeQuery("SELECT storage_unit FROM DOH.vfc_datalogger WHERE probe ='" + probe + "'");

        //conn.close();
        return rs;
    }

    public static String findFacilityCode(String probe) throws ClassNotFoundException, SQLException {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        String URL = "jdbc:oracle:thin:@10.135.71.134:1521:doh50t";
        Connection conn = DriverManager.getConnection(URL, "ttaylor2", "Joshua10475!");

        Statement stmt = conn.createStatement();

        ResultSet rs = stmt.executeQuery("SELECT facility_code FROM DOH.vfc_datalogger WHERE probe ='" + probe + "'");

        rs.next();

        String facilityCode = rs.getNString("facility_code");

        //conn.close();
        return facilityCode;
    }

    public static void writeTemperatureData(String insertsql) throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        String URL = "jdbc:oracle:thin:@10.135.71.134:1521:doh50t";
        Connection conn = DriverManager.getConnection(URL, "ttaylor2", "Joshua10475!");

        Statement stmt = conn.createStatement();

        stmt.execute(insertsql);
        stmt.execute("commit");

        //conn.close();
    }

    public static void tempConvert(double inputTemp, String unit) {
        if (unit.equals("f")) {
            fTemp = inputTemp;
            cTemp = (float) ((inputTemp - 32.0) * (5.0 / 9.0));
        } else {
            fTemp = (float) (inputTemp * (9.0 / 5.0) + 32.0);
            cTemp = inputTemp;
        }
    }

    public void start() throws Exception //Was FileNotFoundException
    {
        String temperatureUnit;
        String sensorName = null;

        //readDeviceIdProperties();
        DecimalFormat df = new DecimalFormat("###.#");
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        PrintWriter log = new PrintWriter(logFile);
        log.print("Device ID,Facility Code,Channel Number,Storage Unit,Value Celsius,Value Fahrenheit,Temp Unit,Log Time,Model,Probe Name,\n");

        for (Device device : api.getDevices()) {
            String deviceId = device.getToken();
            String serialNum = api.getSerialNumber(deviceId);

            for (Channel channel : device.getChannels()) {
                Long channelId = channel.getId();
                String deviceName = api.getDeviceName(deviceId) + "_" + channel.getId();
                temperatureUnit = api.getTempUnit(deviceId, channelId);

                //System.out.println("The deviceName is : " + deviceName);
                ResultSet rs = (findProbe(deviceName));

                if (rs.next()) {
                    if (rs.getNString("storage_unit").equals("Freezer")) {
                        sensorName = "Freezer";
                    } else if (rs.getNString("storage_unit").equals("Refrigerator")) {
                        sensorName = "Refrigerator";
                    } else if (rs.getNString("storage_unit").equals("NULL")) {
                        continue;
                    }
                } else if (!rs.next()) {
                    sendAlert(findFacilityCode(deviceName));
                    continue;
                }

                for (DataPoint dataPoint : channel.getDataPoints()) {
                    java.util.Date time = new java.util.Date((dataPoint.getAt()));

                    tempConvert(dataPoint.getValue(), temperatureUnit);

                    log.print(deviceId + ',' + findFacilityCode(deviceName) + ',' + channelId + ',' + sensorName + ',' + df.format(cTemp) + ',' + df.format(fTemp) + ',' + temperatureUnit + ',' + sdf.format(time) + ',' + "Dickson" + ',' + deviceName + ',' + "\n");

                    String insertsql = "INSERT INTO doh.vfc_temperature (facility_code, reading_datetime, refrigerator_name, sensor_name, temperature_f, temperature_c, device_type) VALUES ('" + findFacilityCode(deviceName) + "', to_date('" + sdf.format(time) + "', 'dd-mm-yyyy hh24:mi:ss'), '" + deviceName + "', '" + sensorName + "', " + df.format(fTemp) + ", " + df.format(cTemp) + ", '" + "Dickson" + "')";
                    writeTemperatureData(insertsql);
                    //Create SQL Insert Statement here instead of log file and add connection to server
                }
            }
        }

        log.close();
    }
}
