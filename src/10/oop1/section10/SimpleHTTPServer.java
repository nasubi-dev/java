package oop1.section10;

import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

public class SimpleHTTPServer {
  private static final int PORT = 8088;

  public static void main(String[] args) {
    try (ServerSocket serverSocket = new ServerSocket(PORT)) {
      System.out.println("SimpleHTTPServer started on port " + PORT);
      System.out.println("Access to http://localhost:" + PORT);

      while (true) {
        try (Socket clientSocket = serverSocket.accept()) {
          handleRequest(clientSocket);
        } catch (IOException e) {
          System.err.println("Error handling client request: " + e.getMessage());
        }
      }
    } catch (IOException e) {
      System.err.println("Server error: " + e.getMessage());
    }
  }

  private static void handleRequest(Socket clientSocket) throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

    // HTTPリクエストの最初の行を読み取る
    String requestLine = in.readLine();
    if (requestLine == null) {
      return;
    }

    System.out.println("Request: " + requestLine);

    // リクエストラインを解析
    StringTokenizer tokenizer = new StringTokenizer(requestLine);
    tokenizer.nextToken();
    String path = tokenizer.nextToken();

    String response = generateResponse(path);
    out.print(response);
    out.flush();
  }

  private static String generateResponse(String path) {
    String actualPath = path;
    String queryString = "";
    int queryIndex = path.indexOf('?');
    if (queryIndex != -1) {
      actualPath = path.substring(0, queryIndex);
      queryString = path.substring(queryIndex + 1);
    }

    switch (actualPath) {
      case "/":
      case "/index.html":
        return generateIndexResponse();
      case "/style.css":
        return generateCSSResponse();
      case "/script.js":
        return generateJSResponse();
      default:
        if (actualPath.equals("/hello") && queryString.startsWith("name=")) {
          String name = queryString.substring(5);
          name = name.replace("+", " ");
          return generateHelloResponse(name);
        }
        return generate404Response();
    }
  }

  private static String generateIndexResponse() {
    return """
        HTTP/1.0 200 OK
        Content-Type: text/html; charset=utf-8

        <!DOCTYPE html>
        <html lang="ja">
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1">
          <meta charset="UTF-8">
          <title>SimpleHTTPServer</title>
          <link rel="stylesheet" href="style.css">
        </head>
        <body>
          <main>
            <h1>このページはSimpleHTTPServerより生成されて返されています。</h1>
            <p><button class="fire">Push!!</button></p>
            <p class="copyright"> k24083 - 鈴木 稜生</p>
            </p>
          </main>
          <script src="script.js"></script>
        </body>
        </html>
        """;
  }

  private static String generateHelloResponse(String name) {
    return """
        HTTP/1.0 200 OK
        Content-Type: text/html; charset=utf-8

        <!DOCTYPE html>
        <html lang="ja">
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1">
          <meta charset="UTF-8">
          <title>SimpleHTTPServer</title>
          <link rel="stylesheet" href="style.css">
        </head>
        <body>
          <main>
            <h1>こんにちは！""" + name + """
        さん！！</h1>
          </main>
        </body>
        </html>
        """;
  }

  private static String generateCSSResponse() {
    return """
        HTTP/1.0 200 OK
        Content-Type: text/css; charset=utf-8

        * {
          margin: 0;
          padding: 0;
          box-sizing: border-box;
        }
        body {
          height: 100vh;
          display: flex;
          justify-content: center;
          align-items: center;
        }
        main {
          height: 450px;
          max-height: 90vh;
          width: 800px;
          max-width: 90vw;
          border-radius: 10px;
          box-shadow: rgba(0, 0, 0, 0.1) 0px 20px 60px -10px;
          display: flex;
          justify-content: center;
          align-items: center;
          flex-direction: column;
        }
        h1 {
          padding: 0 3em;
          margin-bottom: 2em;
          text-align: center;
        }
        button {
          font-size: 1.25em;
          padding: 0.5em 1em;
        }
        .copyright {
          margin-top: 20px;
          text-decoration: underline;
          font-style: italic;
        }
        """;
  }

  private static String generateJSResponse() {
    return """
        HTTP/1.0 200 OK
        Content-Type: text/javascript; charset=utf-8

        var btn = document.querySelector('button.fire');
        btn.addEventListener('click', function() {
          alert('Hello, SimpleHTTPServer!!');
        });
        """;
  }

  private static String generate404Response() {
    return """
        HTTP/1.0 404 Not Found
        Content-Type: text/html; charset=utf-8

        <!DOCTYPE html>
        <html lang="ja">
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1">
          <meta charset="UTF-8">
          <title>404</title>
          <link rel="stylesheet" href="style.css">
        </head>
        <body>
          <main>
            <h1>404... Not Found!</h1>
          </main>
        </body>
        </html>
        """;
  }
}
