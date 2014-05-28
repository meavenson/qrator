package ${rel_interface};

import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.<#if ordered>OrderedRelationMM<#else>RelationMM</#if>;
<#list imports as import>
import ${import};
</#list>

<#include "cls_com.ftl">
public interface ${name} extends <#if ordered>OrderedRelationMM<#else>RelationMM</#if><${from.type},${to.type}> {

    Iterator<${from.type}> get${from.plural}(${to.type} ${to.name}, Filter<${from.type}> filter<#if user??>, ${user.type} ${user.name}</#if>);

    Iterator<${to.type}> get${to.plural}(${from.type} ${from.name}, Filter<${to.type}> filter<#if user??>, ${user.type} ${user.name}</#if>);

    Iterator<${from.type}> get${from.plural}(${to.type} ${to.name}, Filter<${from.type}> filter<#if user??>, ${user.type} ${user.name}</#if>, int offset, int limit);

    Iterator<${to.type}> get${to.plural}(${from.type} ${from.name}, Filter<${to.type}> filter<#if user??>, ${user.type} ${user.name}</#if>, int offset, int limit);
<#if ordered>

    ${from.type} get${from.role}(${to.type} ${to.name}<#if user??>, ${user.type} ${user.name}</#if>, int index);

    ${to.type} get${to.role}(${from.type} ${from.name}<#if user??>, ${user.type} ${user.name}</#if>, int index);
</#if>

}