package unit;

import org.testng.Assert;
import org.testng.annotations.Test;
import utils.json.JsonUtils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * @author Kishore
 */
public class FilesTest {

    @Test
    public void compare() {
       String amount = "1499.00";
        String instaAmount = amount.split("\\.")[0];
        System.out.println(instaAmount);
    }




    @Test
    public void filter(){
        String q = "A scratch card rewards you with exclusive benefits! When you scratch the surface of the card, you find a hidden coupon code that you can enter on the platform to claim your reward.If you have received a scratch card and wish to use it, go to the cashier, select \"Scratch Card,\" enter your coupon code, and click on \"Redeem.\"]";
        String e = "A scratch card rewards you with exclusive benefits! When you scratch the surface of the card, you find a hidden coupon code that you can enter on the platform to claim your reward.If you have received a scratch card and wish to use it, go to the cashier, select \"Scratch Card,\" enter your coupon code, and click on \"Redeem.\"]";
        String d =q.replace(",","").trim();
        System.out.println(d);

    }

    @Test
    public void date() throws Exception {
        List<String> usernames = JsonUtils.extractUsernames("src/test/resources/functional_users.json");
        System.out.println(usernames);
    }
}
