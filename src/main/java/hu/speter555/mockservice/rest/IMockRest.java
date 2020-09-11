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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import hu.icellmobilsoft.coffee.dto.exception.BaseException;

/**
 * Rest endpoints
 *
 * @author speter555
 */
@Path("/")
public interface IMockRest {

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
    Response get() throws BaseException;

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
    Response delete() throws BaseException;

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
    Response options() throws BaseException;

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
    Response post() throws BaseException;

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
    Response put() throws BaseException;

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
    Response head() throws BaseException;

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
    Response patch() throws BaseException;
}
