package nl.utwente.star.message.application;

import nl.utwente.star.message.Message;

/// FAULT-01
public class Fault extends Message {
    @Override
    public String toString() {
        return "Fault";
    }
}
