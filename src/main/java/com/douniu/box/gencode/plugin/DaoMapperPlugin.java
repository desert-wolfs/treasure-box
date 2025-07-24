package com.douniu.box.gencode.plugin;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;

import java.lang.reflect.Field;
import java.util.List;

public class DaoMapperPlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean clientGenerated(Interface interfaze, IntrospectedTable introspectedTable) {
        // 获取原接口的全限定类名
        FullyQualifiedJavaType originalType = interfaze.getType();
        // 获取原接口短名称
        String originalShortName = originalType.getShortName();
        // 将 Mapper 替换为 Dao
        String newShortName = originalShortName.replace("Mapper", "Dao");
        // 获取原接口包名
        String packageName = originalType.getPackageName();
        // 创建新的全限定类名
        FullyQualifiedJavaType newType = new FullyQualifiedJavaType(packageName + "." + newShortName);
        // 修改接口的类型
        try {
            Field typeField = interfaze.getClass().getSuperclass().getSuperclass().getDeclaredField("type");
            typeField.setAccessible(true);
            typeField.set(interfaze, newType);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 修改 IntrospectedTable 中的 MyBatis 3 Java Mapper 类型
        introspectedTable.setMyBatis3JavaMapperType(newType.getFullyQualifiedName());

        // 修改 XML 映射文件名称
        String originalXmlMapperName = introspectedTable.getMyBatis3XmlMapperFileName();
        String newXmlMapperName = originalXmlMapperName.replace("Mapper", "Dao");
        introspectedTable.setMyBatis3XmlMapperFileName(newXmlMapperName);


        // 导入 @Mapper 注解类
        FullyQualifiedJavaType mapperAnnotation = new FullyQualifiedJavaType("org.apache.ibatis.annotations.Mapper");
        interfaze.addImportedType(mapperAnnotation);
        // 添加 @Mapper 注解
        interfaze.addAnnotation("@Mapper");
        return true;
    }
}