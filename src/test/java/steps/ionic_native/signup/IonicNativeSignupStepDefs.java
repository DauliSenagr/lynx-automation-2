package steps.ionic_native.signup;

import com.adda52.context.scenario.ScenarioContext;
import com.adda52.controllers.ExecutionController;
import com.adda52.logging.Logging;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import models.geo.Geo;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;
import pages.ionic_native.login.IonicNativeLoginPage;
import pages.ionic_native.main_footer.IonicNativeMainFooterPage;
import pages.ionic_native.signup.IonicNativeSignupPage;
import utils.db.MySqlCalls;
import utils.geo.UserGeoUtils;
import utils.keys.CpSource;
import utils.keys.KeyReferences;
import utils.support.SupportUtils;
import utils.users.UserManager;

import java.util.Map;

/**
 * @author Kishore
 */
public class IonicNativeSignupStepDefs implements Logging {

    IonicNativeLoginPage loginPage;
    IonicNativeMainFooterPage footerPage;

    IonicNativeSignupPage signupPage;

    SoftAssert softAssert;

    MySqlCalls sqlCalls;

    private String mobile = "";

    private String newUserName = "";

    private String referral_code = "";

    public IonicNativeSignupStepDefs() {
        signupPage = new IonicNativeSignupPage();
        loginPage = new IonicNativeLoginPage();
        footerPage = new IonicNativeMainFooterPage();
        sqlCalls = new MySqlCalls();
        softAssert = new SoftAssert();
    }

    @When("the user inputs a valid mobile number into the Enter mobile number field on the Login or Register screen of the Ionic native app.")
    public void theUserInputsAValidMobileNumberIntoTheEnterMobileNumberFieldOnTheLoginOrRegisterScreenOfTheIonicNativeApp() {
        loginPage.waitForMobileNumberFieldToAppear();
        ExecutionController.pauseExecution(2);
        loginPage.clickOnMobileNumberField();
        mobile = SupportUtils.generateRandomMobileNumber();
        loginPage.enterMobileNumber(mobile);
    }

    @Then("the OTP verification drawer should appear on the Ionic native app.")
    public void theOTPVerificationDrawerShouldAppearOnTheIonicNativeApp() {
        loginPage.waitForOtpVerificationDrawerToAppear();
    }

    @And("user taps on the T&Cs checkbox on the Login or Register screen of the Ionic native app.")
    public void userTapsOnTheTCsCheckboxOnTheLoginOrRegisterScreenOfTheIonicNativeApp() {
        signupPage.clickOnCheckbox();
    }

    @Then("the get OTP button gets disabled on the Login or Register screen of the Ionic native app.")
    public void theGetOTPButtonGetsDisabledOnTheLoginOrRegisterScreenOfTheIonicNativeApp() {
        loginPage.waitForGetOtpButtonToAppear();
        ExecutionController.pauseExecution(2);
        softAssert.assertEquals(signupPage.getCheckboxStatus(), "false", "Checkbox is marked");
        softAssert.assertEquals(signupPage.isGetOtpButtonDisabled(), "true", "Get OTP button is enabled");
        softAssert.assertAll();
    }

    @Then("the get OTP button gets enabled on the Login or Register screen of the Ionic native app.")
    public void theGetOTPButtonGetsEnabledOnTheLoginOrRegisterScreenOfTheIonicNativeApp() {
        loginPage.waitForGetOtpButtonToAppear();
        softAssert.assertEquals(signupPage.getCheckboxStatus(), "true", "Checkbox is unmarked");
        softAssert.assertTrue(signupPage.isGetOtpButtonEnabled(), "Get OTP button is disabled");
        softAssert.assertAll();
    }

    @Then("the OTP verification drawer disappears from the Login or Register screen on the Ionic native app and user can change the mobile number")
    public void theOTPVerificationDrawerDisappearsFromTheLoginOrRegisterScreenOnTheIonicNativeAppAndUserCanChangeTheMobileNumber() {
        loginPage.waitForVerificationDrawerToDisappear();
        ExecutionController.pauseExecution(1);
        loginPage.waitForLoginViaPasswordLinkToAppear();

    }

