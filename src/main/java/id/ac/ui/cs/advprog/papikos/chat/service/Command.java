package id.ac.ui.cs.advprog.papikos.chat.service;

public interface Command {
    void execute();
    void undo();
}
