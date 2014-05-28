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

import static edu.uga.qrator.logic.QConfiguration.*;
import edu.uga.qrator.logic.manage.RoleManager;
import edu.uga.qrator.logic.manage.UserManager;
import edu.uga.qrator.logic.session.QAuthentication;
import edu.uga.qrator.obj.entity.QRole;
import edu.uga.qrator.obj.entity.QUser;
import static edu.uga.qrator.service.ServiceUtil.ADMIN;
import static edu.uga.qrator.service.ServiceUtil.INVALID_SESSION;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.ws.rs.*;
import org.apache.commons.mail.HtmlEmail;
import org.json.simple.JSONValue;
import persist.util.ResponseHelper;
import persist.util.StringUtil;
import session.LoginException;
import session.Session;
import session.SessionManager;

/**
 * @author Matthew
 *
 *
 */
@Path("admin")
public class AdminService{
    
    private static final Configuration CONFIG;
    private static final String TEMPLATE_LOCATION = "/config/qrator";
    
    static{
        // for processing the password recovery template
        CONFIG = new Configuration();

        CONFIG.setTemplateLoader(new ClassTemplateLoader(AdminService.class, TEMPLATE_LOCATION));
        CONFIG.setObjectWrapper(new DefaultObjectWrapper());
        CONFIG.setDefaultEncoding("UTF-8");
        CONFIG.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        CONFIG.setIncompatibleImprovements(new Version(2, 3, 20));  // FreeMarker 2.3.20
    }
    
    @GET @Path("/list")
    @Produces("application/json")
    public static String listUsers(@QueryParam("ssid") String ssid, 
                                   @QueryParam("offset") int offset,
                                   @QueryParam("limit") int limit){
        ResponseHelper response = new ResponseHelper(ssid);
        if(response.session != null){
            UserManager uManager = new UserManager(response.conn);
            QUser sessionUser = ServiceUtil.getUser(response.session.getUserId(), uManager);
            if(uManager.hasRole(sessionUser, ADMIN)){
                try{
                    int count = 0;
                    List<Map<String,Object>> uList = new ArrayList<Map<String,Object>>();
                    for(Iterator<QUser> users = uManager.list(); users.hasNext();){
                        QUser user = users.next();
                        Map<String, Object> state = ServiceUtil.userState(user);
                        List<String> rList = new ArrayList<String>();
                        for(Iterator<QRole> roles = uManager.getRoles(user); roles.hasNext();){
                            rList.add(roles.next().getName());
                        }
                        state.put("roles", rList);
                        uList.add(state);
                        count++;
                    }
                    response.add("count", count);
                    response.add("objs", uList);
                    if(uList.size() > 0)
                        response.message("List users successful.");
                    else response.message("No users match.");
                }catch(Exception qe){
                    response.error(qe.getMessage());
                }
            }else response.error("You do not have permission to view users.");
        }else response.error(INVALID_SESSION);
        return JSONValue.toJSONString(response.getResponse());
    }
    
    @GET @Path("/add/role/{userId}")
    @Produces("application/json")
    public static String addRole(@QueryParam("ssid")  String ssid, 
                                 @PathParam("userId") long userId, 
                                 @QueryParam("role")  String role){

        ResponseHelper response = new ResponseHelper(ssid);
        if(response.session != null){
            UserManager uManager = new UserManager(response.conn);
            QUser sessionUser = ServiceUtil.getUser(response.session.getUserId(), uManager);
            if(uManager.hasRole(sessionUser, ADMIN)){
                try{
                    QUser user = ServiceUtil.getUser(userId, uManager);
                    uManager.addRole(user, role);
                    response.message("Role added.");
                }catch(Exception qe){
                    response.error(qe.getMessage());
                }
            }else response.error("You do not have permission to add user roles.");
        }else response.error(INVALID_SESSION);
        return JSONValue.toJSONString(response.getResponse());
    }

    @GET @Path("/remove/role/{userId}")
    @Produces("application/json")
    public static String removeRole(@QueryParam("ssid")  String ssid, 
                                    @PathParam("userId") long userId,
                                    @QueryParam("role")  String role){
        
        ResponseHelper response = new ResponseHelper(ssid);
        if(response.session != null){
            UserManager uManager = new UserManager(response.conn);
            QUser sessionUser = ServiceUtil.getUser(response.session.getUserId(), uManager);
            if(uManager.hasRole(sessionUser, ADMIN)){
                try{
                    QUser user = ServiceUtil.getUser(userId, uManager);
                    uManager.removeRole(user, role);
                    response.message("Role removed.");
                }catch(Exception qe){
                    response.error(qe.getMessage());
                }
            }else response.error("You do not have permission to remove user roles.");
        }else response.error(INVALID_SESSION);
        return JSONValue.toJSONString(response.getResponse());
    }
    
