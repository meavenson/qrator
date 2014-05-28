package ${rel_interface};

import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.<#if ordered>OrderedRelationM1<#else>RelationM1</#if>;
<#list imports as import>
import ${import};
</#list>

<#include "cls_com.ftl">
public interface ${name} extends <#if ordered>OrderedRelationM1<#else>RelationM1</#if><${from.type},${to.type}> {

    Iterator<${from.type}> get${from.plural}(${to.type} ${to.name}, Filter<${from.type}> filter<#if user??>, ${user.type} ${user.name}</#if>);

    Iterator<${from.type}> get${from.plural}(${to.type} ${to.name}, Filter<${from.type}> filter<#if user??>, ${user.type} ${user.name}</#if>, int offset, int limit);

    ${to.type} get${to.role}(${from.type} ${from.name}<#if user??>, ${user.type} ${user.name}</#if>);

    void set${to.role}(${from.type} ${from.name}, ${to.type} ${to.name}<#if user??>, ${user.type} ${user.name}</#if>);
<#if ordered>

    ${from.type} get${from.role}(${to.type} ${to.name}<#if user??>, ${user.type} ${user.name}</#if>, int index);
</#if>

}