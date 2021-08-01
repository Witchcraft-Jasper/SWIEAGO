package com.example.demo.Service;
import com.example.demo.Entity.Point;
import com.example.demo.Repository.PointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

/**
 * @author Witchcraft
 */

@Transactional
@Service
public class PointService {
    @Autowired
    private PointRepository pointRepository;
    public List<Point> findByName(String name)
    {
        return pointRepository.findByName(name);
    }
    public List<Point> findByFloor(int floor)
    {
        return pointRepository.findByFloor(floor);
    }
}
