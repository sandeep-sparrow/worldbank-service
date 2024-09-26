package com.engineeringwithsandeep.worldbankservice.socket.serial.command;

public abstract class Command {

    protected final String[] command;

    public Command(String[] command) {
        this.command = command;
    }

    public abstract String execute();
}
