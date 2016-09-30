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
import sezielioter.locator.beanInterface.DBReaderRemote;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import sezielioter.locator.data.TagData;

@Stateless
public class DBReader implements DBReaderRemote {

    @EJB
    protected DBPropertiesRemote properties;
    public static final String NOT_FOUND="Not Found";
    protected Connection connection;
    protected PreparedStatement getDestinationName, getTagData;

    @PostConstruct
    public void init(){    
        // DB CONNECTION
        try{
            Class.forName(properties.getDBDriver());
        }
        catch(ClassNotFoundException e){
            System.err.println("---------------> DB Driver class not found: " + e.getMessage());
        }
        
        try{            
            connection = DriverManager.getConnection(properties.getDbUrl(), properties.getUserName(), properties.getPassword());
            
            // Prepare Statements
            getDestinationName = connection.prepareStatement("SELECT " + properties.getDestination() +
                                                            " FROM " + properties.getTable() +
                                                            " WHERE " + properties.getTagID() +
                                                            " =?");
            
            getTagData = connection.prepareStatement("SELECT "  + properties.getTagLocation() + ", "
                                                                + properties.getDestination() + ", "
                                                                + properties.getLatitude() + ", "
                                                                + properties.getLongitude() + ", "
                                                                + properties.getCount() 
                                                    + " FROM "  + properties.getTable() 
                                                    + " WHERE " + properties.getTagID() + "=?");        
        }
        catch(SQLException e){
            System.out.println("------------>  Error during DBAccessor statement preparation: " + e.getMessage());
            System.out.println("--> connection was " + connection);
        }
    }

    @Override
    public synchronized String getDestinationName(final String id) throws SQLException {
        getDestinationName.setString(1, id);
        ResultSet rs = getDestinationName.executeQuery();
        
            try{
                if(rs.next())
                    return rs.getString(properties.getDestination());
            }
            finally{
                if(rs != null)
                    rs.close();
            }
            
        return NOT_FOUND;
    }

    @Override
    public synchronized TagData getTagData(String tagID) throws SQLException {
        
        getTagData.setString(1, tagID);
        System.out.println("=======>/n========> getTagData set String was executed");
        TagData tagData = new TagData();
        ResultSet dataSet = getTagData.executeQuery();
        
        try{
            if(dataSet.next()){
                System.out.println("-=-=-=-=-=- dataSet had next");
                tagData.setTagID(tagID);
                tagData.setDestinationLatitude(dataSet.getFloat(properties.getLatitude()));
                tagData.setDestinationLongitude(dataSet.getFloat(properties.getLongitude()));
                tagData.setDestinationName(dataSet.getString(properties.getDestination()));
                tagData.setTagLocation(dataSet.getString(properties.getTagLocation()));
                tagData.setCount(dataSet.getInt(properties.getCount()));
                return tagData;
            }
            else System.out.println("---====---===---=== data set did not contain any results");
        }
        finally{
            if(dataSet != null)
                dataSet.close();
        }
        
        return null;
    }
    
    
}
