package oop1.challenge;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
  private static final int PORT = 8080;
  private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
  private final Map<String, Set<String>> groups = new ConcurrentHashMap<>();
  private final ExecutorService threadPool = Executors.newCachedThreadPool();
  private ServerSocket serverSocket;

  public static void main(String[] args) {
    new ChatServer().start();
  }

  public void start() {
    try {
      serverSocket = new ServerSocket(PORT);
      System.out.println("Chat Server started on port " + PORT);
      System.out.println("Use 'nc localhost 8080' to connect or run ChatClient");

      while (true) {
        Socket clientSocket = serverSocket.accept();
        ClientHandler clientHandler = new ClientHandler(clientSocket, this);
        threadPool.execute(clientHandler);
        System.out.println("New client connected: " + clientSocket.getRemoteSocketAddress());
      }
    } catch (IOException e) {
      System.err.println("Server error: " + e.getMessage());
    }
  }

  public synchronized String registerUser(String username, ClientHandler handler) {
    if (username == null || !username.matches("^[a-zA-Z0-9_]{1,20}$")) {
      return "401 ERROR INVALID_USERNAME_FORMAT";
    }

    if (clients.containsKey(username)) {
      return "400 ERROR USERNAME_ALREADY_EXISTS";
    }

    clients.put(username, handler);
    handler.setUsername(username);
    System.out.println("User registered: " + username);
    return "200 OK REGISTERED " + username;
  }

  public synchronized void unregisterUser(String username) {
    if (username != null) {
      clients.remove(username);
      // 全グループから削除
      for (Set<String> groupMembers : groups.values()) {
        groupMembers.remove(username);
      }
      System.out.println("User unregistered: " + username);
    }
  }

  public synchronized String joinGroup(String username, String groupName) {
    if (username == null) {
      return "403 ERROR NOT_REGISTERED";
    }

    groups.computeIfAbsent(groupName, k -> ConcurrentHashMap.newKeySet());
    Set<String> groupMembers = groups.get(groupName);

    if (groupMembers.contains(username)) {
      return "402 ERROR ALREADY_JOINED " + groupName;
    }

    groupMembers.add(username);
    System.out.println("User " + username + " joined group " + groupName);
    return "200 OK JOINED " + groupName;
  }

  public synchronized String leaveGroup(String username, String groupName) {
    if (username == null) {
      return "403 ERROR NOT_REGISTERED";
    }

    Set<String> groupMembers = groups.get(groupName);
    if (groupMembers == null || !groupMembers.contains(username)) {
      return "404 ERROR NOT_IN_GROUP " + groupName;
    }

    groupMembers.remove(username);
    System.out.println("User " + username + " left group " + groupName);
    return "200 OK LEFT " + groupName;
  }

  public synchronized String broadcastMessage(String sender, String message) {
    if (sender == null) {
      return "403 ERROR NOT_REGISTERED";
    }

    String broadcastMsg = "100 INFO BROADCAST_MESSAGE " + sender + " " + message;
    for (ClientHandler client : clients.values()) {
      client.sendMessage(broadcastMsg);
    }

    System.out.println("Broadcast from " + sender + ": " + message);
    return "200 OK MESSAGE_SENT";
  }

  public synchronized String sendGroupMessage(String sender, String groupName, String message) {
    if (sender == null) {
      return "403 ERROR NOT_REGISTERED";
    }

    Set<String> groupMembers = groups.get(groupName);
    if (groupMembers == null || !groupMembers.contains(sender)) {
      return "404 ERROR NOT_IN_GROUP " + groupName;
    }

    String groupMsg = "100 INFO GROUP_MESSAGE " + groupName + " " + sender + " " + message;
    for (String memberName : groupMembers) {
      ClientHandler client = clients.get(memberName);
      if (client != null) {
        client.sendMessage(groupMsg);
      }
    }

    System.out.println("Group message in " + groupName + " from " + sender + ": " + message);
    return "200 OK GROUP_MESSAGE_SENT";
  }

  public synchronized String getStatus(String username) {
    if (username == null) {
      return "403 ERROR NOT_REGISTERED";
    }

    StringBuilder status = new StringBuilder();
    status.append("100 INFO STATUS Connected as: ").append(username).append("\n");
    status.append("100 INFO STATUS Online users: ").append(clients.keySet()).append("\n");
    status.append("100 INFO STATUS Joined groups: ");

    List<String> userGroups = new ArrayList<>();
    for (Map.Entry<String, Set<String>> entry : groups.entrySet()) {
      if (entry.getValue().contains(username)) {
        userGroups.add(entry.getKey());
      }
    }
    status.append(userGroups);

    return status.toString();
  }
}

