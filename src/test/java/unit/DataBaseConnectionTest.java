package unit;

import com.adda52.controllers.ExecutionController;
import com.adda52.logging.Logging;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Before;
import org.junit.Test;
import org.testng.Assert;
import utils.api.ApiCalls;
import utils.db.InitDatabaseConnection;
import utils.db.KycStatus;
import utils.db.MongoCalls;
import utils.db.MySqlCalls;
import utils.keys.KeyReferences;
import utils.support.SupportUtils;
import utils.users.UserManager;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Kishore
 */
public class DataBaseConnectionTest implements Logging {

    MySqlCalls sqlCalls = new MySqlCalls();

    ApiCalls apiCalls = new ApiCalls();

    private String token;
    @Before
    public void init(){new InitDatabaseConnection();}



    @Test
    public void status(){
        HashMap<String,String> data =new MySqlCalls().getUserPreferences("GORO");
        System.out.println(data.get("show_hand_strength"));
        System.out.println(data.get("sound_mute"));
        System.out.println(data.get("vibrate_mode"));
        System.out.println(data.get("auto_post_BB"));
        System.out.println(data.get("auto_muck"));
    }

    @Test
    public void updateProfile(){
      String p =new MySqlCalls().getUserEncryptedPass("mohit");
        System.out.println(p);
    }




    @Test
    public void getOtp(){
//       String name = new MySqlCalls().getUsername("7788996637");
       String otp2 = new MySqlCalls().getSignUpOtp("3535351413");
//        String otp1 = new MySqlCalls().getLoginOtp("7X8X9Y6Y3Y");
        System.out.println(otp2);
    }

    @Test
    public void getUserId(){
       new MySqlCalls().setLoyaltyLevel("virat",2);
    }

    @Test
    public void getUserData(){
       Map<String,String> d = new MySqlCalls().getCpRafRefereeData("autumn391");
        System.out.println(d.get(String.valueOf(KeyReferences.RAF.referee_user_id)));
        System.out.println(d.get(String.valueOf(KeyReferences.RAF.referrer_user_id)));
        System.out.println(d.get(String.valueOf(KeyReferences.RAF.referral_code)));
    }

    @Test
    public void updateApplication(){
        MySqlCalls calls = new MySqlCalls();

        calls.updateKycApplicationUsers("badri", "0", "0", "0", "0");
        calls.updateUserBankInfo("badri", "0", "0", KycStatus.STATUS_INITIATED);
        calls.updateUserPanInfo("badri", KycStatus.STATUS_INITIATED, KycStatus.DOC_VALIDATE);
        calls.updateUserKycInfo("badri", KycStatus.STATUS_INITIATED, KycStatus.DOC_SUCCESS);
    }

    @Test
    public void userVerified(){
        MySqlCalls calls = new MySqlCalls();

        calls.updateKycApplicationUsers("rayden", "1", "1", "1", "1");
        calls.updateUserBankInfo("rayden", "1", "1", KycStatus.STATUS_VERIFIED);
        calls.updateUserPanInfo("rayden", KycStatus.STATUS_VERIFIED, KycStatus.DOC_SUCCESS);
        calls.updateUserKycInfo("rayden", KycStatus.STATUS_VERIFIED, KycStatus.DOC_SUCCESS);
    }

    @Test
    public void setInvestment(){
        MySqlCalls calls = new MySqlCalls();

        calls.updateUserLedgerStatus("badri","0","0","0");

    }

    @Test
    public void updateBal(){
        MySqlCalls calls = new MySqlCalls();

        calls.clearAllTransactions("reptile");

        calls.updateUserLedgerStatus("reptile","100","0","0");

        calls.updateUserAccountBalance("reptile","1000","0","0","0");


    }

    @Test
    public void getVIP(){
        MySqlCalls calls = new MySqlCalls();

        String a=calls.getVipBalance("badri");
        System.out.println(a);


    }

    @Test
    public void instaRedeemCriteria(){
        MySqlCalls calls = new MySqlCalls();
        calls.clearAllTransactions("goro");
        ExecutionController.pauseExecution(1);

        calls.setInstaRedeemStatus("goro","1");
        ExecutionController.pauseExecution(1);

        calls.setLoyaltyLevel("goro",3);

//        calls.setDailyRedeemLimit("goro","2");

        calls.updateVipHoldForUser("goro","0");


//       calls.setInstaRedeemAmountLimit("jax","2500");




        calls.setSpecialTagsForUser("goro","");
        ExecutionController.pauseExecution(1);

        calls.setUserAsADepositor("goro","10000","1");

        calls.updateUserAccountBalance("goro","10000","0","0","0");
        ExecutionController.pauseExecution(1);

        calls.updateUserLedgerStatus("goro","10000","0","0");
        ExecutionController.pauseExecution(1);

    }

