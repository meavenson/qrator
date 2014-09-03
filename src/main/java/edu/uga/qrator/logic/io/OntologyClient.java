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

import edu.uga.ccrc.ontology.webapi.client.AccountClient;
import edu.uga.ccrc.ontology.webapi.client.GlycanClient;
import edu.uga.ccrc.ontology.webapi.client.GlycanTreeClient;
import edu.uga.ccrc.ontology.webapi.client.ReferenceClient;
import edu.uga.ccrc.ontology.webapi.client.data.ExcutionResult;
import edu.uga.ccrc.ontology.webapi.client.data.ListObject;
import edu.uga.ccrc.ontology.webapi.client.data.LoginResult;
import edu.uga.ccrc.ontology.webapi.client.data.glycan.id.Comment;
import edu.uga.ccrc.ontology.webapi.client.data.glycan.id.Database;
import edu.uga.ccrc.ontology.webapi.client.data.glycan.id.Glycan;
import edu.uga.ccrc.ontology.webapi.client.data.glycan.id.GlycanTreeURI;
import edu.uga.ccrc.ontology.webapi.client.data.glycan.id.Glycan_Class;
import edu.uga.ccrc.ontology.webapi.client.data.glycan.id.Glycan_Classes;
import edu.uga.ccrc.ontology.webapi.client.data.glycan.id.Provenance;
import edu.uga.ccrc.ontology.webapi.client.data.glycantree.Glycantree;
import edu.uga.ccrc.ontology.webapi.client.data.reference.add.Reference;
import edu.uga.ccrc.ontology.webapi.client.data.reference.add.ReferenceAnnotationGroup;
import edu.uga.ccrc.ontology.webapi.client.exception.ErrorMessageException;
import edu.uga.ccrc.ontology.webapi.client.exception.InvalidMessageExpetion;
import edu.uga.ccrc.ontology.webapi.client.exception.WebApiExecutionException;
import edu.uga.glydeII.gom.CompositeResidue;
import edu.uga.qrator.except.QException;
import edu.uga.qrator.logic.QConfiguration;
import edu.uga.qrator.logic.manage.AnnotationManager;
import edu.uga.qrator.logic.manage.GlydeStructure;
import edu.uga.qrator.logic.manage.ProvenanceManager;
import edu.uga.qrator.logic.manage.ReferenceManager;
import edu.uga.qrator.logic.manage.StructureManager;
import edu.uga.qrator.logic.manage.TreeManager;
import edu.uga.qrator.obj.entity.QAnnotation;
import edu.uga.qrator.obj.entity.QProvenance;
import edu.uga.qrator.obj.entity.QReference;
import edu.uga.qrator.obj.entity.QSource;
import edu.uga.qrator.obj.entity.QStructure;
import edu.uga.qrator.obj.entity.QStructureType;
import edu.uga.qrator.obj.entity.QTree;
import edu.uga.qrator.obj.entity.QUser;
import edu.uga.qrator.util.FileUtils;
import edu.uga.qrator.util.QratorUtils;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.json.simple.JSONValue;
import org.xml.sax.SAXException;
import persist.query.filter.Filter;

/**
 * Acquires tree information from a connected OntologyWebAPI and sends
 * structure information for inclusion in the API's GlycO ontology.
 * 
 * @author Matthew Eavenson (durandal@uga.edu)
 */
public class OntologyClient {
    
    // the account client, which keeps account credentials
    private final AccountClient account;
    
    private final GlycanClient glycanClient;
    private final ReferenceClient refClient;
    private final GlycanTreeClient treeClient;
    
    // the web address of the API, as a URL
    private static final URL address = QConfiguration.APIADDRESS;
    
    // the URI prefix for the GlycO ontology -- should possibly be transferred to the config file
    private static final String PREFIX = "http://glycomics.ccrc.uga.edu/ontologies/GlycO#";
    
