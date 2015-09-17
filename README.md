DataBuddy [![Build Status](https://travis-ci.org/aarontharris/DataBuddy.svg)](https://travis-ci.org/aarontharris/DataBuddy)
======

Its a datastore that aims to be ultra portable, lightweight and very powerful.

It can be bundled up with your desktop application or deployed on a server.

<b>Index:</b>
- Status
- Project Details
  - Overview
  - How it works
  - Availability
- How to implement

Status
======

Sep 05, 2015:

DataBuddy is now backed by SQLite and has been asbtracted away so replacing the backing is very easy.  If you want to use something more powerful such as MySql you just replace the adapter.

Server side encryption support is read, though that implementation is up to you since this is open source and you don't want to reveal the handshake logic specific to your product.

Aug 31, 2015:

Currently DataBuddy is incomplete and much of it is proof of concept hacked together, don't judge me at this point I'm like 3 hours invested.  The server is working and is capable of handling concurrent users and concurrent commands from users.  However put and get data is not yet persisted (a key requirement) but it will be there soon. Like hours, maybe days...?

Project Details
======

<b>Overview</b>

When developing a game, its painful to abstract storing data so that it can be agnostic about whether that game is running locally or a client to a remove server.
DataBuddy takes care of that.  DataBuddy follows a client server model so that it can be deployed remotely, but simplifies persistence so that you don't have to think about that.
You always code against the same simple API so that your game and your code doesn't have to care.  Of course there's some difference around latency but with a little care it is minimized.

<b>How it works</b>

DataBuddy itself is a server listening on a configurable port.  You can telnet to it and issue commands like 'put' and 'get'.
Coupled with a Client API, your game code can open a connection and read and write data very quickly.  DataBuddy is concurrency aware and should be able to handle a good number of simultaneous users.
DataBuddy is backed by a simple database stored on the filesystem (like sqlite) so that it can be portable all that is abstracted away and you shouldn't have to think about it.
You simply send data to the server associated with a topic and an id, and later you can retrieve that same data by topic and id.
You will need to be aware that performance is greatly dependent on how you structure your data as poor associations can get messy.
There are no joins, so its on you.

EX: put (topic) (id) (data)

EX: get (topic) (id)

<b>Security</b>
DataBuddy isn't fancy.  It knows about a system operator role and a user role.  To connect you must introduce yourself as one of these roles and present the proper credentials.
A user can by anonymous or a registered user with a username and password. The sysop must have a username and password.  Sysop access gives you some additional functionality over a user such as removing users, resetting the service, etc.

<b>Availability</b>

Databuddy is written in Java and can be used anywhere you can fit a JVM (Java 1.8_60).  If there's no Client API available for the language you're lookng for, it's pretty simple to write -- kind of like talking to Memcached, open a port and write some bytes, then read some bytes.
Given that I use this in Unity, there should no doubt be some progress on a C# Client API.  If you do produce a Client API for other useful languages, please let me know and I can link to for others to find.

How to implement
======

Open the project in eclipse, run Main.java and telnet to locahost on port 25564:

```
telnet localhost 25564
```

Now paste the following into telnet to authrorize yourself as a user:

```
auth request_auth=user&username=theUsername&password=thePassword
```

Now you may put and get data:

```
put topic subtopic {key=value}

get topic subtopic
```


The project code as it is now, is sort of in a testing state for ease of development.  I'm sort of developing it as I go for the needs of my own project and abstracting away for general use.  My specific bits are not committed here so I apologize if some of the code appears fragmented.  However it is always in a debugging runnable state in the form of a pseudo-sample implementation.  Having said that, here's some key places to look to plug in your own bits.

- DataBuddy.java - The face of the server.  It greets the client and quickly delegates off to the ClientConnection.
  - The main connection listen loop runs here in the main thread.
  - Since this is the core of the server, commands that involve other connections pass through here, commands such as relaying messages from one client to another.  Client-A must identify Client-B via DataBuddy as DataBuddy is the parent and knows all its children, but Client-A and Client-B are siblings and are not directly aware of eachother.  Binding clients is not advisable as connections can be lost at any time.  Best practice is to associate a client to a user or session and ask DataBuddy (the parent) to find the client-sibling by name (user or session, etc) for safety.
- ClientConnection.java - The heart[s] of the server.  Each client gets its own instance running in its own thread.
  - This class is the root of the client instance, but your interraction with it should be minimal as there is an abstraction provided to allow you to create various "modes" of operation
- ConnectionDelegate.java - The hands of the server. Your customizeable interface to the ClientConnection.
  - Here you control how to deal with received messages.
  - Currently there are some example User or Sysop ConnectionDelegates that get launched based on the user's login.
  - When the ClientConnection first launches, it starts in the Handshake ConnectionDelegate called HandshakeConnection.  This connection authenticates the user and if successful tells the ClientConnection to switch modes from the HandshakeConnection to the UserConnection or SystemConnection.  From there the ClientConnection blocks its thread waiting for I/O.

Please feel free to contact me if you want to know more.
