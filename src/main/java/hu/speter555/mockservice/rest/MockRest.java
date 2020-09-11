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
package hu.speter555.mockservice.rest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import javax.enterprise.inject.Model;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import hu.icellmobilsoft.coffee.cdi.logger.AppLogger;
import hu.icellmobilsoft.coffee.cdi.logger.ThisLogger;
import hu.icellmobilsoft.coffee.dto.exception.BaseException;
import hu.icellmobilsoft.coffee.rest.rest.BaseRestService;
import hu.speter555.mockservice.httpclient.ApacheHttpClient;
import hu.speter555.mockservice.util.CacheFileHelper;
import hu.speter555.mockservice.util.FileUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * Rest endpoints implementations
 *
 * @author speter555
 */
@Model
public class MockRest extends BaseRestService implements IMockRest {

    private static final String REDIRECT_HEADER_URL = "MOCKSERVICE-REDIRECT-URL";
    private static final String REDIRECT_HEADER_HEADERS = "MOCKSERVICE-REDIRECT-HEADERS";
    
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
     * Apache HTTP client for redirect calls
     */
    @Inject
    private ApacheHttpClient apacheHttpClient;

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
     * {@inheritDoc}
     */
    @Override
    public Response get() throws BaseException {
        return getFile();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response delete() throws BaseException {
        return getFile();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response options() throws BaseException {
        return getFile();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response post() throws BaseException {
        return getFile();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response put() throws BaseException {
        return getFile();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response head() throws BaseException {
        return getFile();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response patch() throws BaseException {
        return getFile();
    }

    /**
     * Get Response file
     *
     * @return mock response with http 200 status, end the entity
     * @throws BaseException if error is created
     */
    public Response getFile() throws BaseException {
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
            MediaType mediaType = MediaType.valueOf(httpServletRequest.getHeader(HttpHeaders.ACCEPT));
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
                    response = apacheHttpClient.sendClientPatch(redirectUrl, request, mediaType);
                    break;
                case HttpMethod.POST:
                    response = apacheHttpClient.sendClientPost(redirectUrl, request, mediaType);
                    break;
                case HttpMethod.PUT:
                    response = apacheHttpClient.sendClientPut(redirectUrl, request, mediaType);
                    break;
                default:
                    break;
            }
        }
        return response;
    }
}
