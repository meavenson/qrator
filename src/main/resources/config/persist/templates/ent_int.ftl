package ${package};

<#list imports as import>
import ${import};
</#list>

<#include "cls_com.ftl">
public interface ${name} <#if extends??>extends <#list extends as extend>${extend}<#if extend_has_next>, </#if></#list></#if> {

<#list enums?keys as enum>
    public enum ${enum}{
    <#list enums[enum] as value>
        ${value}<#if value_has_next>,</#if>
    </#list>
    }
</#list>

<#list methods as method>
    ${method.type} <#if method.type == "boolean">is<#else>get</#if>${method.name}();
    
    void set${method.name}(${method.type} ${method.field});

</#list>
}
        