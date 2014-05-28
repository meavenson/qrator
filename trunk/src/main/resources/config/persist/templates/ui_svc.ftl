/*********************************************************************************
 * Web service functions for ${plural}.
 * generated: ${date?datetime}
 */

var ${name}Service = function(app){
    this.app = app;
    this.address = "service/${lower}/";
    this.util = new ServiceUtil(app, this.address);
};

${name}Service.prototype = {

    create: function(${lower}, <#list keys as key><#if !key.creator>${key.name}, </#if></#list>after){
        var t = this;
        var action = "create";
        var data = { "ssid": t.app.ssid,
        <#list fields as field>
            "${field}":${lower}.${field}<#if field_has_next || (keys?size > 0)>,</#if>
        </#list>
        <#list keys as key>
            <#if !key.creator>
            "${key.name}":${key.name}.id
            </#if>
        </#list>
        };
        t.util.post(action, data, after);
    },

    list: function(filter, after){
        var t = this;
        var action = "list";
        var data = { "ssid": t.app.ssid,
        <#list fields as field>
            "${field}":filter.${field},
        </#list>
            "offset": filter.offset,
            "limit": filter.limit
        };
        t.util.get(action, data, after);
    },

    modify: function(${lower}, after){
        var t = this;
        var action = "modify";
        var data = { "ssid": t.app.ssid,
            "${lower}Id": ${lower}.id,
        <#list fields as field>
            "${field}":${lower}.${field}<#if field_has_next>,</#if>
        </#list>
        };
        t.util.post(action, data, after);
    },

    remove: function(${lower}, after){
        var t = this;
        var action = "remove";
        var data = { "ssid": t.app.ssid,
            "${lower}Id": ${lower}.id
        };
        t.util.get(action, data, after);
    }<#if (relations?size > 0)>,</#if>
<#list relations as rel>

    get${rel.method}: function(${lower}, after){
        var t = this;
        var action = "get${rel.method}";
        var data = { "ssid": t.app.ssid,
            "${lower}Id":${lower}.id
        };
        t.util.get(action, data, after);
    },

    add${rel.method}: function(${lower}, ${rel.opp}, after){
        var t = this;
        var action = "add${rel.method}";
        var data = { "ssid": t.app.ssid,
            "${lower}Id":${lower}.id,
            "${rel.opp}Id":${rel.opp}.id
        };
        t.util.get(action, data, after);
    }<#if rel.nullable>,

    remove${rel.method}: function(<#if rel.from>${lower}<#if !rel.m11m>, ${rel.opp}</#if><#else>${rel.opp}<#if !rel.m11m>, ${lower}</#if></#if>, after){
        var t = this;
        var action = "remove${rel.method}";
        var data = { "ssid": t.app.ssid,
        <#if rel.from>
            "${lower}Id":${lower}.id<#if !rel.m11m>, 
            "${rel.opp}Id":${rel.opp}.id
            </#if>
        <#else>
            "${rel.opp}Id":${rel.opp}.id<#if !rel.m11m>, 
            "${lower}Id":${lower}.id
            </#if>
        </#if>
        };
        t.util.get(action, data, after);
    }</#if><#if rel_has_next>,</#if>
<#if rel.ordered>

    orderedAdd${rel.method}: function(${lower}, ${rel.opp}, index, after){
        var t = this;
        var action = "orderedAdd${rel.method}";
        var data = { "ssid": t.app.ssid,
            "${lower}Id":${lower}.id,
            "${rel.opp}Id":${rel.opp}.id,
            "index":index
        };
        t.util.get(action, data, after);
    }
</#if>

</#list>
};
