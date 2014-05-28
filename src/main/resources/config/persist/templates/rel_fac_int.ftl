package ${rel_interface};

<#include "cls_com.ftl">
public interface ${name} {
<#list relations as relation>

    ${relation} get${relation}();
</#list>
	
}
        