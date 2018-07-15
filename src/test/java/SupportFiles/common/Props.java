package SupportFiles.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Props {

    private String filePath;

    public Props(String FilePath){
        this.filePath = FilePath;
    }

    public String getProperty(String propertyName) throws IOException {
        InputStream testConfig = new FileInputStream(this.filePath);
        java.util.Properties configProps = new java.util.Properties();
        configProps.load(testConfig);
        return configProps.getProperty(propertyName);
    }

}
