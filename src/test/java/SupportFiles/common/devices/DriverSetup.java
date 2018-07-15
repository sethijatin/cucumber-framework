package SupportFiles.common.devices;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import StepDefinitions.World;
import SupportFiles.common.Props;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public class DriverSetup {

    private String remoteDriverUrl;
    private String build_number;
    private String remoteTunnel;
    private boolean executeLocally;
    private String testName;
    private World world;

    public DriverSetup(String testName, World world) throws Exception {

        //Additionally referencing a world function to pass on the session id for remote drivers.
        this.world = world;
        this.testName = testName;
        Props props = new Props(this.world.testConfigurationPath);
        this.remoteDriverUrl = props.getProperty("REMOTE_DRIVER_URL");
        this.build_number = props.getProperty("BUILD_NUMBER");
        this.executeLocally = props.getProperty("EXECUTION_STRATEGY").toLowerCase().equals("sanity");
        this.remoteTunnel = props.getProperty("TUNNEL_NAME");
    }

    private WebDriver provideRemoteWebDriverInstance(HashMap<String, String> capabilityList) throws Exception {

        //Set the device or browser where test needs to be executed
        DesiredCapabilities capabilities = getDesiredCapabilities(capabilityList.get("desiredCapabilities"));

        //Set the capabilities provided in the capabilityList HashMap
        for (Map.Entry capability : capabilityList.entrySet()) {
            capabilities.setCapability(capability.getKey().toString(), capability.getValue().toString());
        }

        //Set property to accept insecure certificate errors
        capabilities.setAcceptInsecureCerts(true);

        //Provide Test Case Name & Build Number To Sauce Labs
        capabilities.setCapability("name",this.testName);
        capabilities.setCapability("build",this.build_number);
        capabilities.setCapability("tunnelIdentifier",this.remoteTunnel);

        //Prepare to return driver, and set session id.
        WebDriver driver = new RemoteWebDriver(new URL(this.remoteDriverUrl), capabilities);
        this.world.RemoteSession = ((RemoteWebDriver)driver).getSessionId();
        return driver;
    }

    private DesiredCapabilities getDesiredCapabilities(String device) throws UnsupportedDeviceException, IOException {

        DesiredCapabilities capabilities;

        switch (device) {
            case "chrome":
                capabilities = DesiredCapabilities.chrome();
                break;
            case "firefox":
                capabilities = DesiredCapabilities.firefox();
                break;
            case "internetExplorer":
                capabilities = DesiredCapabilities.internetExplorer();
                break;
            case "edge":
                capabilities = DesiredCapabilities.edge();
                break;
            case "safari":
                capabilities = DesiredCapabilities.safari();
                break;
            case "iPhone":
                capabilities = DesiredCapabilities.iphone();
                break;
            case "iPad":
                capabilities = DesiredCapabilities.ipad();
                break;
            case "android":
                capabilities = DesiredCapabilities.android();
                break;
            default:
                Props props = new Props(this.world.testConfigurationPath);
                throw new UnsupportedDeviceException(props.getProperty("BROWSER_CONFIG"));
        }
        return capabilities;
    }

    private WebDriver generateLocalPhantomInstances(HashMap<String, String> dimension) throws InvalidPhantomJSSetupException, IOException {

        //Setup phantom js path
        Props props = new Props(this.world.testConfigurationPath);
        File file = new File(props.getProperty("PHANTOM_JS__PATH"));
        System.setProperty("phantomjs.binary.path", file.getAbsolutePath());

        //Setup phantom js capabilities
        ArrayList<String> phantomCapabilities = new ArrayList<>();
        phantomCapabilities.add("--web-security=false");
        phantomCapabilities.add("--ssl-protocol=any");
        phantomCapabilities.add("--ignore-ssl-errors=true");
        phantomCapabilities.add("--webdriver-loglevel=ERROR");

        //Setup desired capabilities object for phantom js
        DesiredCapabilities capabilities = DesiredCapabilities.phantomjs();
        capabilities.setCapability("takesScreenshot", true);
        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomCapabilities);

        //Phantom instance is ready.
        WebDriver driver = new PhantomJSDriver(capabilities);

        //Set window size
        try {
            int width = Integer.parseInt(dimension.get("width") == null ? "-1" : dimension.get("width"));
            int height = Integer.parseInt(dimension.get("height") == null ? "-1" : dimension.get("height"));

            if (width == -1 || height == -1){
                throw new InvalidPhantomJSSetupException();
            }
            driver.manage().window().setSize(new Dimension(width, height));
        }
        catch (Exception e){
            //Shut down the driver because test will not be run due to this exception.
            driver.quit();
            System.out.println(e.getMessage());
            throw new InvalidPhantomJSSetupException();
        }
        return driver;
    }

    public WebDriver requestDriverInstance(String device) throws Exception {

        //Check if a device has been specified for the test. If not immediately fail the test case
        if (Objects.equals(device, "")){ throw new DeviceNotDefinedForTestCaseException(); }

        //Create a hashMap to store device list.
        HashMap<String, HashMap<String, String>> deviceList = new HashMap<>();
        Props props = new Props(this.world.testConfigurationPath);

        //Map the YML using Jackson Library such that we get the list of desktops, mobiles, and tables separately.
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        DeviceList devices = mapper.readValue(new File(props.getProperty("BROWSER_CONFIG")), DeviceList.class);

        //Put all devices in the list in the device list hash map
        deviceList.putAll(devices.getDesktops().getDevices());
        deviceList.putAll(devices.getMobiles().getDevices());
        deviceList.putAll(devices.getTablets().getDevices());

        //Create a Hash Map to store capabilities and pass it on the create the remoteDriver Instance
        HashMap<String, String> capabilities;

        if (!this.executeLocally){

            capabilities = deviceList.get(device);
            if (capabilities == null){
                throw new InvalidDeviceException("Device: " + device + " defined in test case is not found in >>>> " +
                                                  props.getProperty("BROWSER_CONFIG") +" <<<<");
            }
            return  provideRemoteWebDriverInstance(capabilities);
        }
        else {

            if (devices.getDesktops().getDevices().get(device) != null) {
                capabilities = devices.getDesktops().getDevices().get("Phantom");
            }
            else if (devices.getTablets().getDevices().get(device) != null) {
                capabilities = devices.getTablets().getDevices().get("Phantom");
            }
            else  {
                capabilities = devices.getMobiles().getDevices().get("Phantom");
            }
            //Execution Tag is set to local. Prepare a phantom js instance
            return generateLocalPhantomInstances(capabilities);
        }
    }

    public static class DeviceNotDefinedForTestCaseException extends  Exception {
        DeviceNotDefinedForTestCaseException(){
            super("No test device key has been provided with test. Failing this test case.");
        }
    }

    public static class UnsupportedDeviceException extends Exception {
        UnsupportedDeviceException(String filePath){
            super("Device name defined in the file >>>" + filePath + "<<< "
                   + "must contain character sequences 'chrome', 'firefox', 'ie', 'edge', 'ios', or 'android'. \n"
                   + "Each device needs a different set of desired capability, which needs to be configured in the " +
                    " >>> DriverSetup.java <<<. Please reach out to jsethi@sapient.com for help on this issue."
            );
        }
    }

    public static class InvalidDeviceException extends Exception {
        InvalidDeviceException(String message){
            super(message);
        }
    }

    public static class InvalidPhantomJSSetupException extends Exception {
        InvalidPhantomJSSetupException(){
            super("Phantom JS runs locally on your machine. Please make sure that it browser-config contains an entry " +
                  "for each platform Desktop, Tablets, and Mobile. \n" +
                    "Desktop: \n" +
                    "\t PhantomJS: \n" +
                    "\t \t Width: <Integer Value> (Mandatory) \n" +
                    "\t \t Height: <Integer Value> (Mandatory) \n" +
                    "This error will always be shown in case of local execution when width & height are not defined."
            );
        }
    }
}