package com.engineeringwithsandeep.worldbankservice.socket.command;

import com.engineeringwithsandeep.worldbankservice.socket.service.WorldBankServiceImpl;

public class ReportCommand extends Command {

    private final WorldBankServiceImpl service;

    public ReportCommand(String[] command, WorldBankServiceImpl service) {
        super(command);
        this.service = service;
    }

    @Override
    public String execute() {
        return service.getCountryReport(command[1], command[2]);
    }
}
