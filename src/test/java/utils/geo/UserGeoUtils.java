package utils.geo;

import com.google.gson.JsonObject;
import com.adda52.logging.Logging;
import models.geo.Geo;
import com.adda52.http.client.RestApiClient;

import java.io.IOException;

/**
 * @author Dauli Sengar
 * @since 24th November 2022
 */
public class UserGeoUtils implements Logging {

    public Geo getUserGeoInfo(String url) throws IOException {
        JsonObject json = RestApiClient.getJsonResponseFromUrl(url);
        JsonObject data = json.getAsJsonObject("data");
        JsonObject userAddress = data.getAsJsonObject("userAddress");

        return new Geo(
                userAddress.get("city").getAsString(),
                userAddress.get("state").getAsString(),
                userAddress.get("country").getAsString(),
                data.get("isBlock").getAsString(),
                data.get("userState").getAsString()
        );

    }
}
