# CS5800 — Simple Chat Application
*A clean, extensible client-server chat system demonstrating multiple software design patterns.*

---

## 🚀 Overview
Simple Chat is a lightweight console-based messaging system supporting:

- User registration & login  
- Direct messaging between users  
- Server-based (channel) group messaging  
- Friend requests & acceptance  
- Blocking / unblocking users  
- Presence states (online / offline / away / busy)  
- Persistent storage with SQLite  
- A clean architecture leveraging multiple design patterns  
- Full **unit tests** and **integration tests** using Maven + JUnit  

---

# 🛠️ Building, Testing, and Running

## Requirements
- Java 17+  
- Maven 3.x  

---

## 1. Compile the Project

```bash
mvn clean compile
```

---

## 2. Run All Tests

```bash
mvn test
```

Run a specific test:

```bash
mvn -Dtest=ChatClientTest test
```

---

## 3. Run the Server

```bash
mvn exec:java@run-server
```

The server will listen on port **8888**.

---

## 4. Run the Client

You can directly run the client using:

```bash
java -cp target/classes ChatClient {userId} {username}
```

Examples:

```bash
java -cp target/classes ChatClient u1 Alice
java -cp target/classes ChatClient u2 Bob
```

---

## 5. Reset the Database (Optional)

```bash
rm chat.db   # macOS / Linux
del chat.db  # Windows
```

The schema will regenerate automatically when the server starts.

---
## 🌐 Remote Access (LAN & WAN)

This chat application fully supports remote connections, allowing clients to connect to a server running on another machine—either on the same local network (LAN) or over the internet (WAN).

---

## ✅ Running the Server
Start the server on the host machine:

```bash
java -cp target/classes ChatServer
```

The server listens on:

```
0.0.0.0:8888
```

This means it accepts connections from any network interface.

---

## 🔌 Connecting From Another Machine (Client)

Use the following syntax:

```bash
java -cp target/classes ChatClient <userId> <username> <serverHost>
```

### Example (LAN)

On any machine on the same Wi-Fi / Ethernet network:

```bash
java -cp target/classes ChatClient u1 Alice 192.168.1.105
```

Replace `192.168.1.105` with the LAN IP address of the server machine.

To find the server’s LAN IP:

- **Windows:** `ipconfig`
- **macOS/Linux:** `ifconfig` or `ip addr`

---

## 🌍 Connecting Over the Internet (WAN)

To allow clients outside your local network to connect:

### 1. Enable Port Forwarding
Forward **TCP port 8888** on your router to your server’s local IP.

### 2. Allow Firewall Access
On the server machine, allow inbound connections on port 8888:

- **Windows Firewall:** Add an inbound rule for `TCP 8888`
- **macOS/Linux:** Allow port in your firewall or security settings

### 3. Share Your Public IP
Find your public IP at:

```
https://whatismyip.com
```

---

## 🔒 Security Note
This project is designed for educational purposes and does not implement TLS/SSL or encrypted password storage.  
For real-world deployment, secure network traffic and credential handling are strongly recommended.

---

# 🏛️ Design Pattern Summary

| Pattern | Purpose | Implementation |
|--------|----------|----------------|
| Factory | Clean message creation | `MessageFactory` |
| Builder | Build complex objects | `UserBuilder` |
| Decorator | Add message formatting | `TimestampDecorator` |
| State | Represent user presence | `OnlineState`, etc. |
| Singleton | Central DB lifecycle | `SchemaManager` |
| Mediator | Coordinate communication | `ChatServer` |

---


# 🧠 Architectural Design Patterns Implemented

This project demonstrates six major software design patterns commonly used in production systems.

---

## 1️⃣ Factory Pattern — `MessageFactory`
Centralizes message creation for:

- Direct messages
- Server messages
- Friend requests
- Online/offline notifications
- Server join/leave

This ensures consistent message construction and easy extensibility.

---

## 2️⃣ Builder Pattern — `UserBuilder`
Builds complex `User` objects without long constructors.

Supports configuration of:

- userId
- username
- email
- friends
- direct message history
- initial online state

Improves clarity and maintainability.

---

## 3️⃣ Decorator Pattern — Message Formatting
Adds dynamic presentation features to messages:

- `BaseMessage`
- `TimestampDecorator`
- `SenderNameDecorator`

Allows flexible combinations such as:

```
[12:30:15] Alice: Hello!
```

without modifying core logic.

---

## 4️⃣ State Pattern — User Presence
Represents user presence with interchangeable states:

- `OnlineState`
- `OfflineState`
- `BusyState`
- `AwayState`

Avoids conditional logic and makes future state extensions trivial.

---

## 5️⃣ Singleton Pattern — `SchemaManager` (Database Layer)
Ensures:

- One SQLite database connection
- One schema initialization routine
- Coordinated access across the server

Prevents corruption and inconsistencies.

---

## 6️⃣ Mediator Pattern — `ChatServer` (Communication Hub)
The `ChatServer` acts as a centralized mediator for all chat interactions.

### Responsibilities
- Routing direct messages
- Processing friend requests
- Broadcasting server/channel messages
- Applying block rules
- Notifying presence changes
- Managing user lists and sessions

Clients do not interact with each other directly; all communication is coordinated by the server.

---

# 📘 Final Notes
This project demonstrates:

- Clean Code principles  
- A modular, maintainable architecture  
- Multiple design patterns working cohesively  
- A fully tested client–server application  
- Persistent storage and reliable communication flow  