    // create a new account client and login
    public OntologyClient(){
        account = new AccountClient(address.toExternalForm(), null);
        try{
            LoginResult loginresult=account.login( QConfiguration.APIUSERNAME, QConfiguration.APIPASSWORD );
            if(!loginresult.isError()){
                account.setSsid(loginresult.getSsid());
                account.setPath(loginresult.getPath());
                account.setDomain(loginresult.getDomain());
            }
            glycanClient = new GlycanClient(address.toExternalForm(), account.getSsid(), account.getDomain(), account.getPath());
            refClient = new ReferenceClient(address.toExternalForm(), account.getSsid(), account.getDomain(), account.getPath());
            treeClient = new GlycanTreeClient(address.toExternalForm(), account.getSsid(), account.getDomain(), account.getPath());
        }catch(ErrorMessageException e){
            throw new QException(e);
        }catch(WebApiExecutionException e) {
            throw new QException(e);
        }catch(InvalidMessageExpetion e) {
            throw new QException(e);
        }
    }
    
    /**
     * Loads canonical tree information from the OntologyWebAPI web service.
     * 
     * @param manager  a {@code TreeManager} for updating tree information
     */
    public void loadTrees(TreeManager manager){
        try{
            List<ListObject> response=treeClient.getList();
            for(ListObject obj: response){
                String name = obj.getName();
                Glycantree glycanTree = treeClient.get(name);
                String glyde = glycanTree.getGlycan().getSequence();
                CompositeResidue root = FileUtils.parseGlydeRoot(glyde);
                String spec = JSONValue.toJSONString(GlydeStructure.getGlydeStructure(root).toMap());
                name = name.replace("_glyco_tree", "");
                QTree tree = manager.get(name);
                if(tree != null){
                    tree.setSpec(spec);
                    manager.update(tree);
                }
            }
        }catch(WebApiExecutionException e){
            throw new QException(e);
        }catch(ErrorMessageException e) {
            throw new QException(e);
        }catch(InvalidMessageExpetion e) {
            throw new QException(e);
        }catch(ParserConfigurationException e) {
            throw new QException(e);
        }catch(SAXException e) {
            throw new QException(e);
        }catch(IOException e) {
            throw new QException(e);
        }
    }
    
    
    /**
     * Submits a {@code QStructure}'s information to the OntologyWebAPI web service.
     * 
     * @param manager  a {@code StructureManager} for obtaining a structure's
     * tree and type information
     * @param structure  a {@code QStructure} to be submitted
     */
    public void submitStructure(StructureManager manager, QStructure structure){

        String glyde = StructureExporter.writeStructure(structure);
        QStructureType type = manager.getType(structure);
        QTree tree = manager.getTree(structure);
        
        // create a new Glycan object to transmit structure info to the ontology api
        Glycan glycan = new Glycan();
        glycan.setSequence(glyde);
        
        // init the class list and add structure type's oligosaccharide
        List<Glycan_Class> classList = new ArrayList<Glycan_Class>();
        Glycan_Class gc = new Glycan_Class();
        gc.setUri(PREFIX+type.getGlycoName());
        classList.add(gc);
        
        // init the classes object and add the class list
        Glycan_Classes classes = new Glycan_Classes();
        classes.setClassList(classList);
        
        // set the classes
        glycan.setClassList(classes);
        
        // set the tree
        GlycanTreeURI treeURI = new GlycanTreeURI();
        treeURI.setUri(PREFIX+tree.getName()+"_glyco_tree");
        glycan.setGlycantree(treeURI);
        
        // attempt to send it to the ontology api
        try{
            ExcutionResult response = glycanClient.post(glycan, true);
            String xml = response.getResponse();
            
            // parse the structure's URI from the response and update the structure
            String uri = QratorUtils.getMatch(xml, "uri=\".*?\"")
                                    .replace("uri=", "")
                                    .replace("\"", "");
            structure.setUri(uri);
            manager.update(structure);
        }catch(WebApiExecutionException ex){
            ex.printStackTrace();
            throw new QException(ex);
        }catch(ErrorMessageException ex){
            ex.printStackTrace();
            throw new QException(ex);
        }catch(InvalidMessageExpetion ex){
            ex.printStackTrace();
            throw new QException(ex);
        }
    }
        
