package hu.letscode;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;

public class Collector implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Collector.class);

    private final DataSource dataSource;
    private final Properties properties;
    private final String sqlQuery;
    private final Map<String, Integer> values;
    private MetricRegistry metrics;
    private String prefixForMetric;

    Collector(DataSource dataSource, Properties properties, String sqlQuery, Map<String, Integer> values, MetricRegistry metrics, String prefixForMetric) {
        this.dataSource = dataSource;
        this.properties = properties;
        this.sqlQuery = sqlQuery;
        this.values = values;
        this.metrics = metrics;
        this.prefixForMetric = prefixForMetric;
    }

    @Override
    public void run() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement(
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY
        )) {
            statement.executeQuery(sqlQuery);
            try (ResultSet resultSet = statement.getResultSet()) {
                resultSet.next();
                while (resultSet.next()) {
                    String value = resultSet.getString(2);
                    String key = normalize(resultSet.getString(1));
                    if (valueCanBeConverted(value) && keyToBeReported(key)) {
                        int converted = Integer.valueOf(value);
                        values.put(key, converted);
                        if (!metrics.getMetrics().containsKey(key)) {
                            LOGGER.info("Metric " + key + " registered.");

                            metrics.register(key, (Gauge<Integer>) () -> values.get(key));
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.error("Error during querying data.", ex);
        }
    }

    private String normalize(String string) {
        return String.format("%s.%s", prefixForMetric, string.toLowerCase());
    }

    private boolean keyToBeReported(String key) {
        return properties.containsKey(key);
    }

    private boolean valueCanBeConverted(String value) {
        try {
            Integer.valueOf(value);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
