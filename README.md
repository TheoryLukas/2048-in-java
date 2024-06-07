# 2048-in-java

This is a version of the Game "2048" made in Java with JavaFX for its GUI.

It also has a Leaderboard-Server that can be Self-Hosted.
There is also a version hosted at game.lukas-hannawald.net.

JavaFx https://gluonhq.com/products/javafx/
sqlite jdbc https://github.com/xerial/sqlite-jdbc

options for client
--module-path
./Additional-Libraries/javafx-sdk-21.0.2/lib
--add-modules
javafx.base,javafx.controls,javafx.graphics,javafx.media,javafx.fxml

systemd service file

[Unit]
Description=Manage Java service

[Service]
WorkingDirectory=/mnt/VAULT/Apps/Other/2048-Gameserver
ExecStart=/bin/java -jar Game_2048_Server_jar/Game-2048.jar /mnt/VAULT/Apps/Other/2048-Gameserver
User=game
Type=simple
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
