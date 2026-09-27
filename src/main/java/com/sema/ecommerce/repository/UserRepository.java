package com.sema.ecommerce.repository;

import com.sema.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository< User,Long> {
}
