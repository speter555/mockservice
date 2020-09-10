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

import javax.enterprise.inject.Model;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import hu.icellmobilsoft.coffee.cdi.logger.AppLogger;
import hu.icellmobilsoft.coffee.cdi.logger.ThisLogger;
import hu.icellmobilsoft.coffee.dto.exception.BaseException;
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

    @Inject
    @ThisLogger
    private AppLogger logger;

    @Inject
    private CacheFileHelper cacheFileHelper;

    /**
     * All Get endpoint
     *
     * @param ui Uri info
     * @return mock response
     * @throws BaseException if error
     */
    @GET
    @Path("{any: .*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPath(@Context UriInfo ui) throws BaseException {
        return getFile(ui);
    }

    /**
     * All Post endpoint
     *
     * @param ui      Uri info
     * @param request any request
     * @return mock response
     * @throws BaseException if error
     */
    @POST
    @Path("{any: .*}")
    @Consumes
    @Produces(MediaType.APPLICATION_JSON)
    public Response postPath(@Context UriInfo ui, String request) throws BaseException {
        logger.info("request: " + request);
        return getFile(ui);
    }

    /**
     * All Delete endpoint
     *
     * @param ui      Uri info
     * @param request any request
     * @return mock response
     * @throws BaseException if error
     */
    @DELETE
    @Path("{any: .*}")
    @Consumes
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePath(@Context UriInfo ui, String request) throws BaseException {
        logger.info("request: " + request);
        return getFile(ui);
    }

    /**
     * All Put endpoint
     *
     * @param ui      Uri info
     * @param request any request
     * @return mock response
     * @throws BaseException if error
     */
    @PUT
    @Path("{any: .*}")
    @Consumes
    @Produces(MediaType.APPLICATION_JSON)
    public Response putPath(@Context UriInfo ui, String request) throws BaseException {
        logger.info("request: " + request);
        return getFile(ui);
    }

    /**
     * Get Response by uriInfo.
     *
     * @param ui Uri info
     * @return mock response
     * @throws BaseException if error
     */
    private Response getFile(UriInfo ui) throws BaseException {
        String path = ui.getPath().replaceFirst("/", StringUtils.EMPTY);
        logger.info("path: " + path);
        if (StringUtils.isBlank(path)) {
            path = "root";
        }
        String filePath = path + ".json";
        logger.info("fileName: " + filePath);
        String response;
        if (cacheFileHelper.containsKey(filePath)) {
            response = cacheFileHelper.get(filePath);
        } else {
            response = FileUtil.readFileFromResource(filePath);
            cacheFileHelper.put(filePath, response);
        }
        return Response.ok().entity(response).build();
    }
}
