package utils.support;

import com.adda52.logging.Logging;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import pages.ionic_native.login.IonicNativeLoginPage;
import utils.api.ApiCalls;

public class ExecutionSupport implements Logging {
    private static boolean isUpdateAvailable = false;


    public static void handleUpdateAvailable() {
        IonicNativeLoginPage loginPage = new IonicNativeLoginPage();
        if (isUpdateAvailable) {
            loginPage.waitForUpdateAvailableDrawerToAppear();
            loginPage.clickOnLaterButton();
        }
    }

    public void checkForAppUpdate() {
        String input = Constants.getBuildName();
        String version = input.split("\\|")[0].trim();
        String responseString = null;

        try {
            responseString = new ApiCalls().getVersionUpdateInfo(Constants.getIonicAndroidClientName(), version);
        } catch (Exception e) {
            getLogger().error("Failed to get version update info", e);
        }

        try {
            JsonObject jsonResponse = JsonParser.parseString(responseString).getAsJsonObject();
            String innerResponse = jsonResponse.get("response").getAsString();
            JsonObject data = JsonParser.parseString(innerResponse).getAsJsonObject().get("data").getAsJsonObject();

            String newUpdate = data.get("isNewUpdate").getAsString();
            String mandatoryUpdate = data.get("isMandatory").getAsString();

            if (newUpdate.equals("true") && mandatoryUpdate.equals("false")) {
                isUpdateAvailable = true;
            } else if (newUpdate.equals("true") && mandatoryUpdate.equals("true")) {
                isUpdateAvailable = true;
            }
        } catch (Exception e) {
            getLogger().error("Unable to get version update info", e);
        }
    }
}
