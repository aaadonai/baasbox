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
package com.baasbox.service.storage;

import java.security.InvalidParameterException;
import java.util.List;

import org.codehaus.jackson.JsonNode;

import com.baasbox.dao.DocumentDao;
import com.baasbox.dao.GenericDao;
import com.baasbox.dao.NodeDao;
import com.baasbox.dao.PermissionsHelper;
import com.baasbox.dao.RoleDao;
import com.baasbox.dao.exception.InvalidCollectionException;
import com.baasbox.dao.exception.InvalidModelException;
import com.baasbox.exception.SqlInjectionException;
import com.baasbox.util.QueryParams;
import com.orientechnologies.orient.core.record.impl.ODocument;


public class DocumentService {


	public static final String FIELD_LINKS = NodeDao.FIELD_LINK_TO_VERTEX;
	
	public static ODocument create(String collection, JsonNode bodyJson) throws Throwable, InvalidCollectionException,InvalidModelException {
		DocumentDao dao = DocumentDao.getInstance(collection);
		
		ODocument doc = dao.create();
		dao.update(doc,(ODocument) (new ODocument()).fromJSON(bodyJson.toString()));

		PermissionsHelper.grantRead(doc, RoleDao.getFriendRole());	
	    dao.save(doc);
		return doc;//.toJSON("fetchPlan:*:0 _audit:1,rid");
	}
	
	/**
	 * 
	 * @param collectionName
	 * @param rid
	 * @param bodyJson
	 * @return the updated document, null if the document is not found or belongs to another collection
	 * @throws InvalidCollectionException
	 */
	public static ODocument update(String collectionName,String rid, JsonNode bodyJson) throws InvalidCollectionException,InvalidModelException {
		ODocument doc=get(collectionName,rid);
		if (doc==null) throw new InvalidParameterException(rid + " is not a valid document");
		//update the document
		DocumentDao dao = DocumentDao.getInstance(collectionName);
		dao.update(doc,(ODocument) (new ODocument()).fromJSON(bodyJson.toString()));
		return doc;//.toJSON("fetchPlan:*:0 _audit:1,rid");
	}//update

	
	public static ODocument get(String collectionName,String rid) throws IllegalArgumentException,InvalidCollectionException,InvalidModelException {
		DocumentDao dao = DocumentDao.getInstance(collectionName);
		ODocument doc=dao.get(rid);
		return doc;
	}
	
	
	public static long getCount(String collectionName, QueryParams criteria) throws InvalidCollectionException, SqlInjectionException{
		DocumentDao dao = DocumentDao.getInstance(collectionName);
		return dao.getCount(criteria);
	}
	
	public static List<ODocument> getDocuments(String collectionName, QueryParams criteria) throws SqlInjectionException, InvalidCollectionException{
		DocumentDao dao = DocumentDao.getInstance(collectionName);
		return dao.get(criteria);
	}

	/**
	 * @param rid
	 * @return
	 */
	public static ODocument get(String rid) {
		GenericDao dao = GenericDao.getInstance();
		ODocument doc=dao.get(rid);
		return doc;
	}

	/**
	 * @param collectionName
	 * @param rid
	 * @throws Throwable 
	 */
	public static void delete(String collectionName, String rid) throws Throwable {
		DocumentDao dao = DocumentDao.getInstance(collectionName);
		try {
			dao.delete(rid);
		} catch (InvalidModelException e) {
			throw new InvalidCollectionException("The document " + rid + " does no belong to the collection " + collectionName);
		}
	}
}
