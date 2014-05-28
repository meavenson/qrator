package ${svc_cls};

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import persist.query.filter.Filter;
import ${ent_int}.*;
//custom imports
//end custom imports

/**
 * Helper methods for web services.
 *
 * @author  ${author}
 * @date    ${date?datetime}
 */
public class ServiceHelper {

<#list entities as ent>
    public static ${ent.type} get${ent.type}(EntityFactory factory, long id<#if ent.access>, ${user} user</#if>){
        Filter<${ent.type}> filter = new Filter<${ent.type}>(${ent.type}.class).eq("${ent.key}", id+"");
        Iterator<${ent.type}> results = factory.find${ent.plural}(filter<#if ent.access>, user</#if>);
        if(results.hasNext()) return results.next();
        else return null;
    }

    public static Map<String, Object> get${ent.type}State(${ent.type} ${ent.name}){
        Map<String, Object> state = new HashMap<String, Object>();
        state.put("${ent.key}", ${ent.name}.getId());
        <#list ent.fields as field>
        <#if field.string>
        state.put("${field.lower}", ${ent.name}.<#if field.type == "Boolean">is<#else>get</#if>${field.name}());
        <#else>
        state.put("${field.lower}", ${ent.name}.get${field.name}().toString());
        </#if>
        </#list>
        return state;
    }

</#list>
//custom methods
//end custom methods
}
