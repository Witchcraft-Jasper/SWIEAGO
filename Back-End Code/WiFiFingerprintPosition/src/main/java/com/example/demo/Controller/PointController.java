package com.example.demo.Controller;

import com.example.demo.Response.ResponseCode;
import com.example.demo.Response.ResponseBody;
import com.example.demo.Service.PointService;
import com.example.demo.Entity.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * @author Witchcraft
 */
@RestController
@RequestMapping(path = "/Point")
public class PointController {
    @Autowired
    private PointService pointService;

    @GetMapping(path = "/get")
    public ResponseBody  findByName(@RequestParam(value = "name")String name)
    {
        List<Point> list = pointService.findByName(name);
        return new ResponseBody(!list.isEmpty()?ResponseCode.QUERY_SUCCESS:ResponseCode.QUERY_FAILED,!list.isEmpty()?list:"");
    }
    @GetMapping(path = "/get1")
    public ResponseBody  findByFloor(@RequestParam int floor)
    {
        List<Point> list = pointService.findByFloor(floor);
        return new ResponseBody(!list.isEmpty()?ResponseCode.QUERY_SUCCESS:ResponseCode.QUERY_FAILED,!list.isEmpty()?list:"");
    }
}
