package ${rel_class};

import java.sql.Connection;
import ${rel_interface}.*;

<#include "cls_com.ftl">
public class ${name}Impl implements ${name} {
    
    private final Connection conn;

    /**
    * Construct a ${name}Impl.
    */
    protected ${name}Impl(Connection conn){
        this.conn = conn;
    }

    public static ${name} getFactory(Connection conn){
        return new ${name}Impl(conn);
    }

<#list relations as relation>
    @Override
    public ${relation} get${relation}(){
        return new ${relation}Impl(conn);
    }

</#list>
}