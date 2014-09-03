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
package edu.uga.qrator.util;

import edu.uga.glydeII.gom.BaseType;
import edu.uga.glydeII.gom.CompositeResidue;
import edu.uga.glydeII.gom.Molecule;
import edu.uga.glydeII.gom.PartEntity;
import edu.uga.glydeII.gom.Residue;
import edu.uga.glydeII.gom.WholeEntity;
import edu.uga.glydeII.io.GOMReader;
import edu.uga.qrator.except.QException;
import edu.uga.qrator.except.StructureDuplicateException;
import edu.uga.qrator.except.StructureParsingException;
import edu.uga.qrator.logic.QConfiguration;
import edu.uga.qrator.logic.manage.ReferenceManager;
import edu.uga.qrator.logic.manage.SourceManager;
import edu.uga.qrator.logic.manage.StructureManager;
import edu.uga.qrator.obj.entity.QReference;
import edu.uga.qrator.obj.entity.QSource;
import edu.uga.qrator.obj.entity.QStructure;
import edu.uga.qrator.obj.entity.QUser;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarImporterGlycoCTCondensed;
import org.eurocarbdb.MolecularFramework.io.Glyde.SugarExporterGlydeIIC;
import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.eurocarbdb.application.glycanbuilder.GlycanRendererAWT;
import org.eurocarbdb.application.glycoworkbench.GlycanWorkspace;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import persist.util.IOUtil;

/**
 *
 * @author durandal
 */
public class FileUtils {
    
    private static final String XML_EXT = ".xml";
    private static final String ZIP_EXT = ".zip";
    private static final String TARGZ_EXT = ".tar.gz";
    private static final String TGZ_EXT = ".tgz";
    private static final String GWS_EXT = ".gws";
    private static final GlycanWorkspace m_gwb = new GlycanWorkspace(new GlycanRendererAWT());
    
    public static Map<String, Object> extractStructures(StructureManager manager, String filename, byte[] contents, QUser user){
        
        Map<String,Object> report = new HashMap<String,Object>();
        List<Map<String, Object>> statuses = new ArrayList<Map<String, Object>>();
        report.put("status", statuses);
        
        ByteArrayInputStream fis = new ByteArrayInputStream(contents);
        BufferedInputStream bis = new BufferedInputStream(fis);
        try{
            // handle gzipped tar structures
            if(filename.endsWith(TARGZ_EXT) || filename.endsWith(TGZ_EXT)){
                                
                GZIPInputStream gis = new GZIPInputStream(bis);
                byte[] bytes = IOUtil.getStreamContents(gis);
                
                // read from the reconstituted tar archive
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                BufferedInputStream tarbis = new BufferedInputStream(bais);
                ArchiveInputStream input = new ArchiveStreamFactory().createArchiveInputStream(new BufferedInputStream(tarbis));

                statuses.addAll(readArchive(manager, input, user));

                input.close();
                tarbis.close();
                bais.close();
                
            // handle regular zip archives
            }else if(filename.endsWith(ZIP_EXT)){
                ArchiveInputStream input = new ArchiveStreamFactory().createArchiveInputStream(bis);
                statuses.addAll(readArchive(manager, input, user));
            // handle plain xml structures
            }else if(filename.endsWith(XML_EXT) && !filename.startsWith("\\.")){
                statuses.add(createFromGlydeII(manager, filename, contents, user));
            }else if(filename.endsWith(GWS_EXT) && !filename.startsWith("\\.")){
                statuses.addAll(createFromGWS(manager, filename, contents, user));
            }else{
                report.put("error", "Unsupported file type: "+filename);
            }
        }catch(IOException ioe){
            ioe.printStackTrace();
            report.put("error", ioe.getMessage());
        }catch(ArchiveException ae){
            ae.printStackTrace();
            report.put("error", ae.getMessage());
        }
        return report;
    }
    