    @Test
    public void setUserAsDuplicateUser(){
        MySqlCalls calls = new MySqlCalls();
        calls.setSpecialTagsForUser("badri","DUPLICATE_USER");
    }

    @Test
    public void setUserAsNonDepositor() {
        sqlCalls.clearAllTransactions("Goro");
        sqlCalls.setUserAsNonDepositor("goro");
        sqlCalls.updateUserAccountBalance("Goro", "100", "0", "0", "0");
        sqlCalls.updateVipHoldForUser("Goro", "200");
        ExecutionController.pauseExecution(1);

    }

    @Test
    public void setUserAsDepositor(){
        sqlCalls.setUserAsADepositor("badri","10000","1");



    }

    @Test
    public void clearTransactions(){
        MySqlCalls calls = new MySqlCalls();
        calls.clearAllTransactions("bahubali");


    }

    @Test
    public void setInstaRedeemStatus(){
        MySqlCalls calls = new MySqlCalls();
        calls.setInstaRedeemStatus("virat","1");

    }

    @Test
    public void setInstaRedeemAmount(){
        MySqlCalls calls = new MySqlCalls();
        calls.setInstaRedeemAmountLimit("goro","500");

    }

    @Test
    public void setHoldAmount(){
        new MySqlCalls().updateVipHoldForUser("badri","0");
    }

    @Test
    public void resetChips(){
        MySqlCalls calls = new MySqlCalls();
        calls.resetChipsInPlay("goro");

    }

    @Test
    public void nonDuplicateUser(){
        new MySqlCalls().setSpecialTagsForUser("badri","");
    }

    @Test
    public void userId(){
        String a = new MySqlCalls().getUserID("Reptile");
//        6002241
        System.out.println(a);
    }

    @Test
    public void unverifyEmail(){
        new MySqlCalls().setEmailStatus("goro","0");

    }

    @Test
    public void setMobile(){
        new MySqlCalls().setMobileStatus("goro","1");

    }

    @Test
    public void verifyEmail(){
        new MySqlCalls().setEmailStatus("virat","1");
    }

    @Test
    public void setLoyalty(){
        new MySqlCalls().setLoyaltyLevel("bahubali",4);
    }

    @Test
    public void setDailyRedeemLimit(){
        new MySqlCalls().setDailyRedeemLimit("bahubali","2");
    }


    @Test
    public void unblockRedemption(){
        sqlCalls.setRedeemBlockStatus("virat","0");
    }



    @Test
    public void getLastRedeemDetails(){
        MySqlCalls calls = new MySqlCalls();
        Map<String, String> data = calls.getLastRedeemDetails("reptile");


        System.out.println(data.get("amount"));
        System.out.println(data.get("redeem_mode"));
        System.out.println(data.get("status"));
        System.out.println(data.get("redeem_type"));
        System.out.println(data.get("redeem_source"));
        System.out.println(data.get("remarks"));
        System.out.println(data.get("transfer_status"));



    }

    @Test
    public void geo() throws IOException {
        MySqlCalls calls = new MySqlCalls();
        Map<String, String> data = calls.getControlRecordsDetails("jax");
        System.out.println(data.get("relation"));
        System.out.println(data.get("amount"));
        System.out.println(data.get("narration"));
        System.out.println(data.get("account_name"));

    }

    @Test
    public void ledger() throws IOException {
        MySqlCalls calls = new MySqlCalls();
        Map<String, String> data = calls.getUserLedgerDetails("reptile");
        System.out.println(data.get("total_investment"));
        System.out.println(data.get("total_tds"));
        System.out.println(data.get("total_redeem"));


    }

    @Test
    public void otp() {

        Map<String,String> g= sqlCalls.getCpRafRefereeData("bloodhammer953");
        System.out.println(g.get("referral_code"));
    }

    @Test
    public void signUpOtp(){
       String name = sqlCalls.getSignUpOtp("2024062512");

        System.out.println(name);
    }

    @Test
    public void update(){
       sqlCalls.addBankDetailsInUserBankInfo("rayden");
    }

    @Test
    public void updateLM(){
        MongoCalls mongoCalls = new MongoCalls();
        mongoCalls.expireResponsibleGamingLM("regressionnewpoker", SupportUtils.getPastDate(3));

    }

    @Test
    public void username(){
        String name = sqlCalls.getUserID("paparazzi552");

        System.out.println(name);
    }

    @Test
    public void tourneyStats() throws IOException, ParseException, URISyntaxException {




        Map<String, String> data = sqlCalls.getLastRedeemDetails("jax");
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



}
