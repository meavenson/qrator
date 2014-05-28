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
package edu.uga.qrator.logic.manage;

import edu.uga.glydeII.gom.AtomLink;
import edu.uga.glydeII.gom.CompositeResidue;
import edu.uga.glydeII.gom.GlydeException;
import edu.uga.glydeII.gom.Residue;
import edu.uga.qrator.logic.match.motif.MotifManager;
import edu.uga.qrator.util.QratorUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Matthew
 */
public class GlydeStructure {
    public final GlydeResidue root;

    private GlydeStructure(GlydeResidue root){ 
        this.root = root;
    }

    public Map<String, Object> toMap(){
        return root.toMap();
    }
    
    public static GlydeStructure getGlydeStructure( Map<String,Object> json ){
        return new GlydeStructure( new GlydeResidue(json) );
    }
    
    public static GlydeStructure getGlydeStructure( CompositeResidue cr ){
        return new GlydeStructure( getGlydeResidue(cr) );
    }
    
    private static GlydeResidue getGlydeResidue( CompositeResidue cr ){
        GlydeResidue residue = new GlydeResidue(cr);
        for (CompositeResidue each : QratorUtils.getChildren(cr)) {
            residue.addChild(getGlydeResidue(each));
        }
        return residue;
    }
    
    public int residueCount(){
        return root.residueCount();
    }
    
    public static class GlydeResidue {
        public final String id;
        public final String anomer;
        public final String linkNum;
        public final String from;
        public final String to;
        private String match;
        private final List<GlydeResidue> children;
        
        public GlydeResidue(String id, String anomer, String linkNum, String from, String to){
            this.id = id;
            this.anomer = anomer;
            this.linkNum = linkNum;
            this.from = from;
            this.to = to;
            children = new ArrayList<GlydeResidue>();
        }

        private GlydeResidue(CompositeResidue cr){
            String partId = cr.getPartId();
            String altName = cr.getName();
            id = partId != null? partId : altName!=null? altName:"unknown";
            String aStr = null;
            try{
                aStr = cr.getAnomericConfiguration();
            }catch(GlydeException ge){}
            anomer = aStr;
            linkNum = MotifManager.getLinkPosition(cr);
            
            Residue base = cr.getBaseType() == null?
                            cr.getSubstituents().iterator().next().getGeneralType() : 
                            cr.getBaseType().getGeneralType();
            if(base.getLinkOut() != null){
                AtomLink atomLink = base.getLinkOut().getSubLinks().first();
                from = atomLink.getFrom().getPartId();
                to = atomLink.getTo().getPartId();
            }else{
                from = null;
                to = null;
            }
            children = new ArrayList<GlydeResidue>();
        }
        
        private GlydeResidue(Map<String,Object> json){
            id = json.get("id").toString();
            anomer =  json.get("anomer") != null?  json.get("anomer").toString() : null;
            linkNum = json.get("link") != null?    json.get("link").toString() :   null;
            from =    json.get("from") != null?    json.get("from").toString() :   null;
            to =      json.get("to") != null?      json.get("to").toString() :     null;
            children = new ArrayList<GlydeResidue>();
            @SuppressWarnings("unchecked")
            List<Map<String,Object>> childList = (List<Map<String,Object>>) json.get("children");
            if(childList != null){
                for(Map<String,Object> child: childList){
                    GlydeResidue childRes = new GlydeResidue(child);
                    children.add(childRes);
                }
            }
        }
        
        public void setMatch(String match){
            this.match = match;
        }
        
        public String getMatch(){
            return match;
        }
        
        public int residueCount(){
            int count = 1;
            for(GlydeResidue child: children){
                count += child.residueCount();
            }
            return count;
        }

        public void addChild(GlydeResidue child){
            children.add(child);
        }
        
        public List<GlydeResidue> getChildren(){
            return children;
        }
        
        private Map<String,Object> toMap(){
            Map<String, Object> map = new HashMap<String, Object>();
            List<Map<String, Object>> childList = new ArrayList<Map<String, Object>>();
            map.put("id", id);
            if(anomer != null) map.put("anomer", anomer);
            if(linkNum != null) map.put("link", linkNum);
            if(from != null) map.put("from", from);
            if(to != null) map.put("to", to);
            if(match != null) map.put("match", match);
            
            for(GlydeResidue child: children){
                childList.add(child.toMap());
            }
            if(!childList.isEmpty())
                map.put("children", childList);
            return map;
        }

    }
}
