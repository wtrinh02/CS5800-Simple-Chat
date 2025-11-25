# Simple Chat MVP

A multi-user chat application written in **pure Java** using **sockets and multithreading**.  
Supports public servers, private messaging, friend requests, real-time status updates, and now persistent users + DM history — all from the terminal.

---

## 🚀 Features

- Real-time messaging using Java sockets
- Public server (`general`) auto-join on login
- Custom server creation & joining
- Friend system (add / accept)
- ✅ Persistent users + DM history (saved even after shutdown)
- ✅ Auto-loading of friends, history, and blocked list on login
- ✅ Color-coded terminal UI
- ✅ Message tagging by server
- ✅ Auto-return to `general` when leaving a server
- Blocking users
- DMs between friends only
- Seeing all DM history
- Online/offline presence detection
- Server member listing
- Server-side message broadcasting
- Fully multithreaded server

---

## 📦 Tech Stack

- Language: **Java**  
- Networking: **Java Sockets**  
- Concurrency: **Java Threads**  
- Build: IntelliJ / Command Line

---

## 🗂 Project Structure

```
src/
 └── main/
     └── java/
         ChatServer.java
         ChatClient.java
         ClientHandler.java
         LocalServer.java
         Message.java
         User.java
         UserBuilder.java
```

---

## 🖥 How To Run (Terminal Method)

### 1. Compile the project
Navigate to your project root folder (the one containing `src`):

```bash
javac -d out src/main/java/*.java
```

This will create an `out/` directory with compiled `.class` files.

---

### 2. Start the Server

In your terminal:

```bash
cd out
java ChatServer
```

Expected output:

```
Default 'General' server created
Chat Server started on port 8888
```

Leave this terminal running.

---

### 3. Start Client(s)

Open **new terminals** for each user.

In each new terminal:

```bash
cd out
java ChatClient <userId> <username>
```

Example:

```bash
java ChatClient u1 Alice
java ChatClient u2 Bob
```

---

## 💬 Chat Commands

### User Commands
```
add <userId>        -> Send friend request
accept <userId>     -> Accept friend request
friends             -> View online friends
block <userId>      -> Block a user
unblock <userId>    -> Unblock a user
blocked             -> View blocked users
```

---

### Direct Messaging (Friends Only)
```
dm <userId> <message>
history <userId>    -> See dm chat log with userId
```

Example:
```
dm u2 Hey Bob!
```

Note: Users must be friends before DMs will work.

---

### Server Commands
```
create <serverId> <serverName>
servers              -> List all servers
join <serverId>      -> Join a server
leave <serverId>     -> Leave a server
members <serverId>   -> Show server members
say <message>        -> Send message to current server
```

Example:
```
create gaming Gaming Room
join gaming
say Welcome everyone!
members gaming
```

---

## 🌍 Connecting From Another Computer (Remote Usage)

By default, the client connects to `localhost`, which only works on the same computer.

You can now connect to another machine by **passing the server IP as a 3rd argument**:

```bash
java ChatClient <userId> <username> <server-ip>
```

Example:
```bash
java ChatClient u3 Charlie 192.168.1.105
```

### 🔍 Finding the Server IP (Windows)
On the machine running the server:

1. Open Command Prompt
2. Run:
```bash
ipconfig
```
3. Look for something like:
```
IPv4 Address . . . . . : 192.168.1.105
```
That is the IP other users should connect to.

### 🔒 Firewall Note
If users cannot connect, allow **Java** through Windows Firewall and ensure port **8888** is open.

---

## 📺 Example Demo Flow

User A:
```
add u2
dm u2 Hello!
```

User B:
```
accept u1
dm u1 Hi Alice!
```

Custom Server:
```
create dev Developers Lounge
join dev
say Let's collaborate
```

---

## ⚙ Running in IntelliJ

1. Open project folder in IntelliJ
2. Run `ChatServer.main()` using green ▶ button
3. Create run configs for ChatClient with arguments:
```
u1 Alice
u2 Bob
```
4. Run both clients from IntelliJ

---

## ✅ System Requirements

- Java JDK 8 or higher
- Any terminal or IntelliJ IDEA

---

## 🧠 Design Patterns Used

### 1. Builder Pattern – `UserBuilder`
The Builder pattern is used to construct complex `User` objects in a clean and readable way without relying on large constructors. Instead of passing many parameters directly into the `User` constructor, the `UserBuilder` class provides chained setter methods such as `setUserId()`, `setUsername()`, and `setEmail()`, followed by a `build()` method to finalize creation.

This improves code readability, avoids telescoping constructors, and allows future expansion of the `User` class without breaking existing logic.

Example usage in `ChatServer`:
```java
User user = new UserBuilder()
        .setUserId(userId)
        .setUsername(username)
        .setEmail(email)
        .setOnline(true)
        .build();
```

---

### 2. Mediator Pattern – `ChatServer`
The Mediator pattern is implemented through the `ChatServer` class, which serves as a central communication hub between all connected clients. Instead of clients communicating directly with one another, all requests such as direct messages, friend requests, and server messages are routed through the server.

This reduces coupling between clients and centralizes communication logic inside the server. The `ChatServer` decides how messages are processed, validated, and delivered to the correct recipients.

Key mediator responsibilities include:
- Routing direct messages between users
- Managing friend requests and relationships
- Broadcasting messages to server members
- Notifying users of online/offline status changes

By using the Mediator pattern, the system becomes more modular, scalable, and easier to maintain.

---

## Goals for the Final Product
- Add 3 more design patterns TBD
- Update Persistence to use a Database instead
- Create a better frontend UI to improve user quality
- Implement different types of messages (img,file,video)
- IF have time figure out how voice channels work


