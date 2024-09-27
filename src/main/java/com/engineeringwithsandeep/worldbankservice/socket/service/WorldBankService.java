package com.engineeringwithsandeep.worldbankservice.socket.service;

/**
 * general contract / interface for commands / request
 *
 * @author : EngineeringWithSandeep
 */
public interface WorldBankService {
    String getCountryInfo(String countryCode, String indicatorCode, String year);

    String getCountryReport(String countryCode, String indicatorCode);
}
