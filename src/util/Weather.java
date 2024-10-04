package util;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Weather implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public UUID CS_UUID;
    public String stationID;
    public JSONObject weatherData;
    public ZonedDateTime timestamp;

    public Weather(UUID CS_UUID, JSONObject weatherData) {
        this.CS_UUID = CS_UUID;
        this.weatherData = weatherData;

        this.stationID = weatherData.get("id");
        this.timestamp = convertUTC(weatherData.get("local_date_time_full"), weatherData.get("time_zone"));
    }

    public ZonedDateTime convertUTC(String localDateTime, String timeZone) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        ZoneId zoneID;
        if (timeZone.equals("GMT")) {
            zoneID = ZoneId.of("GMT");
        } else {
            zoneID = ZoneId.systemDefault();
        }
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(localDateTime, formatter.withZone(zoneID));
        return zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"));
    }
}