    @GET @Path("/list/roles")
    @Produces("application/json")
    public static String listRoles(@QueryParam("ssid") String ssid){
        
        ResponseHelper response = new ResponseHelper(ssid);
        if(response.session != null){
            UserManager uManager = new UserManager(response.conn);
            QUser sessionUser = ServiceUtil.getUser(response.session.getUserId(), uManager);
            if(uManager.hasRole(sessionUser, ADMIN)){
                try{
                    RoleManager rManager = new RoleManager(response.conn);
                    List<String> roleList = new ArrayList<String>();
                    for(Iterator<QRole> roles = rManager.list(); roles.hasNext();){
                        roleList.add(roles.next().getName());
                    }
                    response.add("objs", roleList);
                }catch(Exception qe){
                    response.error(qe.getMessage());
                }
            }else response.error("You do not have permission to obtain roles.");
        }else response.error(INVALID_SESSION);
        return JSONValue.toJSONString(response.getResponse());
    }

    @GET @Path("/deactivate/{userId}")
    @Produces("application/json")
    public static String deactivateUser(@QueryParam("ssid") String ssid,
                                        @PathParam("userId") long userId){
        
        ResponseHelper response = new ResponseHelper(ssid);
        if(response.session != null){
            UserManager uManager = new UserManager(response.conn);
            QUser sessionUser = ServiceUtil.getUser(response.session.getUserId(), uManager);
            if(uManager.hasRole(sessionUser, ADMIN)){
                try{
                    QUser user = ServiceUtil.getUser(userId, uManager);
                    user.setActive(false);
                    uManager.update(user);
                    response.message("User \""+user.getUsername()+"\" deactivated.");
                }catch(Exception qe){
                    response.error(qe.getMessage());
                }
            }else response.error("You do not have permission to deactivate users.");
        }else response.error(INVALID_SESSION);
        return JSONValue.toJSONString(response.getResponse());
    }
    
    @GET @Path("/activate/{userId}")
    @Produces("application/json")
    public static String activateUser(@QueryParam("ssid") String ssid,
                                      @PathParam("userId") long userId){
        
        ResponseHelper response = new ResponseHelper(ssid);
        if(response.session != null){
            UserManager uManager = new UserManager(response.conn);
            QUser sessionUser = ServiceUtil.getUser(response.session.getUserId(), uManager);
            if(uManager.hasRole(sessionUser, ADMIN)){
                try{
                    QUser user = ServiceUtil.getUser(userId, uManager);
                    user.setActive(true);
                    uManager.update(user);
                    response.message("User \""+user.getUsername()+"\" activated.");
                }catch(Exception qe){
                    response.error(qe.getMessage());
                }
            }else response.error("You do not have permission to activate users.");
        }else response.error(INVALID_SESSION);
        return JSONValue.toJSONString(response.getResponse());
    }
    
    @GET @Path("/modify")
    @Produces("application/json")
    public static String modify(@QueryParam("ssid") String ssid,
                                @QueryParam("username") String username,
                                @QueryParam("password") String password,
                                @QueryParam("newpass")  String newpass,
                                @QueryParam("name")     String name,
                                @QueryParam("email")    String email){
        
        ResponseHelper response = new ResponseHelper(ssid);
        if(response.session != null){
            UserManager uManager = new UserManager(response.conn);
            try{
                QUser user = ServiceUtil.getUser(response.session.getUserId(), uManager);
                if(password != null){
                    if(password.equals(user.getPassword())){
                        if(username != null) user.setUsername(username);
                        if(name != null) user.setName(name);
                        if(email != null) user.setEmail(email);
                        if(newpass != null) user.setPassword(newpass);
                        uManager.update(user);
                        response.message("Account modified successfully.");
                    }else{
                        response.error("Incorrect password provided.");
                    }
                }else response.error("No password provided.");
            }catch(Exception qe){
                qe.printStackTrace();
                response.error(qe.getMessage());
            }
        }
        return JSONValue.toJSONString(response.getResponse());
    }
    
    @GET @Path("/get/userdetails")
    @Produces("application/json")
    public static String getUser(@QueryParam("ssid")  String ssid){
        
        ResponseHelper response = new ResponseHelper(ssid);
        if(response.session != null){
            UserManager uManager = new UserManager(response.conn);
            try{
                QUser user = ServiceUtil.getUser(response.session.getUserId(), uManager);
                List<String> roleList = new ArrayList<String>();
                for(Iterator<QRole> roles = uManager.getRoles(user); roles.hasNext();){
                    roleList.add(roles.next().getName());
                }
                Map<String, Object> state = ServiceUtil.userState(user);
                state.put("roles", roleList);
                response.add("objs", state);
            }catch(Exception qe){
                qe.printStackTrace();
                response.error(qe.getMessage());
            }
        }
        return JSONValue.toJSONString(response.getResponse());
    }
    
