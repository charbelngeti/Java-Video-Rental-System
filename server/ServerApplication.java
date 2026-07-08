package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Main application class responsible for initializing the RMI Server Registry
 * space.
 */
public class ServerApplication {
  public static void main(String[] args) {
    try {
      // CRITICAL: Update this to match your actual host machine's Wi-Fi / Ethernet IP
      // address
      System.setProperty("java.rmi.server.hostname", "10.75.126.75");

      // Instantiating the service implementation
      VidRentalServiceImpl service = new VidRentalServiceImpl();

      // Open the RMI Registry locally on default communication port 1099
      Registry registry = LocateRegistry.createRegistry(1099);

      // Bind the remote object instance to the public registration name
      registry.rebind("VideoLibraryService", service);

      System.out.println(">>> VLS Server Application status: ONLINE and listening on port 1099...");
      System.out.println("Provide your local IP address to your group members so they can connect!");

    } catch (Exception e) {
      System.err.println("Server bootstrap runtime failure occurred:");
      e.printStackTrace();
    }
  }
}