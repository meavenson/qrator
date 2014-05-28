package ${rel_class};

import ${rel_interface}.${name};
import java.sql.Connection;
import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.sql.<#if ordered>OrderedRelation1MSQL<#else>Relation1MSQL</#if>;
<#list imports as import>
import ${import};
</#list>

<#include "cls_com.ftl">
public class ${name}Impl extends <#if ordered>OrderedRelation1MSQL<#else>Relation1MSQL</#if><${from.type},${to.type}> implements ${name} {

    public ${name}Impl(Connection conn){
        super(conn, ${name}.class);
    }

    @Override
    public Iterator<${to.type}> get${to.plural}(${from.type} ${from.name}, Filter<${to.type}> filter<#if user??>, ${user.type} ${user.name}</#if>){
        return getTo(${from.name}, filter<#if user??>, ${user.name}</#if>);
    }

    @Override
    public Iterator<${to.type}> get${to.plural}(${from.type} ${from.name}, Filter<${to.type}> filter<#if user??>, ${user.type} ${user.name}</#if>, int offset, int limit){
        return getTo(${from.name}, filter<#if user??>, ${user.name}</#if>, offset, limit);
    }

    @Override
    public ${from.type} get${from.role}(${to.type} ${to.name}<#if user??>, ${user.type} ${user.name}</#if>){
        return getFrom(${to.name}<#if user??>, ${user.name}</#if>);
    }

    @Override
    public void set${from.role}(${from.type} ${from.name}, ${to.type} ${to.name}<#if user??>, ${user.type} ${user.name}</#if>){
        add(${from.name}, ${to.name}<#if user??>, ${user.name}</#if>);
    }
<#if ordered>

    @Override
    public ${to.type} get${to.role}(${from.type} ${from.name}<#if user??>, ${user.type} ${user.name}</#if>, int index){
        return getTo(${from.name}<#if user??>, ${user.name}</#if>, index);
    }
</#if>

}