    /**
     * Submits a {@code QStructure}'s information to the OntologyWebAPI web service.
     * 
     * @param manager  a {@code StructureManager} for obtaining a structure's
     * tree and type information
     * @param structure  a {@code QStructure} whose references will be submitted
     */
    public void submitReferences(AnnotationManager aManager, ReferenceManager rManager, ProvenanceManager pManager, QStructure structure){

        for(Iterator<QAnnotation> iter = aManager.list(structure, null); iter.hasNext();){
            QAnnotation next = iter.next();
            QUser author = aManager.getCreator(next);
            
            Comment c = new Comment();
            c.setAnnotationDate(next.getCreatedOn().toString());
            c.setAuthor(author.getName());
            c.setString(next.getComment());
            
            List<Comment> commentList = new ArrayList<Comment>();
            commentList.add(c);
            
            ReferenceAnnotationGroup rag = new ReferenceAnnotationGroup();
            rag.setCommentList(commentList);
            
            Reference refObj = new Reference();
            List<ReferenceAnnotationGroup> ragList = new ArrayList<ReferenceAnnotationGroup>();
            ragList.add(rag);
            refObj.setAnnotationGroupList(ragList);
            
            sendReference(refObj);
        }
        
        for(Iterator<QReference> iter = rManager.list(structure, (Filter) null); iter.hasNext();){
            QReference next = iter.next();
            QUser author = rManager.getCreator(next);
            QSource source = rManager.getSource(next);
            
            Database db = new Database();
            db.setAnnotationDate(next.getCreatedOn().toString());
            db.setAuthor(author.getName());
            db.setNamespace(source.getName());
            db.setServerURL(source.getUri());
            db.setId(next.getSrcId());
            
            List<Database> dbList = new ArrayList<Database>();
            dbList.add(db);
            
            ReferenceAnnotationGroup rag = new ReferenceAnnotationGroup();
            rag.setDatabaseList(dbList);
            
            Reference refObj = new Reference();
            List<ReferenceAnnotationGroup> ragList = new ArrayList<ReferenceAnnotationGroup>();
            ragList.add(rag);
            refObj.setAnnotationGroupList(ragList);
            
            sendReference(refObj);
        }
        
        for(Iterator<QProvenance> iter = pManager.list(structure); iter.hasNext();){
            QProvenance next = iter.next();
            QUser author = pManager.getCreator(next);
            
            Provenance p = new Provenance();
            p.setAnnotationDate(next.getCreatedOn().toString());
            p.setAuthor(author.getName());
            p.setAction(next.getAction().toString());
            
            List<Provenance> provList = new ArrayList<Provenance>();
            provList.add(p);
            
            ReferenceAnnotationGroup rag = new ReferenceAnnotationGroup();
            rag.setProvenanceList(provList);
            
            Reference refObj = new Reference();
            List<ReferenceAnnotationGroup> ragList = new ArrayList<ReferenceAnnotationGroup>();
            ragList.add(rag);
            refObj.setAnnotationGroupList(ragList);
            
            sendReference(refObj);
        }
        
    }
    
    private String sendReference(Reference ref){
        // attempt to send the reference to the ontology api
        try{
            ExcutionResult response = refClient.put(ref, true);
            String xml = response.getResponse();
            
            // parse the structure's URI from the response and update the structure
            String uri = QratorUtils.getMatch(xml, "uri=\".*?\"")
                                    .replace("uri=", "")
                                    .replace("\"", "");
            
            return uri;
        }catch(WebApiExecutionException ex){
            ex.printStackTrace();
            throw new QException(ex);
        }catch(ErrorMessageException ex){
            ex.printStackTrace();
            throw new QException(ex);
        }catch(InvalidMessageExpetion ex){
            ex.printStackTrace();
            throw new QException(ex);
        }
    }
    
}
