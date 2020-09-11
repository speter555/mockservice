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
package hu.speter555.mockservice.httpclient;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.ws.rs.core.Response;

import hu.icellmobilsoft.coffee.dto.exception.AccessDeniedException;
import hu.icellmobilsoft.coffee.dto.exception.BONotFoundException;
import hu.icellmobilsoft.coffee.dto.exception.BaseException;
import hu.icellmobilsoft.coffee.dto.exception.TechnicalException;
import hu.icellmobilsoft.coffee.dto.exception.enums.CoffeeFaultType;
import hu.icellmobilsoft.coffee.rest.apache.BaseApacheHttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * Http Client call client
 *
 * @author speter555
 */
@Dependent
public class ApacheHttpClient extends BaseApacheHttpClient {

    private static final int HTTP_STATUS_I_AM_A_TEAPOT = 418;

    private Map<String, String> additionalHeader = new HashMap<>();

    /**
     * Send GET call to url
     *
     * @param url called url
     * @return response in String
     * @throws BaseException if error
     */
    public String sendClientGet(String url) throws BaseException {
        HttpResponse response = super.sendClientBaseGet(url);
        return handleResponse(response);
    }

    /**
     * Send POST call to url
     *
     * @param url     called url
     * @param request sended object
     * @return response in String
     * @throws BaseException if error
     */
    public String sendClientPost(String url, String request) throws BaseException {
        HttpResponse response = super.sendClientBasePost(url, ContentType.APPLICATION_JSON, request);
        return handleResponse(response);
    }

    /**
     * Send PUT call to url
     *
     * @param url     called url
     * @param request sended object
     * @return response in String
     * @throws BaseException if error
     */
    public String sendClientPut(String url, String request) throws BaseException {
        HttpResponse response = super.sendClientBasePut(url, ContentType.APPLICATION_JSON, request);
        return handleResponse(response);
    }

    /**
     * Send DELETE call to url
     *
     * @param url called url
     * @return response in String
     * @throws BaseException if error
     */
    public String sendClientDelete(String url) throws BaseException {
        HttpResponse response = super.sendClientBaseDelete(url);
        return handleResponse(response);
    }

    /**
     * Send HEAD call to url
     *
     * @param url called url
     * @return response in String
     * @throws BaseException if error
     */
    public String sendClientHead(String url) throws BaseException {
        HttpResponse response = sendClientBaseHead(url);
        return handleResponse(response);
    }

    /**
     * Send OPTIONS call to url
     *
     * @param url called url
     * @return response in String
     * @throws BaseException if error
     */
    public String sendClientOptions(String url) throws BaseException {
        HttpResponse response = sendClientBaseOptions(url);
        return handleResponse(response);
    }

    /**
     * Send PATCH call to url
     *
     * @param url     called url
     * @param request sended object
     * @return response in String
     * @throws BaseException if error
     */
    public String sendClientPatch(String url, String request) throws BaseException {
        HttpResponse response = sendClientBasePatch(url, request);
        return handleResponse(response);
    }

    /**
     * Set headers to calls
     *
     * @param additionalHeader setted key-value pair what will add call
     */
    public void setAdditionalHeader(Map<String, String> additionalHeader) {
        this.additionalHeader = additionalHeader;
    }

    @Override
    protected void beforeAll(HttpRequestBase request) throws BaseException {
        for (Map.Entry<String, String> header : additionalHeader.entrySet()) {
            request.addHeader(header.getKey(), header.getValue());
        }
    }

    private HttpResponse sendClientBaseHead(String url) throws BaseException {

        HttpHead head = new HttpHead(url);
        CloseableHttpClient client = getCloseableHttpClient(url);

        try {
            beforeAll(head);
            logRequest(head);
            return client.execute(head);
        } catch (ClientProtocolException e) {
            throw new TechnicalException(CoffeeFaultType.OPERATION_FAILED, "HTTP protocol exception: " + e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new TechnicalException(CoffeeFaultType.OPERATION_FAILED, "IOException in call: " + e.getLocalizedMessage(), e);
        }
    }

    private HttpResponse sendClientBaseOptions(String url) throws BaseException {

        HttpOptions head = new HttpOptions(url);
        CloseableHttpClient client = getCloseableHttpClient(url);

        try {
            beforeAll(head);
            logRequest(head);
            return client.execute(head);
        } catch (ClientProtocolException e) {
            throw new TechnicalException(CoffeeFaultType.OPERATION_FAILED, "HTTP protocol exception: " + e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new TechnicalException(CoffeeFaultType.OPERATION_FAILED, "IOException in call: " + e.getLocalizedMessage(), e);
        }
    }


    private HttpResponse sendClientBasePatch(String url, String request) throws BaseException {

        HttpPatch patch = new HttpPatch(url);
        CloseableHttpClient client = getCloseableHttpClient(url);

        try {
            beforeAll(patch);
            StringEntity stringEntity = new StringEntity(request, "application/json");
            patch.setEntity(stringEntity);
            logRequest(patch, org.apache.commons.lang3.StringUtils.abbreviate(new String(request), 80));
            return client.execute(patch);
        } catch (ClientProtocolException e) {
            throw new TechnicalException(CoffeeFaultType.OPERATION_FAILED, "HTTP protocol exception: " + e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new TechnicalException(CoffeeFaultType.OPERATION_FAILED, "IOException in call: " + e.getLocalizedMessage(), e);
        }
    }

    private CloseableHttpClient getCloseableHttpClient(String url) throws BaseException {
        RequestConfig config = createRequestConfig().build();
        CloseableHttpClient client = createHttpClientBuilder(config).build();
        handleSSL(client, URI.create(url));
        return client;
    }

    private String handleResponse(HttpResponse response) throws BaseException {
        try {
            byte[] byteEntity = EntityUtils.toByteArray(response.getEntity());
            // loggoljuk a response-t
            logResponse(response, byteEntity);
            String entity = new String(byteEntity, StandardCharsets.UTF_8);
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode == HttpStatus.SC_OK) {
                return entity;
            } else {
                if (responseCode == HTTP_STATUS_I_AM_A_TEAPOT) {
                    throw new BONotFoundException(entity);
                } else if (responseCode == Response.Status.UNAUTHORIZED.getStatusCode()) {
                    throw new AccessDeniedException(entity);
                } else if (responseCode == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
                    throw new BaseException(entity);
                }
                throw new TechnicalException(CoffeeFaultType.OPERATION_FAILED,
                        "HTTP error code[" + response.getStatusLine().getStatusCode() + "], content [" + entity + "]");
            }
        } catch (IOException e) {
            throw new TechnicalException(CoffeeFaultType.OPERATION_FAILED, "IOException in call: " + e.getLocalizedMessage(), e);
        }
    }
}
