# CS:NO2
## CPD Assignment 2 - Group T06G14

#### Table of contents

- [Steps to run](#steps-to-run)
- [Features](#features)
- [Protocol](#protocol)
- [Project Structure](#project-structure)
- [Group Members](#group-members)

### Steps to run

#### Server

```bash
cd assign2/src
javac server/*.java
java server.Server
```

#### Client

```bash
cd assign2/src
javac client/*.java
java client.Client
```

If more than one client is being executed no the same machine, the flag -r can be used to create a new session:

```bash
java client.Client -r
```

To clean `.class` files:

```bash
cd assign2/src
./clean.sh
```

### Features

- [x] Auth: User information kept in file, allowing for registering, logging in and logging out.
- [x] Matchmaking: Casual and ranked matchmaking, queueing players.
- [x] Game: Players can play the game, with the server keeping track of the game state.
- [x] Ranking: Games will affect player rank.
- [x] Client fault tolerance: Client can recover from client crashes.
- [x] Thread safety
- [x] Client timeout to avoid slow clients
- [x] Server side logger to keep track of events
- [x] No thread overheads: Server maintains a thread pool

### Protocol

To enable client-server communication we devised a simple TCP protocol, as described in [doc/Protocol.md](doc/Protocol.md).

### Project Structure

#### Server

On its main server, the Server will be dealing two types of connections:
 - Accepting new clients
 - Reading from idle sockets

Moreover, a thread will be requested and redirect execution to deal with the request.

#### Client

Client's program is composed of a state machine that will iterate through the different stages of the game.
The client acts as a proxy between user and server. 
It will read user input and communicate with the server using the specified protocol.

### Group Members

- José Rodrigues (up202008462@fe.up.pt)
- Martim Henriques (up202004421@fe.up.pt)
- Tiago Barbosa (up202004926@fe.up.pt)