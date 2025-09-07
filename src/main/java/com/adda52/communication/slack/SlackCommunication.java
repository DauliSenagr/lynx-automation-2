package com.adda52.communication.slack;

import com.adda52.logging.Logging;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import com.google.gson.JsonObject;
import java.io.IOException;


public class SlackCommunication implements Logging {

        public JsonObject sendMessage(String url, String channel, String message, String token) throws IOException {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost request = new HttpPost(url);

            // Set headers
            request.addHeader("Authorization", "Bearer " + token);
            // Build multipart entity
            MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                    .addTextBody("channel", channel)
                    .addTextBody("text", message);

            // Set request entity
            request.setEntity(builder.build());

            // Execute request and process response
            try (CloseableHttpResponse httpResponse = httpClient.execute(request)) {
                HttpEntity httpEntity = httpResponse.getEntity();
                String responseBody = EntityUtils.toString(httpEntity);

                JsonObject response = new JsonObject();
                response.addProperty("responseCode", String.valueOf(httpResponse.getStatusLine().getStatusCode()));
                response.addProperty("response", responseBody);
                return response;
            } finally {
                httpClient.close();
            }
        }

}
