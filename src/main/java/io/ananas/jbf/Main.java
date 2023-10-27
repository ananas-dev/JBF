package io.ananas.jbf;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length == 1) {
            compileFile(args[0]);
        } else {
            System.out.println("Usage: jbf [script]");
            System.exit(64);
        }
    }

    private static void compileFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        String source = new String(bytes, Charset.defaultCharset());

        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Compiler compiler = new Compiler(tokens);
        bytes = compiler.compile();

        try (FileOutputStream stream = new FileOutputStream("BFMain.class")) {
            stream.write(bytes);
        } catch (Exception e) {
            // Todo: more robust error logging
            e.printStackTrace();
        }
    }
}
