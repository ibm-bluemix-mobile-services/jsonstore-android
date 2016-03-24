/*
 *     Copyright 2016 IBM Corp.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.jsonstore.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SQLiteStandardDatabase implements Database<SQLiteDatabase> {

    private SQLiteDatabase database;
    @Override
    public SQLiteDatabase openDatabase(String path, String databaseKey, int flags, Context context) {
       return null;
    }

    @Override
    public SQLiteDatabase openDatabase(String path, int flags) {
        this.database = SQLiteDatabase.openDatabase(path, null, flags);
        return this.database;
    }

    @Override
    public Cursor rawQuery(String sql, String[] selectionArgs) {
        return this.database.rawQuery(sql, selectionArgs);
    }

    @Override
    public void setTransactionSuccessful(){
        this.database.setTransactionSuccessful();
    }

    @Override
    public void endTransaction(){
        this.database.endTransaction();
    }



    @Override
    public void close(){
        this.database.close();
    }

    @Override
    public boolean isOpen(){
        return this.database.isOpen();
    }

    @Override
    public void beginTransaction(){
        this.database.beginTransaction();
    }

    @Override
    public long insert(String table, String nullColumnHack, ContentValues values) {
        return this.database.insert(table, nullColumnHack, values);
    }

    @Override
    public int delete(String table, String whereClause, String[] whereArgs) {
        return this.database.delete(table, whereClause, whereArgs);
    }

    @Override
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        return this.database.update(table, values, whereClause, whereArgs);
    }

    @Override
    public void execSQL(String sql){
        this.database.execSQL(sql);
    }
}
