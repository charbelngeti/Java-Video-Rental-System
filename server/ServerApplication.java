package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Main application class responsible for initialization of the RMI Server
 * Registry space.
 */
public class ServerApplication {
  public static void main(String[] args) {
    try {
      // CRITICAL STEP: Replace with your actual local machine's local Wi-Fi /
      // Ethernet IP address
      // This tells RMI how to direct incoming client data requests back to your
      // computer.
      System.setProperty("java.rmi.server.hostname", "192.168.1.100");

      // Instantiating the implementation class
      VidRentalServiceImpl service = new VidRentalServiceImpl();

      // Open the RMI Registry locally on default communication port 1099
      Registry registry = LocateRegistry.createRegistry(1099);

      // Bind the remote object instance to the public registration name
      registry.rebind("VideoLibraryService", service);

      System.out.println(">>> VLS Server Application status: ONLINE and listening on port 1099...");
      System.out.println("Provide your local IP address to your group members so they can look up this registry!");

    } catch (Exception e) {
      System.err.println("Server bootstrap runtime failure occurred:");
      e.printStackTrace();
    }
  }
}