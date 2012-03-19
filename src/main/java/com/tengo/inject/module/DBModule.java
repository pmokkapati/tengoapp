package com.tengo.inject.module;
import com.google.inject.AbstractModule;
import javax.sql.DataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class DBModule extends AbstractModule {
    private final DataSource _d;

    public DBModule() {
        MysqlDataSource source = new MysqlDataSource();
        source.setServerName("xchanjerds");
        source.setDatabaseName("xchanjedb");
        source.setUser("prasad");
        source.setPassword("aka1198");
        _d = source;
    }

    @Override 
    protected void configure() {
        bind(javax.sql.DataSource.class).toInstance(_d);
    }
}
