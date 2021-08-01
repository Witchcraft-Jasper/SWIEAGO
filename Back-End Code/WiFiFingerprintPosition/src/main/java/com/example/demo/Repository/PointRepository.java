package com.example.demo.Repository;

import com.example.demo.Entity.Point;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Witchcraft
 */
@Repository
public interface PointRepository extends CrudRepository<Point, Integer> {
    @Query(value = "select * from point where point.name like CONCAT('%',?1,'%')", nativeQuery = true)
    List<Point> findByName(String name);

    @Query(value = "select * from point where Floor = ?1", nativeQuery = true)
    List<Point> findByFloor(int floor);
}