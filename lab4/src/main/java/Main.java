import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.streams.Pump;
import java.io.*;


public class Main extends AbstractVerticle {
    public String getFileContent ( FileInputStream fis ) throws IOException {
        StringBuilder sb = new StringBuilder();
        Reader r = new InputStreamReader(fis, "UTF-8");
        char[] buf = new char[1024];
        int amt = r.read(buf);
        while(amt > 0) {
            sb.append(buf, 0, amt);
            amt = r.read(buf);
        }
        return sb.toString();
    }

    @Override
    public void start()  {

        vertx.createHttpServer().requestHandler(req -> {

            req.pause();
            try {
                FileInputStream fil = new FileInputStream("C:\\Users\\Михаил\\IdeaProjects\\lab4\\serverfiles\\checksumoffiles.txt");
                String lifi  = getFileContent(fil);

                String filename = req.getHeader("content-name");
                String chk = req.getHeader("content-checksum")+ " ";
                String in = "" + req.hashCode();
                boolean isFound = lifi.indexOf(chk) !=-1? true: false;
                if(!isFound) {
                    vertx.fileSystem().open("C:\\Users\\Михаил\\IdeaProjects\\lab4\\serverfiles\\" + filename, new OpenOptions(), ares -> {
                        AsyncFile file = ares.result();
                        Pump pump = Pump.pump(req, file);
                        req.endHandler(v1 -> file.close(v2 -> {
                            System.out.println("Uploaded to " + filename);
                            req.response().end();
                        }));
                        pump.start();
                        req.resume();

                    });
                    FileOutputStream os = new FileOutputStream("C:\\Users\\Михаил\\IdeaProjects\\lab4\\serverfiles\\checksumoffiles.txt",true);
                    os.write(chk.getBytes(), 0, chk.length());
                    os.close();
                }
                else{
                    System.out.println("Error that file already here!");
                }
            }catch (IOException e ){
                System.out.println(e.getCause());
            }
        }).listen(8080);

    }
    public static void main(String[] args) {
        Vertx.clusteredVertx(new VertxOptions(), event -> event.result().deployVerticle(new Main()));
    }
}


