package unit;

import com.adda52.controllers.ExecutionController;
import com.adda52.logging.Logging;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import models.geo.Geo;
import org.junit.Before;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.api.ApiCalls;
import utils.api.pojo.mystats_cash.MyStatsCashGameResponse;
import utils.db.InitDatabaseConnection;
import utils.db.MySqlCalls;
import utils.geo.UserGeoUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Map;

/**
 * @author Kishore
 */
public class ApiCallTest implements Logging {

    ApiCalls apiCalls = new ApiCalls();

    MySqlCalls sqlCalls = new MySqlCalls();

    @Before
    public void init(){new InitDatabaseConnection();}




    @Test
    public void tourneyStats() throws IOException, ParseException, URISyntaxException {




        Map<String, String> data = sqlCalls.getLastRedeemDetails("goro");
        String redeemId = data.get("id");
        try {
            String response = apiCalls.confiscateRedeemRequest(redeemId, "chip dumping", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhZG1pbklkIjoxNCwiYWRtaW5OYW1lIjoibW9oaXQiLCJpYXQiOjE3Mzg2NDQ0NjZ9.F4s2EcO4UlfQ_T1yXhz8P746NNXiYNaElQEcAY_-jH0");
            JsonObject responseObject = JsonParser.parseString(response).getAsJsonObject();
            String responseCode = responseObject.get("responseCode").toString();
            if (responseCode.equalsIgnoreCase("\"201\"")) {
                getLogger().info("Redeem confiscated.");
            } else {
                Assert.fail("Unable to confiscate redeem status." + " Response code: " + responseCode);
            }
        } catch (Exception e) {
            Assert.fail("Unable to confiscate redeem request", e);
        }


    }

    @Test
    public void cashGameStats() throws IOException, ParseException, URISyntaxException {
        String cashResponse = new ApiCalls().getCashGameStats("ALL", "All Time", "rayden", "3f22fa8d9703f79c7ddae1bb1748c613deb1422d7cad5004235e15b188d952c7");


        JsonObject resObject = new Gson().fromJson(cashResponse, JsonObject.class);
        String responseData = resObject.get("response").getAsString();
        System.out.println(responseData);

        ObjectMapper mapper = new ObjectMapper();

        MyStatsCashGameResponse cashTableResponse = mapper.readValue(responseData, MyStatsCashGameResponse.class);

        System.out.println(cashTableResponse.getRespData().getRingDataBean().getGvHandDealtData().getHoldem().getHandDelt());
        System.out.println(cashTableResponse.getRespData().getRingDataBean().getGvHandDealtData().getPlo().getHandDelt());
        System.out.println(cashTableResponse.getRespData().getRingDataBean().getGvHandDealtData().getPlo5().getHandDelt());
        System.out.println(cashTableResponse.getRespData().getRingDataBean().getGvHandDealtData().getPlo6().getHandDelt());


    }

    @Test
    public void getPromotionData() throws IOException, URISyntaxException {
        new ApiCalls().getAdminToken("mohit", "ca978112ca1bbdcafac231b39a23dc4da786eff8147c4e72b9807785afee48bb");
    }


    @Test
    public void geo() throws IOException {
        Geo geo = new UserGeoUtils().getUserGeoInfo("https://api.adda52poker.com/api/v1/website/geoIp/block");
        System.out.println(geo.city());
        System.out.println(geo.country());
        System.out.println(geo.state());
    }

    @Test
    public void count() throws IOException, URISyntaxException {
        String response = new ApiCalls().getLiveUserCount("mohit", "ca978112ca1bbdcafac231b39a23dc4da786eff8147c4e72b9807785afee48bb");

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode jsonNode = objectMapper.readTree(response);
        String innerResponse = jsonNode.get("response").asText();
        System.out.println(innerResponse);
        JsonNode innerJsonNode = objectMapper.readTree(innerResponse);
        JsonNode data = innerJsonNode.path("data").get(0).path("data");

        String tc = data.path("tableCount").toString();
        String pc = data.path("playersCount").toString();

        System.out.println(pc);
        System.out.println(tc);


    }

    @Test
    public void blockUser() throws IOException, URISyntaxException {
        new InitDatabaseConnection();
        MySqlCalls calls = new MySqlCalls();
        ApiCalls apiCalls = new ApiCalls();
        String token = apiCalls.getAdminToken(Constants.getAdminUser(), Constants.getAdminPass());
        String userId = calls.getUserID("baka");
        String response = apiCalls.blockUser(userId, "block", "3", "bakabon", "Chip Dumping", token);
        getLogger().error("response : " + response);
        JsonElement outerResponse = JsonParser.parseString(response).getAsJsonObject().get("response");
        JsonObject innerResponse = JsonParser.parseString(outerResponse.getAsString()).getAsJsonObject();
        JsonElement message = innerResponse.get("message");
        getLogger().error("message : " + message);
        if (message.toString().equalsIgnoreCase("\"User account Blocked request send for approval successfully\"")) {
            ExecutionController.pauseExecution(2);
            String blockRef = calls.getBlockReferenceId("baka");
            String approvalResponse = apiCalls.approveBlockUser("3", userId, blockRef, "approve", "Chip dumpoing", token);
            getLogger().info(approvalResponse);
            JsonElement outerApprovalResponse = JsonParser.parseString(approvalResponse).getAsJsonObject().get("response");
            JsonObject innerApprovalResponse = JsonParser.parseString(outerApprovalResponse.getAsString()).getAsJsonObject();
            JsonElement approvalMessage = innerApprovalResponse.get("message");
            if(approvalMessage.toString().equalsIgnoreCase("\"Success\"")){
                getLogger().info("User blocked successfully");
            }
        }
    }

    @Test
    public void approveBlockUser() throws IOException {
        new InitDatabaseConnection();
        MySqlCalls calls = new MySqlCalls();
        ApiCalls apiCalls = new ApiCalls();
        String token = apiCalls.getAdminToken(Constants.getAdminUser(), Constants.getAdminPass());
        String userId = calls.getUserID("baka");
        String blockRef = calls.getBlockReferenceId("baka");
        String response = apiCalls.approveBlockUser("3", userId, blockRef, "approve", "Chip dumpoing", token);
        getLogger().error(response);
    }


    @Test
    public void refTest() {
        String url = "https://api.adda52poker.com/api/v1/website/geoIp/block";
        Geo geo = null;
        try {
            geo = new UserGeoUtils().getUserGeoInfo(url);
        } catch (Exception e) {
            getLogger().error("Unable to get geo location");
        }
        if (!(geo == null)) {
            System.out.println(geo.city());
            System.out.println(geo.country());
            System.out.println(geo.state());
        }

    }

    @Test
    public void token() throws IOException {
        String token = apiCalls.getAdminToken(Constants.getAdminUser(),Constants.getAdminPass());
        System.out.println(token);
    }


}
