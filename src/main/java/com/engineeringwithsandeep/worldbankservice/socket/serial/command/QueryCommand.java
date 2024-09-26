package com.engineeringwithsandeep.worldbankservice.socket.serial.command;

import com.engineeringwithsandeep.worldbankservice.socket.serial.service.WorldBankServiceImpl;

public class QueryCommand extends Command {

    private final WorldBankServiceImpl service;

    public QueryCommand(String[] command, WorldBankServiceImpl service) {
        super(command);
        this.service = service;
    }

    @Override
    public String execute() {
        if (command.length == 3) {
            return service.getCountryInfo(command[0],command[1],command[2]);
        } else if (command.length == 4) {
            try {
                return service.getCountryInfo(command[1], command[2], command[3]);
            } catch (Exception e) {
                return "Error;Bad Command";
            }
        } else {
            return "Wrong number of arguments";
        }
    }
}
