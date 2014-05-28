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
package edu.uga.qrator.logic.io;


import edu.uga.qrator.except.QException;
import edu.uga.qrator.logic.manage.StructureManager;
import edu.uga.qrator.logic.manage.UserManager;
import edu.uga.qrator.obj.entity.QStructure;
import edu.uga.qrator.obj.entity.QUser;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import persist.util.IOUtil;
import persist.util.PersistenceUtil;



/**
 *
 * @author durandal
 */
public class StructureImporter {
    
    
    public static void importStructures(File structureDirectory, QUser creator) throws SQLException{
        
        List<File> contents = new ArrayList<File>();
        if(structureDirectory.isDirectory()){
            File[] structs = structureDirectory.listFiles();
            contents.addAll(Arrays.asList(structs));
        }else{
            contents.add(structureDirectory);
        }
        
        Connection conn = PersistenceUtil.connect();
        StructureManager sManager = new StructureManager(conn);
        
        Map<String, List<String>> messages = new HashMap<String, List<String>>();
        System.out.println("\n---------Importing files---------\n");
        
        for(File f: contents){
            String fileName = f.getName();
            if(fileName.startsWith(".") || f.isDirectory() || !fileName.endsWith(".xml")) continue;
            System.out.println("--Importing - "+fileName);
            try{
                // extract file contents
                byte[] fileContents = IOUtil.getFileContents(f);
                String glydeXML = new String(fileContents);
                // check for pre-existing structure
                String hash = StructureManager.generateHash(glydeXML);
                QStructure structure = sManager.getByHash(hash);
                if(structure != null){
                    System.out.println(fileName+" already exists ("+structure.getFilename()+"). Skipping.");
                    continue;
                }
                // create the structure if not present
                sManager.create(fileName, fileContents, null, creator);
                
                System.out.println("-------------end "+fileName+"-------------\n\n");
            }catch(IOException ioe){
                ioe.printStackTrace();
            }catch(QException qe){
                String message = qe.getMessage();
                message = message.replace("Unknown monosaccharide detected: ","");
                List<String> list = messages.get(message);
                if(list == null){
                    list = new ArrayList<String>();
                    messages.put(message, list);
                }
                list.add(fileName+" : "+qe.getMessage());
                
            }
        }
        
        for(String key: messages.keySet()){
            List<String> list = messages.get(key);
            for(String s : list){
                
                if(s.contains("phospho-ethanolamine") ||
                    s.contains("n-methyl") ||
                    s.contains("fluoro") ||
                    s.contains("n-formyl") ||
                    s.contains("thio") ||
                    s.contains("pyruvate") ||
                    s.contains("formyl") ||
                    s.contains("chloro") ){
                    
                }else
                    System.out.println(s);
            }
        }
        
        for(String key: messages.keySet()){
            if(key.contains("phospho-ethanolamine") ||
                    key.contains("n-methyl") ||
                    key.contains("fluoro") ||
                    key.contains("n-formyl") ||
                    key.contains("thio") ||
                    key.contains("pyruvate") ||
                    key.contains("formyl") ||
                    key.contains("chloro") ){
                
            }else{
                List<String> list = messages.get(key);
                System.out.println(list.size()+"\t"+key);   
            }
        }
        
        System.out.println("\n-------Done importing files-------\n");
        
        System.out.println("-------Finished-------");
        conn.close();
    }
    
    public static void main(String[] args){
        
        Connection conn = PersistenceUtil.connect();
        UserManager manager = new UserManager(conn);
        QUser glycomeDB = manager.getUserByUsername("glycomedb");
        
        try{
            importStructures(new File("/Users/durandal/Development/Qrator/Files/OMan/fully-defined/"), glycomeDB);
        
            conn.close();
        }catch(SQLException sqe){
            sqe.printStackTrace();
        }
        
    }
    
}
