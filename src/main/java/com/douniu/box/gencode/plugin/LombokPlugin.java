package com.douniu.box.gencode.plugin;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;

import java.util.List;


public class LombokPlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // 添加 @Data 注解
        topLevelClass.addImportedType(new FullyQualifiedJavaType("lombok.Data"));
        topLevelClass.addAnnotation("@Data");

//        // 添加 @NoArgsConstructor 注解
//        topLevelClass.addImportedType(new FullyQualifiedJavaType("lombok.NoArgsConstructor"));
//        topLevelClass.addAnnotation("@NoArgsConstructor");
//
//        // 添加 @AllArgsConstructor 注解
//        topLevelClass.addImportedType(new FullyQualifiedJavaType("lombok.AllArgsConstructor"));
//        topLevelClass.addAnnotation("@AllArgsConstructor");

        // 添加 Serializable 接口
        FullyQualifiedJavaType serializable = new FullyQualifiedJavaType("java.io.Serializable");
        topLevelClass.addImportedType(serializable);
        topLevelClass.addSuperInterface(serializable);

        // 添加 serialVersionUID 字段
        Field serialVersionUID = new Field("serialVersionUID", new FullyQualifiedJavaType("long"));
        serialVersionUID.setFinal(true);
        serialVersionUID.setStatic(true);
        serialVersionUID.setVisibility(JavaVisibility.PRIVATE);
        serialVersionUID.setInitializationString("1L");
        topLevelClass.addField(serialVersionUID);
        return true;
    }

    @Override
    public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass,
                                              IntrospectedColumn introspectedColumn,
                                              IntrospectedTable introspectedTable,
                                              ModelClassType modelClassType) {
        // 阻止生成 Getter 方法
        return false;
    }

    @Override
    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass,
                                              IntrospectedColumn introspectedColumn,
                                              IntrospectedTable introspectedTable,
                                              ModelClassType modelClassType) {
        // 阻止生成 Setter 方法
        return false;
    }
}