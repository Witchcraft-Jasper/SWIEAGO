package com.example.demo.Entity;

import com.example.demo.Controller.WifiController;
import com.example.demo.Position.Map;
import com.example.demo.Service.MapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.util.HashMap;

/**
 * @author Ireland
 */

@Component
public class MapData{
    public static HashMap<String,Integer>[] Hmap = new HashMap[WifiController.REGION];
    public static HashMap<Integer,String>[] pamH = new HashMap[WifiController.REGION];

    @Autowired
    private MapService mapService;

    public static HashMap<String,Integer>[] getHmap(){
        return Hmap;
    }
    public static HashMap<Integer,String>[] getPamH(){
        return pamH;
    }



    @PostConstruct
    public void readData(){
        //首先初始化map数组
        for(int i = 0;i < WifiController.REGION; ++i){
            Hmap[i] = new HashMap<>(WifiController.NUM);
            pamH[i] = new HashMap<>(WifiController.NUM);
        }
        //获取数据库中所以map
        Iterable<Map> allMap = mapService.getAllMap();
        //计数用
        int region = 0, num = 0;
        for (Map map : allMap) {
            Hmap[region].put(map.getMAC(), map.getId());
            pamH[region].put(map.getId(), map.getMAC());
            num++;
            if(num == WifiController.NUM) {
                num = 0;
                ++region;
            }
        }
    }
}
