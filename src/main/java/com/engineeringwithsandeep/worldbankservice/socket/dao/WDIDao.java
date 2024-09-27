package com.engineeringwithsandeep.worldbankservice.socket.dao;

import com.engineeringwithsandeep.worldbankservice.socket.util.CsvReader;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Data
@NoArgsConstructor
@Component
public class WDIDao {
    public static HashMap<String, CountryData> database = new HashMap<>(); // Store CSV data in memory

    // Load data from CSV only once
    public HashMap<String, CountryData> load() {
        if (database.isEmpty()) {
            System.out.println("Loading data from CSV...");
            CsvReader.readCsv("src/main/resources/WDICSV.csv");
        }
        return database;
    }

    public static void save(CountryData countryData) {
        if (database == null) {
            database = new HashMap<>();
        }
        // inorder to main unique key's for each country code - a key with combination of countryCode and indicatorCode is used
        database.put(countryData.getCountryCode() + "_" + countryData.getIndicatorCode(), countryData);
    }

    public void clear() {
        database.clear();
    }
}
