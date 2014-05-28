package ${ent_class};

import java.sql.Connection;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.Iterator;
import persist.query.filter.Filter;
import persist.entity.manage.EntityManager;
import persist.entity.manage.sql.EntityManagerSQL;
import persist.entity.PEntity;
import persist.relation.PRelation;
<#if access??>import ${access.class};
import ${access.exception};</#if>
import ${ent_interface}.*;
import ${rel_interface}.*;
<#list imports as import>
import ${import};
</#list>

<#include "cls_com.ftl">
public class ${name}Impl implements ${name} {
    
    private final EntityManager manager;
    <#if access??>private final ${access.name} checker;</#if>
    
    protected ${name}Impl(Connection conn){
        manager = new EntityManagerSQL(conn);
        <#if access??>checker = new ${access.name}(conn);</#if>
    }

    public static ${name} getFactory(Connection conn){
        return new ${name}Impl(conn);
    }

<#list entities as entity>
    @Override
    public ${entity.type} create${entity.type}(<#list entity.fields as field>${field.type} ${field.name}<#if field_has_next>, </#if></#list><#if (entity.fields?size > 0) && (entity.relations?size > 0)>, </#if><#list entity.relations as relation>${relation.type} ${relation.name}<#if relation_has_next>, </#if></#list><#if !entity.created && entity.user??>, ${entity.user.type} ${entity.user.name}</#if>){
        <#if entity.user??>
        if(!checker.hasCreatePermission(${entity.user.rel}, ${entity.type}.class))
            throw new AccessException("Permission denied to create ${entity.type}");
        </#if>
        ${entity.type} ${entity.type?lower_case} = new ${entity.type}Impl(<#list entity.fields as field>${field.name}<#if field_has_next>, </#if></#list>);
        Map<Class<? extends PRelation>, PEntity> keys = new HashMap<Class<? extends PRelation>, PEntity>();
    <#list entity.relations as key>
        keys.put(${key.relation}.class, ${key.name});
    </#list>
        manager.add(${entity.type?lower_case}, keys);
        ${entity.type?lower_case} = manager.get(${entity.type?lower_case}.getId(), ${entity.type}.class);
        return ${entity.type?lower_case};
    }

    <#if entity.defaults?size != entity.fields?size>
    @Override
    public ${entity.type} create${entity.type}(<#list entity.defaults as field>${field.type} ${field.name}<#if field_has_next>, </#if></#list><#if (entity.defaults?size > 0) && (entity.relations?size > 0)>, </#if><#list entity.relations as relation>${relation.type} ${relation.name}<#if relation_has_next>, </#if></#list><#if !entity.created && entity.user??>, ${entity.user.type} ${entity.user.name}</#if>){
        <#if entity.user??>
        if(!checker.hasCreatePermission(${entity.user.rel}, ${entity.type}.class))
            throw new AccessException("Permission denied to create ${entity.type}");
        </#if>
        ${entity.type} ${entity.type?lower_case} = new ${entity.type}Impl(<#list entity.defaults as field>${field.name}<#if field_has_next>, </#if></#list>);
        Map<Class<? extends PRelation>, PEntity> keys = new HashMap<Class<? extends PRelation>, PEntity>();
    <#list entity.relations as key>
        keys.put(${key.relation}.class, ${key.name});
    </#list>
        manager.add(${entity.type?lower_case}, keys);
        ${entity.type?lower_case} = manager.get(${entity.type?lower_case}.getId(), ${entity.type}.class);
        return ${entity.type?lower_case};
    }

    </#if>
    @Override
    public Iterator<${entity.type}> find${entity.plural}(Filter<${entity.type}> filter<#if entity.user??>, ${entity.user.type} ${entity.user.name}</#if>){
        if(filter != null){<#if entity.user??>
            checker.addFilterPermissions(${entity.user.name}, filter);</#if>
            return manager.get(filter);
        }else return manager.get(${entity.type}.class);
    }

    @Override
    public Iterator<${entity.type}> find${entity.plural}(Filter<${entity.type}> filter<#if entity.user??>, ${entity.user.type} ${entity.user.name}</#if>, int offset, int limit){
        if(filter != null){<#if entity.user??>
            checker.addFilterPermissions(${entity.user.name}, filter);</#if>
            return manager.get(filter, offset, limit);
        }else return manager.get(${entity.type}.class, offset, limit);
    }

    @Override
    public void update${entity.type}(${entity.type} ${entity.type?lower_case}<#if entity.user??>, ${entity.user.type} ${entity.user.name}</#if>){
        <#if entity.user??>if(!checker.hasUpdatePermission(${entity.user.name}, ${entity.type?lower_case}))
            throw new AccessException("Permission denied to update ${entity.type}");</#if>
        manager.update(${entity.type?lower_case});
    }

    @Override
    public void remove${entity.plural}(Filter<${entity.type}> filter<#if entity.user??>, ${entity.user.type} ${entity.user.name}</#if>){
        <#if entity.user??>checker.addFilterPermissions(${entity.user.name}, filter);</#if>
        manager.remove(filter);
    }

    @Override
    public void remove${entity.type}(${entity.type} ${entity.type?lower_case}<#if entity.user??>, ${entity.user.type} ${entity.user.name}</#if>){
        <#if entity.user??>if(!checker.hasRemovePermission(${entity.user.name}, ${entity.type?lower_case}))
            throw new AccessException("Permission denied to remove ${entity.type}");</#if>
        manager.remove(${entity.type?lower_case});
    }

    @Override
    public long count${entity.plural}(Filter<${entity.type}> filter<#if entity.user??>, ${entity.user.type} ${entity.user.name}</#if>){
        <#if entity.user??>checker.addFilterPermissions(${entity.user.name}, filter);</#if>
        return manager.count(filter);
    }

</#list>
}
        