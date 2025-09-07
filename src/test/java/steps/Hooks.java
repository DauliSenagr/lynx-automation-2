package steps;

import com.adda52.context.driver.DriverContext;
import com.adda52.context.scenario.ScenarioContext;
import com.adda52.controllers.ExecutionController;
import com.adda52.device.DeviceController;
import com.adda52.driver.DriverFactory;
import com.adda52.driver.DriverManager;
import com.adda52.driver.DriverType;
import com.adda52.logging.Logging;
import com.adda52.utils.screenshot.ScreenshotUtil;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.service.ExtentService;
import io.appium.java_client.android.AndroidDriver;
import io.cucumber.java.*;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import reporting.InitReporting;
import utils.db.InitDatabaseConnection;
import utils.db.KycStatus;
import utils.db.MongoCalls;
import utils.db.MySqlCalls;
import utils.db.PSQLCalls;
import utils.keys.KeyReferences;
import utils.support.ExecutionSupport;
import utils.users.UserFactory;
import utils.users.UserManager;

import java.util.List;

/**
 * @author Dauli Sengar
 * @since 15th May 2022
 */

public class Hooks extends DriverFactory implements Logging {

    private static final UserFactory userFactory;

    static Logger logger;

    static {
        userFactory = new UserFactory();
        logger = Logging.getLogger(Hooks.class);
    }

    @BeforeAll
    public static void initialize() {
        logger.info("Starting test execution on server: " + Constants.getServerName());
        userFactory.initializeJsonUserPool();
        logger.info("Initializing database connection");
        new InitDatabaseConnection();
        new InitDatabaseConnection(Constants.getPSQLPrefix()+"poker_lobby");
        ExtentReports extentReports = ExtentService.getInstance();
        new InitReporting(extentReports);
        new ExecutionSupport().checkForAppUpdate();
    }

    @Before
    public void setUp(Scenario scenario) {
        if (scenario.getSourceTagNames().contains("@IonicNativeApp")) {
            userFactory.createUserInstance();

            if (DriverManager.getDriver() == null || !Constants.getAppiumSessionType().equalsIgnoreCase("persistent")) {
                if (Constants.getBrowser().equalsIgnoreCase("ANDROID")) {
                    createDriverInstance(DriverType.ANDROID);
                } else if (Constants.getBrowser().equalsIgnoreCase("IOS")) {
                    createDriverInstance(DriverType.IOS);
                }
            }

            if (DriverManager.getDriver() != null && Constants.getAppiumSessionType().equalsIgnoreCase("persistent")) {
                ExecutionController.pauseExecution(5);
                DriverContext.switchToContext(Constants.getIonicAppWebContext());
            }

            if (Constants.getAppiumExecutionType().equalsIgnoreCase("lambdatest")) {
                ExecutionController.setLambdaTestName(scenario.getName());
            }
            String scenarioStartAlertMessage = "Execution for scenario: " + scenario.getName() + " has started. User under test: " + UserManager.getUser().getUsername();
            getLogger().info(scenarioStartAlertMessage);

            handleSetupRequirements(scenario);
        }

        MySqlCalls calls = new MySqlCalls();
        if (scenario.getSourceTagNames().contains("@UpdateThemeSettings")) {
            getLogger().info("Updating themes settings for user :" + UserManager.getUser().getUsername());
            calls.updateSettings(String.valueOf(KeyReferences.SETTINGS.NG_TABLE_COLOR), "1", UserManager.getUser().getUsername());
            calls.updateSettings(String.valueOf(KeyReferences.SETTINGS.NG_BACKGROUND_COLOR), "1", UserManager.getUser().getUsername());
            calls.updateSettings(String.valueOf(KeyReferences.SETTINGS.NG_CARD_BACKGROUND), "0", UserManager.getUser().getUsername());
            calls.updateSettings(String.valueOf(KeyReferences.SETTINGS.FOUR_COLOR_DECK), "0", UserManager.getUser().getUsername());
            ExecutionController.pauseExecution(3);
        }
        if (scenario.getSourceTagNames().contains("@UpdateBuyInPreferences")) {
            getLogger().info("Updating Buy-In preferences settings for user :" + UserManager.getUser().getUsername());
            calls.updateSettings(String.valueOf(KeyReferences.SETTINGS.NG_AUTO_BUY_IN), "0", UserManager.getUser().getUsername());
            calls.updateSettings(String.valueOf(KeyReferences.SETTINGS.NG_AUTO_BUY_IN_BB), "0", UserManager.getUser().getUsername());
            calls.updateSettings(String.valueOf(KeyReferences.SETTINGS.NG_AUTO_REBUY_IN_FORM), "0", UserManager.getUser().getUsername());
            calls.updateSettings(String.valueOf(KeyReferences.SETTINGS.NG_AUTO_ADD_CHIPS), "0", UserManager.getUser().getUsername());
            calls.updateSettings(String.valueOf(KeyReferences.SETTINGS.NG_AUTO_REBUY), "3", UserManager.getUser().getUsername());
            ExecutionController.pauseExecution(3);
        }
        if (scenario.getSourceTagNames().contains("@Redeem")) {
            calls.addBankDetailsInUserBankInfo(UserManager.getUser().getUsername());
        }

        if (scenario.getSourceTagNames().contains("@InvolvesGameAreaInteraction")) {
            calls.updateUserAccountBalance(UserManager.getUser().getUsername(), "10000", "0", "0", "0");
            List<Integer> groupId = new PSQLCalls().getShowDirectTableTrueList();
            ScenarioContext.setScenarioData("groupId", groupId);
            int length = groupId.size();
            for (int i=0; length > i; i++) {
                int groupID = groupId.get(i);
                new PSQLCalls().setShowDirectTableStatus(groupID, false);
            }
        }
    }

