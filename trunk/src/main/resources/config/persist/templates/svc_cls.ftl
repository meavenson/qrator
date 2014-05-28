package ${svc_cls};

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import persist.query.filter.Filter;
import persist.util.ResponseHelper;
import ${ent_int}.*;
import ${rel_int}.*;
import ${ent_cls}.EntityFactoryImpl;
import ${rel_cls}.RelationFactoryImpl;
import org.json.simple.JSONValue;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.FormParam;
<#list imports as import>
import ${import};
</#list>
//custom imports
//end custom imports


<#include "cls_com.ftl">
@Path("${lower}")
public class ${name}Service {

    @POST @Path("/create")
    @Produces("application/json")
    public String create${name}(@FormParam("ssid") String ssid<#list fields as field>, @FormParam("${field.name}") ${field.type} ${field.name}</#list><#list keys as key><#if !key.creator>, @FormParam("${key.name}") long ${key.name}Ref</#if></#list>){
        ResponseHelper response = new ResponseHelper(ssid);
        try{
            if(response.session == null){
                response.error("Invalid session id.");
            }else{
                EntityFactory eFactory = EntityFactoryImpl.getFactory(response.conn);
                ${userClass} user = ServiceHelper.get${userClass}(eFactory, response.session.getUserId());
            <#list keys as key>
                <#if !key.creator>
                ${key.type} ${key.name} = ServiceHelper.get${key.type}(eFactory, ${key.name}Ref<#if key.access>, user</#if>);
                </#if>
            </#list>
                ${name} ${lower} = eFactory.create${name}(<#list fields as field>${field.name}<#if field_has_next>, </#if></#list><#list keys as key><#if key.creator>, user<#else>, ${key.name}</#if></#list><#if !created && !user>, user</#if>);
                response.add("objs", ServiceHelper.get${name}State(${lower}));
                response.message("${name} created.");
            }
        }catch(Exception qe){
            qe.printStackTrace();
            response.error(qe.getMessage());
        }
        return JSONValue.toJSONString(response.getResponse());
    }

    @GET @Path("/list")
    @Produces("application/json")
    public String list${plural}(@QueryParam("ssid") String ssid<#list fields as field>, @QueryParam("${field.name}") ${field.type} ${field.name}</#list>, @QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit){
        ResponseHelper response = new ResponseHelper(ssid);
        try{
            if(response.session == null){
                response.error("Invalid session id.");
            }else{
                Filter<${name}> filter = new Filter<${name}>(${name}.class);
            <#list fields as field>
                if(${field.name} != null) filter.eq("${field.name}", ${field.name}<#if field.type != "String">.toString()</#if>);
            </#list>
                EntityFactory eFactory = EntityFactoryImpl.getFactory(response.conn);<#if access>
                ${userClass} user = ServiceHelper.get${userClass}(eFactory, response.session.getUserId());</#if>
                long count = eFactory.count${plural}(filter<#if access>, user</#if>);
                offset = offset == null? -1 : offset;
                limit = limit == null? -1 : limit;
                List<Map<String,Object>> results = new ArrayList<Map<String,Object>>();
                for(Iterator<${name}> iter = eFactory.find${plural}(filter<#if access>, user</#if>, offset, limit); iter.hasNext();){
                    ${name} elmt = iter.next();
                    results.add(ServiceHelper.get${name}State(elmt));
                }
                response.add("count", count);
                response.message(count+" ${plural} found.");
                response.add("objs", results);
            }
        }catch(Exception qe){
            qe.printStackTrace();
            response.error(qe.getMessage());
        }
        return JSONValue.toJSONString(response.getResponse());
    }

    @POST @Path("/modify")
    @Produces("application/json")
    public String modify${name}(@FormParam("ssid") String ssid, @FormParam("${lower}Id") Long ${lower}Id<#list fields as field>, @FormParam("${field.name}") ${field.type} ${field.name}</#list>){
        ResponseHelper response = new ResponseHelper(ssid);
        try{
            if(response.session == null){
                response.error("Invalid session id.");
            }else{
                EntityFactory eFactory = EntityFactoryImpl.getFactory(response.conn);<#if access>
                ${userClass} user = ServiceHelper.get${userClass}(eFactory, response.session.getUserId());</#if>
                ${name} ${lower} = ServiceHelper.get${name}(eFactory, ${lower}Id<#if access>, user</#if>);
            <#list fields as field>
                if(${field.name} != null) ${lower}.set${field.caps}(${field.name});
            </#list>
                eFactory.update${name}(${lower}<#if access>, user</#if>);
                response.add("objs", ServiceHelper.get${name}State(${lower}));
                response.message("${name} modified.");
            }
        }catch(Exception qe){
            qe.printStackTrace();
            response.error(qe.getMessage());
        }
        return JSONValue.toJSONString(response.getResponse());
    }

    @GET @Path("/remove")
    @Produces("application/json")
    public String remove${name}(@QueryParam("ssid") String ssid, @QueryParam("${lower}Id") Long ${lower}Id){
        ResponseHelper response = new ResponseHelper(ssid);
        try{
            if(response.session == null){
                response.error("Invalid session id.");
            }else{
                EntityFactory eFactory = EntityFactoryImpl.getFactory(response.conn);<#if access>
                ${userClass} user = ServiceHelper.get${userClass}(eFactory, response.session.getUserId());</#if>
                ${name} ${lower} = ServiceHelper.get${name}(eFactory, ${lower}Id<#if access>, user</#if>);
                eFactory.remove${name}(${lower}<#if access>, user</#if>);
                response.message("${name} removed.");
            }
        }catch(Exception qe){
            qe.printStackTrace();
            response.error(qe.getMessage());
        }
        return JSONValue.toJSONString(response.getResponse());
    }

<#list relations as rel>

    @GET @Path("/get${rel.method}")
    @Produces("application/json")
    public String get${rel.method}(@QueryParam("ssid") String ssid, @QueryParam("${lower}Id") Long ${lower}Id){
        ResponseHelper response = new ResponseHelper(ssid);
        try{
            if(response.session == null){
                response.error("Invalid session id.");
            }else{
                RelationFactory rFactory = RelationFactoryImpl.getFactory(response.conn);
                EntityFactory eFactory = EntityFactoryImpl.getFactory(response.conn);<#if access>
                ${userClass} user = ServiceHelper.get${userClass}(eFactory, response.session.getUserId());</#if>
                ${rel.type} ${rel.name} = rFactory.get${rel.type}();
                ${name} ${lower} = ServiceHelper.get${name}(eFactory, ${lower}Id<#if access>, user</#if>);
            <#if rel.many>
                long count = ${rel.name}.count<#if rel.from>To<#else>From</#if>(${lower}, null);
                List<Map<String,Object>> results = new ArrayList<Map<String,Object>>();
                for(Iterator<${rel.opp.type}> iter = ${rel.name}.get${rel.opp.plural}(${lower}, null<#if rel.access>, user</#if>); iter.hasNext();){
                    ${rel.opp.type} elmt = iter.next();
                    results.add(ServiceHelper.get${rel.opp.type}State(elmt));
                }
                response.message(count+" ${rel.opp.plural} found.");
                response.add("objs", results);
            <#else>
                ${rel.opp.type} ${rel.opp.name} = ${rel.name}.get${rel.opp.role}(${lower}<#if rel.access>, user</#if>);
                response.add("objs", ServiceHelper.get${rel.opp.type}State(${rel.opp.name}));
            </#if>
            }
        }catch(Exception qe){
            qe.printStackTrace();
            response.error(qe.getMessage());
        }
        return JSONValue.toJSONString(response.getResponse());
    }

    @GET @Path("/add${rel.method}")
    @Produces("application/json")
    public String add${rel.method}(@QueryParam("ssid") String ssid, @QueryParam("${lower}Id") Long ${lower}Id, @QueryParam("${rel.opp.name}Id") Long ${rel.opp.name}Id){
        ResponseHelper response = new ResponseHelper(ssid);
        try{
            if(response.session == null){
                response.error("Invalid session id.");
            }else{
                RelationFactory rFactory = RelationFactoryImpl.getFactory(response.conn);
                EntityFactory eFactory = EntityFactoryImpl.getFactory(response.conn);<#if rel.opp.access || rel.access || access>
                ${userClass} user = ServiceHelper.get${userClass}(eFactory, response.session.getUserId());</#if>
                ${rel.type} ${rel.name} = rFactory.get${rel.type}();
                ${name} ${lower} = ServiceHelper.get${name}(eFactory, ${lower}Id<#if access>, user</#if>);
                ${rel.opp.type} ${rel.opp.name} = ServiceHelper.get${rel.opp.type}(eFactory, ${rel.opp.name}Id<#if rel.opp.access>, user</#if>);
                ${rel.name}.add(<#if rel.from>${lower}, ${rel.opp.name}<#else>${rel.opp.name}, ${lower}</#if><#if rel.access>, user</#if>);
                response.message("${rel.opp.plural} added.");
            }
        }catch(Exception qe){
            qe.printStackTrace();
            response.error(qe.getMessage());
        }
        return JSONValue.toJSONString(response.getResponse());
    }

<#if rel.nullable>
    @GET @Path("/remove${rel.method}")
    @Produces("application/json")
    public String remove${rel.method}(@QueryParam("ssid") String ssid<#if rel.from><#if rel.card == "OneToMany">, @QueryParam("${rel.opp.name}Id") Long ${rel.opp.name}Id<#else>, @QueryParam("${lower}Id") Long ${lower}Id</#if><#else><#if rel.card == "OneToMany">, @QueryParam("${lower}Id") Long ${lower}Id<#else>, @QueryParam("${rel.opp.name}Id") Long ${rel.opp.name}Id</#if></#if><#if rel.card == "ManyToMany"><#if rel.from>, @QueryParam("${rel.opp.name}Id") Long ${rel.opp.name}Id<#else>, @QueryParam("${lower}Id") Long ${lower}Id</#if></#if>){
        ResponseHelper response = new ResponseHelper(ssid);
        try{
            if(response.session == null){
                response.error("Invalid session id.");
            }else{
                RelationFactory rFactory = RelationFactoryImpl.getFactory(response.conn);
                EntityFactory eFactory = EntityFactoryImpl.getFactory(response.conn);<#if access || rel.opp.access>
                ${userClass} user = ServiceHelper.get${userClass}(eFactory, response.session.getUserId());</#if>
                ${rel.type} ${rel.name} = rFactory.get${rel.type}();
        <#if rel.from>
            <#if rel.card == "OneToMany">
                ${rel.opp.type} ${rel.opp.name} = ServiceHelper.get${rel.opp.type}(eFactory, ${rel.opp.name}Id<#if rel.opp.access>, user</#if>);
            <#else>
                ${name} ${lower} = ServiceHelper.get${name}(eFactory, ${lower}Id<#if access>, user</#if>);
            </#if>
        <#else>
            <#if rel.card == "OneToMany">
                ${name} ${lower} = ServiceHelper.get${name}(eFactory, ${lower}Id<#if access>, user</#if>);
            <#else>
                ${rel.opp.type} ${rel.opp.name} = ServiceHelper.get${rel.opp.type}(eFactory, ${rel.opp.name}Id<#if rel.opp.access>, user</#if>);
            </#if>
        </#if>
        <#if rel.card == "ManyToMany">
            <#if rel.from>
                ${rel.opp.type} ${rel.opp.name} = ServiceHelper.get${rel.opp.type}(eFactory, ${rel.opp.name}Id<#if rel.opp.access>, user</#if>);
            <#else>
                ${name} ${lower} = ServiceHelper.get${name}(eFactory, ${lower}Id<#if access>, user</#if>);
            </#if>
        </#if>
                ${rel.name}.remove(<#if rel.from><#if rel.card == "OneToMany">${rel.opp.name}<#else>${lower}</#if><#else><#if rel.card == "OneToMany">${lower}<#else>${rel.opp.name}</#if></#if><#if rel.card == "ManyToMany"><#if rel.from>, ${rel.opp.name}<#else>, ${lower}</#if></#if><#if rel.access>, user</#if>);
                response.message("Relation removed.");
            }
        }catch(Exception qe){
            qe.printStackTrace();
            response.error(qe.getMessage());
        }
        return JSONValue.toJSONString(response.getResponse());
    }

</#if>
<#if rel.ordered>
    @GET @Path("/orderedAdd${rel.method}")
    @Produces("application/json")
    public String orderedAdd${rel.method}(@QueryParam("ssid") String ssid, @QueryParam("${lower}Id") Long ${lower}Id, @QueryParam("${rel.opp.name}Id") Long ${rel.opp.name}Id, @QueryParam("index") Integer index){
        ResponseHelper response = new ResponseHelper(ssid);
        try{
            if(response.session == null){
                response.error("Invalid session id.");
            }else{
                RelationFactory rFactory = RelationFactoryImpl.getFactory(response.conn);
                EntityFactory eFactory = EntityFactoryImpl.getFactory(response.conn);<#if access>
                ${userClass} user = ServiceHelper.get${userClass}(eFactory, response.session.getUserId());</#if>
                ${rel.type} ${rel.name} = rFactory.get${rel.type}();
                ${name} ${lower} = ServiceHelper.get${name}(eFactory, ${lower}Id<#if access>, user</#if>);
                ${rel.opp.type} ${rel.opp.name} = ServiceHelper.get${rel.opp.type}(eFactory, ${rel.opp.name}Id<#if rel.opp.access>, user</#if>);
                ${rel.name}.add(<#if rel.from>${lower}, ${rel.opp.name}<#else>${rel.opp.name}, ${lower}</#if>, index<#if rel.access>, user</#if>);
                response.message("${rel.opp.plural} added.");
            }
        }catch(Exception qe){
            qe.printStackTrace();
            response.error(qe.getMessage());
        }
        return JSONValue.toJSONString(response.getResponse());
    }

</#if>
</#list>
//custom methods
//end custom methods
}