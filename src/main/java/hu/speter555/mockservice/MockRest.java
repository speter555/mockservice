/*-
 * #%L
 * Mockservice
 * %%
 * Copyright (C) 2020 speter555
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package hu.speter555.mockservice;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import javax.enterprise.inject.Model;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import hu.icellmobilsoft.coffee.cdi.logger.AppLogger;
import hu.icellmobilsoft.coffee.cdi.logger.ThisLogger;
import hu.icellmobilsoft.coffee.dto.exception.BaseException;
import hu.speter555.mockservice.httpclient.ApacheHttpClient;
import hu.speter555.mockservice.util.CacheFileHelper;
import hu.speter555.mockservice.util.FileUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * Rest endpoint of all
 *
 * @author speter555
 */
@Model
@Path("/")
public class MockRest {

    private final String REDIRECT_HEADER_URL = "MOCKSERVICE-REDIRECT-URL";
    private final String REDIRECT_HEADER_HEADERS = "MOCKSERVICE-REDIRECT-HEADERS";

    /**
     * For logging...
     */
    @Inject
    @ThisLogger
    private AppLogger logger;

    /**
     * Response cache helper (singleton)
     */
    @Inject
    private CacheFileHelper cacheFileHelper;

    /**
     * Request
     */
    @Context
    private HttpServletRequest httpServletRequest;

    /**
     * Http headers in request
     */
    @Context
    private HttpHeaders httpHeaders;

    /**
     * Apache HTTP client for redirect calls
     */
    @Inject
    private ApacheHttpClient apacheHttpClient;

    /**
     * All Get endpoints
     *
     * @return mock response
     * @throws BaseException if error
     */
    @GET
    @Path("{any: .*}")
    @Consumes
    @Produces
    public Response get() throws BaseException {
        return getFile();
    }

    /**
     * All Delete endpoints
     *
     * @return mock response
     * @throws BaseException if error
     */
    @DELETE
    @Path("{any: .*}")
    @Consumes
    @Produces
    public Response delete() throws BaseException {
        return getFile();
    }

    /**
     * All Options endpoints
     * endpoint
     *
     * @return mock response
     * @throws BaseException if error
     */
    @OPTIONS
    @Path("{any: .*}")
    @Consumes
    @Produces
    public Response options() throws BaseException {
        return getFile();
    }

    /**
     * All Post endpoints
     * endpoint
     *
     * @return mock response
     * @throws BaseException if error
     */
    @POST
    @Path("{any: .*}")
    @Consumes
    @Produces
    public Response post() throws BaseException {
        return getFile();
    }

    /**
     * All Put endpoints
     * endpoint
     *
     * @return mock response
     * @throws BaseException if error
     */
    @PUT
    @Path("{any: .*}")
    @Consumes
    @Produces
    public Response put() throws BaseException {
        return getFile();
    }

    /**
     * All Head endpoints
     * endpoint
     *
     * @return mock response
     * @throws BaseException if error
     */
    @HEAD
    @Path("{any: .*}")
    @Consumes
    @Produces
    public Response head() throws BaseException {
        return getFile();
    }

    /**
     * All Patch endpoints
     * endpoint
     *
     * @return mock response
     * @throws BaseException if error
     */
    @PATCH
    @Path("{any: .*}")
    @Consumes
    @Produces
    public Response patch() throws BaseException {
        return getFile();
    }

    /**
     * Get Response file
     *
     * @return mock response with http 200 status, end the entity
     * @throws BaseException if error is created
     */
    private Response getFile() throws BaseException {
        String path = httpServletRequest.getPathInfo().replaceFirst("/", StringUtils.EMPTY);
        logger.info("path: " + path);
        if (StringUtils.isBlank(path)) {
            path = "root";
        }
        String filePath = path + ".json";
        logger.info("fileName: " + filePath);
        String response = validateRedirect();

        if (Objects.isNull(response)) {
            if (cacheFileHelper.containsKey(filePath)) {
                response = cacheFileHelper.get(filePath);
            } else {
                response = FileUtil.readFileFromResource(filePath);
                cacheFileHelper.put(filePath, response);
            }
        } else {
            cacheFileHelper.put(filePath, response);
        }
        return Response.ok().entity(response).build();
    }

    /**
     * If header contains MOCKSERVICE-REDIRECT-URL header, method call the url what is in MOCKSERVICE-REDIRECT-URL header, and return this response in String.
     * If header not contains MOCKSERVICE-REDIRECT-URL, return null!
     *
     * @return null if no redirect, otherwise redirect call response in String
     * @throws BaseException if error is created
     */
    private String validateRedirect() throws BaseException {
        String redirectUrl = httpHeaders.getHeaderString(REDIRECT_HEADER_URL);
        String response = null;
        if (StringUtils.isNotBlank(redirectUrl)) {
            String method = httpServletRequest.getMethod();
            String request = null;
            if (StringUtils.containsAny(method, HttpMethod.PUT, HttpMethod.PATCH, HttpMethod.POST)) {
                try {
                    request = new String(httpServletRequest.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new BaseException("Error during read request body...", e);
                }
            }
            HashMap<String, String> map = new HashMap<>();

            String redirectHeaders = httpHeaders.getHeaderString(REDIRECT_HEADER_HEADERS);
            if (StringUtils.isNotBlank(redirectHeaders)) {
                String[] headers = redirectHeaders.split(",");
                Arrays.stream(headers).forEach(header -> map.put(header, httpHeaders.getHeaderString(header)));
            }
            apacheHttpClient.setAdditionalHeader(map);
            switch (method) {
                case HttpMethod.DELETE:
                    response = apacheHttpClient.sendClientDelete(redirectUrl);
                    break;
                case HttpMethod.GET:
                    response = apacheHttpClient.sendClientGet(redirectUrl);
                    break;
                case HttpMethod.HEAD:
                    response = apacheHttpClient.sendClientHead(redirectUrl);
                    break;
                case HttpMethod.OPTIONS:
                    response = apacheHttpClient.sendClientOptions(redirectUrl);
                    break;
                case HttpMethod.PATCH:
                    response = apacheHttpClient.sendClientPatch(redirectUrl, request);
                    break;
                case HttpMethod.POST:
                    response = apacheHttpClient.sendClientPost(redirectUrl, request);
                    break;
                case HttpMethod.PUT:
                    response = apacheHttpClient.sendClientPut(redirectUrl, request);
                    break;
                default:
                    break;
            }
        }
        return response;
    }

}
