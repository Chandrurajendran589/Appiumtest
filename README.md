# developer-tools
Tools that aid us in our development efforts.

## **IMPORTANT**
Several of these scripts require Akamai QA credentials. They look for these credentials in a file called .akamai-creds in your home directory.
One of the following user names can be used pbqaenv, pkqaenv, ptqaenv, or weqaenv.
Passwords can be found here: https://confluence.wsgc.com/pages/viewpage.action?pageId=10263772

Run the following command to create this file:
* echo pbqaenv:PASSWORD-HERE | base64 > ~/.akamai-creds

## Installing Scripts

The *installScripts* script is provided to link all of the developer tool scripts into a directory that is on your PATH.
Run the script as follows:

* git clone https://github.wsgc.com/eCommerce-Bedrock/developer-tools
* cd developer-tools
* mkdir ~/bin
* ./installScripts ~/bin

The example above uses the ~/bin directory, but you can pick any directory that makes sense to you as long as that directory is added to your path.

You can also uninstall the scripts using the following:

* uninstallScripts

## Directory Structure

| **Directory** | **Tools for** |
| :--- | :--- |
| akamai          | Retrieve Akamai ESI script and page information for a specified page. |
| build           | Maven and UI setup related build tools. |
| caching         | Interact with DP caches. |
| cacerts         | Import wsi certificates to JDK trust store. |
| local_env_setup | Configuring a local development runtime environment to mirror a QA/IntDev/Integration setup. |
| logs            | Configuring log levels and pull logs from QA/IntDev/Integration environments. |
| oauth           | Interacting with OAuth, profile, user, and action code services. |
| partner         | Partner business connector. |
| performance     | Monitoring a Java process. |
| pricing         | Pricing services. |
| repository      | Merging and committing svn shortcut. |
| settings        | Setting and configuring values in QA/IntDev/Integration environments. |
| setup           | Configuration for development tools. |
| utility         | Common scripts that can be leveraged by other scripts. | 
