package StepDefinitions;

import com.github.sethijatin.creative.layout.CreativeElement;
import cucumber.api.java.en.Given;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.openqa.selenium.*;
import java.io.File;
import java.util.concurrent.TimeUnit;

public class CookiePolicy {

    private World world;

    public CookiePolicy(World world){
        this.world = world;
    }

    @Given("^User navigates to ([^\"]*) and take screenshot$")
    public void userNavigatesToPageAndTakeScreenshot(String page) throws Throwable {

        //Open the page in webDriver
        WebDriver driver = this.world.driver;
        driver.manage().timeouts().pageLoadTimeout(150, TimeUnit.SECONDS);

        try {
            driver.get(page);
            WebElement cookieArea = driver.findElement(By.cssSelector("div[class=cookie_policy]"));
            WebElement utilArea = driver.findElement(By.cssSelector("[class=util_menu]"));
            CreativeElement creativeCookieArea = new CreativeElement(cookieArea);
            CreativeElement creativeUtilArea = new CreativeElement(utilArea);
            Assert.assertTrue(creativeUtilArea.isInside().isInside(creativeCookieArea));
        }
        catch (org.openqa.selenium.TimeoutException e){
            driver.findElement(By.tagName("body")).sendKeys("Keys.ESCAPE");
        }
        finally {
            Thread.sleep(10000);
            //Update the page name and exclude https / https
            page = page.replace("http://","").replace("https://", "");
            page =  System.getProperty("user.dir") + File.separator + page.substring(0, page.length()-1) + ".jpg";
            //Screenshot
            File screenshot = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(screenshot, new File(page));
        }

    }
}
