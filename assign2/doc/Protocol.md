# Communication Protocol

This file describes a simple protocol for communicating between a client and a server.

## Overview

There are two types of requests: client-initiated and server-initiated.
Client requests are always followed with a response from the server.
It is designed to be easy to implement so the different methods are going to be divided into the modules they are used in.

## Requests

Requests are formed by three lines:
 - Module name: `AUTH`, `GAME`
 - Method name: `LOGIN`, `LOGOUT`, `JOIN_GAME`, `MOVE`, ...
 - Parameters in JSON format: `{"username": "user", "password": "pass"}`, ...

## Responses

Server will yield a status code and an optional message:

| Code | Description | Message       |
|------|-------------|---------------|
| 0    | OK          | Optional      |
| 1    | Error       | Error message |


## Example

Client:
```
AUTH
LOGIN
{"username": "user", "password": "pass"}
```

Server:
```
0
```