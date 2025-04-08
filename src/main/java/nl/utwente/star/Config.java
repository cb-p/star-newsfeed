package nl.utwente.star;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find config.properties");
            }
            properties.load(input);
        } catch (IOException exception) {
            throw new RuntimeException("Error reading config.properties", exception);
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }
}
