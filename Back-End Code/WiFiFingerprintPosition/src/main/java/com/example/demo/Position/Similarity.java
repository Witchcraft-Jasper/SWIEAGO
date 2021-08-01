package com.example.demo.Position;

import com.example.demo.Controller.WifiController;
import com.example.demo.Entity.Coordinate;

import java.util.*;
import java.util.Map;

/**
 * @author Ireland
 */
public class Similarity {
    //设定的KNN算法的参数，选取K个最近邻
    public static final int K = 4;
    //wifi指纹库中每个区域的指纹数
    //public static final int[] N = new int[]{10,12,15,8,18,12,8,16,21,20,
    //12,20,14,9,7,9,9,7,9,9};
    //总指纹数
    public static final int N = 245;
    //N个位置相似度组成的数组
    private final Double[] Similarity = new Double[N];

    //getter and setter
    public void setSimilarity(double similarity, int index) {
        Similarity[index - 1] = similarity;
    }

    public Double[] getSimilarity() {
        return Similarity;
    }

    //计算相似度并赋值到数组中
    public void calSimilarity(FingerData[] a, FingerData[] b, int index) {
        //用N维向量夹角公式计算相似度
        int inner = 0, squaresA = 0, squaresB = 0;
        //计算内积和平方和
        for (int i = 0; i < WifiController.NUM; ++i) {
            inner += (a[i].getRSSI() * b[i].getRSSI());
            squaresA += Math.pow(a[i].getRSSI(), 2);
            squaresB += Math.pow(b[i].getRSSI(), 2);
        }
        double result = 0;
        if(squaresA != 0 && squaresB != 0)
            result = inner / (Math.sqrt(squaresA) * Math.sqrt(squaresB));
        setSimilarity(result, index);
    }

    //返回K个最近相似度的下标，以便在数据库中获取指定下标位置的(x,y)坐标
    public List<Integer> returnIndexes() {
        List<Integer> indexes = new ArrayList<>();
        Map<Double, Integer> map = new HashMap<>();
        //map存相似度和下标的对应关系
        for (int i = 0; i < N; ++i) {
            map.put(Similarity[i], i+1);
        }
        //升序排列
        Arrays.sort(Similarity);
        int count = 0;
        //取前K个最大相似度对应的索引存入数组
        for (int i = 0; i < N; ++i) {
            if(Similarity[N - 1 - i] == 0){
                break;
            }
            indexes.add(map.get(Similarity[N - 1 - i]));
            count++;
            if(count == K){
                break;
            }
        }
        //当没有任何合理的地方时，返回-1让客户端保持坐标不变
        if(count == 0){
            indexes.add(-1);
        }
        return indexes;
    }

    //KNN回归，将这K个指纹的位置坐标取平均，得到作为定位结果
    public static Coordinate knnRegression(int[] Xs, int[] Ys) {
        int len = Xs.length;
        int sumX = 0, sumY = 0;
        for (int i = 0; i < len; ++i) {
            //求和
            sumX += Xs[i];
            sumY += Ys[i];
        }
        return new Coordinate(sumX / len, sumY / len);
    }
}