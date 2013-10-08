package org.sean.hiking.user;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.sean.hiking.Credentials;
import org.sean.hiking.WrappedResponse;

import com.google.common.base.Optional;

@Path("/api/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {
	
	private final UserManager userManager;
	
	public UserResource(UserManager userManager) {
		this.userManager = userManager;
	}
	
    @POST
    public WrappedResponse<Credentials> insert(
    		@HeaderParam(HttpHeaders.AUTHORIZATION) String auth,
    		CreateUserRequest req) {
    	Optional<User> authUser = this.userManager.getUserFromAuthString(auth);
    	if (authUser.isPresent()) return WrappedResponse.failure("You are already logged in. Cannot create new account.");
    	
    	return userManager.createUser(req.getUsername(), req.getEmail(), req.getPassword());
    }
    
    @POST
    @Path("/login")
    public WrappedResponse<Credentials> login(
    		@HeaderParam(HttpHeaders.AUTHORIZATION) String auth,
    		LoginRequest req) {
    	Optional<User> authUser = this.userManager.getUserFromAuthString(auth);  	
    	if (authUser.isPresent()) return WrappedResponse.failure("You are already logged in.");
    	
    	return userManager.getCredentials(req.getIdentifier(), req.getPassword());
    }
    
    

}
