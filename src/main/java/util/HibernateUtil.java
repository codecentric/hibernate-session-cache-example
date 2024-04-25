package util;


import entities.SomeEntity;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.h2.jdbcx.JdbcDataSource;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.dialect.H2Dialect;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static org.hibernate.cfg.AvailableSettings.AUTOCOMMIT;
import static org.hibernate.cfg.Environment.*;

public final class HibernateUtil {

    private HibernateUtil() {
    }

    public static Session getSession() {
        return buildSessionFactory().openSession();
    }

    private static SessionFactory buildSessionFactory() {
        try {
            final Map<String, Object> settings = new HashMap<>();
            settings.put(DATASOURCE, buildProxyDataSource());
            settings.put(HBM2DDL_AUTO, "create-drop");
            settings.put(DIALECT, H2Dialect.class.getName());
            settings.put(AUTOCOMMIT, false);

            final StandardServiceRegistryBuilder standardRegistryBuilder = new StandardServiceRegistryBuilder();
            standardRegistryBuilder.applySettings(settings);

            final StandardServiceRegistry standardRegistry = standardRegistryBuilder.build();

            final MetadataSources metadataSources = new MetadataSources(standardRegistry)
                    .addAnnotatedClass(SomeEntity.class);

            final Metadata metadata = metadataSources.getMetadataBuilder().build();
            return metadata.buildSessionFactory();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static DataSource buildProxyDataSource() {
        return ProxyDataSourceBuilder.create(buildDataSource())
                .name("ProxyDataSource")
                .countQuery()
                .build();
    }

    private static DataSource buildDataSource() {
        final JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test_db;DB_CLOSE_DELAY=-1");
        return dataSource;
    }
}
