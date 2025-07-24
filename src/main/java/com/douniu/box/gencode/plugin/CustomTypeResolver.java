package com.douniu.box.gencode.plugin;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.internal.types.JavaTypeResolverDefaultImpl;

import java.sql.Types;
import java.util.Properties;

public class CustomTypeResolver extends JavaTypeResolverDefaultImpl {

    @Override
    public void addConfigurationProperties(Properties properties) {
        super.addConfigurationProperties(properties);
    }

    @Override
    public FullyQualifiedJavaType calculateJavaType(IntrospectedColumn introspectedColumn) {
        if (introspectedColumn.getJdbcType() == Types.TINYINT) {
            // 将 tinyint 类型映射为 Integer
            return new FullyQualifiedJavaType(Integer.class.getName());
        }
        return super.calculateJavaType(introspectedColumn);
    }
}