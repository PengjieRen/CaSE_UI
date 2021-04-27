package com.sdu.irlab.chatlabelling.datasource.domain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
public class User extends BaseEntity implements UserDetails {
    @Column(unique = true)
    private String name;
    private String password;

    private String role;
    private int connectionCount;

    @Lob
    private String blackList;


    @OneToMany(mappedBy = "sysUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Conversation> sysConversation;

    @OneToMany(mappedBy = "cusUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Conversation> cusConversation;

    @Lob
    private String profile;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Conversation> getSysConversation() {
        return sysConversation;
    }

    public void setSysConversation(List<Conversation> sysConversation) {
        this.sysConversation = sysConversation;
    }

    public List<Conversation> getCusConversation() {
        return cusConversation;
    }

    public void setCusConversation(List<Conversation> cusConversation) {
        this.cusConversation = cusConversation;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> al = new ArrayList<GrantedAuthority>();
        al.add(new SimpleGrantedAuthority("User"));
        return al;
    }

    @Override
    public String getUsername() {
        return this.name;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return user.getId() == this.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public int getConnectionCount() {
        return connectionCount;
    }

    public void setConnectionCount(int connectionCount) {
        this.connectionCount = connectionCount;
    }

    public String getBlackList() {
        return blackList;
    }

    public void setBlackList(String blackList) {
        this.blackList = blackList;
    }
}