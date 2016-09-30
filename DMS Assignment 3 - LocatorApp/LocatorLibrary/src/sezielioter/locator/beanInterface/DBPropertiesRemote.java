/*
 * AUT DMS S1 2016
 * Assignment : - Android Distributed Application
 *  Green, Terry (0829446)
 *  Prouting, Sez (0308852)
 *  Shaw, Aziel (14847095)
 *  
 *  
 */
package sezielioter.locator.beanInterface;

import javax.ejb.Remote;

@Remote
public interface DBPropertiesRemote {

    String getDBDriver();

    String getDbUrl();

    String getUserName();

    String getPassword();

    String getTable();

    String getTagID();

    String getTagLocation();

    String getDestination();

    String getLatitude();

    String getLongitude();

    String getCount();
    
}
