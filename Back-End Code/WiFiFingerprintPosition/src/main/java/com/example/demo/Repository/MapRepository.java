package com.example.demo.Repository;

import com.example.demo.Position.Map;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Witchcraft
 */
@Repository
public interface MapRepository extends CrudRepository<Map, Integer> {
}