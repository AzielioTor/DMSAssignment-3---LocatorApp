/**
 * AUT DMS S1 2016
 * Assignment : - Android Distributed Application
 *  Green, Terry (0829446)
 *  Prouting, Sez (0308852)
 *  Shaw, Aziel (14847095)
 *  
 *  @author Sez
 */
package com.sezielioter.androiddbaccess;

import java.io.Serializable;

public class TagData implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private String tagID, destinationName, tagLocation;
    private double latitude, longitude;
    private int count=-1;
    
    public TagData(){
    }
   
// ****************************************************************************
//        GETTERS
// ****************************************************************************

    /**
     * Provides access to the ID string of (this) NFC tag
     * @return String representation of the NFC tag's unique identifier
     */
    public String getTagID() {
        return tagID;
    }

    /**
     * Provides a human readable destination which is associated with the tag.
     * This is assigned by the database administrator and may not be unique.
     * @return Destination name
     */
    public String getDestinationName() {
        return destinationName;
    }

    /**
     * Provides the (human-readable) current position of the tag, as recorded in
     * the database. There is no guarantee this will match the position as recorded
     * in the tag itself.
     * @return Tag current location
     */
    public String getTagLocation() {
        return tagLocation;
    }

    /**
     * Provides the latitude part of the geo coordinate which is assigned as
     * this tag's destination
     * @return Destination latitude
     */
    public double getDestinationLatitude() {
        return latitude;
    }

    /**
     * Provides the longitude part of the geo coordinate which is assigned as
     * this tag's destination
     * @return Destination Longitude
     */
    public double getDestinationLongitude() {
        return longitude;
    }

    /**
     * Provides a count of the number of times this tag's  database record has 
     * been accessed
     * @return Database access count
     */
    public int getCount() {
        return count;
    }
    
    
// ****************************************************************************
//        SETTERS
// ****************************************************************************

    
    public void setTagID(String tagID) {
        this.tagID = tagID;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public void setTagLocation(String tagLocation) {
        this.tagLocation = tagLocation;
    }

    public void setDestinationLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setDestinationLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setCount(int count) {
        this.count = count;
    }
    
    public String toString(){
       return   "ID: " + tagID + "\n" +
                "Location: " +tagLocation+ "\n" +
                "Destination: " + destinationName+ "\n" +
                "Latitude: " + latitude + "\n" +
                "Longitude: " + longitude + "\n" +
                "Count: " + count;
    }
}

