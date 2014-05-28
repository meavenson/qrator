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
package edu.uga.qrator.logic;

import edu.uga.glydeII.io.MonosaccharideMap;
import edu.uga.qrator.logic.manage.StructureCheckout;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * QConfiguration
 *
 * Generates and keeps track of the Qrator's
 * configuration based on the qrator.prop file.
 *
 * @author Matthew Eavenson (durandal@uga.edu)
 *
 */
public class QConfiguration {
    
    public static final MonosaccharideMap MONOMAP;        // mapping of labels to their corresponding monosaccharides
    public static final StructureCheckout CHECKOUT;
    
    public static final String MOTIFFILE;
    public static final String TREEFILE;
    
    public static final String ADMINEMAIL;
    
    public static final boolean APIENABLED;
    public static final URL APIADDRESS;
    public static final String APIUSERNAME;
    public static final String APIPASSWORD;
    
    public static final String MAILSERVER;
    public static final int MAILPORT;
    public static final String MAILACCOUNT;
    public static final String MAILPASSWORD;
    public static final boolean MAILTLS;
    
    static{
        Properties p = new Properties();
        String propLocation = "config/qrator/qrator.prop";
        try{            
            ClassLoader loader = QConfiguration.class.getClassLoader();
            if(loader==null)
                loader = ClassLoader.getSystemClassLoader();
            
            Object propFile = new File(propLocation);
            // if the property file doesn't exist at this location,
            // try to load it from the classpath
            if( !((File)propFile).exists() ){
                propFile = loader.getResource(propLocation);
                p.load( ((URL)propFile).openStream() );
            }else{
                p.load( new FileInputStream((File)propFile) );
            }
            String monoMapLocation = p.getProperty("monomap");
            MOTIFFILE = p.getProperty("motifs");
            TREEFILE = p.getProperty("trees");
            
            ADMINEMAIL = p.getProperty("admin_email");
            
            String apiEnabled = p.getProperty("api_enabled");
            APIENABLED = apiEnabled != null && apiEnabled.equals("true");
            String address = p.getProperty("api_address");
            APIADDRESS = address != null? new URL(address) : null;
            APIUSERNAME = p.getProperty("api_username");
            APIPASSWORD = p.getProperty("api_password");
            
            MAILSERVER = p.getProperty("mail_server");
            String port = p.getProperty("mail_port");
            MAILPORT = port != null? Integer.parseInt(port) : -1;
            MAILACCOUNT = p.getProperty("mail_account");
            MAILPASSWORD = p.getProperty("mail_password");
            String tls = p.getProperty("mail_tls");
            MAILTLS = tls != null && tls.equals("true");
            
            URL monoMapFile = loader.getResource(monoMapLocation);
            MONOMAP = new MonosaccharideMap(monoMapFile.openStream());
        }catch(IOException ioe){
            throw new RuntimeException(ioe);
        }

        CHECKOUT = new StructureCheckout();
        CHECKOUT.start();
    }
    
	
}
