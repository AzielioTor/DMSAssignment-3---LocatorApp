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

import java.sql.SQLException;
import javax.ejb.Remote;

@Remote
public interface DBReaderRemote {

    String getDestinationName(final String id) throws SQLException;

    Object getTagData(String tadID) throws SQLException;
    
}
