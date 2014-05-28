package ${mng_cls};

import java.sql.Connection;
import java.util.Date;
import java.util.Iterator;
import persist.query.filter.Filter;
import ${ent_int}.*;
import ${rel_int}.*;
import ${ent_cls}.EntityFactoryImpl;
import ${rel_cls}.RelationFactoryImpl;
<#list imports as import>
import ${import};
</#list>
//custom imports
//end custom imports


<#include "cls_com.ftl">
public class ${name}Manager {
    
    private final EntityFactory eFactory;
    private final RelationFactory rFactory;

    public ${name}Manager(Connection conn){
        eFactory = EntityFactoryImpl.getFactory(conn);
        rFactory = RelationFactoryImpl.getFactory(conn);
    }

    public ${name} create${name}(<#list fields as field>${field.type} ${field.name}<#if field_has_next || (keys?size > 0)>, </#if></#list><#list keys as key>${key.type} ${key.name}<#if key_has_next>, </#if></#list><#if !created && !user>, ${userClass} user</#if>){
        ${name} ${lower} = eFactory.create${name}(<#list fields as field>${field.name}<#if field_has_next || (keys?size > 0)>, </#if></#list><#list keys as key>${key.name}<#if key_has_next>, </#if></#list><#if !created && !user>, user</#if>);
        return ${lower};
    }

    <#if defaults?size != fields?size>
    public ${name} create${name}(<#list defaults as field>${field.type} ${field.name}<#if field_has_next || (keys?size > 0)>, </#if></#list><#list keys as key>${key.type} ${key.name}<#if key_has_next>, </#if></#list><#if !created && !user>, ${userClass} user</#if>){
        ${name} ${lower} = eFactory.create${name}(<#list defaults as field>${field.name}<#if field_has_next || (keys?size > 0)>, </#if></#list><#list keys as key>${key.name}<#if key_has_next>, </#if></#list><#if !created && !user>, user</#if>);
        return ${lower};
    }

    </#if>
    public Iterator<${name}> list${plural}(Filter<${name}> filter<#if access>, ${userClass} user</#if>, int offset, int limit){
        return eFactory.find${plural}(filter<#if access>, user</#if>, offset, limit);
    }

    public Iterator<${name}> list${plural}(Filter<${name}> filter<#if access>, ${userClass} user</#if>){
        return eFactory.find${plural}(filter<#if access>, user</#if>);
    }
    <#list uniques as field>

    public ${name} getBy${field.caps}(${field.type} ${field.name}<#if access>, ${userClass} user</#if>){
        Filter<${name}> filter = new Filter<${name}>(${name}.class).eq("${field.name}", ${field.name}<#if field.type != "String">.toString()</#if>);
        Iterator<${name}> results = list${plural}(filter<#if access>, user</#if>);
        if(results.hasNext()) return results.next();
        return null;
    }
    </#list>
    <#if combos??>
    <#list combos as combo>
    
    public ${name} getBy<#list combo as field>${field.caps}<#if field_has_next>And</#if></#list>(<#list combo as field>${field.type} ${field.name}<#if field_has_next>, </#if></#list><#if access>, ${userClass} user</#if>){
        Filter<${name}> filter = new Filter<${name}>(${name}.class)<#list combo as field>.eq("${field.name}", ${field.name}<#if field.type != "String">.toString()</#if>)</#list>;
        Iterator<${name}> results = list${plural}(filter<#if access>, user</#if>);
        if(results.hasNext()) return results.next();
        return null;
    }
    </#list>
    </#if>

    public ${name} getById(long id<#if access>, ${userClass} user</#if>){
        Filter<${name}> filter = new Filter<${name}>(${name}.class).eq("${key}", id+"");
        Iterator<${name}> results = list${plural}(filter<#if access>, user</#if>);
        if(results.hasNext()) return results.next();
        else return null;
    }
    
    <#if (fields?size > 0)>
    public Iterator<${name}> list${plural}(<#list fields as field>${field.type} ${field.name}<#if field_has_next>, </#if></#list><#if access>, ${userClass} user</#if>, int offset, int limit){
        Filter<${name}> filter = new Filter<${name}>(${name}.class);
    <#list fields as field>
        if(${field.name} != null) filter.eq("${field.name}", ${field.name}<#if field.type != "String">.toString()</#if>);
    </#list>
        return eFactory.find${plural}(filter<#if access>, user</#if>, offset, limit);
    }

    </#if>
    public void update${name}(${name} ${lower}<#if access>, ${userClass} user</#if>){
        eFactory.update${name}(${lower}<#if access>, user</#if>);
    }

    public void remove${name}(${name} ${lower}<#if access>, ${userClass} user</#if>){
        eFactory.remove${name}(${lower}<#if access>, user</#if>);
    }

<#list relations as rel>
    public <#if rel.many>Iterator<${rel.opp.type}><#else>${rel.opp.type}</#if> get${rel.method}(${name} ${lower}<#if rel.access>, ${userClass} user</#if>){
        ${rel.type} ${rel.name} = rFactory.get${rel.type}();
    <#if rel.many>
        return ${rel.name}.get${rel.opp.plural}(${lower}, null<#if rel.access>, user</#if>);
    <#else>
        return ${rel.name}.get${rel.opp.role}(${lower}<#if rel.access>, user</#if>);
    </#if>
    }
    <#if rel.many>

    public Iterator<${rel.opp.type}> get${rel.method}(${name} ${lower}, Filter<${rel.opp.type}> filter<#if rel.access>, ${userClass} user</#if>){
        ${rel.type} ${rel.name} = rFactory.get${rel.type}();
        return ${rel.name}.get${rel.opp.plural}(${lower}, filter<#if rel.access>, user</#if>);
    }

    public Iterator<${rel.opp.type}> get${rel.method}(${name} ${lower}, Filter<${rel.opp.type}> filter<#if rel.access>, ${userClass} user</#if>, int offset, int limit){
        ${rel.type} ${rel.name} = rFactory.get${rel.type}();
        return ${rel.name}.get${rel.opp.plural}(${lower}, filter<#if rel.access>, user</#if>, offset, limit);
    }
    </#if>

    public void add${rel.method}(${name} ${lower}, ${rel.opp.type} ${rel.opp.name}<#if rel.access>, ${userClass} user</#if>){
        ${rel.type} ${rel.name} = rFactory.get${rel.type}();
        ${rel.name}.add(<#if rel.from>${lower}, ${rel.opp.name}<#else>${rel.opp.name}, ${lower}</#if><#if rel.access>, user</#if>);
    }

<#if rel.nullable>
    public void remove${rel.method}(<#if rel.from><#if rel.card == "OneToMany">${rel.opp.type} ${rel.opp.name}<#else>${name} ${lower}</#if><#else><#if rel.card == "OneToMany">${name} ${lower}<#else>${rel.opp.type} ${rel.opp.name}</#if></#if><#if rel.card == "ManyToMany"><#if rel.from>, ${rel.opp.type} ${rel.opp.name}<#else>, ${name} ${lower}</#if></#if><#if rel.access>, ${userClass} user</#if>){
        ${rel.type} ${rel.name} = rFactory.get${rel.type}();
        ${rel.name}.remove(<#if rel.from><#if rel.card == "OneToMany">${rel.opp.name}<#else>${lower}</#if><#else><#if rel.card == "OneToMany">${lower}<#else>${rel.opp.name}</#if></#if><#if rel.card == "ManyToMany"><#if rel.from>, ${rel.opp.name}<#else>, ${lower}</#if></#if><#if rel.access>, user</#if>);
    }

</#if>
<#if rel.ordered>
    public void add${rel.method}(${name} ${lower}, ${rel.opp.type} ${rel.opp.name}, int index<#if rel.access>, ${userClass} user</#if>){
        ${rel.type} ${rel.name} = rFactory.get${rel.type}();
        ${rel.name}.add(<#if rel.from>${lower}, ${rel.opp.name}<#else>${rel.opp.name}, ${lower}</#if>, index<#if rel.access>, user</#if>);
    }

</#if>
</#list>
//custom methods
//end custom methods
}
