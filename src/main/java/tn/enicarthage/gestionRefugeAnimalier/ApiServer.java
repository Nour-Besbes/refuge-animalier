package tn.enicarthage.gestionRefugeAnimalier;

import com.sun.net.httpserver.HttpServer;

import tn.enicarthage.gestionRefugeAnimalier.router.Router;

import java.io.IOException;
import java.net.InetSocketAddress;

/** Cree le serveur HTTP integre au JDK et branche toutes les routes sur le Router. */
public class ApiServer {

    private final HttpServer server;

    public ApiServer(int port) throws IOException {
        Router router = new Router();
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.createContext("/", router::dispatcher);
        this.server.setExecutor(null);
    }

    public void demarrer() {
        server.start();
    }

    public void arreter() {
        server.stop(0);
    }
}
