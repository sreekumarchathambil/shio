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
package com.viglet.shio.graphql.endpoint;

import graphql.ExecutionResult;
import graphql.spring.web.servlet.ExecutionResultHandler;
import graphql.spring.web.servlet.GraphQLInvocation;
import graphql.spring.web.servlet.GraphQLInvocationData;
import graphql.spring.web.servlet.JsonSerializer;
import graphql.spring.web.servlet.components.GraphQLRequestBody;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import com.viglet.shio.utils.ShUserUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
public class ShGraphQLEndpoint {

	@Autowired
	GraphQLInvocation graphQLInvocation;

	@Autowired
	ExecutionResultHandler executionResultHandler;

	@Autowired
	JsonSerializer jsonSerializer;

	@Autowired
	private ShUserUtils shUserUtils;

	@RequestMapping(value = "graphql", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public Object graphqlPOST(@RequestHeader(value = HttpHeaders.CONTENT_TYPE, required = false) String contentType,
			@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
			@RequestParam(value = "query", required = false) String query,
			@RequestParam(value = "operationName", required = false) String operationName,
			@RequestParam(value = "variables", required = false) String variablesJson,
			@RequestBody(required = false) String body, WebRequest webRequest) throws IOException {

		if (this.isAuthenticated(authorization)) {
			if (body == null) {
				body = "";
			}
			
			if (MediaType.APPLICATION_JSON_VALUE.equals(contentType)) {
				GraphQLRequestBody request = jsonSerializer.deserialize(body, GraphQLRequestBody.class);
				if (request.getQuery() == null) {
					request.setQuery("");
				}
				return executeRequest(request.getQuery(), request.getOperationName(), request.getVariables(),
						webRequest);
			}

			if (query != null) {
				return executeRequest(query, operationName, convertVariablesJson(variablesJson), webRequest);
			}

			if ("application/graphql".equals(contentType)) {
				return executeRequest(body, null, null, webRequest);
			}
		}
		throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Could not process GraphQL request");
	}

	private boolean isAuthenticated(String authorization) {
		boolean authenticated = true;
		if (!StringUtils.isEmpty(authorization) && authorization.toLowerCase().startsWith("basic")) {
			String base64Credentials = authorization.substring("Basic".length()).trim();
			byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
			String credentials = new String(credDecoded, StandardCharsets.UTF_8);
			final String[] values = credentials.split(":", 2);
			String username = values[0];
			String password = values[1];
			authenticated = shUserUtils.isValidUserAndPassword(username, password);
		}
		return authenticated;
	}

	@RequestMapping(value = "graphql", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Object graphqlGET(@RequestParam("query") String query,
			@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
			@RequestParam(value = "operationName", required = false) String operationName,
			@RequestParam(value = "variables", required = false) String variablesJson, WebRequest webRequest) {

		if (this.isAuthenticated(authorization)) {
			return executeRequest(query, operationName, convertVariablesJson(variablesJson), webRequest);
		}
		throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Could not process GraphQL request");
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> convertVariablesJson(String jsonMap) {
		if (jsonMap == null) {
			return Collections.emptyMap();
		}
		return jsonSerializer.deserialize(jsonMap, Map.class);
	}

	private Object executeRequest(String query, String operationName, Map<String, Object> variables,
			WebRequest webRequest) {
		GraphQLInvocationData invocationData = new GraphQLInvocationData(query, operationName, variables);
		CompletableFuture<ExecutionResult> executionResult = graphQLInvocation.invoke(invocationData, webRequest);
		return executionResultHandler.handleExecutionResult(executionResult);
	}

}