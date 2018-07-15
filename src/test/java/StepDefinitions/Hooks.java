package StepDefinitions;

import SupportFiles.common.devices.DriverSetup;
import SupportFiles.common.Props;
import com.saucelabs.saucerest.SauceREST;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;

import org.json.JSONException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class Hooks {

    private World world;

    public Hooks(World world){
        this.world = world;
    }

    @Before
    public void setDriverInstance (Scenario scenario) throws Exception {

        String device = "";
        // --> Update the browser configuration based on tags provided by the user <-- //

        Object[] tags = scenario.getSourceTagNames().toArray();
        for (Object tag : tags) {
            String name = tag.toString();
            if (name.toLowerCase().contains("device=")) {
                //We need to exclude our wild card keywords.
                if (!name.split("=")[1].toLowerCase().contains("device")){
                    device = name.split("=")[1];
                }
            }

        }
        String testName = scenario.getName();
        DriverSetup ds = new DriverSetup(testName, this.world);
        world.driver = ds.requestDriverInstance(device);
    }

    private void UpdateResultsAtSauce(boolean testResults) throws JSONException, IOException {

        Props props = new Props(this.world.testConfigurationPath);
        SauceREST saucerest = new SauceREST(props.getProperty("USERNAME"), props.getProperty("ACCESS_KEY"));
        Map<String, Object> updates = new HashMap<String, Object>();
        updates.put("passed", testResults);
        saucerest.updateJobInfo(this.world.RemoteSession.toString(), updates);

    }

    @After
    public void updateSauceLabs(Scenario scenario) throws IOException, JSONException {

        if (scenario.isFailed()){
            byte[] screenshot =   ((TakesScreenshot) world.driver).getScreenshotAs(OutputType.BYTES);
            scenario.embed(screenshot, "image/png");
        }

        if (world.RemoteSession != null){
            UpdateResultsAtSauce(!scenario.isFailed());
        }

        world.driver.quit();
    }
}