class ClientHandler implements Runnable {
  private final Socket socket;
  private final ChatServer server;
  private final BufferedReader reader;
  private final PrintWriter writer;
  private String username;

  public ClientHandler(Socket socket, ChatServer server) throws IOException {
    this.socket = socket;
    this.server = server;
    this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    this.writer = new PrintWriter(socket.getOutputStream(), true);
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void sendMessage(String message) {
    writer.println(message);
  }

  @Override
  public void run() {
    try {
      writer.println("100 INFO CONNECTED Welcome to Chat Server");
      writer
          .println("100 INFO HELP Available commands: REGISTER, BROADCAST, JOIN, LEAVE, GROUPCAST, STATUS, QUIT, HELP");
      writer.print("> ");
      writer.flush();

      String inputLine;
      while ((inputLine = reader.readLine()) != null) {
        String response = processCommand(inputLine.trim());
        if (response != null) {
          if (response.contains("\n")) {
            // 複数行の応答の場合
            String[] lines = response.split("\n");
            for (String line : lines) {
              writer.println(line);
            }
          } else {
            writer.println(response);
          }
        }

        if (inputLine.trim().equals("QUIT")) {
          break;
        }

        writer.print("> ");
        writer.flush();
      }
    } catch (IOException e) {
      System.err.println("Client handler error: " + e.getMessage());
    } finally {
      cleanup();
    }
  }

  private String processCommand(String input) {
    if (input.isEmpty()) {
      return null;
    }

    String[] parts = parseCommand(input);
    String command = parts[0].toUpperCase();

    switch (command) {
      case "REGISTER":
        if (parts.length != 2) {
          return "400 ERROR INVALID_COMMAND_FORMAT";
        }
        return server.registerUser(parts[1], this);

      case "BROADCAST":
        if (parts.length < 2) {
          return "400 ERROR INVALID_COMMAND_FORMAT";
        }
        String broadcastMsg = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
        return server.broadcastMessage(username, broadcastMsg);

      case "JOIN":
        if (parts.length != 2) {
          return "400 ERROR INVALID_COMMAND_FORMAT";
        }
        return server.joinGroup(username, parts[1]);

      case "LEAVE":
        if (parts.length != 2) {
          return "400 ERROR INVALID_COMMAND_FORMAT";
        }
        return server.leaveGroup(username, parts[1]);

      case "GROUPCAST":
        if (parts.length < 3) {
          return "400 ERROR INVALID_COMMAND_FORMAT";
        }
        String groupMsg = String.join(" ", Arrays.copyOfRange(parts, 2, parts.length));
        return server.sendGroupMessage(username, parts[1], groupMsg);

      case "STATUS":
        return server.getStatus(username);

      case "QUIT":
        return "200 OK GOODBYE";

      case "HELP":
        return "100 INFO HELP Commands:\n" +
            "100 INFO HELP   REGISTER <username> - Register your username\n" +
            "100 INFO HELP   BROADCAST <message> - Send message to all users\n" +
            "100 INFO HELP   JOIN <group> - Join a group\n" +
            "100 INFO HELP   LEAVE <group> - Leave a group\n" +
            "100 INFO HELP   GROUPCAST <group> <message> - Send message to group\n" +
            "100 INFO HELP   STATUS - Show your current status\n" +
            "100 INFO HELP   QUIT - Disconnect from server";

      default:
        return "400 ERROR UNKNOWN_COMMAND";
    }
  }

  private String[] parseCommand(String input) {
    List<String> parts = new ArrayList<>();
    boolean inQuotes = false;
    StringBuilder currentPart = new StringBuilder();

    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);

      if (c == '"' && (i == 0 || input.charAt(i - 1) != '\\')) {
        inQuotes = !inQuotes;
      } else if (c == ' ' && !inQuotes) {
        if (currentPart.length() > 0) {
          parts.add(currentPart.toString());
          currentPart = new StringBuilder();
        }
      } else {
        currentPart.append(c);
      }
    }

    if (currentPart.length() > 0) {
      parts.add(currentPart.toString());
    }

    return parts.toArray(new String[0]);
  }

  private void cleanup() {
    try {
      server.unregisterUser(username);
      socket.close();
    } catch (IOException e) {
      System.err.println("Error closing socket: " + e.getMessage());
    }
  }
}
