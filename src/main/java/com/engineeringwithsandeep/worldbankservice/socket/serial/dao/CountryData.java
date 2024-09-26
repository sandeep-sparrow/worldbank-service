package com.engineeringwithsandeep.worldbankservice.socket.serial.dao;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;

/**
 * This is Datas access object class which holds a single records information for WDI
 * @author : EngineeringWithSandeep
 */
@Data
@AllArgsConstructor
public class CountryData {
    private String countryName;
    private String countryCode;
    private String indicatorName;
    private String indicatorCode;
    private HashMap<Integer, Double> yearData = new HashMap<>(); // HasMap to maintain <Year, Data>

    public CountryData(String countryName, String countryCode, String indicatorName, String indicatorCode) {
        this.countryName = countryName;
        this.countryCode = countryCode;
        this.indicatorName = indicatorName;
        this.indicatorCode = indicatorCode;
    }
}