    @Then("the user should be registered successfully and logged in to the Ionic native app.")
    public void theUserShouldBeRegisteredSuccessfullyAndLoggedInToTheIonicNativeApp() {
        footerPage.waitForCashierMenuLabelToAppear();
        newUserName = new MySqlCalls().getUsername(mobile);
        ScenarioContext.setScenarioData("newUser", newUserName);
    }

    @And("the user taps the {string} link on the Login or Register screen of the Ionic native app")
    public void theUserTapsThePromoReferralCodeLinkOnTheLoginOrRegisterScreenOfTheIonicNativeApp(String label) {
        if (label.equalsIgnoreCase(signupPage.getPromoCodeLinkLabel())) {
            signupPage.clickOnPromoOrReferralCodeLink();
        }
    }


    @And("the user inputs the valid {string} on the Login or Register screen of the Ionic native app")
    public void theUserInputsTheValidOnTheLoginOrRegisterScreenOfTheIonicNativeApp(String code) {
        signupPage.clickOnPromoCodeTextField();
        signupPage.enterPromoCode(code);

    }

    @Then("the Apply button gets enabled on the Login or Register screen of the Ionic native app")
    public void theApplyButtonGetsEnabledOnTheLoginOrRegisterScreenOfTheIonicNativeApp() {
        Assert.assertTrue(signupPage.isApplyButtonEnabled(), "Apply button is not enabled");
    }

    @When("the user taps the Apply button on the Login or Register screen of the Ionic native app")
    public void theUserTapsTheApplyButtonOnTheLoginOrRegisterScreenOfTheIonicNativeApp() {
        signupPage.clickOnApplyButton();
    }

    @Then("the {string} toast message appears on the Login or Register screen of the Ionic native app")
    public void theToastMessageAppearsOnTheLoginOrRegisterScreenOfTheIonicNativeApp(String message) {
        ExecutionController.pauseExecution(1);
        Assert.assertEquals(message, signupPage.getToastMessage().trim(), "Toast message is not correct");

    }

    @Then("the {string} drawer should appear on the Login or Register screen of the Ionic native app")
    public void theDrawerShouldAppearOnTheLoginOrRegisterScreenOfTheIonicNativeApp(String drawer) {
        ExecutionController.pauseExecution(2);
        if (drawer.equalsIgnoreCase(signupPage.getDrawerLabel())) {
            signupPage.waitForPromoCodeDrawerToAppear();
        }

    }

    @When("user input an invalid OTP {int} times into the OTP field on the Ionic native app.")
    public void userInputAnInvalidOTPTimesIntoTheOTPFieldOnTheIonicNativeApp(int count) {
        for (int i = 0; i <= count; i++) {
            ExecutionController.pauseExecution(2);
            loginPage.enterOtp("000000");
            ExecutionController.pauseExecution(2);
        }


    }

    @Then("the user is unable to register and receives the alert {string} on the Ionic native app")
    public void theUserIsUnableToRegisterAndReceivesTheAlertOnTheIonicNativeApp(String message) {
        ExecutionController.pauseExecution(1);
        switch (message) {
            case "Please enter valid OTP. 4 attempts remaining", "Please enter valid OTP. 1 attempts remaining", "Please enter valid OTP. 2 attempts remaining", "Please enter valid OTP. 3 attempts remaining" ->
                    softAssert.assertEquals(message, signupPage.getSignUpOtpErrorMessage().trim(), "Error message is not correct");
            case "Limit reached for maximum resend attempts." ->
                    softAssert.assertEquals(message, signupPage.getMaximumResendAttemptErrorMessage().trim(), "Error message is not correct");
            case "OTP attempt limit has reached." ->
                    softAssert.assertEquals(signupPage.getOtpAttemptErrorMessage(), message, "Error message is not correct");
        }
        softAssert.assertAll();

    }

