package com.example.demo.Repository;

import com.example.demo.Entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Witchcraft
 */
@Repository
public interface UserRepository extends CrudRepository<User,Integer>
{
    @Query(value = "select * from user where username = ?1 and password = ?2",nativeQuery = true)
    User findByUsernameAndPassword(String username, String password);

    boolean existsByUsername(String username);
}