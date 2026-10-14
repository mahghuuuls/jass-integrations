package com.mahghuuuls.jassintegrations.client;

import com.mahghuuuls.jassintegrations.CommonProxy;
import com.mahghuuuls.jassintegrations.integration.IntegrationGate;

/** Client start-up: the client parts of every running integration. */
public class ClientProxy extends CommonProxy {

    @Override
    public void startClientParts() {
        IntegrationGate.startClientParts();
    }
}
