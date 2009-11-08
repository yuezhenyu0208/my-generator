/*
 *  Copyright 2009 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.ibatis.ibator.generator.ibatis3.xmlmapper.elements;

import org.apache.ibatis.ibator.api.IntrospectedColumn;
import org.apache.ibatis.ibator.api.dom.java.FullyQualifiedJavaType;
import org.apache.ibatis.ibator.api.dom.xml.Attribute;
import org.apache.ibatis.ibator.api.dom.xml.TextElement;
import org.apache.ibatis.ibator.api.dom.xml.XmlElement;
import org.apache.ibatis.ibator.config.GeneratedKey;
import org.apache.ibatis.ibator.generator.ibatis3.Ibatis3FormattingUtilities;

/**
 * 
 * @author Jeff Butler
 *
 */
public class InsertSelectiveElementGenerator extends AbstractXmlElementGenerator {

    public InsertSelectiveElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("insert"); //$NON-NLS-1$

        answer.addAttribute(new Attribute("id", introspectedTable.getInsertSelectiveStatementId())); //$NON-NLS-1$
        
        FullyQualifiedJavaType parameterType =
            introspectedTable.getRules().calculateAllFieldsClass();
        
        answer.addAttribute(new Attribute("parameterType", //$NON-NLS-1$
                parameterType.getFullyQualifiedName()));

        ibatorContext.getCommentGenerator().addComment(answer);

        GeneratedKey gk = introspectedTable.getGeneratedKey();

        if (gk != null && gk.isBeforeInsert()) {
            IntrospectedColumn introspectedColumn = introspectedTable.getColumn(gk.getColumn());
            // if the column is null, then it's a configuration error. The
            // warning has already been reported
            if (introspectedColumn != null) {
                // pre-generated key
                answer.addElement(getSelectKey(introspectedColumn, gk));
            }
        }
        
        StringBuilder sb = new StringBuilder();

        sb.append("insert into "); //$NON-NLS-1$
        sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));
        
        XmlElement insertElement = new XmlElement("trim"); //$NON-NLS-1$
        insertElement.addAttribute(new Attribute("prefix", "(")); //$NON-NLS-1$ //$NON-NLS-2$
        insertElement.addAttribute(new Attribute("suffix", ")")); //$NON-NLS-1$ //$NON-NLS-2$
        insertElement.addAttribute(new Attribute("suffixOverrides", ",")); //$NON-NLS-1$ //$NON-NLS-2$
        answer.addElement(insertElement);
        
        XmlElement valuesElement = new XmlElement("trim"); //$NON-NLS-1$
        valuesElement.addAttribute(new Attribute("prefix", "values (")); //$NON-NLS-1$ //$NON-NLS-2$
        valuesElement.addAttribute(new Attribute("suffix", ")")); //$NON-NLS-1$ //$NON-NLS-2$
        valuesElement.addAttribute(new Attribute("suffixOverrides", ",")); //$NON-NLS-1$ //$NON-NLS-2$
        answer.addElement(valuesElement);
        
        for (IntrospectedColumn introspectedColumn : introspectedTable.getAllColumns()) {
            if (introspectedColumn.isIdentity()) {
                // cannot set values on identity fields
                continue;
            }
            
            XmlElement insertNotNullElement = new XmlElement("if"); //$NON-NLS-1$
            sb.setLength(0);
            sb.append(introspectedColumn.getJavaProperty());
            sb.append(" != null");
            insertNotNullElement.addAttribute(new Attribute("test", sb.toString())); //$NON-NLS-1$

            sb.setLength(0);
            sb.append(Ibatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            sb.append(',');
            insertNotNullElement.addElement(new TextElement(sb.toString()));
            insertElement.addElement(insertNotNullElement);
            
            XmlElement valuesNotNullElement = new XmlElement("if"); //$NON-NLS-1$
            sb.setLength(0);
            sb.append(introspectedColumn.getJavaProperty());
            sb.append(" != null");
            valuesNotNullElement.addAttribute(new Attribute("test", sb.toString())); //$NON-NLS-1$
            
            sb.setLength(0);
            sb.append(Ibatis3FormattingUtilities.getParameterClause(introspectedColumn));
            sb.append(',');
            valuesNotNullElement.addElement(new TextElement(sb.toString()));
            valuesElement.addElement(valuesNotNullElement);
        }
        
        if (gk != null && !gk.isBeforeInsert()) {
            IntrospectedColumn introspectedColumn = introspectedTable.getColumn(gk.getColumn());
            // if the column is null, then it's a configuration error. The
            // warning has already been reported
            if (introspectedColumn != null) {
                // pre-generated key
                answer.addElement(getSelectKey(introspectedColumn, gk));
            }
        }

        if (ibatorContext.getPlugins().sqlMapInsertSelectiveElementGenerated(answer, introspectedTable)) {
            parentElement.addElement(answer);
        }
    }
}
