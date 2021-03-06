/*
     Copyright 2012-2013 
     Claudio Tesoriero - c.tesoriero-at-baasbox.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.baasbox.controllers;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.Path;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.codehaus.jackson.JsonNode;

import play.Logger;
import play.Play;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;

import com.baasbox.controllers.actions.filters.AdminLogin;
import com.baasbox.controllers.actions.filters.BasicAuthHeader;
import com.baasbox.controllers.actions.filters.CheckAPPCode;
import com.baasbox.controllers.actions.filters.ConnectToDB;
import com.baasbox.dao.UserDao;
import com.baasbox.db.DbHelper;
import com.baasbox.exception.SqlInjectionException;
import com.baasbox.service.user.UserService;
import com.baasbox.util.JSONFormats;
import com.baasbox.util.JSONFormats.Formats;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;


@Api(value = "/user", listingPath = "/api-docs.{format}/user", description = "Operations about users")
public class User extends Controller {
	private static String prepareResponseToJson(ODocument doc){
		response().setContentType("application/json");
		return JSONFormats.prepareResponseToJson(doc,JSONFormats.Formats.USER);
	}
	
	private static String prepareResponseToJson(List<ODocument> listOfDoc) throws IOException{
		response().setContentType("application/json");
		return  JSONFormats.prepareResponseToJson(listOfDoc,JSONFormats.Formats.USER);
	}
		
	  @Path("/{id}")
	  @ApiOperation(value = "Get info about current user", notes = "", httpMethod = "GET")
	  @With ({CheckAPPCode.class, BasicAuthHeader.class, ConnectToDB.class})	
	  public static Result getCurrentUser() throws SqlInjectionException{
		  Logger.trace("Method Start");
		  ODocument profile = UserService.getCurrentUser();
		  String result=prepareResponseToJson(profile);
		  Logger.trace("Method End");
		  return ok(result);
	  }

	  @With ({CheckAPPCode.class, AdminLogin.class, ConnectToDB.class})
	  @BodyParser.Of(BodyParser.Json.class)
	  public static Result signUp(){
		  Logger.trace("Method Start");
		  Http.RequestBody body = request().body();
		  
		  JsonNode bodyJson= body.asJson();
		  Logger.debug("signUp bodyJson: " + bodyJson);
	
		  //check and validate input
		  if (!bodyJson.has("username"))
			  return badRequest("The 'username' field is missing");
		  if (!bodyJson.has("password"))
			  return badRequest("The 'password' field is missing");		

		  //extract mandatory fields
		  JsonNode nonAppUserAttributes = bodyJson.get(UserDao.ATTRIBUTES_VISIBLE_BY_ANONYMOUS_USER);
		  JsonNode privateAttributes = bodyJson.get(UserDao.ATTRIBUTES_VISIBLE_ONLY_BY_THE_USER);
		  JsonNode friendsAttributes = bodyJson.get(UserDao.ATTRIBUTES_VISIBLE_BY_FRIENDS_USER);
		  JsonNode appUsersAttributes = bodyJson.get(UserDao.ATTRIBUTES_VISIBLE_BY_REGISTERED_USER);
		  String username=(String) bodyJson.findValuesAsText("username").get(0);
		  String password=(String)  bodyJson.findValuesAsText("password").get(0);
		  
		  //try to signup new user
		  try {
			  UserService.signUp(username, password, nonAppUserAttributes, privateAttributes, friendsAttributes, appUsersAttributes);
		  } catch (Throwable e){
			  Logger.warn("signUp", e);
			  if (Play.isDev()) return internalServerError(ExceptionUtils.getFullStackTrace(e));
			  else return internalServerError(e.getMessage());
		  }
		  Logger.trace("Method End");
		  return created();
	  }
	  
 
	  @With ({CheckAPPCode.class, BasicAuthHeader.class, ConnectToDB.class})
	  @BodyParser.Of(BodyParser.Json.class)
	  public static Result updateProfile(){
		  Logger.trace("Method Start");
		  Http.RequestBody body = request().body();
		  
		  JsonNode bodyJson= body.asJson();
		  Logger.debug("updateProfile bodyJson: " + bodyJson);
		  
		  //extract the profile	 fields
		  JsonNode nonAppUserAttributes = bodyJson.get(UserDao.ATTRIBUTES_VISIBLE_BY_ANONYMOUS_USER);
		  JsonNode privateAttributes = bodyJson.get(UserDao.ATTRIBUTES_VISIBLE_ONLY_BY_THE_USER);
		  JsonNode friendsAttributes = bodyJson.get(UserDao.ATTRIBUTES_VISIBLE_BY_FRIENDS_USER);
		  JsonNode appUsersAttributes = bodyJson.get(UserDao.ATTRIBUTES_VISIBLE_BY_REGISTERED_USER);

		  ODocument profile;
		  try {
			  profile=UserService.updateCurrentProfile(nonAppUserAttributes, privateAttributes, friendsAttributes, appUsersAttributes);
		  } catch (Throwable e){
			  Logger.warn("updateProfile", e);
			  if (Play.isDev()) return internalServerError(ExceptionUtils.getFullStackTrace(e));
			  else return internalServerError(e.getMessage());
		  }
		  Logger.trace("Method End");
		  
		  return ok(prepareResponseToJson(profile)); 
	  }//updateProfile
	 

	  
	  @With ({CheckAPPCode.class, AdminLogin.class, ConnectToDB.class})
	  public static Result exists(String username){
		  return status(NOT_IMPLEMENTED);
		  /*
		  boolean result = true;//UserService.exists(username);
		  return ok ("{\"response\": \""+result+"\"}");
		  */
	  }
	  

	  
	  public static Result resetPasswordStep1(){
		  return status(NOT_IMPLEMENTED);
	  }
	  
	  public static Result resetPasswordStep2(){
		  return status(NOT_IMPLEMENTED);
	  }
	  
	  @With ({CheckAPPCode.class, BasicAuthHeader.class, ConnectToDB.class})
	  @BodyParser.Of(BodyParser.Json.class)
	  public static Result changePassword(){
		  Logger.trace("Method Start");
		  Http.RequestBody body = request().body();
		  
		  JsonNode bodyJson= body.asJson();
		  Logger.debug("changePassword bodyJson: " + bodyJson);
		  
		  //check and validate input
		  if (!bodyJson.has("old"))
			  return badRequest("The 'old' field is missing");
		  if (!bodyJson.has("new"))
			  return badRequest("The 'new' field is missing");	
		  
		  String currentPassword = DbHelper.getCurrentHTTPPassword();
		  String oldPassword=(String) bodyJson.findValuesAsText("old").get(0);
		  String newPassword=(String)  bodyJson.findValuesAsText("new").get(0);
		  
		  if (!oldPassword.equals(currentPassword)){
			  return badRequest("The old password does not match with the current one");
		  }
		  
		  UserService.changePassword(newPassword);
		  Logger.trace("Method End");
		  return ok();
	  }	  
	  
		 /*Postponed: now we use only the Basic Auth 
	  @With ({CheckAPPCode.class, ApplicationDefaultCredentials.class, ConnectToOrientDB.class})
	  public static Result login(){
		  boolean result = service.user.User.exists(username);
		  return ok ("{\"response\": \""+result+"\"}");
	  }
	   */

}
