package hu.letscode;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.mysql.cj.jdbc.MysqlDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static hu.letscode.PropertyFactory.*;

public class EntryPoint {

    private static final Map<String, Integer> values = new HashMap<>();
    private static final MetricRegistry metrics = new MetricRegistry();
    private static final String SHOW_GLOBAL_VARIABLES = "SHOW GLOBAL VARIABLES";
    private static final String SHOW_GLOBAL_STATUS = "SHOW GLOBAL STATUS";
    private static final String SHOW_SLAVE_STATUS = "SHOW SLAVE STATUS";

    public static void main(String[] args) throws SQLException, IOException {
        PropertyFactory propertyFactory = new PropertyFactory();
        Properties properties = propertyFactory.create(args);
        DataSource dataSource = createDataSource(properties);
        setupReporter(properties);
        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(3);
        scheduleCollector(properties, dataSource, scheduledExecutorService, SHOW_GLOBAL_VARIABLES, "variables");
        scheduleCollector(properties, dataSource, scheduledExecutorService, SHOW_GLOBAL_STATUS, "status");
        scheduleCollector(properties, dataSource, scheduledExecutorService, SHOW_SLAVE_STATUS, "slave");
    }

    private static void setupReporter(Properties properties) {
        final Graphite graphite = new Graphite(new InetSocketAddress(properties.getProperty(GRAPHITE_HOST), Integer.valueOf(properties.getProperty(GRAPHITE_PORT))));
        final GraphiteReporter reporter = GraphiteReporter.forRegistry(metrics)
                .prefixedWith(properties.getProperty(GRAPHITE_PREFIX))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .filter(MetricFilter.ALL)
                .build(graphite);
        reporter.start(Integer.valueOf(properties.getProperty(GRAPHITE_INTERVAL)), TimeUnit.SECONDS);
    }

    private static void scheduleCollector(Properties properties, DataSource dataSource, ScheduledExecutorService scheduledExecutorService, String sqlQuery, String prefixForMetric) {
        scheduledExecutorService.scheduleAtFixedRate(new Collector(dataSource, properties, sqlQuery, values, metrics, prefixForMetric), 0L, Long.valueOf(properties.getProperty(QUERY_INTERVAL)), TimeUnit.SECONDS);
    }

    private static DataSource createDataSource(Properties properties) throws SQLException {
        MysqlDataSource ds = new MysqlDataSource();
        ds.setPassword(properties.getProperty(MYSQL_PASS));
        ds.setUrl(String.format("jdbc:mysql://%s:%s/?user=%s",
                properties.getProperty(MYSQL_HOST),
                properties.getProperty(MYSQL_PORT),
                properties.getProperty(MYSQL_USER)
                ));
        ds.setAutoReconnect(true);
        return ds;
    }
}
