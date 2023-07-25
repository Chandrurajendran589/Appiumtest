# Build Related Tools
Scripts/tools for working with DP builds.

## mvnskip

This script runs the following mvn command to skip tests and JavaDoc processing. It speeds up builds significantly.
**IMPORTANT**: Do not use this script exclusively, committing code that breaks unit tests affects other developers.

* mvn -T 4 clean install -Dmaven.javadoc.skip=true -Dmaven.test.skip=true -Dmaven.source.skip=true

## mvnskipdoc

This script runs the following mvn command to skip JavaDoc processing but still run unit tests.

* mvn -T 4 clean install -Dmaven.javadoc.skip=true -Dmaven.source.skip=true

## mvnall

This script runs the following mvn command for DP using all.xml and skips test as well as JavaDoc processing.
**IMPORTANT**: Do not use this script exclusively, committing code that breaks unit tests affects other developers.

* mvn -T 4 -f all.xml clean install -Dmaven.javadoc.skip=true -Dmaven.test.skip=true -Dmaven.source.skip=true

## coverage

Use Jacoco to check code coverage of unit tests.  If tests are successful, open the Jacoco reports in the user's default browser.

* mvn -Dmaven.source.skip=true -Dmaven.javadoc.skip=true -e org.jacoco:jacoco-maven-plugin:0.8.1:prepare-agent clean test org.jacoco:jacoco-maven-plugin:0.8.1:report

## buildUI

This tool is related to some changes that were made to sites/common/content/build.xml that allows you to put a file called "skipUISetup" in you home directory to keep UI related Ant targets from running when DP starts up.
See: https://confluence.wsgc.com/display/~JScanlon/Skip+Webpack+Setup

## buildUIs

Will run buildUI for all brands.
See: https://confluence.wsgc.com/display/~JScanlon/Skip+Webpack+Setup

Usage: `./buildUIs /path/to/your/sites/parent`

Example: `./buildUIs /Users/mshah1/work/svn/edinburgh-shortcut`

## mvnsvcskip

Copy sample-dp.xml into the root directory of your SVN DP shortcut as svc.xml. You can then use mvndpskip to build all of DP.

Usage:
* cp sample-dp.xml ~/work/svn/trunk
* cd ~/work/svn/trunk
* mvndpskip 

# Jenkins Scripts

## getJobs

Retrieve a list of all the jobs that WSI Jenkins knows about or filter the list by passing an argument and only retrieve jobs that contain the specified string.

* Usage
  * getJobs [filterString]
  * getJobs
  * getJobs rainier
  * getJobs deploy

## getBuilds

This script lists all of the running jobs in WSI's Jenkins instance. If an argument is passed it only lists the jobs that contain the string passed in the argument.

Usage:
* getBuilds [filterString]
* getBuilds deploy
* getBuilds qa21

## watchBuild

This script grabs the list of running jobs from Jenkins that contain a given string and flashes the screen when that job leaves the list.

Usage:
* watchBuild qa21
* watchBuild deploy-qa21-mg

## triggerBuild

Trigger a Jenkins build for an organization/repository/branch that doesn't take parameters.

* Usage
  * triggerBuild organization repository branch
  * triggerBuild eCommerce-Rainier dp-baselogic release
  
## triggerDeployBuild

Trigger a Jenkins deploy build that needs 3 parameters, environment, project, and releasename.

* Usage
  * triggerDeployBuild job environment project releaseName
  * triggerDeployBuild deploy-rgs1-ws-WAR_release-2.4.40.x ws captainmarval
  
## getBuildStatus

Retrieve the Jenkins JSON output that contains all of the build status information.
buildNumber is optional, if left off, the status of the lastBuild is retrieved.

* Usage
  * getBuildStatus organization repository branch [buildNumber]
  * getBuildStatus job [buildNumber]
  * getBuildStatus eCommerce-Rainier dp-baselogic release
  * getBuildStatus eCommerce-Rainier dp-baselogic release 10
  * getBuildStatus eCommerce-Bedrock dp-sites release
  * getBuildStatus eCommerce-Bedrock dp-sites release 103
  * getBuildStatus deploy-qa21-ws-CONTENT_rainier-wrk-191002
  * getBuildStatus deploy-qa21-ws-CONTENT_rainier-wrk-191002 123

## getBuildConsoleOutput

Retrieve the Jenkins console output for a particular build number or the lastBuild if buildNumber is not provided.
If 4 arguments are passed, they are assumed to be an org/repo/branch/buildNumber
If 2 arguments are passed, they are assumed to be jobName/buildNumber

* Usage
  * getBuildConsoleOutput organization repository branch [buildNumber]
  * getBuildConsoleOutput jobName [buildNumber]
  * getBuildConsoleOutput eCommerce-Rainier dp-baselogic release 60
  * getBuildConsoleOutput deploy-uat3-we-WAR_release-2.4.40.x 10

## getSvcReleaseVersions

Get the latest release versions of all ecom-svc-*
* Usage
  * getSvcReleaseVersions