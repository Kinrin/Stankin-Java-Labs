import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.streams.Pump;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;


public class Main extends AbstractVerticle {

    @Override
    public void start()  {

        vertx.createHttpServer().requestHandler(req -> {

            req.pause();

                Path path = Paths.get("C:\\Users\\Михаил\\IdeaProjects\\lab4\\serverfiles\\checksumoffiles.txt");

                try {
                    String lifi = "";
                    List<String> tmp = Files.readAllLines(path);
                    for (String s : tmp)
                    {
                        lifi += s + " ";
                    }
                    String filename = req.getHeader("content-name");
                    String chk = req.getHeader("content-checksum") + " ";
                    boolean isFound = lifi.indexOf(chk) != -1 ? true : false;
                    if (!isFound) {
                        while (new File("C:\\Users\\Михаил\\IdeaProjects\\lab4\\serverfiles\\" + filename).exists()) {
                            filename = "new_" + filename;
                        }
                        vertx.fileSystem().open("C:\\Users\\Михаил\\IdeaProjects\\lab4\\serverfiles\\" + filename, new OpenOptions(), ares -> {
                            AsyncFile file = ares.result();
                            Pump pump = Pump.pump(req, file);
                            req.endHandler(v1 -> file.close(v2 -> {
                                req.response().end();
                            }));
                            pump.start();
                            req.resume();

                        });
                        Path os = Paths.get("C:\\Users\\Михаил\\IdeaProjects\\lab4\\serverfiles\\checksumoffiles.txt");
                        Files.write(os, chk.getBytes(), StandardOpenOption.APPEND);

                    } else {
                        System.out.println("Error that file already here!");
                        req.response().setStatusCode(500);
                        req.response().end();

                    }
                }  catch (IOException ex) {
                ex.printStackTrace();
            }
        }).listen(8080);

    }
    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new Main());
    }
}


