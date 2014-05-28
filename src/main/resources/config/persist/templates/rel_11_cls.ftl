package ${rel_class};

import ${rel_interface}.${name};
import java.sql.Connection;
import persist.relation.sql.<#if ordered>OrderedRelation11SQL<#else>Relation11SQL</#if>;
<#list imports as import>
import ${import};
</#list>

<#include "cls_com.ftl">
public class ${name}Impl extends Relation11SQL<${from.type},${to.type}> implements ${name} {

    public ${name}Impl(Connection conn){
        super(conn, ${name}.class);
    }

    @Override
    public ${from.type} get${from.role}(${to.type} ${to.name}<#if user??>, ${user.type} ${user.name}</#if>){
        return getFrom(${to.name}<#if user??>, ${user.name}</#if>);
    }

    @Override
    public ${to.type} get${to.role}(${from.type} ${from.name}<#if user??>, ${user.type} ${user.name}</#if>){
        return getTo(${from.name}<#if user??>, ${user.name}</#if>);
    }

}