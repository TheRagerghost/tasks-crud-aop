package com.avragerghost.tasks_crud_aop.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.avragerghost.tasks_crud_aop.models.User;

public interface UserRepository extends JpaRepository<User, Long> {

}
