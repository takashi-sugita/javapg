import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class App {
    static List<Todo> todos = new ArrayList<>();
    static int nextId = 1;

    public static void main(String[] args) throws Exception {
        load();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            String message;
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");

            if (path.equals("/add") && method.equals("POST")) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String value = body.substring(5);
                String title = URLDecoder.decode(value, StandardCharsets.UTF_8);
                if (!title.isEmpty()) {
                    todos.add(new Todo(nextId, title));
                    nextId++;
                    save();
                }
                exchange.getResponseHeaders().set("Location", "/");
                exchange.sendResponseHeaders(303, -1);
                exchange.close();
                return;
            } else if (path.equals("/done") && method.equals("GET")) {
                String query = exchange.getRequestURI().getQuery();
                if (query != null && query.startsWith("id=") && query.length() > 3) {
                    try {
                        int id = Integer.parseInt(query.substring(3));
                        for (Todo todo : todos) {
                            if (todo.getId() == id) {
                                todo.setDone(true);
                                save();
                                break;
                            }
                        }
                    } catch (NumberFormatException e) {
                    }
                }
                exchange.getResponseHeaders().set("Location", "/");
                exchange.sendResponseHeaders(303, -1);
                exchange.close();
                return;
            } else if (path.equals("/delete") && method.equals("GET")) {
                String query = exchange.getRequestURI().getQuery();
                if (query != null && query.startsWith("id=") && query.length() > 3) {
                    try {
                        int id = Integer.parseInt(query.substring(3));
                        todos.removeIf(todo -> todo.getId() == id);
                        save();
                    } catch (NumberFormatException e) {
                    }
                }
                exchange.getResponseHeaders().set("Location", "/");
                exchange.sendResponseHeaders(303, -1);
                exchange.close();
                return;
            } else if (path.equals("/")) {
                String html = "<!doctype html><html lang='ja'><head><meta charset='UTF-8'>"
                        + "<title>わたしのTodo</title><style>body { max-width: 640px; margin: 24px auto; "
                        + "padding: 0 16px; font-size: 16px; }</style></head><body>"
                        + "<h1>わたしのTodo</h1>"
                        + "<form method='post' action='/add'><input name='todo'><button>追加</button></form>";
                if (todos.isEmpty()) {
                    html += "<p>やることは、いまゼロです</p>";
                } else {
                    html += "<ul>";
                    for (Todo todo : todos) {
                        String mark = "";
                        String title = todo.getTitle();
                        if (todo.isDone()) {
                            mark = " ✅";
                            title = "<span style='color: #888; text-decoration: line-through;'>"
                                    + title + "</span>";
                        }
                        html += "<li>" + title + mark + " <a href='/done?id=" + todo.getId()
                                + "'>完了</a> <a href='/delete?id=" + todo.getId() + "'>削除</a></li>";
                    }
                    html += "</ul>";
                }
                html += "</body></html>";
                message = html;
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            } else {
                message = "ページが見つかりません";
            }
            byte[] body = message.getBytes("UTF-8");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.getResponseBody().close();
        });
        server.start();
        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）");
    }

    static void save() throws java.io.IOException {
        List<String> lines = new ArrayList<>();
        for (Todo todo : todos) {
            String title = todo.getTitle();
            if (title.contains(",") || title.contains("\"") || title.contains("\n") || title.contains("\r")) {
                title = "\"" + title.replace("\"", "\"\"") + "\"";
            }
            lines.add(todo.getId() + "," + (todo.isDone() ? "1" : "0") + "," + title);
        }
        Files.write(Path.of("todos.csv"), lines, StandardCharsets.UTF_8);
    }

    static void load() throws java.io.IOException {
        Path file = Path.of("todos.csv");
        if (!Files.exists(file)) {
            return;
        }

        int largestId = 0;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            int firstComma = line.indexOf(',');
            int secondComma = line.indexOf(',', firstComma + 1);
            if (firstComma < 1 || secondComma < 0) {
                continue;
            }
            try {
                int id = Integer.parseInt(line.substring(0, firstComma));
                boolean done = line.substring(firstComma + 1, secondComma).equals("1");
                String title = line.substring(secondComma + 1);
                if (title.startsWith("\"") && title.endsWith("\"")) {
                    title = title.substring(1, title.length() - 1).replace("\"\"", "\"");
                }
                Todo todo = new Todo(id, title);
                todo.setDone(done);
                todos.add(todo);
                if (id > largestId) {
                    largestId = id;
                }
            } catch (NumberFormatException e) {
            }
        }
        nextId = largestId + 1;
    }
}

class Todo {
    private final int id;
    private final String title;
    private boolean done;

    Todo(int id, String title) {
        this.id = id;
        this.title = title;
        this.done = false;
    }

    int getId() {
        return id;
    }

    String getTitle() {
        return title;
    }

    boolean isDone() {
        return done;
    }

    void setDone(boolean done) {
        this.done = done;
    }
}
