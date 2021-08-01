package com.example.demo.Repository;

import com.example.demo.Entity.Wifi;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Ireland
 */
@Repository
public interface WifiRepository extends CrudRepository<Wifi,Integer>
{
    @Query(value = "select * from wifi where id = ?1",nativeQuery = true)
    Wifi findById(int id);
}
