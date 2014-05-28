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


import edu.uga.qrator.logic.manage.StructureManager;
import edu.uga.qrator.logic.manage.TreeManager;
import edu.uga.qrator.logic.match.matcher.MatchNode;
import edu.uga.qrator.logic.match.motif.MotifManager;
import edu.uga.qrator.obj.entity.QStructure;
import edu.uga.qrator.obj.entity.QTree;
import static edu.uga.qrator.service.ServiceUtil.*;
import java.util.List;
import javax.ws.rs.*;
import org.json.simple.JSONValue;
import persist.util.ResponseHelper;

/**
 * @author Matthew
 *
 */
@Path("match")
public class MatchingService{
    
    @GET @Path("/list/{structure}")
    @Produces("application/json")
    public static String listMatches(@QueryParam("ssid")     String ssid,
                                     @PathParam("structure") long structId){
        ResponseHelper response = new ResponseHelper(ssid);
        if(response.session != null){
            StructureManager manager = new StructureManager(response.conn);
            QStructure structure = getStructure(structId, manager);
            //QStructureType type = manager.getType(structure);
            QTree tree = manager.getTree(structure);
            if(tree.getName().equals("Unknown")){
                response.error("A type must be assigned before matching.");
            }else if(tree.getSpec() == null){
                response.error("Tree is not loaded.");
            }else{
                List<MatchNode> matches = StructureManager.listMatches(TreeManager.getRoot(tree), structure);
                response.add("objs", MotifManager.getMatchSpecs(manager, matches));
                response.add("count", matches.size());
            }
        }else response.error(INVALID_SESSION);
        return JSONValue.toJSONString(response.getResponse());
    }
    
}
