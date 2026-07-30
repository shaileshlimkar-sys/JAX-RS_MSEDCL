package com.msedcl.jaxrs.urlshortner.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.msedcl.jaxrs.urlshortner.model.ErrorMessage;

@Provider
public class InvalidUrlExceptionMapper implements ExceptionMapper<InvalidUrlException>{

	
	@Override
	public Response toResponse(InvalidUrlException exception) {
		ErrorMessage errorMessage = new ErrorMessage(exception.getMessage(), 400);
		return Response.status(Status.BAD_REQUEST)
				        .entity(errorMessage).build();
	}

}
