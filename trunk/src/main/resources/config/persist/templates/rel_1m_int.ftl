package ${rel_interface};

import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.<#if ordered>OrderedRelation1M<#else>Relation1M</#if>;
<#list imports as import>
import ${import};
</#list>

<#include "cls_com.ftl">
public interface ${name} extends <#if ordered>OrderedRelation1M<#else>Relation1M</#if><${from.type},${to.type}> {

    Iterator<${to.type}> get${to.plural}(${from.type} ${from.name}, Filter<${to.type}> filter<#if user??>, ${user.type} ${user.name}</#if>);

    Iterator<${to.type}> get${to.plural}(${from.type} ${from.name}, Filter<${to.type}> filter<#if user??>, ${user.type} ${user.name}</#if>, int offset, int limit);

    ${from.type} get${from.role}(${to.type} ${to.name}<#if user??>, ${user.type} ${user.name}</#if>);

    void set${from.role}(${from.type} ${from.name}, ${to.type} ${to.name}<#if user??>, ${user.type} ${user.name}</#if>);
<#if ordered>

    ${to.type} get${to.role}(${from.type} ${from.name}<#if user??>, ${user.type} ${user.name}</#if>, int index);
</#if>

}