/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2020 Liberty Global B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lgi.appstore.metadata.api.error;

import com.lgi.appstore.metadata.model.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String DEFAULT_MESSAGE = "Details not available";

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAll(Exception ex, WebRequest request) {
        return handleGenericResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(MaintainerNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(Exception ex, WebRequest request) {
        return handleGenericResponse(ex, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler({ApplicationAlreadyExistsException.class, MaintainerAlreadyExistsException.class})
    public ResponseEntity<Object> handleConflict(Exception ex, WebRequest request) {
        return handleGenericResponse(ex, HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        final ErrorResponse errorResponse = new ErrorResponse();

        if (ex.getRequiredType() != null) {
            errorResponse.message(ex.getName() + " should be of type " + ex.getRequiredType().getName());
        } else {
            errorResponse.message(ex.getName() + " has invalid type");
        }

        return handleGenericResponse(ex, errorResponse, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(JsonException.class)
    public ResponseEntity<Object> handleJsonException(JsonException ex, WebRequest request) {
        final String invalidJsonString = ex.getInvalidJsonString();
        if (invalidJsonString != null) {
            LOG.warn("Could not process: '{}'", invalidJsonString);
        }

        return handleGenericResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleGenericResponse(ex, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status,
            WebRequest request) {
        final ErrorResponse errorResponse = new ErrorResponse().message(ex.getParameterName() + " parameter is missing");

        return handleGenericResponse(ex, errorResponse, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(MissingServletRequestPartException ex, HttpHeaders headers, HttpStatus status,
            WebRequest request) {
        return handleGenericResponse(ex, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status,
            WebRequest request) {
        final List<String> errors = new ArrayList<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }

        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
        }

        final ErrorResponse errorResponse = new ErrorResponse().message(errors.toString());

        return handleGenericResponse(ex, errorResponse, headers, HttpStatus.BAD_REQUEST, request);
    }

    private ResponseEntity<Object> handleGenericResponse(Exception ex, HttpStatus httpStatus, WebRequest webRequest) {
        return handleGenericResponse(ex, (HttpHeaders) null, httpStatus, webRequest);
    }

    private ResponseEntity<Object> handleGenericResponse(Exception ex, ErrorResponse errorResponse, HttpStatus httpStatus, WebRequest webRequest) {
        return handleGenericResponse(ex, errorResponse, null, httpStatus, webRequest);
    }

    private ResponseEntity<Object> handleGenericResponse(Exception ex, @Nullable HttpHeaders httpHeaders, HttpStatus httpStatus, WebRequest webRequest) {
        return handleGenericResponse(ex, createResponse(ex), httpHeaders, httpStatus, webRequest);
    }

    private ResponseEntity<Object> handleGenericResponse(Exception ex, ErrorResponse errorResponse, @Nullable HttpHeaders httpHeaders, HttpStatus httpStatus,
            WebRequest webRequest) {
        LOG.warn("Exception: ", ex);
        return handleExceptionInternal(ex, errorResponse, httpHeaders != null ? httpHeaders : new HttpHeaders(), httpStatus, webRequest);
    }

    private ErrorResponse createResponse(Exception ex) {
        final String message = ex.getMessage();

        return new ErrorResponse().message((message != null && !message.isBlank()) ? message : DEFAULT_MESSAGE);
    }
}
