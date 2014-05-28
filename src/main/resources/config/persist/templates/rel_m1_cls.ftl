package ${rel_class};

import ${rel_interface}.${name};
import java.sql.Connection;
import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.sql.<#if ordered>OrderedRelationM1SQL<#else>RelationM1SQL</#if>;
<#list imports as import>
import ${import};
</#list>

<#include "cls_com.ftl">
public class ${name}Impl extends <#if ordered>OrderedRelationM1SQL<#else>RelationM1SQL</#if><${from.type},${to.type}> implements ${name} {

    public ${name}Impl(Connection conn){
        super(conn, ${name}.class);
    }

    @Override
    public Iterator<${from.type}> get${from.plural}(${to.type} ${to.name}, Filter<${from.type}> filter<#if user??>, ${user.type} ${user.name}</#if>){
        return getFrom(${to.name}, filter<#if user??>, ${user.name}</#if>);
    }

    @Override
    public Iterator<${from.type}> get${from.plural}(${to.type} ${to.name}, Filter<${from.type}> filter<#if user??>, ${user.type} ${user.name}</#if>, int offset, int limit){
        return getFrom(${to.name}, filter<#if user??>, ${user.name}</#if>, offset, limit);
    }

    @Override
    public ${to.type} get${to.role}(${from.type} ${from.name}<#if user??>, ${user.type} ${user.name}</#if>){
        return getTo(${from.name}<#if user??>, ${user.name}</#if>);
    }

    @Override
    public void set${to.role}(${from.type} ${from.name}, ${to.type} ${to.name}<#if user??>, ${user.type} ${user.name}</#if>){
        add(${from.name}, ${to.name}<#if user??>, ${user.name}</#if>);
    }
<#if ordered>

    @Override
    public ${from.type} get${from.role}(${to.type} ${to.name}<#if user??>, ${user.type} ${user.name}</#if>, int index){
        return getFrom(${to.name}<#if user??>, ${user.name}</#if>, index);
    }
</#if>

}