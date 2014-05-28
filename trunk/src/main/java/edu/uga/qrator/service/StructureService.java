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
import edu.uga.qrator.except.QException;
import edu.uga.qrator.except.StructureParsingException;
import edu.uga.qrator.logic.QConfiguration;
import edu.uga.qrator.logic.io.OntologyClient;
import edu.uga.qrator.logic.io.StructureExporter;
import edu.uga.qrator.logic.manage.*;
import static edu.uga.qrator.logic.manage.StructureManager.getCandidateSpec;
import static edu.uga.qrator.logic.manage.StructureManager.getCandidateSpec;
import edu.uga.qrator.logic.match.motif.MotifManager;
import edu.uga.qrator.logic.match.motif.SearchMotif;
import edu.uga.qrator.obj.entity.*;
import edu.uga.qrator.obj.entity.QStructure.ReviewStatus;
import static edu.uga.qrator.service.ServiceUtil.*;
import edu.uga.qrator.util.FileUtils;
import edu.uga.qrator.util.QratorUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.simple.JSONValue;
import persist.core.PersistenceException;
import persist.util.PersistenceUtil;
import persist.util.ResponseHelper;
import session.Session;

/**
 * @author Matthew
 *
 *
 */
@Path("structure")
public class StructureService{
    
    
    @POST @Path("/upload")
    @Produces("application/json")
    @Consumes("multipart/form-data")
    public static String uploadFiles( @Context HttpServletRequest request ){
        ResponseHelper response = new ResponseHelper();
        PersistenceUtil.disableAutoCommit(response.conn);
        try{
            if(ServletFileUpload.isMultipartContent(request)){
                // Create a new file upload handler
                ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());

                String ssid = null;
                byte[] contents = null;
                String filename = null;
                // Parse the request
                @SuppressWarnings("unchecked")
                List<FileItem> items = (List<FileItem>)upload.parseRequest(request);
                for( FileItem fi : items ) {
                    String fieldname = fi.getFieldName();
                    if (fieldname.equals("file")) {
                        filename = fi.getName();
                        contents = fi.get();
                    }else ssid = fi.getString();
                }

                if(ssid != null){
                    Session session = response.session(ssid);
                    StructureManager manager = new StructureManager(response.conn);
                    UserManager uManager = new UserManager(response.conn);
                    QUser user = ServiceUtil.getUser(response.session.getUserId(), uManager);
                    if(session == null) response.error(INVALID_SESSION);
                    else{
                        Map<String, Object> report = FileUtils.extractStructures( manager, filename, contents, user );
                        response.add("report", report);
                        response.message("Files uploaded successfully.");
                    }
                }else response.error("Invalid request.");
                PersistenceUtil.commit(response.conn);
                PersistenceUtil.enableAutoCommit(response.conn);
            }
        }catch(Exception ex) {
            //ex.printStackTrace();
            PersistenceUtil.rollback(response.conn);
            PersistenceUtil.enableAutoCommit(response.conn);
            response.error("There was an error processing your files - "+ex.getMessage());
        }
        return JSONValue.toJSONString(response.getResponse());
    }
    
    @GET @Path("/downloadAll")
    @Produces("application/json")
    public static String downloadFiles(@QueryParam("status") String status,
                                @QueryParam("tree") Long treeId,
                                @QueryParam("type") Long typeId,
                                @QueryParam("owned") Boolean owned,
                                @Context HttpServletResponse servletResponse){
        
        ResponseHelper response = new ResponseHelper();
        StructureManager sManager = new StructureManager(response.conn);
        TreeManager treeManager = new TreeManager(response.conn);
        TypeManager typeManager = new TypeManager(response.conn);
        try{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);
            
            QTree tree = null;
            QStructureType type = null;
            ReviewStatus rStatus = null;
            QUser owner = null;
            if(owned != null && owned){
                UserManager uManager = new UserManager(response.conn);
                owner = ServiceUtil.getUser(response.session.getUserId(), uManager);
            }
            if(treeId != null) tree = getTree(treeId, treeManager);
            if(typeId != null) type = getType(typeId, typeManager);
            if(status != null) rStatus = ReviewStatus.valueOf(status);
            long count = sManager.count(rStatus, tree, type, owner);
            Iterator<QStructure> structs = sManager.list(rStatus, tree, type, owner, -1, -1);
            while(structs.hasNext()){
                QStructure struct = structs.next();
                byte[] buffer = StructureExporter.writeStructure(struct).getBytes();
                zos.putNextEntry( new ZipEntry( struct.getId()+".glyde.xml" ) );
                zos.write(buffer, 0, buffer.length);
                zos.closeEntry();
            }
            zos.finish();
            zos.flush();
            zos.close();

            if(servletResponse != null){
                ServletOutputStream sos = servletResponse.getOutputStream();
                servletResponse.setHeader("Content-Disposition", "attachment; filename=\"qrator_exported.zip\"");
                servletResponse.setContentType("application/octet-stream");
                servletResponse.setContentLength(baos.size());

                sos.write(baos.toByteArray());
                sos.flush();
                sos.close();
            }
            response.message(count+" files downloaded.");
        }catch(Exception ex){
            ex.printStackTrace();
            response.error(ex.getMessage());
        }
        return JSONValue.toJSONString(response.getResponse());
    }
    
    @GET @Path("/download")
    @Produces("application/json")
    public static String downloadFile(@QueryParam("id") long id,
                               @Context HttpServletResponse servletResponse){
        
        ResponseHelper response = new ResponseHelper();
        StructureManager sManager = new StructureManager(response.conn);
        try{
            QStructure struct = ServiceUtil.getStructure(id, sManager);
            byte[] buffer = StructureExporter.writeStructure(struct).getBytes();
            
            if(servletResponse != null){
                ServletOutputStream sos = servletResponse.getOutputStream();
                servletResponse.setHeader("Content-Disposition", "attachment; filename=\""+struct.getId()+".glyde.xml\"");
                servletResponse.setContentType("application/xml");
                servletResponse.setContentLength(buffer.length);
                
                sos.write(buffer);
                sos.flush();
                sos.close();
            }
            response.message("File downloaded.");
        }catch(Exception ex){
            ex.printStackTrace();
            response.error(ex.getMessage());
        }
        return JSONValue.toJSONString(response.getResponse());
    }
    
    @POST @Path("/build")
    @Produces("application/json")
    public static String build(@FormParam("ssid") String ssid, @FormParam("spec") String specJson){
        
        ResponseHelper response = new ResponseHelper(ssid);
        if(response.session != null){
            StructureManager sManager = new StructureManager(response.conn);
            UserManager uManager = new UserManager(response.conn);
            QUser user = ServiceUtil.getUser(response.session.getUserId(), uManager);
            try{
                Map<String,Object> specMap = (Map<String,Object>) JSONValue.parse(specJson);
                String spec = JSONValue.toJSONString(MotifManager.motifToSpec(specMap));
                Date now = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy_hh:mm:ss");
                String name = "sb_"+sdf.format(now);
                String glydeXML = StructureExporter.writeGlycan(StructureExporter.convertStructure(spec));
                QStructure struct = sManager.create(name, glydeXML.getBytes(), null, user);
                response.add("obj", ServiceUtil.structureState(struct, sManager));
                response.message("Structure created successfully.");
            }catch(Exception ex){
                ex.printStackTrace();
                response.error(ex.getMessage());
            }
        }else response.error(INVALID_SESSION);
        return JSONValue.toJSONString(response.getResponse());
    }
    
    /*@POST @Path("/search")
    @Produces("application/json")
    public static String search(@FormParam("motif") String motifJson, 
                                @FormParam("offset") int offset, 
                                @FormParam("limit") int limit){
        
        ResponseHelper response = new ResponseHelper();
        StructureManager sManager = new StructureManager(response.conn);
        try{
            Map<String,Object> motifMap = (Map<String,Object>) JSONValue.parse(motifJson);
            SearchMotif motif = new SearchMotif();
            motif.addPositive(motifMap);
            int count = 0;
            List<Map<String,Object>> sList = new ArrayList<Map<String,Object>>();
            for(Iterator<QStructure> structs = MotifManager.search(sManager, motif); structs.hasNext();){
                QStructure struct = structs.next();
                if(count >= offset && count < offset+limit){
                    sList.add(structureState(struct, sManager));
                }
                count++;
            }
            response.add("count", count);
            response.add("objs", sList);
            if(sList.size() > 0)
                response.message("Search structures successful.");
            else response.message("No structures match.");
        }catch(Exception ex){
            ex.printStackTrace();
            response.error(ex.getMessage());
        }
        return JSONValue.toJSONString(response.getResponse());
    }*/
    
    @GET @Path("/count")
    @Produces("application/json")
    public static String countStructures(){
        ResponseHelper response = new ResponseHelper();
        StructureManager manager = new StructureManager(response.conn);
        TreeManager treeManager = new TreeManager(response.conn);
        TypeManager typeManager = new TypeManager(response.conn);
        try{
            Map<String,Map<String,Object>> counts = new HashMap<String,Map<String,Object>>();
            for(Iterator<QTree> trees = treeManager.list(); trees.hasNext();){
                QTree tree = trees.next();
                Map<String,Object> typeMap = new HashMap<String,Object>();
                typeMap.put("id", tree.getId());
                for(Iterator<QStructureType> types = typeManager.listTypes(tree); types.hasNext();){
                    Map<String,Long> statusMap = new HashMap<String,Long>();
                    QStructureType type = types.next();
                    for(ReviewStatus status : ReviewStatus.values()){
                        long count = manager.count(status, null, type, null);
                        statusMap.put(status.name(), count);
                    }
                    statusMap.put("id", type.getId());
                    typeMap.put(type.getName(), statusMap);
                }
                counts.put(tree.getName(), typeMap);
            }
            response.add("count", counts);
        }catch(Exception qe){
            response.error(qe.getMessage());
        }
        return JSONValue.toJSONString(response.getResponse());
    }
    
    @GET @Path("/get/{source}/{refId}")
    @Produces("application/json")
    public static String getById(@PathParam("source") Long srcId,
                                 @PathParam("refId") String refId){
        ResponseHelper response = new ResponseHelper();
        StructureManager sManager = new StructureManager(response.conn);
        SourceManager srcManager = new SourceManager(response.conn);
        ReferenceManager refManager = new ReferenceManager(response.conn);
        try{
            List<Map<String,Object>> sList = new ArrayList<Map<String,Object>>();
            QSource source = ServiceUtil.getSource(srcId, srcManager);
            QReference ref = refManager.get(source, refId);
            if(ref != null){
                QStructure struct = refManager.getStructure(ref);
                if(struct != null) sList.add(structureState(struct, sManager));
                response.add("count", sList.size());
                response.add("objs", sList);
                if(sList.size() > 0)
                    response.message("List structures successful.");
                else response.message("No structures match.");
            }else response.message("No structures match.");
        }catch(QException qe){
            response.error(qe.getMessage());
        }
        return JSONValue.toJSONString(response.getResponse());
    }
    
    @POST @Path("/list")
    @Produces("application/json")
    public static String listStructures(@FormParam("ssid") String ssid,
                                        @FormParam("status") String status,
                                        @FormParam("tree") Long treeId,
                                        @FormParam("type") Long typeId,
                                        @FormParam("owned") Boolean owned,
                                        @FormParam("motif") String motifJson,
                                        @FormParam("offset") int offset,
                                        @FormParam("limit") int limit){
        ResponseHelper response = new ResponseHelper(ssid);
        StructureManager sManager = new StructureManager(response.conn);
        TreeManager treeManager = new TreeManager(response.conn);
        TypeManager typeManager = new TypeManager(response.conn);
        
        try{
            QTree tree = null;
            QStructureType type = null;
            ReviewStatus rStatus = null;
            QUser owner = null;
            if(owned != null && owned){
                if(response.session != null){
                    UserManager uManager = new UserManager(response.conn);
                    owner = ServiceUtil.getUser(response.session.getUserId(), uManager);
                }
            }
            if(treeId != null) tree = getTree(treeId, treeManager);
            if(typeId != null) type = getType(typeId, typeManager);
            if(status != null) rStatus = ReviewStatus.valueOf(status);
            List<Map<String,Object>> sList = new ArrayList<Map<String,Object>>();
            long count;
            Iterator<QStructure> structs;
            
            if(motifJson != null){
                structs = sManager.list(rStatus, tree, type, owner, -1, -1);
                Map<String,Object> motifMap = (Map<String,Object>) JSONValue.parse(motifJson);
                SearchMotif motif = new SearchMotif();
                motif.addPositive(motifMap);
                count = 0;
                for(Iterator<QStructure> matches = MotifManager.search(structs, motif); matches.hasNext();){
                    QStructure struct = matches.next();
                    if(count >= offset && count < offset+limit){
                        sList.add(structureState(struct, sManager));
                    }
                    count++;
                }
            }else{
                structs = sManager.list(rStatus, tree, type, owner, offset, limit);
                count = sManager.count(rStatus, tree, type, owner);
                while(structs.hasNext()){
                    sList.add(structureState(structs.next(), sManager));
                }
            }
            response.add("count", count);
            response.add("objs", sList);
            if(sList.size() > 0)
                response.message("List structures successful.");
            else response.message("No structures match.");
        }catch(QException qe){
            response.error(qe.getMessage());
        }
        return JSONValue.toJSONString(response.getResponse());
    }
    
    @GET @Path("/type/{structure}/{type}")
    @Produces("application/json")
    public static String assignStructureType(@QueryParam("ssid") String ssid, @PathParam("structure") Long structId, @PathParam("type") Long typeId){
        ResponseHelper response = new ResponseHelper(ssid);
        if(response.session != null){
            UserManager uManager = new UserManager(response.conn);
            QUser user = ServiceUtil.getUser(response.session.getUserId(), uManager);
            if(uManager.hasRole(user, CURATE)){
                StructureManager sManager = new StructureManager(response.conn);
                TypeManager tManager = new TypeManager(response.conn);
                try{
                    QStructure structure = ServiceUtil.getStructure(structId, sManager);
                    QStructureType type = ServiceUtil.getType(typeId, tManager);
                    if(structure != null && type != null){
                        ReviewStatus status = structure.getStatus();
                        if(status == ReviewStatus.pending){
                            sManager.setType(structure, type);
                            response.add("obj", ServiceUtil.structureState(structure, sManager));
                            response.message("Structure type set to "+type.getName().replaceAll("_", " ")+".");
                        }else response.error("Structure types cannot be altered after pending status.");
                    }else if(structure == null) response.error("The structure does not exist.");
                    else if(type == null) response.error("The type does not exist.");
                }catch(Exception qe){
                    response.error(qe.getMessage());
                }
            }else response.error("You do not have permission to assign a structure's type.");
        }else response.error(INVALID_SESSION);
        return JSONValue.toJSONString(response.getResponse());
    }
    
    @GET @Path("/compare/{structure}")
    @Produces("application/json")
    public static String compareStructure(@PathParam("structure") Long structId){
        ResponseHelper response = new ResponseHelper();
        StructureManager manager = new StructureManager(response.conn);
        try{
            QStructure structure = ServiceUtil.getStructure(structId, manager);
            if(structure != null){
                response.add("obj", manager.differenceFromTree(structure));
                response.message("Structure comparison successful.");
            }else response.error("The structure does not exist.");
        }catch(Exception qe){
            response.error(qe.getMessage());
        }
        return JSONValue.toJSONString(response.getResponse());
    }
    
    @GET @Path("/get/{structure}")
    @Produces("application/json")
    public static String getStructureById(@PathParam("structure") Long structId){
        ResponseHelper response = new ResponseHelper();
        StructureManager manager = new StructureManager(response.conn);
        try{
            QStructure structure = ServiceUtil.getStructure(structId, manager);
            if(structure != null){
                response.add("obj", ServiceUtil.structureState(structure, manager));
                response.message("Get structure successful.");
            }else response.error("The structure does not exist.");
        }catch(Exception qe){
            response.error(qe.getMessage());
        }
        return JSONValue.toJSONString(response.getResponse());
    }
    
    /*@GET @Path("/checkout/{structure}")
    @Produces("application/json")
    public static String checkoutStructure(@QueryParam("ssid")     String ssid,
                                           @PathParam("structure") long structId){
        ResponseHelper response = new ResponseHelper(ssid);
        if(response.session != null){
            UserManager uManager = new UserManager(response.conn);
            QUser user = ServiceUtil.getUser(response.session.getUserId(), uManager);
            if(uManager.hasRole(user, CURATE)){
                StructureManager sManager = new StructureManager(response.conn);
                try{
                    QStructure structure = getStructure(structId, sManager);
                    if(sManager.checkout(structure)){
                        response.message("Checkout successful.");
                    }else response.error("Structure is already checked out.");

                }catch(QException qe){
                    response.error(qe.getMessage());
                }
            }else response.error("You do not have permission to checkout structures.");
        }else response.error(INVALID_SESSION);
        return JSONValue.toJSONString(response.getResponse());
    }

    @GET @Path("/checkin/{structure}")
    @Produces("application/json")
    public static String checkinStructure(@QueryParam("ssid") String ssid,
                                          @PathParam("structure") long structId){
        ResponseHelper response = new ResponseHelper(ssid);
        if(response.session != null){
            UserManager uManager = new UserManager(response.conn);
            QUser user = ServiceUtil.getUser(response.session.getUserId(), uManager);
            if(uManager.hasRole(user, CURATE)){
                StructureManager sManager = new StructureManager(response.conn);
                try{
                    QStructure structure = getStructure(structId, sManager);
                    sManager.checkin(structure);
                    response.message("Checkin successful.");
                }catch(QException qe){
                    response.error(qe.getMessage());
                }
            }else response.error("You do not have permission to checkin structures.");
        }else response.error(INVALID_SESSION);
        return JSONValue.toJSONString(response.getResponse());
    }*/
    
    @POST @Path("/approve/{structure}")
    @Produces("application/json")
    public static String approveStructure(@FormParam("ssid") String ssid,
                                          @PathParam("structure") long structId,
                                          @FormParam("comment") String comment){
        ResponseHelper response = new ResponseHelper(ssid);
        if(response.session != null){
            UserManager uManager = new UserManager(response.conn);
            QUser user = ServiceUtil.getUser(response.session.getUserId(), uManager);
            if(uManager.hasRole(user, CURATE)){
                StructureManager sManager = new StructureManager(response.conn);
                try{
                    QStructure structure = getStructure(structId, sManager);
                    sManager.approve(structure, user, comment);
                    response.message("Structure approved.");
                }catch(QException qe){
                    response.error(qe.getMessage());
                }
            }else response.error("You do not have permission to approve structures.");
        }else response.error(INVALID_SESSION);
        return JSONValue.toJSONString(response.getResponse());
    }
    
    @POST @Path("/reject/{structure}")
    @Produces("application/json")
    public static String rejectStructure(@FormParam("ssid") String ssid,
                                         @PathParam("structure") long structId,
                                         @FormParam("comment") String comment){
        ResponseHelper response = new ResponseHelper(ssid);
        if(response.session != null){
            UserManager uManager = new UserManager(response.conn);
            QUser user = ServiceUtil.getUser(response.session.getUserId(), uManager);
            if(uManager.hasRole(user, REVIEW) || 
               uManager.hasRole(user, CURATE)){
                StructureManager sManager = new StructureManager(response.conn);
                try{
                    QStructure structure = getStructure(structId, sManager);
                    sManager.reject(structure, user, comment);
                    response.message("Structure rejected.");
                }catch(QException qe){
                    response.error(qe.getMessage());
                }
            }else response.error("You do not have permission to reject structures.");
        }else response.error(INVALID_SESSION);
        return JSONValue.toJSONString(response.getResponse());
    }
    
    @POST @Path("/defer/{structure}")
    @Produces("application/json")
    public static String deferStructure(@FormParam("ssid") String ssid,
                                        @PathParam("structure") long structId,
                                        @FormParam("comment") String comment){
        ResponseHelper response = new ResponseHelper(ssid);
        if(response.session != null){
            UserManager uManager = new UserManager(response.conn);
            QUser user = ServiceUtil.getUser(response.session.getUserId(), uManager);
            if(uManager.hasRole(user, REVIEW) || 
               uManager.hasRole(user, CURATE)){
                StructureManager sManager = new StructureManager(response.conn);
                try{
                    QStructure structure = getStructure(structId, sManager);
                    sManager.defer(structure, user, comment);
                    response.message("Structure deferred.");
                }catch(QException qe){
                    response.error(qe.getMessage());
                }
            }else response.error("You do not have permission to defer structures.");
        }else response.error(INVALID_SESSION);
        return JSONValue.toJSONString(response.getResponse());
    }
    
    @GET @Path("/commit")
    @Produces("application/json")
    public static String commitStructures(@QueryParam("ssid") String ssid){
        ResponseHelper response = new ResponseHelper(ssid);
        PersistenceUtil.disableAutoCommit(response.conn);
        if(response.session != null){
            UserManager uManager = new UserManager(response.conn);
            QUser user = ServiceUtil.getUser(response.session.getUserId(), uManager);
            if(uManager.hasRole(user, ADMIN)){
                StructureManager sManager = new StructureManager(response.conn);
                TreeManager tManager = new TreeManager(response.conn);
                try{
                    int structs = 0;
                    Map<String, Integer> residueCounts = new HashMap<String, Integer>();
                    for(Iterator<QStructure> iter = sManager.list(ReviewStatus.approved, null, null, null, -1, -1); iter.hasNext();){
                        QStructure struct = iter.next();
                        QTree tree = sManager.getTree(struct);
                        Integer residues = residueCounts.get(tree.getName());
                        if(residues == null) residues = 0;
                        if(tree.getSpec() != null){
                            struct.setStatus(ReviewStatus.committed);
                            sManager.update(struct);
                            residues += tManager.updateTree(struct);
                            residueCounts.put(tree.getName(), residues);
                        }
                        if(QConfiguration.APIENABLED){
                            OntologyClient.submitStructure(sManager, struct);
                        }
                        structs++;
                    }
                    response.add("residues", residueCounts);
                    response.add("structs", structs);
                    response.message("Commit successful.");
                    PersistenceUtil.commit(response.conn);
                    PersistenceUtil.enableAutoCommit(response.conn);
                }catch(QException qe){
                    PersistenceUtil.rollback(response.conn);
                    PersistenceUtil.enableAutoCommit(response.conn);
                    response.error(qe.getMessage());
                }
            }else{
                PersistenceUtil.rollback(response.conn);
                PersistenceUtil.enableAutoCommit(response.conn);
                response.error("You do not have permission to commit structures.");
            }
        }else{
            PersistenceUtil.rollback(response.conn);
            PersistenceUtil.enableAutoCommit(response.conn);
            response.error(INVALID_SESSION);
        }
        return JSONValue.toJSONString(response.getResponse());
    }
    
    @POST @Path("/review/{structure}")
    @Produces("application/json")
    public static String reviewStructure(@FormParam("ssid")      String ssid,
                                         @PathParam("structure") long structId,
                                         @FormParam("spec")      String spec,
                                         @FormParam("type")      long typeId,
                                         @FormParam("comment")   String comment){
        ResponseHelper response = new ResponseHelper(ssid);
        if(response.session != null){
            UserManager uManager = new UserManager(response.conn);
            QUser user = ServiceUtil.getUser(response.session.getUserId(), uManager);
            if(uManager.hasRole(user, REVIEW)){
                StructureManager manager = new StructureManager(response.conn);
                try{
                    spec = JSONValue.toJSONString(JSONValue.parse(spec));
                    QStructureType type = getType(typeId, new TypeManager(response.conn));
                    QStructure structure = ServiceUtil.getStructure(structId, manager);
                    String hash = QratorUtils.hashSpec(spec);
                    if(!structure.getHash().equals(hash)){  // if the structure has changed
                        ReferenceManager refManager = new ReferenceManager(response.conn);
                        SourceManager srcManager = new SourceManager(response.conn);
                        AnnotationManager annoManager = new AnnotationManager(response.conn);

                        QSource glycomeDB = srcManager.get("GlycomeDB");
                        // get all references to GlycomeDB (should be only one)
                        Iterator<QReference> refs = refManager.list(structure, glycomeDB);
                        if(refs.hasNext()){  // if there was one, insert an annotation that this structure was modified
                            QReference reference = refs.next();
                            String srcId = reference.getSrcId();
                            annoManager.create("Modified from GlycomeDB "+srcId, structure, user);
                        }

                        // remove all references
                        refManager.removeAll(structure);
                    }
                    structure.setSpec(QratorUtils.formatMatchSpec(spec));
                    structure.setHash(hash);
                    manager.update(structure);
                    manager.setType(structure, type);
                    manager.reviewed(structure, user, comment);
                    response.message("Structure reviewed.");
                    response.add("id", structure.getId());
                }catch(Exception e){
                    //e.printStackTrace();
                    response.error(e.getMessage());
                }
            }else response.error("You do not have permission to review structures.");
        }else response.error(INVALID_SESSION);
        return JSONValue.toJSONString(response.getResponse());
    }
    
    @POST @Path("/add/annotation/{structure}")
    @Produces("application/json")
    public static String addAnnotation(@FormParam("ssid")      String ssid,
                                       @PathParam("structure") long structId,
                                       @FormParam("comment")   String comment){
        ResponseHelper response = new ResponseHelper(ssid);
        if(response.session != null){
            AnnotationManager aManager = new AnnotationManager(response.conn);
            UserManager uManager = new UserManager(response.conn);
            QUser user = ServiceUtil.getUser(response.session.getUserId(), uManager);
            try{
                QStructure structure = getStructure(structId, new StructureManager(response.conn));
                aManager.create(comment, structure, user);
                response.message("Comment added.");
            }catch(PersistenceException qe){
                response.error(qe.getMessage());
            }
        }else response.error(INVALID_SESSION);
        return JSONValue.toJSONString(response.getResponse());
    }
    
    @GET @Path("/remove/annotation/{annotation}")
    @Produces("application/json")
    public static String removeAnnotation(@QueryParam("ssid") String ssid,
                                          @PathParam("annotation") long refId){
        ResponseHelper response = new ResponseHelper(ssid);
        if(response.session != null){
            AnnotationManager aManager = new AnnotationManager(response.conn);
            UserManager uManager = new UserManager(response.conn);
            QUser user = ServiceUtil.getUser(response.session.getUserId(), uManager);
            try{
                QAnnotation annotation = getAnnotation(refId, aManager);
                QUser creator = aManager.getCreator(annotation);
                if(user.getId() == creator.getId() || uManager.hasRole(user, "admin")){
                    aManager.remove(annotation);
                    response.message("Annotation removed.");
                }else response.error("You do not have permission to remove this comment.");
            }catch(PersistenceException qe){
                response.error(qe.getMessage());
            }
        }else response.error(INVALID_SESSION);
        return JSONValue.toJSONString(response.getResponse());
    }
    
    @GET @Path("/add/reference/{structure}")
    @Produces("application/json")
    public static String addReference(@QueryParam("ssid")     String ssid,
                                      @PathParam("structure") long structureId,
                                      @QueryParam("source")   long sourceId,
                                      @QueryParam("refId")    String refId){

        ResponseHelper response = new ResponseHelper(ssid);
        if(response.session != null){
            ReferenceManager rManager = new ReferenceManager(response.conn);
            UserManager uManager = new UserManager(response.conn);
            QUser user = ServiceUtil.getUser(response.session.getUserId(), uManager);
            try{
                QStructure file = getStructure(structureId, new StructureManager(response.conn));
                QSource source = getSource(sourceId, new SourceManager(response.conn));
                QReference reference = rManager.create(refId, source, file, user);
                Map<String, Object> state = referenceState(reference, rManager);
                state.put("createdBy", user.getName());
                response.add("objs", state);
                response.message("Reference added.");
            }catch(QException qe){
                response.error(qe.getMessage());
            }
        }else response.error(INVALID_SESSION);
        return JSONValue.toJSONString(response.getResponse());
    }

    @GET @Path("/remove/reference/{reference}")
    @Produces("application/json")
    public static String removeReference(@QueryParam("ssid") String ssid,
                                         @PathParam("reference") long refId){
        ResponseHelper response = new ResponseHelper(ssid);
        if(response.session != null){
            ReferenceManager rManager = new ReferenceManager(response.conn);
            UserManager uManager = new UserManager(response.conn);
            QUser user = ServiceUtil.getUser(response.session.getUserId(), uManager);
            try{
                QReference reference = getReference(refId, rManager);
                QUser creator = rManager.getCreator(reference);
                if(user.getId() == creator.getId() || uManager.hasRole(user, "admin")){
                    rManager.remove(reference);
                    response.message("Reference removed.");
                }else response.error("You do not have permission to remove this reference.");
            }catch(PersistenceException qe){
                response.error(qe.getMessage());
            }
        }else response.error(INVALID_SESSION);
        return JSONValue.toJSONString(response.getResponse());
    }

    
    @GET @Path("/annotations/{structure}")
    @Produces("application/json")
    public static String listAnnotations(@PathParam("structure") long structId,
                                         @QueryParam("offset")   int offset,
                                         @QueryParam("limit")    int limit){

        ResponseHelper response = new ResponseHelper();
        AnnotationManager aManager = new AnnotationManager(response.conn);
        try{
            QStructure structure = getStructure(structId, new StructureManager(response.conn));
            Iterator<QAnnotation> annotations = aManager.list(structure, offset, limit);
            List<Map<String, Object>> aList = new ArrayList<Map<String,Object>>();
            int count = 0;
            while(annotations.hasNext()){
                QAnnotation anno = annotations.next();
                Map<String, Object> state = annotationState(anno, aManager);
                aList.add(state);
                count++;
            }
            if(count > 0){
                response.add("size", count);
                response.add("objs", aList);
                response.message("List annotations successful.");
            }else response.message("No annotations present.");
        }catch(QException qe){
            response.error(qe.getMessage());
        }
        return JSONValue.toJSONString(response.getResponse());
    }

    @GET @Path("/references/{structure}")
    @Produces("application/json")
    public static String listReferences(@PathParam("structure") long structId,
                                        @QueryParam("offset") int offset,
                                        @QueryParam("limit") int limit){

        ResponseHelper response = new ResponseHelper();
        ReferenceManager rManager = new ReferenceManager(response.conn);
        try{
            QStructure structure = getStructure(structId, new StructureManager(response.conn));
            Iterator<QReference> references = rManager.list(structure, offset, limit);
            List<Map<String, Object>> rList = new ArrayList<Map<String,Object>>();
            int count = 0;
            while(references.hasNext()){
                QReference ref = references.next();
                Map<String, Object> state = referenceState(ref, rManager);
                rList.add(state);
                count++;
            }
            if(count > 0){
                response.add("size", count);
                response.add("objs", rList);
                response.message("List references successful.");
            }else response.message("No references present.");
        }catch(QException qe){
            response.error(qe.getMessage());
        }
        return JSONValue.toJSONString(response.getResponse());
    }
    
    @GET @Path("/provenance/{structure}")
    @Produces("application/json")
    public static String listProvenance(@PathParam("structure") long structId,
                                        @QueryParam("offset") int offset,
                                        @QueryParam("limit") int limit){

        ResponseHelper response = new ResponseHelper();
        ProvenanceManager pManager = new ProvenanceManager(response.conn);
        try{
            QStructure structure = getStructure(structId, new StructureManager(response.conn));
            Iterator<QProvenance> provenances = pManager.list(structure, offset, limit);
            List<Map<String, Object>> pList = new ArrayList<Map<String,Object>>();
            int count = 0;
            while(provenances.hasNext()){
                QProvenance pro = provenances.next();
                Map<String, Object> state = provenanceState(pro, pManager);
                pList.add(state);
                count++;
            }
            if(count > 0){
                response.add("size", count);
                response.add("objs", pList);
                response.message("List provenance successful.");
            }else response.message("No provenance present.");
        }catch(QException qe){
            response.error(qe.getMessage());
        }
        return JSONValue.toJSONString(response.getResponse());
    }
    
    @GET @Path("/sources")
    @Produces("application/json")
    public static String listSources(){

        ResponseHelper response = new ResponseHelper();
        SourceManager sManager = new SourceManager(response.conn);
        try{
            Iterator<QSource> sources = sManager.list(null);
            List<Map<String, Object>> sList = new ArrayList<Map<String,Object>>();
            int count = 0;
            while(sources.hasNext()){
                QSource src = sources.next();
                Map<String, Object> state = sourceState(src, sManager);
                sList.add(state);
                count++;
            }
            if(count > 0){
                response.add("size", count);
                response.add("objs", sList);
                response.message("List sources successful.");
            }else response.message("No sources present.");
        }catch(QException qe){
            response.error(qe.getMessage());
        }
        return JSONValue.toJSONString(response.getResponse());
    }
 
    
    public static void main(String[] args) throws IOException{
        
       /* String username = "admin";
        String password = Utils.secureHash("password");
        Connection conn = PersistenceUtil.connect();
        
        QSessionManager sessions = QConfiguration.SESSIONS;
        QAuthentication auth = new QAuthentication(conn);
        auth.setCredential("username", username);
        auth.setCredential("password", password);
        QSession session = sessions.login(auth);
        session = QConfiguration.SESSIONS.getSessionById(session.getId());
        
        //String response = listFiles(session.getId(), null, null, null, null, 0, 10);
        //System.out.println(response);
        TreeManager tManager = new TreeManager(conn);
        StructureManager fManager = new StructureManager(conn);
        StructureManager sManager = new StructureManager(conn);
        ReferenceManager rManager = new ReferenceManager(conn);
        QTree tree = tManager.get("N-glycan"); //"GalNAc-initiated_O-glycan");*/
        //QFile file = ServiceUtil.getFile(2l, fManager);
        
        System.out.println("Starting...");
        
        Connection conn = PersistenceUtil.connect();
        StructureManager manager = new StructureManager(conn);
        
        QStructure struct = ServiceUtil.getStructure(2889, manager);
        
        //File f = new File("/Users/durandal/Development/Qrator/Files/OMan/fully-defined/00045.glyde.xml");
                
        String glydeXML = struct.getContents(); //PersistenceUtil.fileToString(f);
        CompositeResidue root = null;
        try{
            root = FileUtils.parseGlydeRoot(glydeXML);
        }catch(QException qe){
            throw new StructureParsingException(qe.getMessage());
        }
        GlydeStructure candidate = GlydeStructure.getGlydeStructure(root);
        System.out.println("glyde: "+JSONValue.toJSONString(candidate.toMap()));
        System.out.println("spec: "+struct.getSpec());
        
        
        String spec = getCandidateSpec(candidate);
        String hash = QratorUtils.hashSpec(spec);
        
        System.out.println(hash);
        System.out.println(struct.getHash());
        
        
        //System.out.println(compareStructure(2l));
        
        /*Connection conn = PersistenceUtil.connect();
        StructureManager manager = new StructureManager(conn);
        Filter<QStructure> filter = new Filter<QStructure>(QStructure.class);
        filter.ne("uri", null);
        int count = 0;
        for(Iterator<QStructure> iter = manager.list(filter); iter.hasNext();){
            QStructure struct = iter.next();
            System.out.println(struct.getUri());
            struct.setStatus(ReviewStatus.committed);
            manager.update(struct);
            count++;
        }
        System.out.println("Found "+count+".");*/
        
        /*Connection conn = PersistenceUtil.connect();
        File f = new File("/Users/durandal/Development/Qrator/Files/OFuc/fully-defined/test.tgz");
        byte[] contents = PersistenceUtil.getFileContents(f);
        StructureManager manager = new StructureManager(conn);
        Map<String, Object> report = FileUtils.extractStructures(manager, "test.tgz", contents, null);
        System.out.println(JSONValue.toJSONString(report));*/
        
        //getById(1l, "2338");
        
        
        
        
//        List<MatchNode> matches = FileManager.listMatches(tree, file);
//        for(MatchNode match: matches){
//            System.out.println(match.getCanonicalNode().getAttribute(QNode.MONO_NAME));
//        }
        
       /* for(Iterator<QStructure> structures = sManager.list(null); structures.hasNext();){
            QStructure struct = structures.next();
            System.out.println("Scanning "+struct.getId());
            
            String spec = struct.getSpec();
            if(spec.contains("NOATTEMPT"))
                System.out.println(struct.getUri());
        }*/
        
//        GlycanFit fit = new GlycanFit();
//        Set<WholeEntity> struct = FileManager.parseGlyde(file.getContents());
//        WholeEntity we = struct.iterator().next();
//        List<MatchNode> matches = fit.match(we, tree);
//        List<Map<String,Object>> json = sManager.getMatchSpecs(matches);
//        for(Map<String,Object> match: json){
//            System.out.println(JSONValue.toJSONString(match));
//        }
        
        
/*list files with different classifications than the file they came from
 * 
        for(Iterator<QStructure> structures = sManager.list(null); structures.hasNext();){
            QStructure struct = structures.next();
            QStructureType structType = sManager.getType(struct);
            QFile file = sManager.getFile(struct);
            QStructureType fileType = fManager.getType(file);
            if(!structType.getName().equals(fileType.getName())){
                String ref = null;
//                for(Iterator<QReference> references = rManager.list(struct, 0, -1); references.hasNext();){
//                    QReference reference = references.next();
//                    QSource source = rManager.getSource(reference);
//                    if(source.getName().equals("GlycomeDB")){
//                        ref = reference.getSourceId();
//                    }
//                }
                //if(ref == null){
                    ref = file.getName();
                //}
                System.out.println(ref+": "+structType.getName()+
                                   "\nMotif Matcher: "+fileType.getName()+"\n\n");
            }   
        }
*****************************************/
        
//        System.out.println(JSONValue.toJSONString(json));
        System.out.println("Done.");
        
    }
}