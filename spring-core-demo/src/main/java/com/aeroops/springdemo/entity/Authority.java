package com.aeroops.springdemo.entity;

import jakarta.persistence.*;

import java.util.List;
import java.util.Set;

@Entity
public class Authority
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long authorityId;
    @Column(unique = true, nullable = false)
    private String authorityName;
    @ManyToMany(mappedBy = "authorities")
    private List<Role> roles;

    public Long getAuthorityId() {
        return authorityId;
    }

    public void setAuthorityId(Long authorityId) {
        this.authorityId = authorityId;
    }

    public String getAuthorityName() {
        return authorityName;
    }

    public void setAuthorityName(String authorityName) {
        this.authorityName = authorityName;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }
}
