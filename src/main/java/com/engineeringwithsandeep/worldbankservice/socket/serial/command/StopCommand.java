package com.engineeringwithsandeep.worldbankservice.socket.serial.command;

public class StopCommand extends Command {

    public StopCommand(String[] command) {
        super(command);
    }

    @Override
    public String execute() {
        return "Server Stopped";
    }
}
