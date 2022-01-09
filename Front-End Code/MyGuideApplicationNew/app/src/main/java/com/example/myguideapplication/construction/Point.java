package com.example.myguideapplication.construction;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter

@NoArgsConstructor
public class Point {
    private final static ObjectMapper mapper = new ObjectMapper();
    private static List<Point> points=new ArrayList<>();
    private static Point point=new Point();
    private static int judge;
    private int id;
    private int Floor;
    private String name;
    private int x;
    private int y;
    private int Value;
    private String info;
    private byte[] img;

    public static void setPoint(Point p) {
        point=p;
    }
    public static Point getPoint()
    {
        return point;
    }

    public static void setPoints(ArrayList al) {
        points.clear();
        for (int i = 0; i <al.size(); i++) {

            Point p=mapper.convertValue(al.get(i),Point.class);
            points.add(p);
            System.out.println(points.get(i).x+points.get(i).y+points.get(i).Floor+points.get(i).name);
        }
    }
    public static List<Point> getPoints(){
        return points;
    }
    public static void setJudge(int i){judge=i;}
    public static int getJudge(){return judge;}
}
