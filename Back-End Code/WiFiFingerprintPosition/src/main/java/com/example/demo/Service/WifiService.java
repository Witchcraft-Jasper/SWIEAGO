package com.example.demo.Service;

import com.example.demo.Entity.Wifi;
import com.example.demo.Repository.WifiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

/**
 * @author Ireland
 */

@Transactional
@Service
public class WifiService {
    @Autowired
    private WifiRepository wifiRepository;

    public Wifi findById(int id)
    {
        return wifiRepository.findById(id);
    }
}