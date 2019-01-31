# mysql-graphite-reporter

A simple java standalone application for reporting MySQL metrics towards graphite. It simply runs three commands on the server, therefore it needs permissions for:

      SHOW GLOBAL VARIABLES; // metrics will be prefixed by variables.
      SHOW GLOBAL STATUS; // metrics will be prefixed by status
      SHOW SLAVE STATUS; // metrics will be prefixed by slave

These commands produce a set of key-value pairs. Please note that the keys are turned lowercase and any non-numeric value will be ignored.

By default every metrics is prefixed by _mysql_.

Let's say the SHOW GLOBAL STATUS returned:

|Aborted_clients       | 0 |
|Aborted_connects      | 0 |
|Binlog_cache_disk_use | 0 |
|Binlog_cache_use      | 0 |

The Aborted_clients will be reported as _mysql.status.aborted_clients_ by default.

# Usage:

## Build the artifact:

      mvn clean package
      
Then you can run the standalone.jar:

      java -jar target/mysql-graphite-reporter-jar-with-dependencies.jar file.conf
      
The provided parameter is a file on the local filesystem. This file contains the credentials and path for mysql and graphite (Environmental variables are overriding the credentials and path values).  
The file also contains the metrics our application should report in a form mentioned above.

