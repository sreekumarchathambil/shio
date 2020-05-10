/*
 * Copyright (C) 2016-2020 the original author or authors. 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.viglet.shio.persistence.repository.site;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.viglet.shio.persistence.model.site.ShSite;

/**
 * @author Alexandre Oliveira
 */
@Repository
public interface ShSiteRepository extends JpaRepository<ShSite, String> {

	List<ShSite> findAll();

	Optional<ShSite> findById(String id);

	List<ShSite> findByOwner(String owner);
	
	List<ShSite> findByIdIn(Collection<String> ids);
	
	List<ShSite> findByOwnerOrOwnerIsNull(String owner);
	
	ShSite findByName(String name);
	
	ShSite findByFurl(String furl);

	@SuppressWarnings("unchecked")
	ShSite save(ShSite shSite);

	@Modifying
	@Query("delete from ShSite p where p.id = ?1")
	void delete(String shSiteId);
}
