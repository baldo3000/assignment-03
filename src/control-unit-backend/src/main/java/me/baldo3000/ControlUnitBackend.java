package me.baldo3000;

import me.baldo3000.engine.api.Engine;
import me.baldo3000.engine.impl.EngineImpl;

public class ControlUnitBackend {
    public static void main(String[] args) {
        final Engine engine = new EngineImpl();
        engine.initialize();
        engine.mainLoop();
    }
}