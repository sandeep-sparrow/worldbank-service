package com.engineeringwithsandeep.worldbankservice.socket.serial.util;

import com.engineeringwithsandeep.worldbankservice.socket.serial.dao.CountryData;
import com.engineeringwithsandeep.worldbankservice.socket.serial.dao.WDIDao;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.IOException;

/**
 * This is utility class for reading CSV file
 * @author : EngineeringWithSandeep
 */
public class CsvReader {

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
