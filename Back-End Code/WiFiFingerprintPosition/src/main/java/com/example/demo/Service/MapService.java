package com.example.demo.Service;

import com.example.demo.Position.Map;
import com.example.demo.Repository.MapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;

/**
 * @author Ireland
 */

@Transactional
@Service
public class MapService {
    @Autowired
    private MapRepository mapRepository;

    public Iterable<Map> getAllMap() {
        return mapRepository.findAll();
    }
}