    @GET @Path("/reset")
    @Produces("application/json")
    public static String reset(@QueryParam("email") String email){
        
        ResponseHelper response = new ResponseHelper();
        UserManager uManager = new UserManager(response.conn);
        try{
            QUser user = uManager.getUserByEmail(email);
            if(user != null){
                
                String newPassword = generateRandomString(10);
                user.setPassword(StringUtil.secureHash(newPassword));
                uManager.update(user);
                
                Map<String, Object> model = new HashMap<String, Object>();
                model.put("username", user.getUsername());
                model.put("name", user.getName());
                model.put("password", newPassword);
                model.put("date", new Date());
                model.put("admin", ADMINEMAIL);

                try{
                    Template html = CONFIG.getTemplate("pwd_reset_html.ftl");
                    Template plain = CONFIG.getTemplate("pwd_reset.ftl");
                    StringWriter htmlWriter = new StringWriter();
                    StringWriter plainWriter = new StringWriter();
                    html.process(model, htmlWriter);
                    plain.process(model, plainWriter);

                    // Create the email message
                    HtmlEmail mail = new HtmlEmail();
                    mail.setHostName(MAILSERVER);
                    mail.setSmtpPort(MAILPORT);
                    mail.addTo(user.getEmail(), user.getName());
                    mail.setFrom(MAILACCOUNT, "Qrator Admin");
                    mail.setSubject("Qrator - Password Reset");
                    mail.setAuthentication(MAILACCOUNT, MAILPASSWORD);
                    mail.setStartTLSRequired(MAILTLS);

                    // set the html message
                    mail.setHtmlMsg(htmlWriter.toString());

                    // set the alternative message
                    mail.setTextMsg(plainWriter.toString());

                    // send the email
                    mail.send();
                    response.message("Email sent.");
                }catch(TemplateException te){
                    te.printStackTrace();
                    response.error("Error sending email.");
                }
            }else response.error("No user with given email.");
        }catch(Exception qe){
            qe.printStackTrace();
            response.error(qe.getMessage());
        }
        return JSONValue.toJSONString(response.getResponse());
    }

    @GET @Path("/register")
    @Produces("application/json")
    public static String register(@QueryParam("username") String username,
                                  @QueryParam("password") String password,
                                  @QueryParam("name")     String name,
                                  @QueryParam("email")    String email){
        
        ResponseHelper response = new ResponseHelper();
        UserManager uManager = new UserManager(response.conn);
        try{
            QAuthentication auth = new QAuthentication(response.conn);
            auth.setCredential("username", username);
            auth.setCredential("password", password);
            auth.setCredential("name", name);
            auth.setCredential("email", email);
            Session session = SessionManager.getManager().register(auth);
            if(session == null){
                response.error("A user already exists with that username or email.  Please choose another.");
            }else{
                QUser user = ServiceUtil.getUser(response.session.getUserId(), uManager);
                List<String> roleList = new ArrayList<String>();
                for(Iterator<QRole> roles = uManager.getRoles(user); roles.hasNext();){
                    roleList.add(roles.next().getName());
                }
                response.message("Account created successfully.");
                response.add("ssid", session.getId());
                response.add("roles", roleList);
            }
        }catch(LoginException qe){
            response.error(qe.getMessage());
        }catch(Exception qe){
            qe.printStackTrace();
            response.error(qe.getMessage());
        }
        return JSONValue.toJSONString(response.getResponse());
    }

    @GET @Path("/login")
    @Produces("application/json")
    public static String login( @QueryParam("username") String username,
                                @QueryParam("password") String password ){

        ResponseHelper response = new ResponseHelper();
        UserManager uManager = new UserManager(response.conn);
        try{
            QAuthentication auth = new QAuthentication(response.conn);
            auth.setCredential("username", username);
            auth.setCredential("password", password);
            Session session = SessionManager.getManager().login(auth);
            if(session == null){
                response.error("Invalid username or password.");
            }else{
                QUser user = ServiceUtil.getUser(session.getUserId(), uManager);
                List<String> roleList = new ArrayList<String>();
                for(Iterator<QRole> roles = uManager.getRoles(user); roles.hasNext();){
                    roleList.add(roles.next().getName());
                }
                response.add("username", user.getUsername());
                response.message("Logged in.");
                response.add("ssid", session.getId());
                response.add("roles", roleList);
            }
        }catch(LoginException qe){
            response.error(qe.getMessage());
        }catch(Exception qe){
            qe.printStackTrace();
            response.error(qe.getMessage());
        }
        return JSONValue.toJSONString(response.getResponse());
    }

    @GET @Path("/logout")
    @Produces("application/json")
    public static String logout( @QueryParam("ssid") String ssid ){
        ResponseHelper response = new ResponseHelper();
        try{
            SessionManager.getManager().logout(ssid);
            response.message("Logged out.");
        }catch(LoginException qe){
            response.error(qe.getMessage());
        }catch(Exception qe){
            qe.printStackTrace();
            response.error(qe.getMessage());
        }
        return JSONValue.toJSONString(response.getResponse());
    }
    
    public static String generateRandomString(int length) {
        StringBuilder buffer = new StringBuilder();
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$%";
        int charLen = characters.length();

        Random r = new Random();
        for (int i = 0; i < length; i++) {
            buffer.append(characters.charAt(r.nextInt(charLen)));
        }
        return buffer.toString();
    }
    
    public static void main(String[] args){
        System.out.println(reset("durandal@uga.edu"));
    }
    
}