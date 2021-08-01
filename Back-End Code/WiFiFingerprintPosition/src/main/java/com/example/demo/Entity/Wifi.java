package com.example.demo.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * @author Ireland
 */
@Table(name = "wifi")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Wifi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int region;
    private int RSSI1;
    private int RSSI2;
    private int RSSI3;
    private int RSSI4;
    private int RSSI5;
    private int RSSI6;
    private int RSSI7;
    private int RSSI8;
    private int RSSI9;
    private int RSSI10;
    private int x;
    private int y;
    public int getRSSI(int index){
        int rssi = 0;
        switch (index){
            case 1:
                rssi = RSSI1;
                break;
            case 2:
                rssi = RSSI2;
                break;
            case 3:
                rssi = RSSI3;
                break;
            case 4:
                rssi = RSSI4;
                break;
            case 5:
                rssi = RSSI5;
                break;
            case 6:
                rssi = RSSI6;
                break;
            case 7:
                rssi = RSSI7;
                break;
            case 8:
                rssi = RSSI8;
                break;
            case 9:
                rssi = RSSI9;
                break;
            case 10:
                rssi = RSSI10;
                break;
            default:
                break;
        }
        return rssi;
    }
}

