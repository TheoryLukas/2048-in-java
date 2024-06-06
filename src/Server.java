import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.InetSocketAddress;

import java.sql.*;
import java.util.ArrayList;

public class Server
{
    // Main Method
    public static void main(String[] args) throws IOException
    {
        DBInterface.connectToDB(args[0]);

        // Create an HttpServer instance
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        // Create a context for a specific path and set the handler
        server.createContext("/", new handler());

        // Start the server
        server.setExecutor(null); // Use the default executor
        server.start();

        System.out.println("Server is running on port 8000");
    }

    static class DBInterface {

        static Connection connection = null;

        private static void connectToDB(String dbDir) {
            try {
                String dbUrl = "jdbc:sqlite:" + dbDir + "/" + "scores.db";

                connection = DriverManager.getConnection(dbUrl);

                System.out.println("Connection to SQLite has been established.");

            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

        private static ArrayList<String> readFromDB () {
            try {
                Statement statement = connection.createStatement();
                ArrayList<String> data = new ArrayList<>();

                ResultSet rs = statement.executeQuery("SELECT username, score FROM scores ORDER BY score DESC");

                while (rs.next()) {
                    data.add(String.format("%s-%s", rs.getString("username"), rs.getString("score")));
                }

                return data;
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }

            return null;
        }

        private static void writeToDB(String column, String data) {
            try {
                Statement statement = connection.createStatement();

                statement.executeQuery(String.format("insert into scores (%s) values (%s)", column, data));
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    static class handler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            OutputStream os;
            String response;
            int responseCode;

            switch (exchange.getRequestMethod()) {
                case "GET":
                    responseCode = 200;
                    response = """
                            <!DOCTYPE html>
                            <html lang="en">
                            <head>
                                <meta charset="UTF-8">
                                <title>Leaderboard</title>
                                <style>
                                body {
                                    background-color: #87c8db;
                                }
                                div {
                                    text-align: center;
                                }
                                table {
                                    margin-left: auto;
                                    margin-right: auto;
                                    width: 25%;
                                    border-style: dashed;
                                }
                                tr:nth-child(even) {
                                    background-color: #608c99;
                                }
                                </style>
                            </head>
                            <body>
                            <div>
                            <h1> 2048 Leaderboards </h1>
                            <p> More info on <a href="https://github.com/TheoryLukas/2048-in-java">my GitHub</a> </p>
                            """;

                    ArrayList<String> tableData = DBInterface.readFromDB();
                    StringBuilder htmlTable = new StringBuilder("<table>\n<tr>\n<th>User</th>\n<th>Score</th>\n</tr>");

                    assert tableData != null;
                    tableData.forEach((info) -> {
                        String[] spitData = info.split("-");

                        htmlTable.append("\n<tr>\n<td>").append(spitData[0]).append("</td>\n<td>").append(spitData[1]).append("</td>\n</tr>");
                    });

                    htmlTable.append("\n</table>");

                    response = response + htmlTable + "</div>\n</body>\n</html>";

                    exchange.sendResponseHeaders(responseCode, response.length());
                    os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                    break;

                case "POST":
                    StringBuilder sb = new StringBuilder();
                    InputStream ioStream = exchange.getRequestBody();

                    int i;
                    while ((i = ioStream.read()) != -1) {
                        sb.append((char) i);
                    }

                    String[] reqData = sb.toString().split("&");

                    if (reqData.length != 2) {
                        responseCode = 406;
                        response = responseCode + "_INVALID_DATA";

                        exchange.sendResponseHeaders(responseCode, response.length());
                        os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                        return;
                    }

                    for (String reqDataSample : reqData) {
                        if (!(reqDataSample.matches("(username)=(([a-z]|_|-)+)") || reqDataSample.matches("(score)=([0-9]+)"))) {
                            responseCode = 406;
                            response = responseCode + "_INVALID_DATA";

                            exchange.sendResponseHeaders(responseCode, response.length());
                            os = exchange.getResponseBody();
                            os.write(response.getBytes());
                            os.close();
                            return;
                        }
                    }

                    StringBuilder column = new StringBuilder(), data = new StringBuilder("'");

                        for (String reqDataSample : reqData) {
                        String[] splitReqData = reqDataSample.split("=");

                        column.append(splitReqData[0]).append("%s");
                        data.append(splitReqData[1]).append("%s");
                    }

                    column = new StringBuilder(String.format(String.valueOf(column), ", ", ""));
                    data = new StringBuilder(String.format(String.valueOf(data), "', '", "'"));

                    DBInterface.writeToDB(String.valueOf(column), String.valueOf(data));

                    responseCode = 200;
                    response = "OK";

                    exchange.sendResponseHeaders(responseCode, response.length());
                    os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                    break;
            }
        }
    }
}
