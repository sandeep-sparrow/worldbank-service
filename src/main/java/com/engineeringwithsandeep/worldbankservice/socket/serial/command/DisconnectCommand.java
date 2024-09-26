package com.engineeringwithsandeep.worldbankservice.socket.serial.command;

public class DisconnectCommand extends Command {

    public DisconnectCommand(String[] command) {
        super(command);
    }

    @Override
    public String execute() {
        return "Goodbye! ";
    }
}
