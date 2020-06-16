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
package com.viglet.shio.utils;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import com.viglet.shio.api.folder.ShFolderPath;
import com.viglet.shio.persistence.model.auth.ShGroup;
import com.viglet.shio.persistence.model.auth.ShUser;
import com.viglet.shio.persistence.model.folder.ShFolder;
import com.viglet.shio.persistence.model.object.ShObject;
import com.viglet.shio.persistence.model.post.ShPost;
import com.viglet.shio.persistence.model.post.impl.ShPostImpl;
import com.viglet.shio.persistence.model.site.ShSite;
import com.viglet.shio.persistence.repository.auth.ShUserRepository;
import com.viglet.shio.persistence.repository.object.ShObjectRepository;

/**
 * @author Alexandre Oliveira
 */
@Component
public class ShObjectUtils {
	@Autowired
	private ShFolderUtils shFolderUtils;
	@Autowired
	private ShObjectRepository shObjectRepository;
	@Autowired
	private ShUserRepository shUserRepository;
	@Autowired
	private ShPostUtils shPostUtils;

	public ShSite getSite(ShObject shObject) {
		if (shObject instanceof ShPost) {
			return shPostUtils.getSite((ShPostImpl) shObject);
		} else if (shObject instanceof ShFolder) {
			return shFolderUtils.getSite((ShFolder) shObject);
		} else {
			return null;
		}
	}

	public boolean canAccess(Principal principal, String shObjectId) {
		ShUser shUser = null;
		ShObject shObject = shObjectRepository.findById(shObjectId).orElse(null);
		if (shObject != null) {
			if (principal != null)
				shUser = shUserRepository.findByUsername(principal.getName());
			Set<String> shGroups = new HashSet<>();
			Set<String> shUsers = new HashSet<>();
			if (shUser != null && shUser.getShGroups() != null) {
				boolean fullAccess = false;
				for (ShGroup shGroup : shUser.getShGroups()) {
					if (shGroup.getName().equals("Administrator")) {
						fullAccess = true;
					}
				}
				if (fullAccess) {
					return true;
				} else {
					for (ShGroup shGroup : shUser.getShGroups()) {
						shGroups.add(shGroup.getName());
					}
					shUsers.add(shUser.getUsername());
					if (shObjectRepository.countByIdAndShGroupsInOrIdAndShUsersInOrIdAndShGroupsIsNullAndShUsersIsNull(
							shObject.getId(), shGroups, shObject.getId(), shUsers, shObject.getId()) > 0) {
						return true;
					}
				}
			} else {
				return true;
			}
		}

		return false;
	}

	public ShFolderPath objectPath(@PathVariable String id) {
		Optional<ShObject> shObject = shObjectRepository.findById(id);
		if (shObject.isPresent()) {
			if (shObject.get() instanceof ShSite) {
				ShSite shSite = (ShSite) shObject.get();
				ShFolderPath shFolderPath = new ShFolderPath();
				shFolderPath.setFolderPath(null);
				shFolderPath.setCurrentFolder(null);
				shFolderPath.setBreadcrumb(null);
				shFolderPath.setShSite(shSite);
				return shFolderPath;
			} else if (shObject.get() instanceof ShFolder) {
				ShFolder shFolder = (ShFolder) shObject.get();
				ShFolderPath shFolderPath = new ShFolderPath();
				String folderPath = shFolderUtils.folderPath(shFolder, true, false);
				List<ShFolder> breadcrumb = shFolderUtils.breadcrumb(shFolder);
				ShSite shSite = breadcrumb.get(0).getShSite();
				shFolderPath.setFolderPath(folderPath);
				shFolderPath.setCurrentFolder(shFolder);
				shFolderPath.setBreadcrumb(breadcrumb);
				shFolderPath.setShSite(shSite);
				return shFolderPath;
			} else if (shObject.get() instanceof ShPost) {
				ShPostImpl shPost = shPostUtils.loadLazyPost(shObject.get().getId(), false);
				ShFolder shFolder = shPost.getShFolder();
				ShFolderPath shFolderPath = new ShFolderPath();
				String folderPath = shFolderUtils.folderPath(shFolder, true, false);
				List<ShFolder> breadcrumb = shFolderUtils.breadcrumb(shFolder);
				ShSite shSite = breadcrumb.get(0).getShSite();
				shFolderPath.setFolderPath(folderPath);
				shFolderPath.setCurrentFolder(shFolder);
				shFolderPath.setBreadcrumb(breadcrumb);
				shFolderPath.setShSite(shSite);
				shFolderPath.setShPost(shPost);
				return shFolderPath;
			}
		}
		return null;
	}
}
