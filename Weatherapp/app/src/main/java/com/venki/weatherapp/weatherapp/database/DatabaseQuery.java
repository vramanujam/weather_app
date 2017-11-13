package com.venki.weatherapp.weatherapp.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.venki.weatherapp.weatherapp.entity.DatabaseLocationObject;

import java.util.ArrayList;
import java.util.List;

public class DatabaseQuery extends DatabaseObject{

    private final String TABLE_NAME = "data";

    private final String METRIC_TABLE_NAME = "degree";

    private final String KEY_NAME = "_id";

    public DatabaseQuery(Context context) {
        super(context);
    }

    public void createTablesRequired()
    {
        String query = "CREATE TABLE IF NOT EXISTS currentlocation( id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT)";
        Cursor cursor = this.getDbConnection().rawQuery(query, null);
        cursor.close();
    }

    public List<DatabaseLocationObject> getStoredDataLocations(){
        List<DatabaseLocationObject> allLocations = new ArrayList<DatabaseLocationObject>();
        String query = "Select * from data";
        Cursor cursor = this.getDbConnection().rawQuery(query, null);
        if(cursor.moveToFirst()){
            do{
                int id = cursor.getInt(0);
                System.out.println("Response number " + id);
                String storedData = cursor.getString(cursor.getColumnIndexOrThrow("cotent"));
                System.out.println("Response number " + storedData);
                allLocations.add(new DatabaseLocationObject(id, storedData));
            }while (cursor.moveToNext());
        }
        cursor.close();
        return allLocations;
    }

    public String getCurrentCity(){
        //SELECT * FROM Table LIMIT 10 OFFSET 0
        String city = null;
        String query = "Select name from currentlocation limit 1 offset 0";
        Cursor cursor = this.getDbConnection().rawQuery(query, null);
        if(cursor.moveToFirst()){
            city = cursor.getString(0);
        }
        cursor.close();

        return city;
    }

    public String getCityByRowNum(int row){
        //SELECT * FROM Table LIMIT 10 OFFSET 0
        String city = null;
        String query = "Select cotent from data limit 1 offset " + row;
        Cursor cursor = this.getDbConnection().rawQuery(query, null);
        if(cursor.moveToFirst()){
            city = cursor.getString(0);
        }
        cursor.close();

        return city;
    }

    public int getDBIndex(String city){
        int id = -1;
        String query = "Select _id from data where cotent=" + "'"+city+"'";
        Cursor cursor = this.getDbConnection().rawQuery(query, null);
        if(cursor.moveToFirst()){
                id = cursor.getInt(0);
        }
        cursor.close();

        return id;
    }
    public int countAllStoredLocations(){
        int total = 0;
        String query = "Select * from data";
        Cursor cursor = this.getDbConnection().rawQuery(query, null);
        if(cursor.moveToFirst()){
            total = cursor.getCount();
        }
        cursor.close();
        return total;
    }

    public int getRowNumber(String Location){
        int rownum = -1;
        int dbIndex = getDBIndex(Location);
        String query = "select (select count(*) from data as t2 where t2._id <'"+dbIndex +"') as row_num from data as t1";
        Cursor cursor = this.getDbConnection().rawQuery(query, null);
        if(cursor.moveToFirst()){
            rownum = cursor.getInt(0);
        }
        cursor.close();
        System.out.println("getRowNumber " + rownum + " " + Location );
        return rownum;
    }
    private boolean isLocationExist(String location){
        String query = "Select * from data where cotent=" + "'"+location+"'";
        Cursor cursor = this.getDbConnection().rawQuery(query, null);
        if(cursor.moveToFirst()){
            return true;
        }
        cursor.close();
        return false;
    }

    private boolean isCurrentLocationExist(String location){
        String query = "Select * from currentlocation where name=" + "'"+location+"'";
        Cursor cursor = this.getDbConnection().rawQuery(query, null);
        if(cursor.moveToFirst()){
            return true;
        }
        cursor.close();
        return false;
    }

    public boolean insertNewLocation(String cityCountry){
        ContentValues values = new ContentValues();
        boolean bret = false;
        values.put("cotent", cityCountry);
        if(!isLocationExist(cityCountry)){
            bret = true;
            getDbConnection().insert(TABLE_NAME, null, values);
        }
        getDbConnection().close();
        return bret;
    }

    public boolean insertCurrentLocation(String cityName){
        ContentValues values = new ContentValues();
        boolean bret = false;
        values.put("id",1);
        values.put("name", cityName);
        getDbConnection().replace("currentlocation",null,values);
        //getDbConnection().close();
        return bret;
    }
	 public boolean insertUserDegreeMetric(String metric){
        ContentValues values = new ContentValues();
        values.put("id",1);
        values.put("degreeMetric", metric);
        getDbConnection().replace(METRIC_TABLE_NAME, null, values);
        //getDbConnection().close();
        return true;
    }

    public String getUserDegreeMetric () {
        String degMetric = "";
        String query = "Select degreeMetric from degree limit 1 offset 0";
        Cursor cursor = this.getDbConnection().rawQuery(query, null);
        if(cursor.moveToFirst()){
            degMetric = cursor.getString(0);
        }
        cursor.close();

        return degMetric;
    }
    public boolean deleteLocation(int locationId){
        return getDbConnection().delete(TABLE_NAME, KEY_NAME + "=" + locationId, null) > 0;
    }

    public void deleteLocation(String location){
        String query = "delete from data where cotent=" + "'"+location+"'";
        Cursor cursor = this.getDbConnection().rawQuery(query, null);
        cursor.close();
    }

    public void deleteAllLocationContent(){
        getDbConnection().execSQL("delete from " + TABLE_NAME);
    }
}