    private static List<Map<String, Object>> readArchive(StructureManager manager, ArchiveInputStream input, QUser user) throws IOException{
        List<Map<String, Object>> statuses  = new ArrayList<Map<String, Object>>();
        ArchiveEntry tEntry = input.getNextEntry();
        while(tEntry != null){
            String name = tEntry.getName();
            int lastSlash = name.lastIndexOf("/");
            lastSlash = lastSlash == -1? 0 : lastSlash;
            name = name.substring(lastSlash);
            byte[] contents = IOUtil.getStreamContents(input);
            if(name.endsWith(XML_EXT) && !name.startsWith("\\.")){
                statuses.add(createFromGlydeII(manager, name, contents, user));
            }else if(name.endsWith(GWS_EXT) && !name.startsWith("\\.")){
                statuses.addAll(createFromGWS(manager, name, contents, user));
            }else{
                Map<String, Object> status = new HashMap<String, Object>();
                status.put("name", name);
                status.put("error", "Unsupported file type: "+name);
                statuses.add(status);
            }
            tEntry = input.getNextEntry();
        }
        return statuses;
    }
    
    /***************************************************************
     * Creates a QStructure from the contents of a GlydeII file and returns
     * a report of the results.
     * @param  manager   a QStructure manager
     * @param  name      the name of the file
     * @param  contents  the contents of the file
     * @user   user      the user creating the structure
     * @return a map with the name of the file, and either a message saying
     * it was created successfully, or an error containing the exception
     * message.  Also may contain an id of a duplicate structure if one
     * was found.
     */
    private static Map<String, Object> createFromGlydeII(StructureManager manager, String name, byte[] contents, QUser user) throws IOException{
        Map<String, Object> status = new HashMap<String, Object>();
        status.put("name", name);
        try{
            manager.create(name, contents, null, user);
            status.put("message", "Created Successfully");
        }catch(StructureDuplicateException sde){
            QStructure dupe = sde.getDuplicate();
            status.put("error", sde.getMessage());
            status.put("id", dupe.getId());
        }catch(StructureParsingException spe){
            status.put("error", spe.getMessage());
        }catch(NullPointerException npe){
            npe.printStackTrace();
            status.put("error", "An unspecified error occurred");
        }
        return status;
    }
    