    @After
    public void tearDown(Scenario scenario) {
        handleScenarioContexts(scenario);
        if (scenario.getSourceTagNames().contains("@IonicNativeApp")) {
            String scenarioMessage = "Execution for scenario: " + scenario.getName();
            if (UserManager.getUser() != null) {
                scenarioMessage += " has ended. User under test: " + UserManager.getUser().getUsername();

                if (scenario.getSourceTagNames().contains("@ResetPassword")) {
                    ExecutionController.pauseExecution(2);
                    new MySqlCalls().resetUserPassword(UserManager.getUser().getCrypt(), UserManager.getUser().getUsername());
                }
                if (scenario.getSourceTagNames().contains("@BlockedUser")) {
                    new MySqlCalls().unblockUser(UserManager.getUser().getUsername());
                }
                if (scenario.getSourceTagNames().contains("@ResetEmail")) {
                    new MySqlCalls().setEmailStatus(UserManager.getUser().getUsername(), "1");
                }
                if (scenario.getSourceTagNames().contains("@ResetMobile")) {
                    new MySqlCalls().setMobileStatus(UserManager.getUser().getUsername(), "1");
                }
                MySqlCalls calls = new MySqlCalls();
                if (scenario.getSourceTagNames().contains("@UpdateTicketOffers")) {
                    String tktName = (String) ScenarioContext.getScenarioData("tktName");
                    String offerName = (String) ScenarioContext.getScenarioData("offerName");
                    String familyName = (String) ScenarioContext.getScenarioData("familyName");
                    ExecutionController.pauseExecution(3);
                    getLogger().info("Deleting last created Ticket and Offer");
                    calls.deleteCpTicketDetailsQuery(tktName);
                    ExecutionController.pauseExecution(1);
                    calls.deleteTicketFamilyMasterQuery(familyName);
                    ExecutionController.pauseExecution(1);
                    calls.deleteLastCreatedTicketOffer(offerName);

                }

                if (scenario.getSourceTagNames().contains("@ResetEmail") || scenario.getSourceTagNames().contains("@ResetExistingEmail")) {
                    new MySqlCalls().updateEmailAddress(UserManager.getUser().getUsername(), UserManager.getUser().getEmail(), "1");
                }

                UserManager.quitUser(UserManager.getUser());

            } else {
                scenarioMessage += " has ended.";
            }
            getLogger().info(scenarioMessage);

            if (DriverManager.getDriver() != null) {
                if (scenario.isFailed()) {
                    String scenarioFailedMessage = "Scenario: " + scenario.getName() + " has FAILED.";
                    getLogger().info(scenarioFailedMessage);

                    if (Constants.getAppiumExecutionType().equalsIgnoreCase("lambdatest")) {
                        ExecutionController.setLambdaTestExecutionStatus("failed", scenarioFailedMessage);
                    }

                    try {
                        byte[] screenshotBytes = ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
                        byte[] compressedBytes = ScreenshotUtil.compressImage(screenshotBytes, 0.5f);
                        scenario.attach(compressedBytes, "image/jpeg", "Failure Screenshot");
                    } catch (Exception e) {
                        getLogger().error("Unable to capture screenshot for failed scenarios: " + scenario.getName(), e);
                    }
                } else {
                    getLogger().info("Scenario: " + scenario.getName() + " has PASSED.");

                    if (Constants.getAppiumExecutionType().equalsIgnoreCase("lambdatest")) {
                        ExecutionController.setLambdaTestExecutionStatus("passed", "Scenario: " + scenario.getName() + " has PASSED.");
                    }
                }

                if (Constants.getAppiumSessionType().equalsIgnoreCase("persistent")) {
                    DeviceController.resetApp((AndroidDriver) DriverManager.getDriver());
                } else {
                    DriverManager.quitDriver();
                }
            }

        }
    }


