# Application Caching Tools
Scripts/tools for working with DP application caches.

## JMX (Java Management Extensions)

Some of the tools below require a JMX connection on port 39667. To enable a JMX connection on a local system, the following must be added to the Java VM options:

* -Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.port=39667 -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=127.0.0.1 -Dcom.sun.management.jmxremote.local.only=false

All of the QA environment should be configured for JMX connections.

IMPORTANT: The scripts in this directory require groovy. Use "brew install groovy" to install groovy.
           The scripts also depends on the *getHost* script.

## getCacheNames

This script retrieves the names of all of the caches configured for a DP/eCommerce application.

* Usage
  * getCacheNames brand environment
      
## getCacheStats

This script retrieves the statistics for the specified DP/eCommerce cache. The full cache name is not required, only enough to uniquly match the start of the cache name.

* Usage
  * getCacheStats brand environment cacheNamePrefix


## flushCache

This script flushes the contents of a DP/eCommerce cache.
