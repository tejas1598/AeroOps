package com.aeroops.springdemo.repository;

import com.aeroops.springdemo.entity.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Long>
{
    public Optional<Authority> findByAuthorityName(String authorityName);
}
