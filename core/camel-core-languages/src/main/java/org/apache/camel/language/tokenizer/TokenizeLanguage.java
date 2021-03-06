/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.language.tokenizer;

import java.util.Map;

import org.apache.camel.Expression;
import org.apache.camel.Predicate;
import org.apache.camel.support.ExpressionToPredicateAdapter;
import org.apache.camel.support.LanguageSupport;
import org.apache.camel.support.builder.ExpressionBuilder;
import org.apache.camel.util.ObjectHelper;

/**
 * A language for tokenizer expressions.
 * <p/>
 * This tokenizer language can operator in the following modes:
 * <ul>
 * <li>default - using a single tokenizer</li>
 * <li>pair - using both start and end tokens</li>
 * <li>xml - using both start and end tokens in XML mode, support inheriting namespaces</li>
 * </ul>
 * The default mode supports the <tt>headerName</tt> and <tt>regex</tt> options. Where as the pair mode only supports
 * <tt>token</tt> and <tt>endToken</tt>. And the <tt>xml</tt> mode supports the <tt>inheritNamespaceTagName</tt> option.
 */
@org.apache.camel.spi.annotations.Language("tokenize")
public class TokenizeLanguage extends LanguageSupport {

    private String token;
    private String endToken;
    private String inheritNamespaceTagName;
    private String headerName;
    private boolean regex;
    private boolean xml;
    private boolean includeTokens;
    private String group;
    private String groupDelimiter;
    private boolean skipFirst;

    @Deprecated
    public static Expression tokenize(String token) {
        return tokenize(token, false);
    }

    @Deprecated
    public static Expression tokenize(String token, boolean regex) {
        TokenizeLanguage language = new TokenizeLanguage();
        language.setToken(token);
        language.setRegex(regex);
        return language.createExpression((String) null);
    }

    @Deprecated
    public static Expression tokenize(String headerName, String token) {
        return tokenize(headerName, token, false);
    }

    @Deprecated
    public static Expression tokenize(String headerName, String token, boolean regex) {
        TokenizeLanguage language = new TokenizeLanguage();
        language.setHeaderName(headerName);
        language.setToken(token);
        language.setRegex(regex);
        return language.createExpression((String) null);
    }

    @Deprecated
    public static Expression tokenizePair(String startToken, String endToken, boolean includeTokens) {
        TokenizeLanguage language = new TokenizeLanguage();
        language.setToken(startToken);
        language.setEndToken(endToken);
        language.setIncludeTokens(includeTokens);
        return language.createExpression((String) null);
    }

    @Deprecated
    public static Expression tokenizeXML(String tagName, String inheritNamespaceTagName) {
        TokenizeLanguage language = new TokenizeLanguage();
        language.setToken(tagName);
        language.setInheritNamespaceTagName(inheritNamespaceTagName);
        language.setXml(true);
        return language.createExpression((String) null);
    }

    @Override
    public Predicate createPredicate(String expression) {
        return ExpressionToPredicateAdapter.toPredicate(createExpression(expression));
    }

    /**
     * Creates a tokenize expression.
     */
    public Expression createExpression() {
        ObjectHelper.notNull(token, "token");

        // validate some invalid combinations
        if (endToken != null && inheritNamespaceTagName != null) {
            throw new IllegalArgumentException("Cannot have both xml and pair tokenizer enabled.");
        }
        if (isXml() && (endToken != null || includeTokens)) {
            throw new IllegalArgumentException("Cannot have both xml and pair tokenizer enabled.");
        }

        Expression answer = null;
        if (isXml()) {
            answer = ExpressionBuilder.tokenizeXMLExpression(token, inheritNamespaceTagName);
        } else if (endToken != null) {
            answer = ExpressionBuilder.tokenizePairExpression(token, endToken, includeTokens);
        }

        if (answer == null) {
            // use the regular tokenizer
            Expression exp
                    = headerName == null ? ExpressionBuilder.bodyExpression() : ExpressionBuilder.headerExpression(headerName);
            if (regex) {
                answer = ExpressionBuilder.regexTokenizeExpression(exp, token);
            } else {
                answer = ExpressionBuilder.tokenizeExpression(exp, token);
            }
            if (group == null && skipFirst) {
                // wrap in skip first (if group then it has its own skip first logic)
                answer = ExpressionBuilder.skipFirstExpression(answer);
            }
        }

        // if group then wrap answer in group expression
        if (group != null) {
            if (isXml()) {
                answer = ExpressionBuilder.groupXmlIteratorExpression(answer, group);
            } else {
                String delim = groupDelimiter != null ? groupDelimiter : token;
                answer = ExpressionBuilder.groupIteratorExpression(answer, delim, group, skipFirst);
            }
        }

        if (getCamelContext() != null) {
            answer.init(getCamelContext());
        }
        return answer;
    }

    @Override
    public Expression createExpression(String expression) {
        if (ObjectHelper.isNotEmpty(expression)) {
            this.token = expression;
        }
        return createExpression();
    }

    @Override
    public Predicate createPredicate(String expression, Map<String, Object> properties) {
        return ExpressionToPredicateAdapter.toPredicate(createExpression(expression, properties));
    }

    @Override
    public Expression createExpression(String expression, Map<String, Object> properties) {
        TokenizeLanguage answer = new TokenizeLanguage();
        answer.setInheritNamespaceTagName(
                property(String.class, properties, "inheritNamespaceTagName", inheritNamespaceTagName));
        answer.setToken(property(String.class, properties, "token", token));
        answer.setEndToken(property(String.class, properties, "endToken", endToken));
        answer.setHeaderName(property(String.class, properties, "headerName", headerName));
        answer.setRegex(property(boolean.class, properties, "regex", regex));
        answer.setXml(property(boolean.class, properties, "xml", xml));
        answer.setIncludeTokens(property(boolean.class, properties, "includeTokens", includeTokens));
        answer.setGroup(property(String.class, properties, "group", group));
        answer.setGroupDelimiter(property(String.class, properties, "groupDelimiter", groupDelimiter));
        answer.setSkipFirst(property(boolean.class, properties, "skipFirst", skipFirst));
        return answer.createExpression(expression);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEndToken() {
        return endToken;
    }

    public void setEndToken(String endToken) {
        this.endToken = endToken;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public boolean isRegex() {
        return regex;
    }

    public void setRegex(boolean regex) {
        this.regex = regex;
    }

    public String getInheritNamespaceTagName() {
        return inheritNamespaceTagName;
    }

    public void setInheritNamespaceTagName(String inheritNamespaceTagName) {
        this.inheritNamespaceTagName = inheritNamespaceTagName;
    }

    public boolean isXml() {
        return xml;
    }

    public void setXml(boolean xml) {
        this.xml = xml;
    }

    public boolean isIncludeTokens() {
        return includeTokens;
    }

    public void setIncludeTokens(boolean includeTokens) {
        this.includeTokens = includeTokens;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = "0".equals(group) ? null : group;
    }

    public String getGroupDelimiter() {
        return groupDelimiter;
    }

    public void setGroupDelimiter(String groupDelimiter) {
        this.groupDelimiter = groupDelimiter;
    }

    public boolean isSkipFirst() {
        return skipFirst;
    }

    public void setSkipFirst(boolean skipFirst) {
        this.skipFirst = skipFirst;
    }

}
