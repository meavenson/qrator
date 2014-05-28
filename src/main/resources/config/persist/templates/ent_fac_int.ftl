package ${ent_interface};

import persist.query.filter.Filter;
import java.util.Date;
import java.util.Iterator;
<#list imports as import>
import ${import};
</#list>

<#include "cls_com.ftl">
public interface ${name} {
<#list entities as entity>

    ${entity.type} create${entity.type}(<#list entity.fields as field>${field.type} ${field.name}<#if field_has_next>, </#if></#list><#if (entity.fields?size > 0) && (entity.relations?size > 0)>, </#if><#list entity.relations as relation>${relation.type} ${relation.name}<#if relation_has_next>, </#if></#list><#if !entity.created && entity.user??>, ${entity.user.type} ${entity.user.name}</#if>);

    <#if entity.defaults?size != entity.fields?size>
    ${entity.type} create${entity.type}(<#list entity.defaults as field>${field.type} ${field.name}<#if field_has_next>, </#if></#list><#if (entity.defaults?size > 0) && (entity.relations?size > 0)>, </#if><#list entity.relations as relation>${relation.type} ${relation.name}<#if relation_has_next>, </#if></#list><#if !entity.created && entity.user??>, ${entity.user.type} ${entity.user.name}</#if>);

    </#if>
    Iterator<${entity.type}> find${entity.plural}(Filter<${entity.type}> filter<#if entity.user??>, ${entity.user.type} ${entity.user.name}</#if>);

    Iterator<${entity.type}> find${entity.plural}(Filter<${entity.type}> filter<#if entity.user??>, ${entity.user.type} ${entity.user.name}</#if>, int offset, int limit);

    void update${entity.type}(${entity.type} ${entity.type?lower_case}<#if entity.user??>, ${entity.user.type} ${entity.user.name}</#if>);

    void remove${entity.plural}(Filter<${entity.type}> filter<#if entity.user??>, ${entity.user.type} ${entity.user.name}</#if>);

    void remove${entity.type}(${entity.type} ${entity.type?lower_case}<#if entity.user??>, ${entity.user.type} ${entity.user.name}</#if>);

    long count${entity.plural}(Filter<${entity.type}> filter<#if entity.user??>, ${entity.user.type} ${entity.user.name}</#if>);

</#list>
}
        