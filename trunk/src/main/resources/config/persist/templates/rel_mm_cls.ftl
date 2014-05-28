package ${rel_class};

import ${rel_interface}.${name};
import java.sql.Connection;
import java.util.Iterator;
import persist.query.filter.Filter;
import persist.relation.sql.<#if ordered>OrderedRelationMMSQL<#else>RelationMMSQL</#if>;
<#list imports as import>
import ${import};
</#list>

<#include "cls_com.ftl">
public class ${name}Impl extends <#if ordered>OrderedRelationMMSQL<#else>RelationMMSQL</#if><${from.type},${to.type}> implements ${name}{

    public ${name}Impl(Connection conn){
        super(conn, ${name}.class);
    }

    @Override
    public Iterator<${from.type}> get${from.plural}(${to.type} ${to.name}, Filter<${from.type}> filter<#if user??>, ${user.type} ${user.name}</#if>){
        return getFrom(${to.name}, filter<#if user??>, ${user.name}</#if>);
    }

    @Override
    public Iterator<${to.type}> get${to.plural}(${from.type} ${from.name}, Filter<${to.type}> filter<#if user??>, ${user.type} ${user.name}</#if>){
        return getTo(${from.name}, filter<#if user??>, ${user.name}</#if>);
    }

    @Override
    public Iterator<${from.type}> get${from.plural}(${to.type} ${to.name}, Filter<${from.type}> filter<#if user??>, ${user.type} ${user.name}</#if>, int offset, int limit){
        return getFrom(${to.name}, filter<#if user??>, ${user.name}</#if>, offset, limit);
    }

    @Override
    public Iterator<${to.type}> get${to.plural}(${from.type} ${from.name}, Filter<${to.type}> filter<#if user??>, ${user.type} ${user.name}</#if>, int offset, int limit){
        return getTo(${from.name}, filter<#if user??>, ${user.name}</#if>, offset, limit);
    }
<#if ordered>

    @Override
    public ${from.type} get${from.role}(${to.type} ${to.name}<#if user??>, ${user.type} ${user.name}</#if>, int index){
        return getFrom(${to.name}<#if user??>, ${user.name}</#if>, index);
    }

    @Override
    public ${to.type} get${to.role}(${from.type} ${from.name}<#if user??>, ${user.type} ${user.name}</#if>, int index){
        return getTo(${from.name}<#if user??>, ${user.name}</#if>, index);
    }
</#if>

}