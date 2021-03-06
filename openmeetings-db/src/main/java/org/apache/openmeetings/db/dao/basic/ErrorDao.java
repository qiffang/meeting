/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License") +  you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openmeetings.db.dao.basic;

import static org.apache.openmeetings.util.OpenmeetingsVariables.webAppRootKey;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.openmeetings.db.entity.basic.ErrorValue;
import org.apache.openmeetings.db.entity.basic.ErrorValue.Type;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ErrorDao {
	private static final Logger log = Red5LoggerFactory.getLogger(ErrorDao.class, webAppRootKey);

	@PersistenceContext
	private EntityManager em;

	public Long addErrorValues(Long id, Type type, Long labelId) {
		try {
			ErrorValue eValue = new ErrorValue();
			eValue.setId(id);
			eValue.setType(type);
			eValue.setDeleted(false);
			eValue.setInserted(new Date());
			eValue.setLabelId(labelId);
			eValue = em.merge(eValue);
			return eValue.getId();
		} catch (Exception ex2) {
			log.error("[addErrorValues]: ", ex2);
		}
		return null;
	}

	public Long getErrorValueById(Type type, Long labelId) {
		try {
			ErrorValue eValue = new ErrorValue();
			eValue.setType(type);
			eValue.setInserted(new Date());
			eValue.setLabelId(labelId);
			eValue = em.merge(eValue);
			return eValue.getId();
		} catch (Exception ex2) {
			log.error("[getErrorValueById]: ", ex2);
		}
		return null;
	}

	public Long updateErrorValues(Type type, Long labelId) {
		try {
			ErrorValue eValue = new ErrorValue();
			eValue.setType(type);
			eValue.setInserted(new Date());
			eValue.setLabelId(labelId);
			eValue = em.merge(eValue);
			return eValue.getId();
		} catch (Exception ex2) {
			log.error("[addErrorType]: ", ex2);
		}
		return null;
	}

	public ErrorValue get(Long id) {
		try {
			TypedQuery<ErrorValue> query = em.createNamedQuery("getErrorValueById", ErrorValue.class);
			query.setParameter("id", id);
			ErrorValue e = null;
			try {
				e = query.getSingleResult();
			} catch (NoResultException ex) {
			}
			return e;
		} catch (Exception ex2) {
			log.error("[get]", ex2);
		}
		return null;
	}
}