    /***************************************************************
     * Creates a QStructure from the contents of a GWS file and returns
     * a report of the results.
     * @param  manager   a QStructure manager
     * @param  name      the name of the file
     * @param  contents  the contents of the file
     * @user   user      the user creating the structure
     * @return a list of maps, each with the name of the file, and either 
     * a message saying it was created successfully, or an error containing
     * the exception message.  Also may contain an id of a duplicate 
     * structure if one was found.
     */
    private static List<Map<String, Object>> createFromGWS(StructureManager manager, String name, byte[] contents, QUser user) throws IOException{
        List<Map<String, Object>> statuses = new ArrayList<Map<String, Object>>();
        try{
            List<String> glydeList = gwsToGlydeII(new String(contents));
            int i=1;
            for(String glydeII : glydeList){
                Map<String, Object> status = new HashMap<String, Object>();
                status.put("name", name+"_"+i);
                i++;
                try{
                    manager.create(name, glydeII.getBytes(), null, user);
                    status.put("message", "Created Successfully");
                }catch(StructureDuplicateException sde){
                    QStructure dupe = sde.getDuplicate();
                    status.put("error", sde.getMessage());
                    status.put("id", dupe.getId());
                }catch(NullPointerException npe){
                    npe.printStackTrace();
                    status.put("error", "An unspecified error occurred");
                }catch(Exception ex){
                    status.put("error", ex.getMessage());
                }
                statuses.add(status);
            }
        }catch(Exception ex){
            ex.printStackTrace();
            Map<String, Object> status = new HashMap<String, Object>();
            status.put("name", name);
            status.put("error", ex.getMessage());
            statuses.add(status);
        }
        
        return statuses;
    }
    
    
    // TODO: reference extraction needs a rewrite 
    public static Iterator<QReference> extractReferences(ReferenceManager refManager, SourceManager sourceManager, QUser uploader, QStructure structure){
        List<QReference> references = new ArrayList<QReference>();
        List<String> matches = QratorUtils.getMatches(structure.getContents(), "<resource.*?/>", 0);
                
        for(String match: matches){
            try{
                String name = QratorUtils.getMatches(match, "name=\"(.*?)\"", 1).get(0).trim().toLowerCase();
                QSource source = sourceManager.getLowerCase(name);
                if(source != null){
                    String id = QratorUtils.getMatches(match, "id=\"(.*?)\"", 1).get(0);
                
                    QReference ref = refManager.get(structure, source, id);
                    if(ref == null){
                        ref = refManager.create(id, source, structure, uploader);
                    }
                    references.add(ref);
                }else throw new QException("Source "+name+" was not found.");
                                
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return references.iterator();
    }
    
    /***************************************************************
     * Creates a GOMTree representation from the original GLYDE XML structure
     * @param  stream    XML stream to be parsed
     * @return GOMTree   GOMTree object representation of the GLYDE structure.
     */
    public static Set<WholeEntity> parseGlyde(InputStream stream){
        Set<WholeEntity> gom = null;
        try{
            Document xml = QratorUtils.getDocument(stream);
            NodeList list = xml.getElementsByTagName("repeat_block");
            if(list != null && list.getLength() != 0){
                throw new QException("Qrator does not currently support Repeat Blocks.");
            }else{
                GOMReader reader = new GOMReader(QConfiguration.MONOMAP);
                gom = reader.buildGOMWithNames(stream);
            }
        }catch(SAXException e){
            throw new QException("XML Parsing Failed.  Please check the structure of your XML document.");
        }catch(IOException e){
            throw new QException("IO Error Encountered.");
        }catch(Exception e){
            //if(!e.getMessage().contains("CompositeResidue")) e.printStackTrace();
            throw new QException(e.getMessage());
        }
        return gom;
    }
    
    /***************************************************************
     * Creates a GOMTree representation from the original GLYDE XML structure
     * @param  glydeXML  XML string to be parsed
     * @return GOMTree   GOMTree object representation of the GLYDE structure.
     */
    public static Set<WholeEntity> parseGlyde(String glydeXML){
        return parseGlyde(new ByteArrayInputStream(glydeXML.getBytes()));
    }
    
    public static CompositeResidue parseGlydeRoot(String str){
        return parseGlydeRoot(new ByteArrayInputStream(str.getBytes()));
    }
    
    /***************************************************************
     * Creates a GOMTree representation from the original GLYDE XML structure
     * @param  stream  XML string to be parsed
     * @return GOMTree   GOMTree object representation of the GLYDE structure.
     */
    public static CompositeResidue parseGlydeRoot(InputStream stream){
        CompositeResidue root = null;
        try{
            Set<WholeEntity> gomNodes = parseGlyde(stream);
            for(WholeEntity we : gomNodes ){
                if(we instanceof Molecule){
                    Molecule m = (Molecule) we;
                    //System.out.println("---Molecule: "+m.getId());
                    for(PartEntity pe: m.getParts()){
                        if(pe instanceof Residue){
                            Residue r = (Residue)pe;
                            //System.out.println("-----Residue: " + r.getPartId());
                            if(r.getLinkOut() == null){
                                //System.out.println("-------Link out null");
                                Object subType = r.getSpecificType();
                                //System.out.println("-------SubType: "+subType);
                                if(subType instanceof BaseType){
                                    root = ((BaseType)subType).getCompositeResidue();
                                    //System.out.println("---------Root: "+root.getPartId());
                                    return root;
                                }
                            }
                        }
                    }
                }
            }
        }catch(Exception e){
            //e.printStackTrace();
            //if(!e.getMessage().contains("CompositeResidue")) e.printStackTrace();
            throw new QException(e.getMessage());
        }
        return null;
    }
    
    public static List<String> gwsToGlydeII(String gws) throws IOException, SugarImporterException, GlycoVisitorException
    {
        return gwsToGlydeII(new ByteArrayInputStream(gws.getBytes()));
    }

    public static List<String> gwsToGlydeII(InputStream stream) throws IOException, SugarImporterException, GlycoVisitorException
    {
        String contents = new String(IOUtil.getStreamContents(stream));
        
        List<String> t_results = new ArrayList<String>();
        SugarImporterGlycoCTCondensed t_importerGlycoCT = new SugarImporterGlycoCTCondensed();
        SugarExporterGlydeIIC t_exporterGlyde = new SugarExporterGlydeIIC();
        
        // split into single sequences
        String[] t_gws = contents.split(";");
        for (String t_sequenceGWS : t_gws)
        {
            // create GWB glycan object and translate to GlycoCT 
            Glycan t_glycan = Glycan.fromString(t_sequenceGWS);
            String t_sequenceGlycoCT = t_glycan.toGlycoCTCondensed();
            // parse GlycoCT and translate to GlydeII
            Sugar t_sugar = t_importerGlycoCT.parse(t_sequenceGlycoCT);
            t_exporterGlyde.start(t_sugar);
            t_results.add(t_exporterGlyde.getXMLCode());
        }
        return t_results;
    }
    
}
