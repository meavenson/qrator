package ${package}.impl;

import ${package}.${name};
<#list imports as import>
import ${import};
</#list>

<#include "cls_com.ftl">
public class ${name}Impl <#if extends??>extends ${extends} </#if>implements ${name} {

<#list methods as method>
    private ${method.type} ${method.field};
</#list>

    public ${name}Impl(<#list fields as field>${field.type} ${field.field}<#if field_has_next>, </#if></#list>) {
    <#if extends??>
        super(<#list super_fields as field>${field.field}<#if field_has_next>, </#if></#list>);
    </#if>
    <#list owned as field>
        this.${field.field} = ${field.field};
    </#list>
    }

<#if defaults?size != fields?size>
    public ${name}Impl(<#list defaults as field>${field.type} ${field.field}<#if field_has_next>, </#if></#list>) {
    <#if extends?? && (super_defaults?size > 0)>
        super(<#list super_defaults as field>${field.field}<#if field_has_next>, </#if></#list>);
    </#if>
    <#list owned_defaults as field>
        this.${field.field} = ${field.field};
    </#list>
    }

</#if>
<#list methods as method>
    @Override
    public ${method.type} <#if method.type == "boolean">is<#else>get</#if>${method.name}(){
        return ${method.field};
    }
    
    @Override
    public void set${method.name}(${method.type} ${method.field}){
        this.${method.field} = ${method.field};
    }

</#list>
}
        