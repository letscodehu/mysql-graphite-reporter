package hu.letscode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

class PropertyFactory {

    static final String QUERY_INTERVAL = "QUERY_INTERVAL";
    static final String GRAPHITE_INTERVAL = "GRAPHITE_INTERVAL";
    static final String GRAPHITE_PREFIX = "GRAPHITE_PREFIX";
    static final String GRAPHITE_HOST = "GRAPHITE_HOST";
    static final String MYSQL_HOST = "MYSQL_HOST";
    static final String MYSQL_PORT = "MYSQL_PORT";
    static final String MYSQL_PASS = "MYSQL_PASS";
    static final String MYSQL_USER = "MYSQL_USER";
    static final String GRAPHITE_PORT = "GRAPHITE_PORT";

    private static final Map<String, String> DEFAULTS = new HashMap<String, String>() {{
        put(QUERY_INTERVAL, "10");
        put(GRAPHITE_INTERVAL, "10");
        put(GRAPHITE_PREFIX, "mysql");
        put(GRAPHITE_HOST, "localhost");
        put(GRAPHITE_PORT, "2003");
        put(MYSQL_USER, "root");
        put(MYSQL_PASS, "");
        put(MYSQL_PORT, "3306");
        put(MYSQL_HOST, "localhost");
    }};

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyFactory.class);

    Properties create(String[] args) throws IOException {
        if (args.length == 0) {
            throw new IllegalArgumentException("No config file given.");
        }
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(args[0])) {
            props.load(in);
        } catch (IOException e) {
            LOGGER.error("Could not read properties from file: " + args[0], e);
            throw e;
        }
        overrideDefaults(props);
        return props;
    }

    private void overrideDefaults(Properties properties) {
        DEFAULTS.forEach((key, values) -> {
            if (System.getenv().containsKey(key)) {
                properties.put(key, System.getenv(key));
            } else if (!properties.containsKey(key)) {
                properties.put(key,values);
            }
        });
    }

}
