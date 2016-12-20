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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.openmeetings.db.entity.basic.Naviglobal;
import org.apache.openmeetings.db.entity.basic.Navimain;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class NavigationDao {
	private static final Logger log = Red5LoggerFactory.getLogger(NavigationDao.class, webAppRootKey);

	@PersistenceContext
	private EntityManager em;

	public Naviglobal getGlobalMenuEntry(Long globalId) {
		try {
			TypedQuery<Naviglobal> query = em.createNamedQuery("getNavigationById", Naviglobal.class);
			query.setParameter("id", globalId);
			return query.getSingleResult();
		} catch (Exception ex2) {
			log.error("getGlobalMenuEntry", ex2);
		}
		return null;

	}

	public List<Naviglobal> getMainMenu(boolean admin) {
		return em.createNamedQuery("getNavigation", Naviglobal.class)
				.setParameter("levelId", admin ? 3L : 1L)
				.getResultList();
	}

	public Naviglobal addGlobalStructure(int naviorder, String labelId, int levelId, String name, String tooltipLabelId) {
		Naviglobal ng = new Naviglobal();
		ng.setComment("");
		ng.setIcon("");
		ng.setNaviorder(naviorder);
		ng.setLabelId(labelId);
		ng.setDeleted(false);
		ng.setLevelId(levelId);
		ng.setName(name);
		ng.setInserted(new Date());
		ng.setTooltipLabelId(tooltipLabelId);
		// CriteriaBuilder crit = em.getCriteriaBuilder();

		em.persist(ng);
		return ng;
	}

	public void addMainStructure(String action, String params, int naviorder, String labelId,
			int levelId, String name, Long globalId, String tooltipLabelId) {
		Naviglobal ng = getGlobalMenuEntry(globalId);
		List<Navimain> mainEntries = ng.getMainnavi();
		mainEntries = (mainEntries == null) ? new ArrayList<Navimain>() : mainEntries;

		Navimain nm = new Navimain();
		nm.setAction(action);
		nm.setParams(params);
		nm.setComment("");
		nm.setIcon("");
		nm.setLabelId(labelId);
		nm.setNaviorder(naviorder);
		nm.setLevelId(levelId);
		nm.setName(name);
		nm.setDeleted(false);
		nm.setGlobalId(globalId);
		nm.setInserted(new Date());
		nm.setTooltipLabelId(tooltipLabelId);

		mainEntries.add(nm);
		ng.setMainnavi(mainEntries);

		em.persist(ng);
	}

}
