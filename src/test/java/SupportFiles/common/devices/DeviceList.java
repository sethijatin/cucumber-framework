package SupportFiles.common.devices;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;


public class DeviceList {

    @JsonProperty("DesktopDevices")
    private Devices desktops;

    @JsonProperty("TabletDevices")
    private Devices tablets;

    @JsonProperty("MobileDevices")
    private Devices mobiles;

    public void setDesktops(Devices desktops){
        this.desktops = desktops;
    }

    public Devices getDesktops(){
        return this.desktops;
    }

    public void setTablets(Devices tablets){
        this.tablets = tablets;
    }

    public Devices getTablets(){
        return this.tablets;
    }

    public void setMobiles(Devices mobiles){
        this.mobiles = mobiles;
    }

    public Devices getMobiles(){
        return this.mobiles;
    }

    public static class Devices {

        private HashMap<String, HashMap<String, String>> devices = new HashMap<>();

        @JsonAnySetter
        public void setDynamicProperty(String name, HashMap<String, String> hash) {
            devices.put(name, hash);
        }

        public void setDevices(HashMap<String, HashMap<String, String>> devices){
            this.devices = devices;
        }

        public HashMap<String, HashMap<String, String>> getDevices(){
            return this.devices;
        }
    }
}
