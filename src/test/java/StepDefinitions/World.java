package StepDefinitions;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.SessionId;

public class World {

    public final String testConfigurationPath = "./src/test/test-configuration.properties";
    public WebDriver driver;
    public SessionId RemoteSession;

}