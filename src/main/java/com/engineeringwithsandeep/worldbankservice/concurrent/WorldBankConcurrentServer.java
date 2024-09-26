package com.engineeringwithsandeep.worldbankservice.concurrent;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.OptionalDouble;
import java.util.concurrent.*;

import static com.engineeringwithsandeep.worldbankservice.concurrent.WDIConcurrentDao.database;


@RestController
@RequestMapping("/wdi/async")
@AllArgsConstructor
public class WorldBankConcurrentServer {

    private final WorldBankConcurrentServiceImpl worldBankService;

    @GetMapping("/info/{countryCode}/{indicatorCode}/{year}")
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
class WDIConcurrentDao {
    public static ConcurrentHashMap<String, CountryData> database = new ConcurrentHashMap<>(); // Store CSV data in memory

    // Load data from CSV only once
    @Async
    public Future<ConcurrentHashMap<String, CountryData>> loadAsync() {
        if (database.isEmpty()) {
            System.out.println("Loading data from CSV...");
            CsvReader.readCsv("src/main/resources/WDICSV.csv");
        }
        return new AsyncResult<>(database);
    }

    public static void save(CountryData countryData) {
        if (database == null) {
            database = new ConcurrentHashMap<>();
        }
        // inorder to main unique key's for each country code - a key with combination of countryCode and indicatorCode is used
        database.put(countryData.getCountryCode() + "_" + countryData.getIndicatorCode(), countryData);
    }
}

/**
 * general contract / interface for commands / request
 * @author : EngineeringWithSandeep
 */
interface WorldBankConcurrentService {
    String getCountryInfo(String countryCode, String indicatorCode, Integer year);
    String getCountryReport(String countryCode, String indicatorCode);
}

/**
 * This is service class used for access database and implementing business logic
 * @author : EngineeringWithSandeep
 */
@Service
@AllArgsConstructor
class WorldBankConcurrentServiceImpl implements WorldBankConcurrentService {

    private final WDIConcurrentDao wdiDao;

    @Override
    public String getCountryInfo(String countryCode, String indicatorCode, Integer year) {
        try {
            // Wait for the async load to finish if it's not already loaded
            Future<ConcurrentHashMap<String, CountryData>> futureDatabase = getDatabase();
            ConcurrentHashMap<String, CountryData> database = futureDatabase.get();  // Blocking call until it's done

            CountryData countryData = database.get(countryCode + "_" + indicatorCode);
            String countryName = countryData.getCountryName();
            String indicatorName = countryData.getIndicatorName();
            Double data = countryData.getYearData().get(year);
            return String.format("Country: %s had %s of %s in year %s", countryName, indicatorName, data, year);
        } catch (Exception e) {
            return "Error fetching data: " + e.getMessage();
        }
    }

    @Override
    public String getCountryReport(String countryCode, String indicatorCode) {
        try {
            Future<ConcurrentHashMap<String, CountryData>> futureDatabase = getDatabase();
            ConcurrentHashMap<String, CountryData> database = futureDatabase.get();

            CountryData countryData = database.get(countryCode + "_" + indicatorCode);
            String countryName = countryData.getCountryName();
            String indicatorName = countryData.getIndicatorName();
            OptionalDouble optionalDouble = calculateMean(countryName, indicatorCode);
            return String.format("Country: %s has %s with an average of %s from 1960 to 2024",
                    countryName, indicatorName, optionalDouble.isPresent() ? optionalDouble.getAsDouble() : 0);
        } catch (Exception e) {
            return "Error fetching report: " + e.getMessage();
        }
    }

    // Helper method
    private Future<ConcurrentHashMap<String, CountryData>> getDatabase() {
        return wdiDao.loadAsync();
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
                WDIConcurrentDao.save(countryData);
            }
        } catch (IOException | CsvValidationException e) {
            System.out.println(e.getMessage());
        }
    }
}

@Configuration
class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }
}