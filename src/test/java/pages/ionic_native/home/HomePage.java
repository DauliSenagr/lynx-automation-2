package pages.ionic_native.home;

import com.adda52.base.Page;
import com.adda52.controllers.ExecutionController;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class HomePage extends Page {

    @FindBy(xpath = "//div[@class='hamburger']")
    private WebElement hamburgerMenuButton;


}