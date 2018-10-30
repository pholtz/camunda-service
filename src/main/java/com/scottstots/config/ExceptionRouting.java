package com.scottstots.config;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.qvc.productplanning.exceptions.BadRequestException;
import com.qvc.productplanning.exceptions.ConflictingResourceException;
import com.qvc.productplanning.exceptions.DataListNotFoundException;
import com.qvc.productplanning.exceptions.DataNotFoundException;
import com.qvc.productplanning.exceptions.UpstreamServerException;
import com.qvc.productplanning.response.DataListNotFoundResponse;


@ControllerAdvice
public class ExceptionRouting
{
	private static final String ERROR_MESSAGE = "Request {} raised {}";
	private static final Logger log = LoggerFactory.getLogger(ExceptionRouting.class);

	private static final Map<Class<? extends Exception>, HttpStatus> exceptionToHttpStatus;
	static {
		Map<Class<? extends Exception>, HttpStatus> template = new HashMap<Class<? extends Exception>, HttpStatus>();
		template.put(BadRequestException.class, HttpStatus.BAD_REQUEST);
		template.put(MethodArgumentNotValidException.class, HttpStatus.BAD_REQUEST);
		template.put(HttpMessageNotReadableException.class, HttpStatus.BAD_REQUEST);
		template.put(MissingServletRequestParameterException.class, HttpStatus.BAD_REQUEST);
		template.put(ConflictingResourceException.class, HttpStatus.CONFLICT);
		template.put(AccessDeniedException.class, HttpStatus.UNAUTHORIZED);
		template.put(DataNotFoundException.class, HttpStatus.NOT_FOUND);
		template.put(HttpRequestMethodNotSupportedException.class, HttpStatus.METHOD_NOT_ALLOWED);
		template.put(HttpMediaTypeNotSupportedException.class, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
		template.put(UpstreamServerException.class, HttpStatus.BAD_GATEWAY);
		exceptionToHttpStatus = Collections.unmodifiableMap(template);
	}

	@ExceptionHandler 
	public void handleException(HttpServletRequest request, HttpServletResponse response, Exception exception) throws IOException {
		HttpStatus status = exceptionToHttpStatus.getOrDefault(exception.getClass(), HttpStatus.INTERNAL_SERVER_ERROR);
		if(status.is5xxServerError()) {
			log.error(ERROR_MESSAGE, request.getRequestURL(), exception.toString(), exception);	
		} else {
			log.info(ERROR_MESSAGE, request.getRequestURL(), exception.toString());
		}
		response.sendError(status.value(), exception.getMessage());
	}
	
	@ExceptionHandler(DataListNotFoundException.class)
	public ResponseEntity<DataListNotFoundResponse> handleConflict(HttpServletRequest request, HttpServletResponse response, DataListNotFoundException exception) {
		DataListNotFoundResponse responseBody = new DataListNotFoundResponse(ZonedDateTime.now(), 
				HttpStatus.NOT_FOUND.value(), exception.getErrorMessage(), request.getRequestURL().toString(), 
				exception.getKeys());
		log.info(ERROR_MESSAGE, request.getRequestURL(), exception.toString());
		return new ResponseEntity<>(responseBody, HttpStatus.NOT_FOUND);
    }
}