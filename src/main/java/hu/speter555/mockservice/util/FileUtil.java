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
package hu.speter555.mockservice.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.enterprise.inject.Vetoed;

import hu.icellmobilsoft.coffee.dto.exception.BaseException;
import org.apache.commons.lang3.StringUtils;

/**
 * File utils for test environment
 *
 * @author speter555
 */
@Vetoed
public class FileUtil {

    private static final Logger LOG = Logger.getLogger(FileUtil.class.getName());

    /**
     * Read file by java.nio (java 11+)
     *
     * @param first first the path string or initial part of the path string
     * @param more  more additional strings to be joined to form the path string
     * @return file content
     * @throws BaseException if error
     */
    public static String readFile(String first, String... more) throws Exception {
        Path path = Path.of(first, more);
        return readFile(path);
    }

    /**
     * Read file by java.nio (java 11+) from ClassLoader.getSystemResourceAsStream
     *
     * @param fileName filename like token.xml, in src/main/resources source
     *                 directory
     * @return file content
     * @throws BaseException if error
     */
    public static String readFileFromResource(String fileName) throws BaseException {
        if (StringUtils.isBlank(fileName)) {
            return null;
        }
        try (InputStream inputStream = ClassLoader.getSystemResourceAsStream(fileName)) {
            if (inputStream != null) {
                String file = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines()
                        .collect(Collectors.joining("\n"));
                LOG.info(() -> MessageFormat.format("File [{0}] from resources readed!",
                        ClassLoader.getSystemResource(fileName)));
                return file;
            }
        } catch (IOException e) {
            throw new BaseException(MessageFormat.format("Unable to read File [{0}] from resource", fileName), e);
        }
        throw new BaseException(MessageFormat.format("Unable to find File [{0}] from resource", fileName));
    }

    /**
     * Read file by java.nio (java 11+)
     *
     * @param path the path to the file
     * @return file content
     * @throws BaseException if error
     */
    public static String readFile(Path path) throws BaseException {
        if (path == null) {
            throw new BaseException("path is null!");
        }
        try {
            String file = Files.readString(path);
            LOG.info(() -> MessageFormat.format("File from path [{0}] readed!", path.toAbsolutePath()));
            return file;
        } catch (IOException e) {
            throw new BaseException(MessageFormat.format("Unable to read File from path: [{0}]", path.toAbsolutePath()),
                    e);
        }
    }

}
