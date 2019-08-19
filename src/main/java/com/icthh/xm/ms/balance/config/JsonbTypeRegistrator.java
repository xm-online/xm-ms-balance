package com.icthh.xm.ms.balance.config;

import static java.lang.Boolean.TRUE;
import static org.hibernate.usertype.DynamicParameterizedType.IS_DYNAMIC;
import static org.hibernate.usertype.DynamicParameterizedType.RETURNED_CLASS;

import com.icthh.xm.ms.balance.domain.BalanceChangeEvent;
import com.icthh.xm.ms.balance.domain.Pocket;
import com.icthh.xm.ms.balance.domain.PocketChangeEvent;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import java.util.Map;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.internal.MetadataImpl;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.boot.spi.SessionFactoryBuilderFactory;
import org.hibernate.boot.spi.SessionFactoryBuilderImplementor;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.SimpleValue;

@Slf4j
public class JsonbTypeRegistrator implements SessionFactoryBuilderFactory {

    @Override
    public SessionFactoryBuilder getSessionFactoryBuilder(final MetadataImplementor metadata, final SessionFactoryBuilderImplementor defaultBuilder) {

        //metadata.getTypeResolver().registerTypeOverride();

        log.info("Registering custom Hibernate data types");
        update((MetadataImpl) metadata, Pocket.class);
        update((MetadataImpl) metadata, BalanceChangeEvent.class);
        update((MetadataImpl) metadata, PocketChangeEvent.class);
        return defaultBuilder;
    }

    public void update(MetadataImpl metadata, Class<?> entityClass) {
        Property metadataProperty = metadata.getEntityBinding(entityClass.getCanonicalName()).getProperty("metadata");
        Component component = (Component) metadataProperty.getValue();
        Property jsonProperty = component.getProperty("json");
        String fieldClassName = Map.class.getCanonicalName();

        updateMetadata(metadata, jsonProperty, fieldClassName);
    }

    public void updateMetadata(MetadataImpl metadata, Property jsonProperty, String fieldClassName) {
        SimpleValue simpleValue = new SimpleValue(metadata);
        simpleValue.setTypeName(JsonBinaryType.class.getCanonicalName());
        if (simpleValue.getTypeParameters() == null) {
            simpleValue.setTypeParameters(new Properties());
        }
        simpleValue.getTypeParameters().setProperty(IS_DYNAMIC, TRUE.toString());
        simpleValue.getTypeParameters().setProperty(RETURNED_CLASS, fieldClassName);
        simpleValue.setJpaAttributeConverterDescriptor(null);
        ((SimpleValue) jsonProperty.getValue()).copyTypeFrom(simpleValue);
    }
}
