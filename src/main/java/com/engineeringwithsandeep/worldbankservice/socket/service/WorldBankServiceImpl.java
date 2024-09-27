package com.engineeringwithsandeep.worldbankservice.socket.service;

import com.engineeringwithsandeep.worldbankservice.socket.dao.CountryData;
import com.engineeringwithsandeep.worldbankservice.socket.dao.WDIDao;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.OptionalDouble;
import static com.engineeringwithsandeep.worldbankservice.socket.dao.WDIDao.database;

/**
 * This is service class used for access database and implementing business logic
 * @author : EngineeringWithSandeep
 */
@Service
public class WorldBankServiceImpl implements WorldBankService {

    private final WDIDao wdiDao;

    public WorldBankServiceImpl(WDIDao wdiDao) {
        this.wdiDao = wdiDao;
    }

    @Override
    public String getCountryInfo(String countryCode, String indicatorCode, String year) {
        HashMap<String, CountryData> dataHashMap = wdiDao.load();
        CountryData countryData = dataHashMap.get(countryCode + "_" + indicatorCode);

        String countryName = countryData.getCountryName();
        String indicatorName = countryData.getIndicatorName();
        Double data = countryData.getYearData().get(Integer.parseInt(year));

        wdiDao.clear();

        return String.format("country -> %s has %s had %s in year %s", countryName, indicatorName, data, year);
    }

    @Override
    public String getCountryReport(String countryCode, String indicatorCode) {
        HashMap<String, CountryData> dataHashMap = wdiDao.load();
        CountryData countryData = dataHashMap.get(countryCode + "_" + indicatorCode);

        String countryName = countryData.getCountryName();
        String indicatorName = countryData.getIndicatorName();
        OptionalDouble optionalDouble = calculateMean(countryName, indicatorCode);

        wdiDao.clear();

        return String.format("country -> %s has %s with mean average of %s percentage over the year 1960-2024", countryName, indicatorName,
                optionalDouble.isPresent() ? optionalDouble.getAsDouble() : 0);
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