    @And("verify that the data for the new user singed up from Ionic native app in cardplay.cp_source is correct for all columns")
    public void verifyThatTheDataForTheNewUserSingedUpFromIonicNativeAppInCardplayCp_sourceIsCorrectForAllColumns() {
        Map<String, String> sourceData = new MySqlCalls().getCpSourceData(newUserName);
        String userId = new MySqlCalls().getUserID(newUserName);

        softAssert.assertNotEquals(sourceData.get(CpSource.SOURCE_ID.getColumnName()), "", "Data in source_id column is not correct");
        if(sourceData.get(CpSource.NAME.getColumnName()).equalsIgnoreCase("")) {
            softAssert.assertEquals(sourceData.get(CpSource.NAME.getColumnName()), "", "Data in name column is not correct");
        }else {
            softAssert.assertEquals(sourceData.get(CpSource.NAME.getColumnName()),"RAF","Data in name column is not correct");
        }
        softAssert.assertEquals(sourceData.get(CpSource.URL_1.getColumnName()), "https://www.adda52.com/", "Data in url_1 column is not correct");
        softAssert.assertEquals(sourceData.get(CpSource.URL_2.getColumnName()), Constants.getWebUrl(), "Data in url_2 column is not correct");
        softAssert.assertEquals(sourceData.get(CpSource.REFERRAL_URL.getColumnName()), "", "Data in referral_url column is not correct");
        softAssert.assertEquals(sourceData.get(CpSource.DOMAIN_KEY.getColumnName()), "", "Data in domain_key column is not correct");
        softAssert.assertEquals(sourceData.get(CpSource.UTM_SOURCE.getColumnName()), "", "Data in utm_source column is not correct");
        softAssert.assertEquals(sourceData.get(CpSource.UTM_CAMPAIGN.getColumnName()), "", "Data in utm_campaign column is not correct");
        softAssert.assertEquals(sourceData.get(CpSource.UTM_CONTENT.getColumnName()), null, "Data in utm_content column is not correct");
        softAssert.assertEquals(sourceData.get(CpSource.UTM_KEYWORD.getColumnName()), "", "Data in utm_keyword column is not correct");
        softAssert.assertEquals(sourceData.get(CpSource.UTM_MEDIUM.getColumnName()), "", "Data in utm_medium column is not correct");
        softAssert.assertEquals(sourceData.get(CpSource.UTM_TERM.getColumnName()), null, "Data in utm_term column is not correct");
        softAssert.assertEquals(sourceData.get(CpSource.USER_ID.getColumnName()), userId, "Data in user_id column is not correct");
        softAssert.assertEquals(sourceData.get(CpSource.SPECIAL_TAG.getColumnName()), "", "Data in special_tag column is not correct");
        softAssert.assertEquals(sourceData.get(CpSource.CLIENT_NAME.getColumnName()), "poker2android", "Data in client_name column is not correct");
        softAssert.assertNotEquals(sourceData.get(CpSource.ADDED_ON.getColumnName()), "", "Data in added_on column is not correct");
        softAssert.assertNotEquals(sourceData.get(CpSource.LINUX_ADDED_ON.getColumnName()), "", "Data in linux_added_on column is not correct");
        softAssert.assertNotEquals(sourceData.get(CpSource.REGISTER_FLYER_ID.getColumnName()), "", "Data in register_flyer_id column is not correct");
        softAssert.assertEquals(sourceData.get(CpSource.FTD_CONVERT_SOURCE.getColumnName()), null, "Data in ftd_convert_source column is not correct");
        softAssert.assertEquals(sourceData.get(CpSource.SESSION_ID.getColumnName()), "", "Data in session_id column is not correct");

        String url = Constants.getApiV1Url() + "/api/v1/website/geoIp/block";
        Geo geo = null;
        try {
            geo = new UserGeoUtils().getUserGeoInfo(url);
        } catch (Exception e) {
            getLogger().error("Unable to get geo location");
        }
        if (!(geo == null)) {
            softAssert.assertEquals(sourceData.get(CpSource.CITY.getColumnName()), geo.city(), "City name is not correct");
            softAssert.assertEquals(sourceData.get(CpSource.STATE.getColumnName()), geo.state(), "State name is not correct");
            softAssert.assertEquals(sourceData.get(CpSource.COUNTRY.getColumnName()), geo.country(), "Country name is not correct");
        } else {
            softAssert.fail("Unable to get the user geo location. Cannot verify location in cp_source table");
        }
        softAssert.assertAll();
    }


