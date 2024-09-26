package com.engineeringwithsandeep.worldbankservice.serial;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.OptionalDouble;

import static com.engineeringwithsandeep.worldbankservice.serial.WDIDao.database;

/**
 * This is server level, controller call which makes HTTP call's
 * @author : EngineeringWithSandeep
 */
@RestController
@RequestMapping("/wdi")
@AllArgsConstructor
public class WorldBankServer {

    private final WorldBankServiceImpl worldBankService;

    @GetMapping("/{countryCode}/{indicatorCode}/{year}")
    public ResponseEntity<String> getCountryInfo(@PathVariable String countryCode,
                                                 @PathVariable String indicatorCode,
                                                 @PathVariable String year) {
        String response = worldBankService.getCountryInfo(countryCode, indicatorCode, Integer.parseInt(year));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/report/{countryCode}/{indicatorCode}/")
    public ResponseEntity<String> getCountryReport(@PathVariable String countryCode,
                                                 @PathVariable String indicatorCode) {
        String response = worldBankService.getCountryReport(countryCode, indicatorCode);
        return ResponseEntity.ok(response);
    }
}

/**
 * This is Datas access object class which holds a single records information for WDI
 * @author : EngineeringWithSandeep
 */
@Data
@AllArgsConstructor
class CountryData {
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

/**
 * This class holds in-memory database for WDI csv data
 * @author : EngineeringWithSandeep
 */
@Data
@NoArgsConstructor
@Component
class WDIDao {
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
}

/**
 * general contract / interface for commands / request
 * @author : EngineeringWithSandeep
 */
interface WorldBankService {
    String getCountryInfo(String countryCode, String indicatorCode, Integer year);
    String getCountryReport(String countryCode, String indicatorCode);
}

/**
 * This is service class used for access database and implementing business logic
 * @author : EngineeringWithSandeep
 */
@Service
@AllArgsConstructor
class WorldBankServiceImpl implements WorldBankService {

    private final WDIDao wdiDao;

    @Override
    public String getCountryInfo(String countryCode, String indicatorCode, Integer year) {
        CountryData countryData = getDataHashMap().get(countryCode + "_" + indicatorCode);
        String countryName = countryData.getCountryName();
        String indicatorName = countryData.getIndicatorName();
        Double data = countryData.getYearData().get(year);
        return String.format("country: %s has %s had %s in year %s", countryName, indicatorName, data, year);
    }

    @Override
    public String getCountryReport(String countryCode, String indicatorCode) {
        CountryData countryData = getDataHashMap().get(countryCode + "_" + indicatorCode);
        String countryName = countryData.getCountryName();
        String indicatorName = countryData.getIndicatorName();
        OptionalDouble optionalDouble = calculateMean(countryName, indicatorCode);
        return String.format("country: %s has %s with mean average of %s percentage over the year 1960-2024", countryName, indicatorName,
                optionalDouble.isPresent() ? optionalDouble.getAsDouble() : 0);
    }

    // Helper method
    private HashMap<String, CountryData> getDataHashMap() {
        return wdiDao.load();
    }

    // Method to calculate mean value of a given indicator for a specific country
    public static OptionalDouble calculateMean(String countryName, String indicatorCode) {
        for (CountryData data : database.values()) {
            if (data.getCountryName().equalsIgnoreCase(countryName) &&
                    data.getIndicatorCode().equalsIgnoreCase(indicatorCode)) {

                // Extract all the valid year values
                return data.getYearData().values().stream()
                        .mapToDouble(Double::doubleValue)
                        .average();  // Calculate the mean
            }
        }
        return OptionalDouble.empty(); // Return empty if country or indicator not found
    }
}

/**
 * This is utility class for reading CSV file
 * @author : EngineeringWithSandeep
 */
class CsvReader {

    public static void readCsv(String filePath) {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] header = reader.readNext();  // Read header (years)

            String[] line;
            while ((line = reader.readNext()) != null) {
                String countryName = line[0];
                String countryCode = line[1];
                String indicatorName = line[2];
                String indicatorCode = line[3];

                CountryData countryData = new CountryData(countryName, countryCode, indicatorName, indicatorCode);

                for (int i = 4; i < line.length; i++) {
                    double value;
                    int year = Integer.parseInt(header[i]);
                    if (line[i].isEmpty()) {
                        value = 0.0;
                    } else {
                        value = Double.parseDouble(line[i]);
                    }
                    countryData.getYearData().put(year, value);
                }
                // Here you can pass the `countryData` object to the DAO for storing
                WDIDao.save(countryData);
            }
        } catch (IOException | CsvValidationException e) {
            System.out.println(e.getMessage());
        }
    }
}