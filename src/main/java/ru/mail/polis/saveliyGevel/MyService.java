package ru.mail.polis.saveliyGevel;

import com.sun.jdi.IntegerValue;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.tools.doclets.internal.toolkit.Content;
import com.sun.tools.internal.ws.processor.model.Response;
import org.jetbrains.annotations.NotNull;
import ru.mail.polis.KVService;

import javax.naming.Context;
import javax.swing.text.AbstractDocument;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.NoSuchElementException;

/**
 * Created by admin on 24.01.2018.
 */
public class MyService implements KVService {
    private static final String PREFIX = "id=";
    @NotNull
    private final HttpServer server;

    @NotNull
    private static String extractId(@NotNull final String query) {
        if (!query.startsWith(PREFIX)) {
            throw new IllegalArgumentException("Illigal command");
        }

        final String id = query.substring(PREFIX.length());
        if (id.isEmpty()) {
            throw new IllegalArgumentException("Incorrect key");
        }

        return id;
    }

    public MyService(int port, @NotNull MyDAO dao) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);

        this.server.createContext("/v0/status", http -> {
            final String response = "ONLINE";
            http.sendResponseHeaders(200, response.length());
            http.getResponseBody().write(response.getBytes());
            http.close();
        });

        this.server.createContext("/v0/entity", new ErrorHandler(http -> {
            final String id = extractId(http.getRequestURI().getQuery());
            switch (http.getRequestMethod()) {
                case "GET":
                    final byte[] getValue = dao.get(id);
                    http.sendResponseHeaders(200, getValue.length);
                    http.getResponseBody().write(getValue);
                    break;

                case "DELETE":
                    dao.delete(id);
                    http.sendResponseHeaders(202, 0);
                    break;

                case "PUT":
                    final int contentLength = Integer.valueOf(http.getRequestHeaders().getFirst("Content-Length"));
                    final byte[] putValue = new byte[contentLength];

                    if (http.getRequestBody().read(putValue) != putValue.length) {
                        throw new IOException("can not read file");
                    }
                    dao.upsert(id, putValue);
                    http.sendResponseHeaders(201, 0);
                    break;

                default:
                    http.sendResponseHeaders(405, 0);
            }

            http.close();
        }));
    }

    @Override
    public void start() {
        this.server.start();
    }

    @Override
    public void stop() {
        this.server.stop(1);
    }

    private static class ErrorHandler implements HttpHandler {
        private final HttpHandler delegate;

        private ErrorHandler(HttpHandler delegate) {
            this.delegate = delegate;
        }

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            try {
                delegate.handle(httpExchange);
            } catch (IOException e) {
                httpExchange.sendResponseHeaders(201, 0);
                httpExchange.close();
            } catch (NoSuchElementException e) {
                httpExchange.sendResponseHeaders(404, 0);
                httpExchange.close();
            } catch (IllegalArgumentException e) {
                httpExchange.sendResponseHeaders(400, 0);
                httpExchange.close();
            }
        }
    }
}
