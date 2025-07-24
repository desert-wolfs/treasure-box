package com.douniu.box.gencode.plugin;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.List;

public class InsertKeyPlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean sqlMapInsertElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        // 获取主键列
        List<IntrospectedColumn> primaryKeyColumns = introspectedTable.getPrimaryKeyColumns();
        if (!primaryKeyColumns.isEmpty()) {
            IntrospectedColumn primaryKeyColumn = primaryKeyColumns.get(0);
            // 添加 useGeneratedKeys 属性
            element.addAttribute(new Attribute("useGeneratedKeys", "true"));
            // 添加 keyProperty 属性
            element.addAttribute(new Attribute("keyProperty", primaryKeyColumn.getJavaProperty()));
        }
        return true;
    }

    @Override
    public boolean sqlMapInsertSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        // 对 insertSelective 方法也添加相同属性
        return sqlMapInsertElementGenerated(element, introspectedTable);
    }
}