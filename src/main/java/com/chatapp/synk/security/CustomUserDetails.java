package com.chatapp.synk.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

//adding  JsonIgnoreProperties for now as Caching to redis causing json searlization issue for these fields.
@JsonIgnoreProperties({ "enabled", "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "authorities" })
public class CustomUserDetails implements UserDetails {
    private String username;// setting phone no in this field
    private String password;
    private String name;
    private String email;
    private String userRoles;
    private String profilePictureUrl;
    private String id;
    @JsonIgnore
    private Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails() {
    }

    // -- Builder ---------------------------------------------------------------

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String username;
        private String password;
        private String name;
        private String email;
        private String profilePictureUrl;
        private String id;
        private Collection<? extends GrantedAuthority> authorities;

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder profilePictureUrl(String url) {
            this.profilePictureUrl = url;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder authorities(Collection<? extends GrantedAuthority> authorities) {
            this.authorities = authorities;
            return this;
        }

        public CustomUserDetails build() {
            CustomUserDetails details = new CustomUserDetails();
            details.id = this.id;
            details.username = this.username;
            details.password = this.password;
            details.authorities = this.authorities;
            details.name = this.name;
            details.email = this.email;
            details.profilePictureUrl = this.profilePictureUrl;
            details.userRoles = this.authorities != null
                    ? this.authorities.stream().map(GrantedAuthority::getAuthority)
                            .reduce((a, b) -> a + ";" + b).orElse("")
                    : "";
            return details;
        }
    }

    // -- constructors ---------------------------------------------------------
    // this is using in test
    public CustomUserDetails(String username, String name, String email, String userRole, String profilePictureUrl,
            Collection<? extends GrantedAuthority> authorities) {
        this.username = username;
        this.name = name;
        this.email = email;
        this.profilePictureUrl = profilePictureUrl;
        this.userRoles = userRole;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    // custom attributes

    public String getName() {
        return name;
    }

    public String getUserRoles() {
        return userRoles;
    }

    public String getEmail() {
        return email;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public String getId() {
        return id;
    }
}
