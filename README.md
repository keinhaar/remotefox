# remotefox
This is a library to allow remote controlling firefox in debug mode. This is intended to be used in external debuggers like SDBG, but may be useful for some other use cases.

## How to use
Before you can start controlling firefox, you will need to allow it in firefox, by adding this to your prefs.js (you can also set this prefs in the about:config page).
<pre>
  user_pref("devtools.chrome.enabled", true);
  user_pref("devtools.debugger.prompt-connection", false);
  user_pref("devtools.debugger.remote-enabled", true);
</pre>
After that you will need to start firefox with the parameter **--start-debugger-server**.
No you are ready to connect by calling 
<pre>
  DebugConnector con = new DebugConnector("127.0.0.1", PORTNUMBER);
  con.start();
  RootActor actor = con.getRootActor();
</pre>
The RootActor is the starting point. It allows you to list Tabs, and in turn to observe resources, add breakpoints, navigate to other ages a.s.o.
The Actors are named after the Firefox internal classes, but some functionality of other Actors is bundled into the TabActor, to make it easier for beginners to start.
