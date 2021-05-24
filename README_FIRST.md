
# NOTES to future self

## Build and run locally
> mvn clean install && java -jar target/DataBuddy-0.0.1-SNAPSHOT.jar

## Deploy Remote
- locally run buildAndDeployRemotely
- Often when deploying, the remote stop script isn't able to kill the screen process.
- to reboot databuddy...
> cd /home/ubuntu/services/databuddy
> ps aux | grep SCREEN
> kill -KILL (Column 2 is the PID)
> ./start


