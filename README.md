# Field Service UI

###### Signals UI
[Daily Signals](images/field-service-ui.png)

###### Setup instructions
* Import project into Intellij
* From the gradle menu, run the following
    * mvn clean
    * mvn vaadin:clean
    * mvn vaadin:update-theme
    * mvn vaadin:update-widgetset
    * mvn vaadin:compile
    * mvn compile
    * mvn wildfly:run
    
Once your Field Service is up and running (port offset of 100), it should just work.    