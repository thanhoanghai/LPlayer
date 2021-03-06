package com.baby.dbdownloader;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table MOVIE_OBJECT.
*/
public class MovieObjectDao extends AbstractDao<MovieObject, Long> {

    public static final String TABLENAME = "MOVIE_OBJECT";

    /**
     * Properties of entity MovieObject.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property MovieId = new Property(1, String.class, "MovieId", false, "MOVIE_ID");
        public final static Property MovieTitle = new Property(2, String.class, "MovieTitle", false, "MOVIE_TITLE");
        public final static Property Poster = new Property(3, String.class, "Poster", false, "POSTER");
        public final static Property URLLink = new Property(4, String.class, "URLLink", false, "URLLINK");
        public final static Property Path = new Property(5, String.class, "Path", false, "PATH");
        public final static Property Downloaded = new Property(6, String.class, "Downloaded", false, "DOWNLOADED");
        public final static Property Status = new Property(7, String.class, "Status", false, "STATUS");
    };


    public MovieObjectDao(DaoConfig config) {
        super(config);
    }
    
    public MovieObjectDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'MOVIE_OBJECT' (" + //
                "'_id' INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "'MOVIE_ID' TEXT NOT NULL ," + // 1: MovieId
                "'MOVIE_TITLE' TEXT NOT NULL ," + // 2: MovieTitle
                "'POSTER' TEXT," + // 3: Poster
                "'URLLINK' TEXT," + // 4: URLLink
                "'PATH' TEXT," + // 5: Path
                "'DOWNLOADED' TEXT," + // 6: Downloaded
                "'STATUS' TEXT);"); // 7: Status
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'MOVIE_OBJECT'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, MovieObject entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getMovieId());
        stmt.bindString(3, entity.getMovieTitle());
 
        String Poster = entity.getPoster();
        if (Poster != null) {
            stmt.bindString(4, Poster);
        }
 
        String URLLink = entity.getURLLink();
        if (URLLink != null) {
            stmt.bindString(5, URLLink);
        }
 
        String Path = entity.getPath();
        if (Path != null) {
            stmt.bindString(6, Path);
        }
 
        String Downloaded = entity.getDownloaded();
        if (Downloaded != null) {
            stmt.bindString(7, Downloaded);
        }
 
        String Status = entity.getStatus();
        if (Status != null) {
            stmt.bindString(8, Status);
        }
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public MovieObject readEntity(Cursor cursor, int offset) {
        MovieObject entity = new MovieObject( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getString(offset + 1), // MovieId
            cursor.getString(offset + 2), // MovieTitle
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // Poster
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // URLLink
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // Path
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // Downloaded
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7) // Status
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, MovieObject entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setMovieId(cursor.getString(offset + 1));
        entity.setMovieTitle(cursor.getString(offset + 2));
        entity.setPoster(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setURLLink(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setPath(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setDownloaded(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setStatus(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(MovieObject entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(MovieObject entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
