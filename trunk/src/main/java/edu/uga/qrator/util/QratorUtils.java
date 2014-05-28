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

import edu.uga.glydeII.gom.*;
import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * QratorUtils
 *
 * Provides a number of convenience methods.
 *
 * @author Matthew Eavenson (durandal@uga.edu)
 *
 */
public class QratorUtils{

    private static DocumentBuilder parser;
    private static Random random;
    
    static{
        try{
            parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        }catch(Exception e){ e.printStackTrace(); }
        random = new Random();
    }

    /***************************************************************
     * Parse an XML file into a DOM Tree.
     * @param  file     XML file
     * @return Document  DOM Tree for XML document.
     */
    public static Document getDocument(File file) throws SAXException, IOException{        
        return parser.parse(file);
    }
    
    /***************************************************************
     * Parse an XML file into a DOM Tree.
     * @param  str       XML file contents as a String
     * @return Document  DOM Tree for XML document.
     */
    public static Document getDocument(String str) throws SAXException, IOException{
        return getDocument(new ByteArrayInputStream(str.getBytes()));
    }
    
    /***************************************************************
     * Parse an XML file into a DOM Tree.
     * @param  stream    XML file stream
     * @return Document  DOM Tree for XML document.
     */
    public static Document getDocument(InputStream stream) throws SAXException, IOException{
        return parser.parse(stream);
    }

    /***************************************************************
     * Return a new Document.
     * @return a new Document object.
     */
    public static Document newDocument(){
        return parser.newDocument();
    }

    /***************************************************************
     * Render a Calendar object into a date string.
     * @param  c         Calendar to be rendered.
     * @return a String representation of the date encoded by this Calendar.
     */
    public static String formatDate(Calendar c){
        DateFormat df = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        return df.format(c.getTime());
    }
    
    /***************************************************************
     * Render a Date object into a formatted string.
     * @param  d         Date to be formatted
     * @return a String representation of the date encoded by this Calendar
     */
    public static String formatDate(Date d){
        DateFormat df = new SimpleDateFormat("EEE, MMM d, yyyy");
        return df.format(d);
    }

    /***************************************************************
     * Parse a date string into a Calendar object.
     * @param  s         String to be parsed.
     * @return a Calendar object with the date represented by this String.
     */
    public static Calendar parseDate(String s){
        DateFormat df = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        Calendar c = Calendar.getInstance();
        try{ c.setTime(df.parse(s));
        }catch(ParseException pfe){}
        return c;
    }

    public static String formatXSDDate(Date date){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return df.format(date);
    }

    /***************************************************************
     * Sanitize an input String.  This probably should be modified.
     * @param s     The input string to be sanitized.
     * @return a sanitized version of the input String.
     */
    public static String sanitize(String s){
        if(s != null){
            s = s.replaceAll("'", "\\'");
        }
        return s;
    }
    
    public static String hashSpec(String spec){
        String formatted = QratorUtils.formatStructureSpec(spec);
        String hash = QratorUtils.secureHash(formatted);
        return hash;
    }
    
    public static String hashSpec(Map<String, Object> spec){
        Map<String,Object> formatted = QratorUtils.formatStructureSpec(spec);
        String hash = QratorUtils.secureHash(JSONValue.toJSONString(formatted));
        return hash;
    }

    public static String formatStructureSpec(String spec){
        @SuppressWarnings("unchecked")
        Map<String,Object> obj = (JSONObject) JSONValue.parse(spec);
        return JSONValue.toJSONString(formatStructureSpec(obj));
    }
    
    public static String formatMatchSpec(String spec){
        @SuppressWarnings("unchecked")
        Map<String,Object> obj = (JSONObject) JSONValue.parse(spec);
        return JSONValue.toJSONString(formatMatchSpec(obj));
    }

    /***************************************************************
     * Formats a glycan's structural map representation.
     * @param  spec      A glycan's structural representation as a map.
     * @return a modified String representation of the Structure suitable
     * as a structure sequence.
     */
    public static Map<String,Object> formatMatchSpec( Map<String,Object> spec ){
        
        List children = (List) spec.get("children");
        if(children != null){
            for(Object child: children){
                formatMatchSpec( (Map<String,Object>) child );
            }
        }
        spec.remove("diff");
        spec.remove("match");
        
        return spec;
    }
    
    /***************************************************************
     * Formats a glycan's structural map representation for consistent hashing.
     * @param  spec      A glycan's structural representation as a map.
     * @return a modified String representation of the Structure which
     *          will produce a consistent hash.
     */
    public static Map<String,Object> formatStructureSpec( Map<String,Object> spec ){
        Map<String,Object> treeSpec = new TreeMap<String, Object>();
        List children = (List) spec.get("children");
        Map<Integer, Object> formatted = new TreeMap<Integer, Object>();
        
        if(children != null){
            for(Object child: children){
                @SuppressWarnings("unchecked")
                Map<String, Object> cObj = formatStructureSpec( (Map<String,Object>) child );
                String link = cObj.get("link").toString();
                Integer linkNum = null;
                if(link != null){
                    linkNum = Integer.parseInt(link);
                    formatted.put(linkNum, cObj);
                }
            }
            treeSpec.put("children", formatted);
        }
        //if(!root){
            // TODO -- Make this more stable!!
            String id = spec.get("id").toString();
            id = id.replaceAll("_[AB\\d]+", "")
                    .replaceAll(".+_", "")
                    //.replaceAll("core_", "")
                    ;
            treeSpec.put("id", id);
        //}

        //spec.remove("score");
        /*spec.remove("diff");
        spec.remove("match");
        spec.remove("anomer");
        spec.remove("from");
        spec.remove("to");*/
        if(spec.containsKey("link")) treeSpec.put("link", spec.get("link"));
        if(spec.containsKey("from")) treeSpec.put("from", spec.get("from"));
        if(spec.containsKey("to")) treeSpec.put("to", spec.get("to"));
        
        return treeSpec;
    }