    @AfterAll
    public static void wrapUp() {
        if (DriverManager.getDriver() != null) {
            DriverManager.quitDriver();
        }
        ExtentReports extentReports = ExtentService.getInstance();
        logger.info("Flushing extent reports");
        extentReports.flush();
    }

    private void handleScenarioContexts(Scenario scenario) {
        if (scenario.getSourceTagNames().contains("@SignUp")) {
            String username = (String) ScenarioContext.getScenarioData("newUser");
            getLogger().info("Scenario Context : New username shared through scenario context is :" + username);
            if (username != null && !username.equalsIgnoreCase("")) {
                new MySqlCalls().resetSignUpRestriction(username);
            }
        }

        if (scenario.getSourceTagNames().contains("@BlockedUser")) {
            new MySqlCalls().unblockUser(UserManager.getUser().getUsername());
        }

        if (ScenarioContext.getScenarioData("resetPassword") != null) {
            if (ScenarioContext.getScenarioData("resetPassword").toString().equals("true")) {
                new MySqlCalls().resetUserPassword(UserManager.getUser().getCrypt(), UserManager.getUser().getUsername());
            }
        }

        if (ScenarioContext.getScenarioData("resetRGTableLimits") != null) {
            if (ScenarioContext.getScenarioData("resetRGTableLimits").toString().equals("true")) {
                MongoCalls mongoCalls = new MongoCalls();
                mongoCalls.deleteResponsibleGamingCashTableLimits(UserManager.getUser().getUsername());
            }
        }

        if (ScenarioContext.getScenarioData("resetRGTournamentLimits") != null) {
            if (ScenarioContext.getScenarioData("resetRGTournamentLimits").toString().equalsIgnoreCase("true")) {
                MongoCalls mongoCalls = new MongoCalls();
                mongoCalls.deleteResponsibleGamingTournamentTableLimits(UserManager.getUser().getUsername());
            }
        }

        if (ScenarioContext.getScenarioData("resetRGSNGLimits") != null) {
            if (ScenarioContext.getScenarioData("resetRGSNGLimits").toString().equalsIgnoreCase("true")) {
                new MongoCalls().deleteResponsibleGamingSngTableLimits(UserManager.getUser().getUsername());
            }
        }

        if (ScenarioContext.getScenarioData("resetRGDepositLimits") != null) {
            if (ScenarioContext.getScenarioData("resetRGDepositLimits").toString().equalsIgnoreCase("true")) {
                new MongoCalls().deleteResponsibleGamingAllDepositLimitEntery(UserManager.getUser().getUsername());
            }
        }

        if (ScenarioContext.getScenarioData("resetRGSelfExclusion") != null) {
            if (ScenarioContext.getScenarioData("resetRGSelfExclusion").toString().equalsIgnoreCase("true")) {
                new MongoCalls().deleteResponsibleGamingSelfExclusionTableLimits(UserManager.getUser().getUsername());
            }
        }

        if (scenario.getSourceTagNames().contains("@ResetEmail")) {
            new MySqlCalls().updateEmailAddress(UserManager.getUser().getUsername(), UserManager.getUser().getEmail(), "1");
        }

        if (scenario.getSourceTagNames().contains("@BlockRedeem")) {
            new MySqlCalls().setRedeemBlockStatus(UserManager.getUser().getUsername(), "unblock");
            getLogger().info("Redeem status set to unblock");

        }

        if (scenario.getSourceTagNames().contains("@ResetBankInfo")) {
            new MySqlCalls().updateUserBankInfo(UserManager.getUser().getUsername(), "1", "1", KycStatus.STATUS_VERIFIED);
            getLogger().info("Bank Info status set to verified");
        }

        if (scenario.getSourceTagNames().contains("@NonDepositor")) {
            new MySqlCalls().setUserAsADepositor(UserManager.getUser().getUsername(), "10000", "1");
            new MySqlCalls().updateUserPurchaseStatus(UserManager.getUser().getUsername(),"999999");
            getLogger().info("Resetting user as depositor");
        }
        if (scenario.getSourceTagNames().contains("@ResetInstaRedeemLimit")) {
            new MySqlCalls().setInstaRedeemAmountLimit(UserManager.getUser().getUsername(), "0");
            getLogger().info("Resetting Insta Redeem Limit");
        }

        if (ScenarioContext.getScenarioData("resetRGTableLimits") != null) {
            if (ScenarioContext.getScenarioData("resetRGTableLimits").toString().equals("true")) {
                MongoCalls mongoCalls = new MongoCalls();
                mongoCalls.deleteResponsibleGamingCashTableLimits(UserManager.getUser().getUsername());
            }
        }

        if (ScenarioContext.getScenarioData("resetRGTournamentLimits") != null) {
            if (ScenarioContext.getScenarioData("resetRGTournamentLimits").toString().equalsIgnoreCase("true")) {
                MongoCalls mongoCalls = new MongoCalls();
                mongoCalls.deleteResponsibleGamingTournamentTableLimits(UserManager.getUser().getUsername());
            }
        }

        if (ScenarioContext.getScenarioData("resetRGSNGLimits") != null) {
            if (ScenarioContext.getScenarioData("resetRGSNGLimits").toString().equalsIgnoreCase("true")) {
                new MongoCalls().deleteResponsibleGamingSngTableLimits(UserManager.getUser().getUsername());
            }
        }

        if (ScenarioContext.getScenarioData("resetRGDepositLimits") != null) {
            if (ScenarioContext.getScenarioData("resetRGDepositLimits").toString().equalsIgnoreCase("true")) {
                new MongoCalls().deleteResponsibleGamingAllDepositLimitEntery(UserManager.getUser().getUsername());
            }
        }

        if (ScenarioContext.getScenarioData("resetRGSelfExclusion") != null) {
            if (ScenarioContext.getScenarioData("resetRGSelfExclusion").toString().equalsIgnoreCase("true")) {
                new MongoCalls().deleteResponsibleGamingSelfExclusionTableLimits(UserManager.getUser().getUsername());
            }
        }

        if (scenario.getSourceTagNames().contains("@ResetEmail")) {
            new MySqlCalls().updateEmailAddress(UserManager.getUser().getUsername(), UserManager.getUser().getEmail(), "1");
        }

        if (scenario.getSourceTagNames().contains("@InvolvesGameAreaInteraction")) {
            List<Integer> groupId = (List<Integer>) ScenarioContext.getScenarioData("groupId");
            int length = groupId.size();
            for (int i=0; length > i; i++) {
                int groupID = groupId.get(i);
                new PSQLCalls().setShowDirectTableStatus(groupID, true);
            }
        }
    }

