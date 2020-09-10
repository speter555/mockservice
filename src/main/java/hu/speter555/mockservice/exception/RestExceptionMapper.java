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
package hu.speter555.mockservice.exception;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBException;

import hu.icellmobilsoft.coffee.cdi.logger.AppLogger;
import hu.icellmobilsoft.coffee.cdi.logger.ThisLogger;
import hu.icellmobilsoft.coffee.dto.common.commonservice.BONotFound;
import hu.icellmobilsoft.coffee.dto.common.commonservice.BaseExceptionResultType;
import hu.icellmobilsoft.coffee.dto.common.commonservice.BusinessFault;
import hu.icellmobilsoft.coffee.dto.common.commonservice.FunctionCodeType;
import hu.icellmobilsoft.coffee.dto.common.commonservice.InvalidRequestFault;
import hu.icellmobilsoft.coffee.dto.common.commonservice.TechnicalFault;
import hu.icellmobilsoft.coffee.dto.common.commonservice.ValidationType;
import hu.icellmobilsoft.coffee.dto.exception.AccessDeniedException;
import hu.icellmobilsoft.coffee.dto.exception.BONotFoundException;
import hu.icellmobilsoft.coffee.dto.exception.BaseException;
import hu.icellmobilsoft.coffee.dto.exception.BaseExceptionWrapper;
import hu.icellmobilsoft.coffee.dto.exception.InvalidRequestException;
import hu.icellmobilsoft.coffee.dto.exception.XMLValidationError;
import hu.icellmobilsoft.coffee.dto.exception.enums.CoffeeFaultType;
import org.jboss.resteasy.spi.InternalServerErrorException;

/**
 * Exception mapper for handled exception throwing
 *
 * @author peter.szabo
 */
@Provider
public class RestExceptionMapper implements ExceptionMapper<Exception> {

    private static final int HTTP_STATUS_I_AM_A_TEAPOT = 418;

    @Inject
    @ThisLogger
    private AppLogger log;

    @Override
    public Response toResponse(Exception e) {
        Response result = null;
        if (e instanceof BaseException) {
            result = handleBaseException((BaseException) e);
        } else if (e instanceof BaseExceptionWrapper) {
            BaseExceptionWrapper<?> wrappedException = (BaseExceptionWrapper) e;
            if (wrappedException.getException() != null) {
                this.log.info("Wrapped exception. Trying to match the correct mapper...");
                result = this.handleWrappedException(wrappedException.getException());
            } else if (e.getCause() instanceof BaseException) {
                this.log.info("Wrapped BaseException. Trying to match the correct mapper...");
                result = this.handleWrappedException((BaseException) e.getCause());
            } else {
                this.log.error("Unknown error in cause: ", e);
                this.log.writeLogToError();
            }
        } else {
            this.log.error("Unknown error: ", e);
            this.log.writeLogToError();
        }

        return result != null ? result : this.handleException(e);
    }

    private Response handleException(Exception e) {
        TechnicalFault dto = new TechnicalFault();
        addCommonInfo(dto, e, CoffeeFaultType.OPERATION_FAILED);
        Response.Status statusCode = Response.Status.INTERNAL_SERVER_ERROR;
        if (e instanceof InternalServerErrorException) {
            statusCode = Response.Status.BAD_REQUEST;
        }

        Response.ResponseBuilder responseBuilder = Response.status(statusCode);
        return responseBuilder.entity(dto).build();
    }

    private Response handleWrappedException(BaseException exception) {
        if (exception == null) {
            this.log.warn("Failed to map the wrapped exception. Wrapper exception don't have content.");
            return null;
        } else {
            return handleBaseException(exception);
        }
    }

    private Response handleBaseException(BaseException e) {
        log.error("Known error: ", e);
        log.writeLogToError();

        if (e instanceof BONotFoundException) {
            BONotFound dto = new BONotFound();
            addCommonInfo(dto, e);
            return Response.status(HTTP_STATUS_I_AM_A_TEAPOT).entity(dto).build();
        } else if (e instanceof InvalidRequestException) {
            InvalidRequestException ire = (InvalidRequestException) e;
            InvalidRequestFault dto = new InvalidRequestFault();
            addCommonInfo(dto, e);
            addValidationErrors(dto, ire.getErrors());
            return Response.status(Response.Status.BAD_REQUEST).entity(dto).build();
        } else if (e instanceof AccessDeniedException) {
            BusinessFault dto = new BusinessFault();
            addCommonInfo(dto, e, e.getFaultTypeEnum());
            return Response.status(Response.Status.UNAUTHORIZED).entity(dto).build();
        } else {
            // BaseException
            TechnicalFault dto = new TechnicalFault();
            addCommonInfo(dto, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(dto).build();
        }
    }


    private void addCommonInfo(BaseExceptionResultType dto, BaseException e) {
        this.addCommonInfo(dto, e, e.getFaultTypeEnum());
    }

    private void addCommonInfo(BaseExceptionResultType dto, Exception e, Enum<?> faultType) {
        if (e instanceof JAXBException) {
            if (e != null) {
                Throwable t = ((JAXBException) e).getLinkedException();
                dto.setMessage(t != null ? t.getLocalizedMessage() : e.getLocalizedMessage());
            }
        } else {
            dto.setMessage(e.getLocalizedMessage());
        }
        dto.setClassName(e.getClass().getName());
        dto.setException(e.getLocalizedMessage());

        dto.setFuncCode(FunctionCodeType.ERROR);
        dto.setFaultType(faultType.name());
    }

    private void addValidationErrors(InvalidRequestFault dto, List<XMLValidationError> errors) {
        if (errors != null) {
            for (XMLValidationError error : errors) {
                ValidationType valType = new ValidationType();
                valType.setError(error.getError());
                dto.getError().add(valType);
            }
        }
    }
}
