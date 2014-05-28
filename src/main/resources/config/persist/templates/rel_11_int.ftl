package ${rel_interface};

import persist.relation.Relation11;
<#list imports as import>
import ${import};
</#list>

<#include "cls_com.ftl">
public interface ${name} extends Relation11<${from.type},${to.type}> {

    ${from.type} get${from.role}(${to.type} ${to.name}<#if user??>, ${user.type} ${user.name}</#if>);

    ${to.type} get${to.role}(${from.type} ${from.name}<#if user??>, ${user.type} ${user.name}</#if>);

}