    public void handleSetupRequirements(Scenario scenario) {
        MySqlCalls calls = new MySqlCalls();
        if (scenario.getSourceTagNames().contains("@UpdateThemeSettings")) {
            getLogger().info("Updating themes settings for user :" + UserManager.getUser().getUsername());
            calls.updateSettings(String.valueOf(KeyReferences.SETTINGS.NG_TABLE_COLOR), "1", UserManager.getUser().getUsername());
            calls.updateSettings(String.valueOf(KeyReferences.SETTINGS.NG_BACKGROUND_COLOR), "1", UserManager.getUser().getUsername());
            calls.updateSettings(String.valueOf(KeyReferences.SETTINGS.NG_CARD_BACKGROUND), "0", UserManager.getUser().getUsername());
            calls.updateSettings(String.valueOf(KeyReferences.SETTINGS.FOUR_COLOR_DECK), "0", UserManager.getUser().getUsername());
            ExecutionController.pauseExecution(3);
        }
        if (scenario.getSourceTagNames().contains("@UpdateBuyInPreferences")) {
            getLogger().info("Updating Buy-In preferences settings for user :" + UserManager.getUser().getUsername());
            calls.updateSettings(String.valueOf(KeyReferences.SETTINGS.NG_AUTO_BUY_IN), "0", UserManager.getUser().getUsername());
            calls.updateSettings(String.valueOf(KeyReferences.SETTINGS.NG_AUTO_BUY_IN_BB), "0", UserManager.getUser().getUsername());
            calls.updateSettings(String.valueOf(KeyReferences.SETTINGS.NG_AUTO_REBUY_IN_FORM), "0", UserManager.getUser().getUsername());
            calls.updateSettings(String.valueOf(KeyReferences.SETTINGS.NG_AUTO_ADD_CHIPS), "0", UserManager.getUser().getUsername());
            calls.updateSettings(String.valueOf(KeyReferences.SETTINGS.NG_AUTO_REBUY), "3", UserManager.getUser().getUsername());
            ExecutionController.pauseExecution(3);
        }
        if (scenario.getSourceTagNames().contains("@ResetEmail")) {
            getLogger().info("Resetting Email Id for user: " + UserManager.getUser().getUsername());
            calls.resetEmail(UserManager.getUser().getUsername());
            calls.setEmailAsUnverified(UserManager.getUser().getUsername());
            ExecutionController.pauseExecution(4);
        }

        if (scenario.getSourceTagNames().contains("@ResetExistingEmail")) {
            String emailId = "sansa@rommk.com";
            getLogger().info("Resetting Email Id for user: " + UserManager.getUser().getUsername());
            calls.setExistingEmailAsUnverified(emailId, "0");
            calls.resetExistingEmail(emailId);
            ExecutionController.pauseExecution(4);
        }

        if (scenario.getSourceTagNames().contains("@SetEmailAsVerified")) {
            getLogger().info("Setting Email for user: " + UserManager.getUser().getUsername());
            MySqlCalls sqlCalls = new MySqlCalls();
            sqlCalls.setEmail(UserManager.getUser().getUsername(), UserManager.getUser().getEmail());
            sqlCalls.updateEmailAndMobileVerifiedFromUserProfileTable(UserManager.getUser().getUsername(), "0", "1", "1", UserManager.getUser().getMobile());
            ExecutionController.pauseExecution(3);
        }

        if (scenario.getSourceTagNames().contains("@SetAlternateMobileNumber")) {
            String altMobile = "8118881121";
            getLogger().info("Setting Alternate Mobile Number for user: " + UserManager.getUser().getUsername());
            calls.setAlternateMobileNumber(UserManager.getUser().getUsername(), altMobile);
            ExecutionController.pauseExecution(3);
        }


        if (scenario.getSourceTagNames().contains("@ResetAlternateMobileNumber")) {
            getLogger().info("Resetting Alternate Mobile Number for user: " + UserManager.getUser().getUsername());
            calls.resetAlternateMobileNumber(UserManager.getUser().getUsername());
            ExecutionController.pauseExecution(4);
        }

        if (scenario.getSourceTagNames().contains("@ResetExistingAlternateMobileNumber")) {
            String altMobile = "8118881120";
            getLogger().info("Resetting Alternate Mobile Number for user: " + altMobile);
            calls.resetExistingAlternateMobileNumber(altMobile);
            ExecutionController.pauseExecution(4);
        }

        if (scenario.getSourceTagNames().contains("@ResetGender")) {
            getLogger().info("Resetting Gender for user: " + UserManager.getUser().getUsername());
            calls.resetGender(UserManager.getUser().getUsername());
            ExecutionController.pauseExecution(4);
        }

        if (scenario.getSourceTagNames().contains("@SetGender")) {
            getLogger().info("Setting Gender for user: " + UserManager.getUser().getUsername());
            calls.setGender(UserManager.getUser().getUsername());
            ExecutionController.pauseExecution(4);
        }

        if (scenario.getSourceTagNames().contains("@ResetProfile")) {
            getLogger().info("Resetting Profile for user: " + UserManager.getUser().getUsername());
            MySqlCalls sqlCalls = new MySqlCalls();
            sqlCalls.updateEmailAddress(UserManager.getUser().getUsername(), UserManager.getUser().getEmail(), "active");
            sqlCalls.updateEmailAndMobileVerifiedFromUserProfileTable(UserManager.getUser().getUsername(), "0", "0", "1", UserManager.getUser().getMobile());
            sqlCalls.updateKycDataInApplication(UserManager.getUser().getUsername(), "0", "0", "0", "0");
            ExecutionController.pauseExecution(3);
        }

        if (scenario.getSourceTagNames().contains("@SetPanDetails")) {
            getLogger().info("Setting PAN Details for user: " + UserManager.getUser().getUsername());
            calls.updateUserPanDetailInPanInfoTable(UserManager.getUser().getUsername());
            ExecutionController.pauseExecution(4);
        }

        if (scenario.getSourceTagNames().contains("@ResetPanDetails")) {
            getLogger().info("Resetting PAN Details for user: " + UserManager.getUser().getUsername());
            calls.resetUserPanInfo(UserManager.getUser().getUsername());
            ExecutionController.pauseExecution(4);
        }

        if (scenario.getSourceTagNames().contains("@UpdatingPanDetails")) {
            getLogger().info("Resetting PAN Details for user :" + UserManager.getUser().getUsername());
            calls.resetUserPanInfo(UserManager.getUser().getUsername());
            getLogger().info("Setting PAN Details for user :" + UserManager.getUser().getUsername());
            calls.updateUserPanDetailInPanInfoTable(UserManager.getUser().getUsername());
            ExecutionController.pauseExecution(4);
        }

        if (scenario.getSourceTagNames().contains("@SetKycDetails")) {
            getLogger().info("Setting Kyc Details for user: " + UserManager.getUser().getUsername());
            calls.updateUserKycDocInUserKycInfoTable(UserManager.getUser().getUsername());
            calls.updateUserKycInfoUsingUsername(UserManager.getUser().getUsername(), "verified", "doc_validate");
            ExecutionController.pauseExecution(4);
        }

        if (scenario.getSourceTagNames().contains("@ResetKycDetails")) {
            getLogger().info("Resetting Kyc Details for user: " + UserManager.getUser().getUsername());
            calls.resetUserKycInfo(UserManager.getUser().getUsername());
            ExecutionController.pauseExecution(4);
        }

        if (scenario.getSourceTagNames().contains("@UpdatingKycDetails")) {
            getLogger().info("Resetting Kyc Details for user: " + UserManager.getUser().getUsername());
            calls.resetUserKycInfo(UserManager.getUser().getUsername());
            getLogger().info("Setting Kyc Details for user :" + UserManager.getUser().getUsername());
            calls.updateUserKycDocInUserKycInfoTable(UserManager.getUser().getUsername());
            calls.updateUserKycInfoUsingUsername(UserManager.getUser().getUsername(), "verified", "doc_validate");
            ExecutionController.pauseExecution(4);
        }

        if (scenario.getSourceTagNames().contains("@SetAccountName")) {
            getLogger().info("Setting Mobile Number for user: " + UserManager.getUser().getUsername());
            calls.setAccountName(UserManager.getUser().getUsername());
            ExecutionController.pauseExecution(3);
        }

        if (scenario.getSourceTagNames().contains("@ResetBankDetails")) {
            getLogger().info("Resetting Bank Details for user: " + UserManager.getUser().getUsername());
            calls.resetBankDetails(UserManager.getUser().getUsername());
            ExecutionController.pauseExecution(4);
        }

        if (scenario.getSourceTagNames().contains("@SetUserAsCashGamePlayer")) {
            getLogger().info("Setting user under test as a cash game player: " + UserManager.getUser().getUsername());
            new InitDatabaseConnection(Constants.getPSQLPrefix()+"poker_play");
            PSQLCalls psqlCalls = new PSQLCalls();
            if(psqlCalls.getUserGamePlaySummary(UserManager.getUser().getUsername())!=0){
                getLogger().info("User has played cash games");
            }else
            {
                psqlCalls.insertIntoUserGameSummaryStatus(UserManager.getUser().getUsername());
            }
            ExecutionController.pauseExecution(4);
        }

        if (scenario.getSourceTagNames().contains("@SetUserAsNonCashGamePlayer")) {
            getLogger().info("Setting user under test as non cash game player: " + UserManager.getUser().getUsername());
            new InitDatabaseConnection(Constants.getPSQLPrefix()+"poker_play");
            PSQLCalls psqlCalls = new PSQLCalls();
            new InitDatabaseConnection(Constants.getPSQLPrefix()+"poker_play");
            if(psqlCalls.getUserGamePlaySummary(UserManager.getUser().getUsername())==0){
                getLogger().info("User has not played cash games");
            }else
            {
                psqlCalls.deleteFromUserGameSummaryStatus(UserManager.getUser().getUsername());
            }
            ExecutionController.pauseExecution(4);
        }

    }


}


