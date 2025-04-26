import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        String inFile = "";
        String outFile = "";
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--asm")) {
                inFile = args[++i];
            } else if (args[i].equals("--bin")){
                outFile = args[++i];
            }else{
                System.err.println("Неправильный ввод имен файлов");
                    System.exit(1);}

        }
        Machine cod = new Machine(inFile, outFile);
        cod.exec();

    }
}
