package comdailyweightfatcontrol.httpsgithub.dailyfatcontrol_androidapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

public class DataBaseHR extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_DIR = "daily_fat_control";
    private static final String DATABASE_NAME = "database_hr.db";
    private static final String TABLE_NAME = "calories_out";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_HR_VALUE = "hr_value";

    public DataBaseHR(Context context) {
        super(context, Environment.getExternalStorageDirectory()
                + File.separator + DATABASE_DIR
                + File.separator + DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_DATE + " integer UNIQUE, " + /* UNIQUE means that there will not be duplicate entries with the same date */
                COLUMN_HR_VALUE + " integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void DataBaseWriteMeasurement (ArrayList<Measurement> measurementList) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        for (Measurement measurement : measurementList) {
            values.put(COLUMN_DATE, measurement.getDate());
            values.put(COLUMN_HR_VALUE, measurement.getHRValue());

            // Inserting Row
            db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }

        db.close(); // Closing database connection
    }

    public ArrayList<Measurement> DataBaseGetLastDayMeasurements () {

        // Get the MidNightToday and RightNow date values
        Calendar rightNow = Calendar.getInstance();
        long offset = rightNow.get(Calendar.ZONE_OFFSET) + rightNow.get(Calendar.DST_OFFSET);
        long rightNowMillis = rightNow.getTimeInMillis() + offset;
        long sinceMidnightToday = rightNowMillis % (24 * 60 * 60 * 1000);
        long midNightToday = rightNowMillis - sinceMidnightToday;
        rightNowMillis /= 1000; // now in seconds
        midNightToday /= 1000; // now in seconds

        // Query to get all the records starting at last midnight, ordered by date ascending
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_DATE + " BETWEEN " +
                + midNightToday + " AND " + rightNowMillis + " ORDER BY " + COLUMN_DATE +
                " ASC";

        Cursor cursor = db.rawQuery(query, null);

        // Loop to put all the values to the ArrayList<Measurement>
        cursor.moveToFirst();
        int counter = cursor.getCount();
        int date;
        int hr;
        ArrayList<Measurement> measurementList = new ArrayList<Measurement>();
        for ( ; counter > 0; ) {
            if (cursor.isAfterLast()) break;
            date = cursor.getInt(cursor.getColumnIndex(COLUMN_DATE));
            hr = cursor.getInt(cursor.getColumnIndex(COLUMN_HR_VALUE));
            cursor.moveToNext();

            Measurement measurement = new Measurement();
            measurement.setDate(date);
            measurement.setHRValue(hr);
            measurementList.add(measurement);
        }

        cursor.close();
        db.close(); // Closing database connection
        return measurementList;
    }

    public ArrayList<Measurement> DataBaseGetMeasurements (long initialDate, long finalDate) {
        // Query to get all the records starting at last midnight, ordered by date ascending
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_DATE + " BETWEEN " +
                + initialDate + " AND " + finalDate + " ORDER BY " + COLUMN_DATE +
                " ASC";

        Cursor cursor = db.rawQuery(query, null);

        // Loop to put all the values to the ArrayList<Measurement>
        cursor.moveToFirst();
        int counter = cursor.getCount();
        int date;
        int hr;
        ArrayList<Measurement> measurementList = new ArrayList<Measurement>();
        for ( ; counter > 0; ) {
            if (cursor.isAfterLast()) break;
            date = cursor.getInt(cursor.getColumnIndex(COLUMN_DATE));
            hr = cursor.getInt(cursor.getColumnIndex(COLUMN_HR_VALUE));
            cursor.moveToNext();

            Measurement measurement = new Measurement();
            measurement.setDate(date);
            measurement.setHRValue(hr);
            measurementList.add(measurement);
        }

        cursor.close();
        db.close(); // Closing database connection
        return measurementList;
    }

    public long DataBaseGetLastMeasurementDate() {
        SQLiteDatabase db = this.getWritableDatabase();
        // build the query
        String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_DATE + " DESC LIMIT 1";
        // open database
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

        long date = 0;
        if (cursor.getCount() > 0) {
            date = cursor.getInt(cursor.getColumnIndex(COLUMN_DATE));
        }

        cursor.close();
        db.close(); // Closing database connection
        return date;
    }
}
