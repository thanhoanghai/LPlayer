package com.baby.dbdownloader;

import java.util.Map;

import android.database.sqlite.SQLiteDatabase;
import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.AbstractDaoSession;
import de.greenrobot.dao.identityscope.IdentityScopeType;
import de.greenrobot.dao.internal.DaoConfig;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see de.greenrobot.dao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig movieObjectDaoConfig;

    private final MovieObjectDao movieObjectDao;

    public DaoSession(SQLiteDatabase db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        movieObjectDaoConfig = daoConfigMap.get(MovieObjectDao.class).clone();
        movieObjectDaoConfig.initIdentityScope(type);

        movieObjectDao = new MovieObjectDao(movieObjectDaoConfig, this);

        registerDao(MovieObject.class, movieObjectDao);
    }
    
    public void clear() {
        movieObjectDaoConfig.getIdentityScope().clear();
    }

    public MovieObjectDao getMovieObjectDao() {
        return movieObjectDao;
    }

}