    @When("user inputs a valid OTP into the OTP field on the Ionic native app.")
    public void userInputsAValidOTPIntoTheOTPFieldOnTheIonicNativeApp() {
        ExecutionController.pauseExecution(2);
        loginPage.waitForOtpInputFieldToAppear();
        loginPage.enterOtp(sqlCalls.getSignUpOtp(mobile));
    }

    @When("user input an invalid OTP for {string} time into the OTP field on the Ionic native app.")
    public void userInputAnInvalidOTPForTimeIntoTheOTPFieldOnTheIonicNativeApp(String attempts) {
        ExecutionController.pauseExecution(2);
        switch (attempts) {
            case "1st", "2nd", "3rd", "4th", "5th" -> loginPage.enterOtp("010203");
        }

    }

    @Then("the {string} and {string} error message should be displayed on the Login or Register screen of the Ionic native app")
    public void theAndErrorMessageShouldBeDisplayedOnTheLoginOrRegisterScreenOfTheIonicNativeApp(String alert1, String alert2) {
        ExecutionController.pauseExecution(2);
        softAssert.assertEquals(signupPage.getInvalidCodeAlert(), alert1, "Alert message is not correct");
        softAssert.assertEquals(signupPage.getEnterValidCodeAlert(), alert2, "Alert message is not correct");
        softAssert.assertAll();
    }

    @And("the applied {string} is displayed on the Login or Register screen of the Ionic native app")
    public void theAppliedIsDisplayedOnTheLoginOrRegisterScreenOfTheIonicNativeApp(String code) {
        ExecutionController.pauseExecution(1);
        Assert.assertEquals(signupPage.getAppliedPromoCode(), code, "Applied Promo/Referral code is not correct");
    }


    @And("the user taps on the Resend OTP Button on the Ionic native app")
    public void theUserTapsOnTheResendOTPButtonOnTheIonicNativeApp() {
        loginPage.waitForResendOtpEnabledButtonToAppear();
        loginPage.clickOnResendOtpButton();

    }

    @Then("the {string} message is displayed on the Ionic native app")
    public void theMessageIsDisplayedOnTheIonicNativeApp(String message) {
        ExecutionController.pauseExecution(1);
        Assert.assertEquals(signupPage.getResentOtpMessage(), message, "Message is not correct");
    }

    @And("the user inputs the valid referralCode on the Login or Register screen of the Ionic native app")
    public void theUserInputsTheValidReferralCodeOnTheLoginOrRegisterScreenOfTheIonicNativeApp() {
        signupPage.clickOnPromoCodeTextField();
        signupPage.enterPromoCode(sqlCalls.getReferralCode(UserManager.getUser().getUsername()));

    }

    @And("the applied referralCode is displayed on the Login or Register screen of the Ionic native app")
    public void theAppliedReferralCodeIsDisplayedOnTheLoginOrRegisterScreenOfTheIonicNativeApp() {
        ExecutionController.pauseExecution(1);
        referral_code = sqlCalls.getReferralCode(UserManager.getUser().getUsername());
        Assert.assertTrue(signupPage.getAppliedPromoCode().equalsIgnoreCase(referral_code), "Applied Referral code is not correct");
    }

    @And("verify that the data for new user signed up with referral code in cardplay_poker.cp_raf_referee_details.")
    public void verifyThatTheDataForNewUserSignedUpWithReferralCodeInCardplay_pokerCp_raf_referee_details() {
        Map<String,String> data = sqlCalls.getCpRafRefereeData(newUserName);
        softAssert.assertTrue(data.get(String.valueOf(KeyReferences.RAF.referral_code)).equalsIgnoreCase(referral_code),"Referral code is not correct");
        softAssert.assertEquals(data.get(String.valueOf(KeyReferences.RAF.referrer_user_id)),sqlCalls.getUserID(UserManager.getUser().getUsername()),"Referrer userId is not correct");
        softAssert.assertEquals(data.get(String.valueOf(KeyReferences.RAF.referee_user_id)),sqlCalls.getUserID(newUserName),"Referee userId is not correct");
        softAssert.assertAll();
    }
}
