# BlakeBot

_Bot still in early development. More modules and features to be added in the near future._

Multi-purpose Discord bot.
Intends to provide a comprehensive set of features for chat moderation, entertainment, web search, ranking and currency, etc.
And memes. Because everyone loves memes.

Modules not being currently worked on (not including bugfixes):

Modules currently being worked on (only includes major changes, not bugfixes) (checked modules already have at least one release):

- [x] Status: Provides status data about the running instance of the bot.
- [ ] Admin: Provides tools for server Admins/Moderators to more easily manage the users of the server.
- [ ] Fun: Random jokes to be used in text chat.

Planned Modules:

- [ ] Anime: Anime-related jokes and functions.
- [ ] Games: Simple message-based games that can be played by the server's users.
- [ ] Help: Provides detailed information about each module and the commands in them.
- [ ] Kancolle: Kantai Collection-related jokes and functions.
- [ ] NSFW: Doesn't need much explanation.
- [ ] Permissions: Allows server admins to specify what modules are available for users on the server and specific channels.
- [ ] Remote: Provides remote control over the bot's core functions to the owner of the bot account being used.
- [ ] RWBY: RWBY-related jokes and functions.

# Usage

A public instance of the bot is available and can be added to a server through the [invite link]( https://discordapp.com/oauth2/authorize?client_id=263524182497820673&scope=bot&permissions=305589318) (you need to have the Manage Server permission in the target server).

A list of all commands recognized by the bot can be obtained using the `?help` command. Using `?help [command]` gives more information about a specific command.

To run your own instance of the bot:
- Download the core jar and desired module jars from any release version.
- Place the core jar in the desired directory.
- In the same directory, create a `modules` subdirectory and place all module jars in there.
- Run the core jar.

When the core executable is ran, a GUI will be provided to control the bot. If this is the first time the bot is started, it prompts for the bot token to be used (bot tokens described [here](https://discordapp.com/developers/docs/topics/oauth2#bots)). Subsequent restarts of the bot will use the same token. The token can be accessed and changed in the `properties.xml` file created in the same directory that the program is ran from.

# 3rd-party Libraries

Libraries used in this bot include:

- [Discord4J](https://github.com/austinv11/Discord4J) by austinv11, licensed under the [LGPL 3.0](https://www.gnu.org/licenses/lgpl-3.0.en.html).
- [Discordinator](https://github.com/kvnxiao/Discordinator) by kvnxiao, licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).
- [Logback](https://logback.qos.ch/) by QOS.ch, licensed under the [LGPL 2.1](http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html).