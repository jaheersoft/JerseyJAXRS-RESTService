/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jaheer.app.ws.filters;

import com.jaheer.app.ws.annotations.Secured;
import com.jaheer.app.ws.exceptions.AuthenticationException;
import com.jaheer.app.ws.service.UsersService;
import com.jaheer.app.ws.service.impl.UsersServiceImpl;
import com.jaheer.app.ws.shared.dto.UserDTO;
import com.jaheer.app.ws.utils.UserProfileUtils;
import java.io.IOException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author sergeykargopolov
 */
@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {
 
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Extract Authorization header details
        String authorizationHeader
                = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer")) {
                    throw new AuthenticationException("Authorization header must be provided");
        }

        // Extract the token
        String token = authorizationHeader.substring("Bearer".length()).trim();

        // Extract user id
        String userId = requestContext.getUriInfo().getPathParameters().getFirst("id");
 
        validateToken(token, userId);
        
    }
    
private void validateToken(String token, String userId) throws AuthenticationException {
      
        // Get user profile details
        UsersService usersService = new UsersServiceImpl();
        UserDTO userProfile = usersService.getUser(userId);
        
        // Asseble Access token using two parts. One from DB and one from http request.
        String completeToken = userProfile.getToken() + token;
        
        // Create Access token material out of the useId received and salt kept database
        String securePassword = userProfile.getEncryptedPassword();
        String salt = userProfile.getSalt();
        String accessTokenMaterial = userId + salt;
        byte[] encryptedAccessToken = null;
       
        try {
            encryptedAccessToken = new UserProfileUtils().encrypt(securePassword, accessTokenMaterial);
        } catch (InvalidKeySpecException ex) {
            Logger.getLogger(AuthenticationFilter.class.getName()).log(Level.SEVERE, null, ex);
            throw new AuthenticationException("Faled to issue secure access token");
        }
        
        String encryptedAccessTokenBase64Encoded = Base64.getEncoder().encodeToString(encryptedAccessToken);
       
        // Compare two access tokens 
        if (!encryptedAccessTokenBase64Encoded.equalsIgnoreCase(completeToken)) {
            throw new AuthenticationException("Authorization token did not match");
        }
    }
}