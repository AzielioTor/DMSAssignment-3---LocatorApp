package com.sezielioter.locator.Utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Terry on 17/05/16.
 * Co-authored by Sez since 19/05/16
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_TABLE="fav_loc";
    public static final String DATABASE_NAME = "favourites";
    public static final String ROW_ID="rowid";
    public static final String LOC_COL="location";
    public static final String LAT_COL="latitude";
    public static final String LONG_COL="longitude";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE +
                "("   + ROW_ID + " INTEGER PRIMARY KEY, " +
                LOC_COL + " TEXT, " +
                LAT_COL + " DOUBLE, " +
                LONG_COL + " DOUBLE" +
                ")");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
        onCreate(db);
    }

    /**
     * Inserts new favourite destination into the database
     * @param locationName The name of the destination being saved
     * @param latitude The latitudinal geo ref of the destination, Double(10,6)
     * @param longitude The longitudinal geo ref of the destination, Double(10,6)
     * @return true if the destination is added to the database, false if the destination is already in the DB or if it is otherwise not added
     */
    public boolean insertFavourite  (String locationName, Double latitude, Double longitude)
    {
        SQLiteDatabase db = null;
        Cursor currentFaves = null;
        int locNameIndex;
        String tempLoc = "";

/**   CHECK IF DESTINATION IS ALREADY IN DB  ************/
        try {
            currentFaves = getFaveNamesCursor();  //Gets existing list
            locNameIndex = currentFaves.getColumnIndex(LOC_COL);
           // currentFaves.moveToNext();

            while(tempLoc != null
                && !( tempLoc.equals(locationName) )
                && currentFaves.moveToNext() )
            {
                    tempLoc = currentFaves.getString(locNameIndex);
                    System.out.println("tempLoc = " + tempLoc);
            }

/** ADD NEW FAVOURITE DESTINATION IF NOT ALREADY IN DB *************/
            if ( (tempLoc == null)
                || (!tempLoc.equals(locationName)) )
            {
                db = this.getWritableDatabase();
                ContentValues contentValues = new ContentValues();
                contentValues.put(LOC_COL, locationName);
                contentValues.put(LAT_COL, latitude);
                contentValues.put(LONG_COL, longitude);
                db.insert(DATABASE_TABLE, null, contentValues);
                return true;
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        finally{
            if(currentFaves != null) currentFaves.close();
            if(db != null) db.close();
        }
        return false;
    }

    /**
     * Gets an ArrayList of all the destinations saved to favourites
     * @return List of destinations saved to favourites
     */
    public ArrayList<String> getDestinationNames(){
        ArrayList resultList = null;
        Cursor resultSet = null;

        try {
            resultSet =  getFaveNamesCursor();
            resultList = new ArrayList();
            while(resultSet.moveToNext()){
                resultList.add(resultSet.getString(0));
            }
        } catch(SQLException e){
            e.printStackTrace();
        } finally{
            if(resultSet != null) resultSet.close();
        }

        return resultList;
    }

    /**
     * Returns a Cursor with all the names of the favourite destinations
      * @return Cursor with the names of the favourite destinations
     * @throws SQLException
     */
    public Cursor getFaveNamesCursor() throws SQLException{
        SQLiteDatabase db = this.getReadableDatabase();

         Cursor   resultSet =  db.rawQuery( "select " + LOC_COL + " from " +
                    DATABASE_TABLE, null );

        return resultSet;
    }

    /**
     * Returns the latitude and longitudinal goe reference for the specified destination
     * @param locationName Lat/Long values of the specified destination geo reference
     * @return
     */
    public LatLng getDestinationGeoRef(String locationName){
        LatLng location = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor results = db.rawQuery("SELECT "  + LAT_COL + ", "
                                                + LONG_COL +
                                    " FROM " + DATABASE_TABLE +
                                    " WHERE " + LOC_COL + "='" + locationName + "'"
                                    , null);
        if(results.moveToNext()){
            location = new LatLng(results.getDouble(results.getColumnIndex(LAT_COL)),
                                    results.getDouble(results.getColumnIndex(LONG_COL)));
        }
        return location;
    }

    /**
     * Removes a single row from the database, as specified by the destination name
     * @param locationName name of the destination location which is to be removed
     * @return integer as specified by the SQLiteDatabase.delete() method
     */
    public Integer deleteFavourite (String locationName)
    {
        System.out.println("deleteFavourites() - " + locationName);
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            String whereClause = LOC_COL + "=?";
            String[] whereArgs = {locationName};
            return db.delete(DATABASE_TABLE, whereClause, whereArgs);
        }
        finally{
           if(db != null) db.close();
        }
    }
}
