/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jaheer.app.ws.exceptions;

import com.jaheer.app.ws.ui.model.response.ErrorMessage;
import com.jaheer.app.ws.ui.model.response.ErrorMessages;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author sergeykargopolov
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    public Response toResponse(Throwable exception) {
        ErrorMessage errorMessage = new ErrorMessage(exception.getMessage(),
                ErrorMessages.INTERNAL_SERVER_ERROR.name(), "http://appsdeveloperblog.com");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                entity(errorMessage).
                build();
    }

}
