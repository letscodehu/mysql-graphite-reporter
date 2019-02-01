# mysql-graphite-reporter

A simple java standalone application for reporting MySQL metrics towards graphite. It simply runs three commands on the server, therefore it needs permissions for:

      SHOW GLOBAL VARIABLES; // metrics will be prefixed by variables.
      SHOW GLOBAL STATUS; // metrics will be prefixed by status
      SHOW SLAVE STATUS; // metrics will be prefixed by slave

These commands produce a set of key-value pairs. Please note that the keys are turned lowercase and any non-numeric value will be ignored.

By default every metrics is prefixed by _mysql_.

Let's say the SHOW GLOBAL STATUS returned:

       | Aborted_clients       | 0 |
       | Aborted_connects      | 0 |
       | Binlog_cache_disk_use | 0 |
       | Binlog_cache_use      | 0 |

The Aborted_clients will be reported as _mysql.status.aborted_clients_ by default.

# Usage:

## Build the artifact

      mvn clean package
      
Then you can run the standalone.jar:

      java -jar target/mysql-graphite-reporter-jar-with-dependencies.jar file.conf
      
The provided parameter is a file on the local filesystem. This file contains the credentials and path for mysql and graphite (Environmental variables are overriding the credentials and path values).  
The file also contains the metrics our application should report in a form mentioned above.

## docker

You could also use the docker version of the package:

    docker run -d tacsiazuma/mysql-metric-reporter
    
By default it points to localhost 3306 port for MySQL and localhost:2003 for graphite.
You could change this behavior by providing a configuration file:

    docker run -d -v /path/to/config:/some-config-file tacsiazuma/mysql-metric-reporter java -jar app.jar /some-config-file

Or via environmental variables listed here:

    |       Name        |  default  | 
    |GRAPHITE_HOST      |localhost  |
    |GRAPHITE_PORT      |2003       |
    |GRAPHITE_INTERVAL  |10 (sec)   |
    |GRAPHITE_PREFIX    |mysql      |
    |QUERY_INTERVAL     |10 (sec)   |
    |MYSQL_HOST         |localhost  |
    |MYSQL_PORT         |3306       |
    |MYSQL_USER         |root       |
    |MYSQL_PASS         |           |
    
For the configuration format you could look for the file.conf file in the repository.    