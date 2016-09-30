/*
 * AUT DMS S1 2016
 * Assignment : - Android Distributed Application
 *  Green, Terry (0829446)
 *  Prouting, Sez (0308852)
 *  Shaw, Aziel (14847095)
 *  
 *  
 */
package sezieliotor.locator;

import sezielioter.locator.beanInterface.DBPropertiesRemote;
import java.io.IOException;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;

@Singleton
public class DBProperties implements DBPropertiesRemote {
    
    private Properties properties;
    
    @PostConstruct
    private void init(){
        properties = new Properties();
        try{
            properties.loadFromXML(getClass().getResourceAsStream
            ("resources/dbConfig.xml"));
            
        }catch(IOException e){
            System.err.println("======= Cannot access properties file: "+ e.getMessage());
        }
    }

    /*************************************************************************
     *      DATABASE CONNECTION
     * ***********************************************************************/
    @Override
    public String getDBDriver() {
        return properties.get("dbDriver").toString();
    }

    @Override
    public String getDbUrl() {
        return properties.get("dbUrl").toString();
    }

    @Override
    public String getUserName() {
        return properties.get("user").toString();
    }

    @Override
    public String getPassword() {
        return properties.get("password").toString();
    }

    /*************************************************************************
     *      TABLES
     * ***********************************************************************/
    @Override
    public String getTable() {
        return properties.get("table").toString();
    }

    /************************************************************************
     *     COLUMNS
     **********************************************************************/
    @Override
    public String getTagID() {
        return properties.get("tagID").toString();
    }

    @Override
    public String getTagLocation() {
        return properties.get("tagLocation").toString();
    }

    @Override
    public String getDestination() {
        return properties.get("destination").toString();
    }

    @Override
    public String getLatitude() {
        return properties.get("latitute").toString();
    }

    @Override
    public String getLongitude() {
        return properties.get("longitude").toString();
    }

    @Override
    public String getCount() {
        return properties.get("count").toString();
    }
}
