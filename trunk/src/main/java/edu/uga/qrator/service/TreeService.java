/* 
 * Copyright (C) 2014 Matthew Eavenson <matthew.eavenson at gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package edu.uga.qrator.service;


import edu.uga.glydeII.gom.CompositeResidue;
import edu.uga.glydeII.io.GOMSVGWriter;
import edu.uga.qrator.except.QException;
import edu.uga.qrator.logic.manage.TreeManager;
import edu.uga.qrator.logic.manage.TypeManager;
import edu.uga.qrator.logic.manage.UserManager;
import edu.uga.qrator.obj.entity.QStructureType;
import edu.uga.qrator.obj.entity.QTree;
import edu.uga.qrator.obj.entity.QUser;
import static edu.uga.qrator.service.ServiceUtil.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import org.json.simple.JSONValue;
import persist.util.ResponseHelper;
import persist.util.StringUtil;
import session.Session;
import session.SessionManager;

/**
 * @author Matthew
 *
 */
@Path("tree")
public class TreeService{
    
    @GET @Path("/loadTrees")
    @Produces("application/json")
    public static String loadTrees(@QueryParam("ssid") String ssid){
        ResponseHelper response = new ResponseHelper(ssid);
        if(response.session != null){
            UserManager uManager = new UserManager(response.conn);
            QUser user = ServiceUtil.getUser(response.session.getUserId(), uManager);
            if(uManager.hasRole(user, ADMIN)){
                try{
                    TreeManager.loadTrees(response.conn);
                    response.message("Trees loaded successfully.");
                }catch(QException qe){
                    response.error(qe.getMessage());
                } catch (IOException ex) {
                    response.error(ex.getMessage());
                }
            }else response.error("You do not have permission to checkin structures.");
        }else response.error(INVALID_SESSION);
        return JSONValue.toJSONString(response.getResponse());
    }
    
    @GET @Path("/spec")
    @Produces("application/json")
    public static String getTreeSpec(@QueryParam("name") String name){
        ResponseHelper response = new ResponseHelper();
        TreeManager tManager = new TreeManager(response.conn);
        try{
            QTree tree = tManager.get(name);
            response.add("objs", tree.getSpec());
        }catch(Exception qe){
            response.error(qe.getMessage());
        }
        return JSONValue.toJSONString(response.getResponse());
    }
    
    @GET @Path("/diagram")
    @Produces("application/json")
    public static String downloadTreeDiagram(@QueryParam("name") String name, @Context HttpServletResponse servletResponse){
        ResponseHelper response = new ResponseHelper();
        TreeManager tManager = new TreeManager(response.conn);
        try{
            ServletOutputStream out = servletResponse.getOutputStream();
//            servletResponse.setHeader("Pragma", "public");
//            servletResponse.setHeader("Expires", "0"); 
//            servletResponse.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");                
            servletResponse.setHeader("Content-Disposition", "attachment; filename=\""+name+"_canonical_tree.svg.xml\"");
            servletResponse.setContentType("image/svg+xml");

            CompositeResidue root = tManager.getRoot(name);
            
            GOMSVGWriter svg = new GOMSVGWriter();
            byte[] file = svg.toSVG(root).getBytes();
            servletResponse.setContentLength(file.length);

            out.write(file);
            out.flush();
            out.close();
            response.message("Download successful.");
        }catch(Exception qe){
            response.error(qe.getMessage());
        }
        return JSONValue.toJSONString(response.getResponse());
    }

    @GET @Path("/list")
    @Produces("application/json")
    public static String listTrees(@QueryParam("offset") int offset,
                                   @QueryParam("limit") int limit){
        ResponseHelper response = new ResponseHelper();
        TreeManager tManager = new TreeManager(response.conn);
        try{
            List<Map<String,Object>> tList = new ArrayList<Map<String,Object>>();
            Iterator<QTree> trees = tManager.list(offset, limit);
            while(trees.hasNext()){
                tList.add(treeState(trees.next(), tManager));
            }
            response.add("objs", tList);
            if(tList.size() > 0)
                response.message("List trees successful.");
            else response.message("No trees present.");
        }catch(QException qe){
            response.error(qe.getMessage());
        }
        return JSONValue.toJSONString(response.getResponse());
    }
    
    @GET @Path("/types/{tree}")
    @Produces("application/json")
    public static String listTypes(@PathParam("tree") int treeId,
                                   @QueryParam("offset") int offset,
                                   @QueryParam("limit") int limit){
        ResponseHelper response = new ResponseHelper();
        TreeManager tManager = new TreeManager(response.conn);
        TypeManager typeManager = new TypeManager(response.conn);
        QTree tree = getTree(treeId, tManager);
        try{
            List<Map<String,Object>> tList = new ArrayList<Map<String,Object>>();
            Iterator<QStructureType> types = typeManager.listTypes(tree);
            while(types.hasNext()){
                tList.add(typeState(types.next(), typeManager));
            }
            response.add("objs", tList);
            if(tList.size() > 0)
                response.message("List types successful.");
            else response.message("No types present.");
        }catch(QException qe){
            response.error(qe.getMessage());
        }
        return JSONValue.toJSONString(response.getResponse());
    }
    
    public static void main(String[] args) throws IOException{
        
        //Utils.stringToFile(downloadTreeDiagram("N-glycan", null), new File("/Users/durandal/Desktop/test.svg.xml"));
        
        //downloadTreeDiagram("Glc-initiated_glycosphingolipid", null);
        
        System.out.println(AdminService.login("admin", StringUtil.secureHash("qr@t0r")));
        Session session = SessionManager.getManager().getSession(13l);
        loadTrees(session.getId());
        
        //System.out.println(downloadTreeDiagram("N-glycan", null));
        
    }
    
}