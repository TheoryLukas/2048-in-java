# 2048-in-java

This is a version of the Game "2048" made in Java with JavaFX for its GUI.

It also has a Leaderboard-Server that can be Self-Hosted.
There is also a version hosted by me at [game.lukas-hannawald.net](https://game.lukas-hannawald.net).

## Usage

### Client

The Client GUI is made with JavaFx ant therefor requires the Libraries for your Plattform. These files can be Downloades [here](https://gluonhq.com/products/javafx/).

To use them set the following options when running the client:
```
--module-path <path-to-javafx-sdk>/javafx-sdk-21.0.2/lib
--add-modules javafx.base,javafx.controls,javafx.graphics,javafx.media,javafx.fxml
```

### Server

The Server uses a sqlite JDBC to interface with the database. The Java Archives `sqlite-jdbc-3.46.0.0.jar` and `slf4j-api-1.7.36.jar` need to be included via classpath at compile time. Instructions can be found [here](https://github.com/xerial/sqlite-jdbc#Usage).

To run/host the Server, it requires the path (recommended: absolute) to the directory containing the sqlite database file (important exclude the last `/`). Therefore it is recommended to run the file from a Terminal:
```
java -jar 2084-Gameserver.jar <path-to-db-dir>
```

To run the server in the background without forking the process, you can create a systemd service for Linux systems using `systemd.service`.

Template for a systemd service file:
```
[Unit]
Description=2048 Leaderboard Server service

[Service]
WorkingDirectory=<path-to-jar-dir>
ExecStart=/bin/java -jar 2048-Gameserver.jar <path-to-db-dir>
User=<user-to-run-server>    #The user that runs the programm must have read access to the .jar file and read-write access to the database file
Type=simple
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

For further information on systemd.service, please refer to the `systemd.service` [man pages](https://www.freedesktop.org/software/systemd/man/latest/systemd.service.html) or other documentation on `systemd.service`.
