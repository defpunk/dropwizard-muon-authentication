package com.qwickr.muon.auth; 

import java.security.Principal;
import java.util.List;

public class AuthenticatedUser implements Principal {
    
   private final String name;
   private final List<String> roles;

    public AuthenticatedUser(String name, List<String> roles) {
        this.name = name;
        this.roles = roles;
    }

    public String getName() {
        return name;
    }

   public List<String> getRoles(){
	return roles;
   }

    public int getId() {
        return (int) (Math.random() * 100);
    }
}