    /*********************************************************************
     * Hashes the string input using the SHA1 algorithm.
     * @param   input	string to hash.
     * @return  SHA hash of the string.
     */
    public static String secureHash(String input){
        StringBuilder output = new StringBuilder();
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(input.getBytes());
            byte[] digest = md.digest();
            for (int i=0; i<digest.length; i++) {
                String hex = Integer.toHexString(digest[i]);
                if (hex.length() == 1) hex = "0" + hex;
                hex=hex.substring(hex.length()-2);
                output.append(hex);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return output.toString();
    }

    /*********************************************************************
     * Hashes the current time (in milliseconds) to a base 36 hash.
     * @param   input	string to hash.
     * @return  SHA hash of the string.
     */
    public static String randomHash(){
        Long rn = random.nextLong();
        StringBuilder sb = new StringBuilder();
        try{
            String salt = new BigInteger(130, random).toString(32);
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update((salt+rn).getBytes());
            byte[] digest = md.digest();
            // hex encoding
//            for (int i=0; i<digest.length; i++){
//               sb.append(Integer.toHexString((0xFF & digest[i]) + 0x100).substring(1));
//            }
            
            // base36 encoding
            for (int i=0; i<digest.length; i++){
               sb.append(Integer.toString((0xFF & digest[i]) + 0x100, 36).substring(1));
           }
            
        }catch(Exception e){
            e.printStackTrace();
        }
        
        return sb.toString();
    }

    /***************************************************************
     * Return a list of String matches to an input String based on a
     * regular expression.
     * @param  input     An input String.
     * @param  regex     A regular expression String.
     * @param  groupNum  The group number to return.  See MatchResult for details.
     * @return a List of Strings which match the regular expression.
     */
    public static List<String> getMatches(String input, String regex, int groupNum){
        List<String> matches = new ArrayList<String>();
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(input);
        MatchResult mr = null;
        while(m.find()){
            mr = m.toMatchResult();
            matches.add(mr.group(groupNum));
        }
        return matches;
    }

    /***************************************************************
     * Return a single match to an input String based on a
     * regular expression.
     * @param  input     An input String.
     * @param  regex     A regular expression String.
     * @return the first String in the input that matches the regular expression.
     */
    public static String getMatch(String input, String regex){
        String match = null;
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(input);
        MatchResult mr = null;
        if(m.find()){
            mr = m.toMatchResult();
            match = mr.group();
        }
        return match;
    }

    /*********************************************************************
     * Converts the contents of a File to a String.
     * @param   f	File to convert to a String
     * @return  contents of the specified File
     */
    public static String fileToString(File f) throws IOException{
        FileChannel fc = new FileInputStream(f).getChannel();
        ByteBuffer buffer = ByteBuffer.allocate((int)fc.size());
        fc.read(buffer);
        fc.close();
        return new String(buffer.array());
    }


    /*********************************************************************
     * Converts the contents of a String to a File.
     * @param   s		String to convert to a File
     * @param   f		File which the String will be written to.
     * @param   append	append mode on the File.
     */
    public static void stringToFile(String s, File f, boolean append) throws IOException{
        if(f.getParentFile() != null && !f.getParentFile().exists()){
            f.getParentFile().mkdirs();
        }
        FileChannel fc = new FileOutputStream(f,append).getChannel();
        byte[] b = s.getBytes();
        ByteBuffer buffer = ByteBuffer.wrap(b);
        fc.write(buffer);
        fc.close();
    }


    /*********************************************************************
     * Converts the contents of a String to a File.
     * @param   s	String to convert to a File
     * @param   f	File which the String will be written to.
     */
    public static void stringToFile(String s, File f) throws IOException{
        stringToFile(s,f,false);
    }

    /***************************************************************
     * Transforms a Document object into a String.
     * @param  doc     The input Document.
     * @return a String representation of the input Document.
     */
    public static String docToString(Document doc){
        String xml = null;
        try{
            DOMSource domSource = new DOMSource(doc);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING,"ISO-8859-1");
            //transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StringWriter sw = new StringWriter();
            StreamResult sr = new StreamResult(sw);
            transformer.transform(domSource, sr);
            xml = sw.toString();

        }catch(Exception e){ e.printStackTrace(); }

        return xml;
    }

    public static List<CompositeResidue> getChildren( CompositeResidue parent ) {
        List<CompositeResidue> children = new ArrayList<CompositeResidue>();
        if(parent.hasBaseType()){
            Residue baseTypeResidue = parent.getBaseType().getGeneralType();
            SortedSet<ResidueLink> linkIn = baseTypeResidue.getLinkIn();
            for (ResidueLink link : linkIn) {
                ResidueType rt = link.getFrom().getSpecificType();
                if (rt instanceof BaseType )
                    children.add( ((BaseType)link.getFrom().getSpecificType()).getCompositeResidue() );
                else if( rt instanceof Substituent){
                    Substituent sb = (Substituent) rt;
                    if(!sb.getCompositeResidue().equals(parent))
                        children.add( sb.getCompositeResidue() );
                }
            }
        }
        return children;
    }
	
}
