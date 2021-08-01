package com.example.demo.Controller;

import com.example.demo.Entity.Coordinate;
import com.example.demo.Entity.MapData;
import com.example.demo.Position.FingerData;
import com.example.demo.Entity.Wifi;
import com.example.demo.Position.Similarity;
import com.example.demo.Response.ResponseCode;
import com.example.demo.Response.ResponseBody;
import com.example.demo.Service.WifiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

/**
 * @author Ireland
 */
@RestController
@RequestMapping(path = "/Wifi")
public class WifiController {
    //每组MAC的数量
    public static final int NUM = 10;
    //设定的Region数量
    public static final int REGION = 20;
    int times = 1;
    @Autowired
    private WifiService wifiService;

    @PostMapping(path = "/get")
    public ResponseBody findById(@RequestBody List<FingerData> fingerData) {
        times++;
        //获取预读取的对应关系
        HashMap<String, Integer>[] hashMap = MapData.getHmap();
        HashMap<Integer, String>[] paMhsah = MapData.getPamH();

        //接收WIFI信息的二维数组的创建及初始化
        FingerData[][] receives = new FingerData[REGION][NUM];
        for (int i = 0; i < REGION; ++i) {
            receives[i] = new FingerData[NUM];
            for (int j = 0; j < NUM; ++j) {
                receives[i][j] = new FingerData(0, paMhsah[i].get(i * NUM + j + 1));
            }
        }
        //按照map中的对应关系给receives中对应MAC的RSSI赋值
        for (FingerData fingerDatum : fingerData) {
            for (int i = 0; i < REGION; ++i) {
                if (hashMap[i].containsKey(fingerDatum.getMAC())) {
                    receives[i][(hashMap[i].get(fingerDatum.getMAC()) - 1) % 10].setMAC(fingerDatum.getMAC());
                    receives[i][(hashMap[i].get(fingerDatum.getMAC()) - 1) % 10].setRSSI(fingerDatum.getRSSI());
                }
            }
        }

        //存储数据库中WIFI信息的数组的创建
        FingerData[] storage = new FingerData[NUM];
        //WIFI引用
        Wifi wifi;
        //总相似度
        Similarity s = new Similarity();
        //递增序列号
        int id = 1;
        //查询到最后一行时，跳出循环
        while (true) {
            wifi = wifiService.findById(id);
            if (wifi == null) {
                break;
            }
            //给每一组(行)storage赋值
            for (int i = 0; i < NUM; i++) {
                storage[i] = new FingerData(wifi.getRSSI(i + 1), receives[wifi.getRegion() - 1][i].getMAC());
            }
            //计算相似度
            s.calSimilarity(receives[wifi.getRegion() - 1], storage, id);
            id++;
        }

        List<Integer> indexes = s.returnIndexes();
        int len = indexes.size();
        //坐标结果
        Coordinate c;
        //没找到
        if (len == 1 && indexes.get(0) == -1) {
            c = new Coordinate(-1, -1);
        } else {
            int[] Xs = new int[len], Ys = new int[len];
            for (int i = 0; i < len; ++i) {
                int index = indexes.get(i);
                wifi = wifiService.findById(index);
                Xs[i] = wifi.getX();
                Ys[i] = wifi.getY();
            }
            c = Similarity.knnRegression(Xs, Ys);
            System.out.println(c.getX() + "  Ho  " + c.getY());
        }
        String judge = c.getX() + "";
        return new ResponseBody(!"".equals(judge) ? ResponseCode.LOCATE_SUCCESS : ResponseCode.LOCATE_FAILED,
                !"".equals(judge) ? c : "");
    }
}


