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
import android.database.Cursor;
import android.content.Context;


public interface Database<T> {

    public T openDatabase(String path, String databaseKey, int flags, Context context);

    public T openDatabase(String path, int flags);

    public Cursor rawQuery(String sql, String[] selectionArgs);

    public void close();

    public boolean isOpen();

    public void beginTransaction();

    public void setTransactionSuccessful();

    public void endTransaction();

    public long insert(String table, String nullColumnHack, ContentValues values);

    public int delete(String table, String whereClause, String[] whereArgs);

    public int update(String table, ContentValues values, String whereClause, String[] whereArgs);

    public void execSQL(String sql);



}
