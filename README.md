DataBuddy
======

Its a datastore that aims to be ultra portable, lightweight and very powerful.

It can be bundled up with your desktop application or deployed on a server.

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

EX: put [topic] [id] [data]

EX: get [topic] [id]

<b>Security</b>
DataBuddy isn't fancy.  It knows about a system operator role and a user role.  To connect you must introduce yourself as one of these roles and present the proper credentials.
A user can by anonymous or a registered user with a username and password. The sysop must have a username and password.  Sysop access gives you some additional functionality over a user such as removing users, resetting the service, etc.

<b>Availability</b>

Databuddy is written in Java and can be used anywhere you can fit a JVM.  If there's no Client API available for the language you're lookng for, it's pretty simple to write -- kind of like talking to Memcached, open a port and write some bytes, then read some bytes.
Given that I use this in Unity, there should no doubt be some progress on a C# Client API.  If you do produce a Client API for other useful languages, please let me know and I can link to for others to find.



Please feel free to contact me if you want to know more.
