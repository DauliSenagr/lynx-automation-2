package pages.ionic_native.home;

import com.adda52.base.Page;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class HamburgerMenuPage extends Page {

    private final int REFER_AND_EARN_INDEX = 0;


    public static final String IMG = "assets/images/icons/logout.svg";

    @FindBy(xpath = "//div[@class='fs14px fw700']")
    private List<WebElement> hamburgerMenuItems;

    public void scrollToContestButton() {
        scrollToElementUsingJavaScriptExecutor(hamburgerMenuItems.get(CONTEST_INDEX), "Contest button", 2, 2);
    }


}
