import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.streams.Pump;
import javax.swing.*;


class Client extends AbstractVerticle {
    String filename = "";
    String Path = "";
     public Client (String name, String path){
         this.filename = name;
         this.Path = path;
     }

    private static String getFileChecksum(MessageDigest digest, File file) throws IOException
    {
        FileInputStream fis = new FileInputStream(file);
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }
        fis.close();
        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for(int i=0; i< bytes.length ;i++)
        {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
    public void start() throws IOException {

        HttpClient client = vertx.createHttpClient();
        HttpClientRequest req = client.request(HttpMethod.POST, 8080, "localhost", "/", resp -> {
            System.out.println("Response " + resp.statusCode());
        });
        File filechecksum = new File(Path);
        try {
            MessageDigest md5Digest = MessageDigest.getInstance("MD5");
            String checksum = getFileChecksum(md5Digest, filechecksum);
            FileSystem fs = vertx.fileSystem();

                req.headers().set("content-name", "" + filename);
                req.headers().set("content-checksum", "" + checksum);

                fs.open(Path, new OpenOptions(), ares2 -> {
                    AsyncFile file = ares2.result();
                    Pump pump = Pump.pump(file, req);
                    file.endHandler(v -> {
                        req.end();
                    });
                    pump.start();
                });

            }
        catch (NoSuchAlgorithmException e){
            System.out.println(e.getCause());
        }

    }
    public static void main(String[] args) {
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setCurrentDirectory(new File("C:\\Users\\Михаил\\IdeaProjects\\lab4\\clientfiles\\"));
        int result = jFileChooser.showOpenDialog(new JFrame());
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jFileChooser.getSelectedFile();
            Vertx.clusteredVertx(new VertxOptions(),(event ->
                    event.result().deployVerticle(new Client(selectedFile.getName() ,selectedFile.getAbsolutePath()))));
        }
        else{
            System.exit(0);
        }


    }
}


