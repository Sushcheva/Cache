import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Machine {
    static HashMap<String, String> reg = new HashMap<>();
    static HashMap<String, Integer> commandMap = new HashMap<>();
    static HashMap<Character, Integer> fenceMap = new HashMap<>();
    static HashMap<Integer, ArrayList<String>> MemMap = new HashMap<>();
    private static IOException ArrayIndexOutOfBoundsException;
    public static String[][] instructions = new String[114688][];
    public static int[] memory = new int[131072];
    public static ArrayList<byte[]> forwrite= new ArrayList<>();
    public static String filename;
    public static String output;

    public static int pointer = 16384;
    public static String[] arg;
    public Machine(String name, String output){
        this.filename = name;
        this.output = output;
    }

    public void exec() throws IOException {
        reg.put("zero", "00000");
        reg.put("ra", "00001");
        reg.put("sp", "00010");
        reg.put("gp", "00011");
        reg.put("tp", "00100");
        reg.put("t0", "00101");
        reg.put("t1", "00110");
        reg.put("t2", "00111");
        reg.put("s0", "01000");
        reg.put("fp", "01000");
        reg.put("s1", "01001");
        reg.put("a0", "01010");
        reg.put("a1", "01011");
        reg.put("a2", "01100");
        reg.put("a3", "01101");
        reg.put("a4", "01110");
        reg.put("a5", "01111");
        reg.put("a6", "10000");
        reg.put("a7", "10001");
        reg.put("s2", "10010");
        reg.put("s3", "10011");
        reg.put("s4", "10100");
        reg.put("s5", "10101");
        reg.put("s6", "10110");
        reg.put("s7", "10111");
        reg.put("s8", "11000");
        reg.put("s9", "11001");
        reg.put("s10", "11010");
        reg.put("s11", "11011");
        reg.put("t3", "11100");
        reg.put("t4", "11101");
        reg.put("t5", "11110");
        reg.put("t6", "11111");
        commandMap.put("lui", 2);
        commandMap.put("auipc", 2);
        commandMap.put("jal", 2);
        commandMap.put("jalr", 3);
        commandMap.put("beq", 3);
        commandMap.put("bne", 3);
        commandMap.put("blt", 3);
        commandMap.put("bge", 3);
        commandMap.put("bltu", 3);
        commandMap.put("bgeu", 3);
        commandMap.put("lb", 3);
        commandMap.put("lh", 3);
        commandMap.put("lw", 3);
        commandMap.put("lbu", 3);
        commandMap.put("lhu", 3);
        commandMap.put("", 0);
        commandMap.put("sb", 3);
        commandMap.put("sh", 3);
        commandMap.put("sw", 3);
        commandMap.put("addi", 3);
        commandMap.put("slti", 3);
        commandMap.put("sltiu", 3);
        commandMap.put("xori", 3);
        commandMap.put("ori", 3);
        commandMap.put("andi", 3);
        commandMap.put("slli", 3);
        commandMap.put("srli", 3);
        commandMap.put("srai", 3);
        commandMap.put("add", 3);
        commandMap.put("sub", 3);
        commandMap.put("sll", 3);
        commandMap.put("slt", 3);
        commandMap.put("sltu", 3);
        commandMap.put("xor", 3);
        commandMap.put("srl", 3);
        commandMap.put("sra", 3);
        commandMap.put("or", 3);
        commandMap.put("and", 3);
        commandMap.put("mul", 3);
        commandMap.put("mulh", 3);
        commandMap.put("mulhsu", 3);
        commandMap.put("mulhu", 3);
        commandMap.put("div", 3);
        commandMap.put("divu", 3);
        commandMap.put("rem", 3);
        commandMap.put("remu", 3);
        commandMap.put("fence", 2);
        commandMap.put("fence.tso", 0);
        commandMap.put("pause", 0);
        commandMap.put("ecall", 0);
        commandMap.put("ebreak", 0);
        fenceMap.put('i', 8);
        fenceMap.put('o', 4);
        fenceMap.put('r', 2);
        fenceMap.put('w', 1);
        try {
            Scanner scanner = new Scanner(new File(filename));
            ArrayList<String> argument = new ArrayList<>();
//        ПЕРЕПИСАТЬ ПРОВЕРКУ НА ДОБАВЛЕНИЕ В МАССИВ
            boolean comma = false;
            boolean commandIf = false;
            while (scanner.hasNext()) {
                String object2 = scanner.next();
                String[] elements = object2.split(",");
                if (object2.equals(",")) {
                    if (comma) {
                        throw new MyException("Empty argument");
                    }
                    comma = true;
                    continue;
                }

                for (int n = 0; n < elements.length - 1; n++) {
                    elements[n] = elements[n] + ",";
                }
                if (object2.endsWith(",")) {
                    elements[elements.length - 1] = elements[elements.length - 1] + ",";
                }
                for (String object : elements) {
                    if (object2.equals("")) {
                        throw new MyException("Empty argument");
                    }
                    if (object.startsWith(",") && !commandIf) {
                        if (!comma) {
                            object = object.substring(1).toLowerCase();
                            if (!object.isEmpty()) {
                                comma = false;
                            } else {
                                comma = true;
                            }
                        } else {
                            throw new MyException("Argument is empty");
                        }
                    } else {
                        if (commandIf && comma || commandIf && object.startsWith(",") || (object.endsWith(",") && commandMap.getOrDefault(object.substring(0, object.length() - 1), null) != null)) {
                            throw new MyException("Comma after command");
                        } else if (!comma && !(object.startsWith(",")) && !commandIf && commandMap.getOrDefault(object, null) == null) {
                            throw new MyException("No comma between args");
                        } else if (commandMap.getOrDefault(object, null) != null && (object.startsWith(",") || comma)) {
                            throw new MyException("Comma before command");
                        }
                        comma = false;
                    }
                    if (!object.isEmpty() && object.charAt(object.length() - 1) == ',') {
                        object = object.substring(0, object.length() - 1);
                        if (commandMap.getOrDefault(object, null) != null) {
                            throw new MyException("After command there is a comma, empty argument");
                        }
                        comma = true;
                    }
                    if (isInteger(object, 10)) {
                        commandIf = false;
                        argument.add(Integer.toBinaryString(Integer.parseInt(object)));
                    } else if (object.startsWith("0x") || object.startsWith("-0x")) {
                        commandIf = false;
                        int index = object.indexOf("(");
                        if (index != -1) {
                            argument.add(Integer.toBinaryString(Integer.parseInt(object.substring(2, index), 16)));
                            argument.add(object.substring(index + 1, object.length() - 1));
                        } else {
//                    System.err.println(object);
                            if (object.startsWith("-")) {
                                argument.add(Integer.toBinaryString(-Integer.parseInt(object.substring(3), 16)));
                            } else {
                                argument.add(Integer.toBinaryString(Integer.parseInt(object.substring(2), 16)));
                            }
                        }
                    } else if (!object.isEmpty()) {
                        int index = object.indexOf("(");
                        if (index != -1) {
                            argument.add(Integer.toBinaryString(Integer.parseInt(object.substring(0, index))));
                            argument.add(object.substring(index + 1, object.length() - 1));
                        } else {
//                    System.err.println(object);
                            argument.add(object);
                        }
                        if (commandMap.getOrDefault(object, null) != null) {
                            commandIf = true;
                        } else {
                            commandIf = false;
                        }
//                System.err.println(object);

                    }
                }
            }
            int i = 0;
            int numberargs = 0;
            String binaryString = "";
            ArrayList<String> parameter;
            while (i < argument.size()) {
                parameter = new ArrayList<>();
                String command = argument.get(i).toLowerCase();
                int j = commandMap.get(command);
                for (int k = 0; k < j; k++) {
                    parameter.add(argument.get(i + k + 1));
                }
                arg = new String[j + 1];
                arg[0] = command;
                i += j + 1;
                if (command.equals("lui") || command.equals("auipc")) {
                    if (command.equals("lui")) {
                        binaryString = lui_parse(true, parameter);
                    } else {
                        binaryString = lui_parse(false, parameter);
                    }
                } else if (command.equals("jal")) {
                    binaryString = jal_parse(parameter);
                } else if (command.equals("jalr")) {
                    binaryString = jalr_parse(parameter);
                } else if (command.equals("beq") || command.equals("bne") || command.equals("blt") || command.equals("bge") || command.equals("bltu") || command.equals("bgeu")) {
                    binaryString = b_parse(command, parameter);
                } else if (command.equals("lb") || command.equals("lh") || command.equals("lw") || command.equals("lbu") || command.equals("lhu")) {
                    binaryString = l_parse(command, parameter);
                } else if (command.equals("sb") || command.equals("sh") || command.equals("sw")) {
                    binaryString = s_parse(command, parameter);
                } else if (command.equals("addi") || command.equals("slti") || command.equals("sltiu") || command.equals("xori") || command.equals("ori") || command.equals("andi")) {
                    binaryString = a_parse(command, parameter);
                } else if (command.equals("slli") || command.equals("srli") || command.equals("srai")) {
                    binaryString = s_long_parse(command, parameter);
                } else if (command.equals("add") || command.equals("sub") || command.equals("sll") || command.equals("slt") || command.equals("sltu") || command.equals("xor") || command.equals("or") || command.equals("srl") || command.equals("sra") || command.equals("and")) {
                    binaryString = operate_parse(command, parameter);
                } else if (command.equals("mul") || command.equals("mulh") || command.equals("mulhsu") || command.equals("mulhu") || command.equals("div") || command.equals("divu") || command.equals("rem") || command.equals("remu")) {
                    binaryString = mul_parse(command, parameter);
                } else if (command.equals("fence.tso") || command.equals("pause") || command.equals("ecall") || command.equals("ebreak")) {
                    binaryString = fence_parse(command);
                } else if (command.equals("fence")) {
                    String pred = parameter.get(0).toLowerCase();
                    String succ = parameter.get(1).toLowerCase();
                    if (!isInteger(pred, 2)) {
                        int sum = 0;
                        for (int g = 0; g < pred.length(); g++) {
                            sum += fenceMap.get(pred.charAt(g));
                        }
                        pred = Integer.toBinaryString(sum);
                        arg[1] = pred;
                    }
                    if (!isInteger(succ, 2)) {
                        int sum = 0;
                        for (int g = 0; g < succ.length(); g++) {
                            sum += fenceMap.get(succ.charAt(g));
                        }
                        succ = Integer.toBinaryString(sum);
                        arg[2] = succ;
                    }
                    pred = zeros(pred, 4);
                    succ = zeros(succ, 4);
                    binaryString = pred + succ + "0000000000" + "0000000" + "0001111";
                } else {
                    throw new MyException("Wrong number of args");
                }
                memory[pointer] = ExecuteCommands.parseBin(binaryString);
                pointer++;
                String byt;
                byte[] resultBites = new byte[4];
                for (int h = 0; h < 4; h++) {
                    if (h != 3) {
                        byt = binaryString.substring(h * 8, (h + 1) * 8);
                    } else {
                        byt = binaryString.substring(h * 8);
                    }
                    resultBites[3 - h] = (byte) (Integer.parseInt(byt, 2)); // Truncation happens here
                }
                instructions[numberargs] = arg;
                numberargs++;
                forwrite.add(resultBites);

            }
            ExecuteCommands executor = new ExecuteCommands(instructions, memory, memory);
            executor.execute();
            String formattedCommand1 = (executor.triesCommand == 0) ? "nan%" : String.format("%3.5f%%",  (double) executor.successCommand1 / executor.triesCommand * 100);
            String formattedCommand2 = (executor.triesCommand == 0) ? "nan%" : String.format("%3.5f%%",  (double) executor.successCommand2 / executor.triesCommand * 100);
            String formatted1 = (executor.triesData == 0 && executor.triesCommand == 0) ? "nan%" : String.format("%3.5f%%",  (double) (executor.successData1+executor.successCommand1) / (executor.triesData+executor.triesCommand) * 100);
            String formatted2 = (executor.triesData == 0 && executor.triesCommand == 0) ? "nan%" : String.format("%3.5f%%",  (double) (executor.successData2+executor.successCommand2) / (executor.triesData+executor.triesCommand) * 100);
            // Проверка, чтобы избежать деления на ноль
            String formattedData1 = (executor.triesData == 0) ? "nan%" : String.format("%3.5f%%",  (double) executor.successData1 / executor.triesData * 100);
            String formattedData2 = (executor.triesData == 0) ? "nan%" : String.format("%3.5f%%",  (double) executor.successData2 / executor.triesData * 100);
            System.out.printf("replacement\thit rate\thit rate (inst)\thit rate (data)\n" +
                    "        LRU\t%s%%\t%s%%\t%s%%\n", formatted1, formattedCommand1, formattedData1);
            System.out.printf(
                    "       pLRU\t%s%%\t%s%%\t%s%%\n", formatted2,formattedCommand2, formattedData2);
            toWrite(output);
            scanner.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }


    }
    public static void toWrite(String name) throws IOException {
        FileOutputStream fos = new FileOutputStream(name);
        for(byte[] resultBites: forwrite ){
            fos.write(resultBites);
        }


    }
    public static boolean isInteger(String str, int radix) {
        try {
            if (radix == 2){
                ExecuteCommands.parseBin(str);
            }else{
                Integer.parseInt(str, radix);
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String jal_parse(ArrayList<String> parameter) throws MyException {
        String opcode = "1101111";
        String register = parameter.get(0);
        arg[1] = reg.get(register);
        arg[2] = zeros(parameter.get(1),32);
        String imm = zeros(parameter.get(1), 21);

        if (isInteger(imm, 2) && check_register(register)) {
            return imm.charAt(0) + imm.substring(10, 20) + imm.charAt(9) + imm.substring(1, 9) + reg.get(register) + opcode;
        } else {
            throw new MyException("Wrong number of args");
        }
    }

    public static String jalr_parse(ArrayList<String> parameter) throws MyException {
        String opcode = "1100111";
        String registerd = parameter.get(0);
        String register1 = parameter.get(1);
        arg[3] = zeros(parameter.get(2),32);
        String imm = zeros(parameter.get(2), 12);
        arg[1] = reg.get(registerd);
        arg[2] = reg.get(register1);

        if (isInteger(imm, 2) && check_register(register1) && check_register(registerd)) {
            return imm + reg.get(register1) + "000" + reg.get(registerd) + opcode;
        } else {
            throw new MyException("Wrong number of args");
        }
    }

    public static String b_parse(String command, ArrayList<String> parameter) throws MyException {
        String opcode = "1100011";
        String funct;
        if (command.equals("beq")) {
            funct = "000";
        } else if (command.equals("bne")) {
            funct = "001";
        } else if (command.equals("blt")) {
            funct = "100";
        } else if (command.equals("bge")) {
            funct = "101";
        } else if (command.equals("bltu")) {
            funct = "110";
        } else {
            funct = "111";
        }
        String register1 = parameter.get(0);
        String register2 = parameter.get(1);
        arg[3] = zeros(parameter.get(2),32);
        String imm = zeros(parameter.get(2), 13);
        arg[1] = reg.get(register1);
        arg[2] = reg.get(register2);

        if (isInteger(imm, 2) && check_register(register2) && check_register(register1)) {
            return imm.charAt(0) + imm.substring(2, 8) + reg.get(register2) + reg.get(register1) + funct + imm.substring(8, 12) + imm.charAt(1) + opcode;
        } else {
            throw new MyException("Wrong number of args");
        }
    }

    public static String s_parse(String command, ArrayList<String> parameter) throws MyException {
        String opcode = "0100011";
        String funct;
        if (command.equals("sb")) {
            funct = "000";
        } else if (command.equals("sh")) {
            funct = "001";
        } else {
            funct = "010";
        }
        String register2 = parameter.get(0);
        String register1 = parameter.get(2);
        arg[2] = zeros(parameter.get(1), 32);
        String imm = zeros(parameter.get(1), 12);
        arg[1] = reg.get(register2);
        arg[3] = reg.get(register1);

        if (isInteger(imm, 2) && check_register(register2) && check_register(register1)) {
            return imm.substring(0, 7) + reg.get(register2) + reg.get(register1) + funct + imm.substring(7) + opcode;
        } else {
            throw new MyException("Wrong number of args");
        }

    }

    public static String s_long_parse(String command, ArrayList<String> parameter) throws MyException {
        String opcode = "0010011";
        String inn = "0000000";
        String funct;
        if (command.equals("slli")) {
            funct = "001";
        } else if (command.equals("srli")) {
            funct = "101";
        } else {
            inn = "0100000";
            funct = "101";
        }
        String registerd = parameter.get(0);
        String register1 = parameter.get(1);
        arg[3] = zeros(parameter.get(2),32);
        String imm = zeros(parameter.get(2), 5);
        arg[1] = reg.get(registerd);
        arg[2] = reg.get(register1);

        if (isInteger(imm, 2) && check_register(registerd) && check_register(register1)) {
            return inn + imm + reg.get(register1) + funct + reg.get(registerd) + opcode;
        } else {
            throw new MyException("Wrong number of args");
        }

    }

    public static String mul_parse(String command, ArrayList<String> parameter) throws MyException {
        String opcode = "0110011";
        String imm = "0000001";
        String funct;
        if (command.equals("mul")) {
            funct = "000";
        } else if (command.equals("mulh")) {
            funct = "001";
        } else if (command.equals("mulhu")) {
            funct = "011";
        } else if (command.equals("mulhsu")) {
            funct = "010";
        } else if (command.equals("div")) {
            funct = "100";
        } else if (command.equals("divu")) {
            funct = "101";
        } else if (command.equals("rem")) {
            funct = "110";
        } else {
            funct = "111";
        }
        String registerd = parameter.get(0);
        String register1 = parameter.get(1);
        String register2 = parameter.get(2);
        arg[1] = reg.get(registerd);
        arg[2] = reg.get(register1);
        arg[3] = reg.get(register2);
        if (check_register(registerd) && check_register(register1) && check_register(registerd)) {
            return imm + reg.get(register2) + reg.get(register1) + funct + reg.get(registerd) + opcode;
        } else {
            throw new MyException("Wrong number of args");
        }


    }

    public static String l_parse(String command, ArrayList<String> parameter) throws MyException {
        String opcode = "0000011";
        String funct;
        if (command.equals("lb")) {
            funct = "000";
        } else if (command.equals("lh")) {
            funct = "001";
        } else if (command.equals("lw")) {
            funct = "010";

        } else if (command.equals("lbu")) {
            funct = "100";
        } else {
            funct = "101";
        }
        String registerd = parameter.get(0);
        String register1 = parameter.get(2);
        arg[3] = zeros(parameter.get(1),32);
        String imm = zeros(parameter.get(1), 12);
        arg[1] = reg.get(registerd);
        arg[2] = reg.get(register1);

        if (isInteger(imm, 2) && check_register(registerd) && check_register(register1)) {
            return imm + reg.get(register1) + funct + reg.get(registerd) + opcode;
        } else {
            throw new MyException("Wrong number of args");
        }

    }

    public static String a_parse(String command, ArrayList<String> parameter) throws MyException {
        String opcode = "0010011";
        String funct;
        if (command.equals("addi")) {
            funct = "000";
        } else if (command.equals("slti")) {
            funct = "010";
        } else if (command.equals("sltiu")) {
            funct = "011";
        } else if (command.equals("xori")) {
            funct = "100";
        } else if (command.equals("ori")) {
            funct = "110";
        } else {
            funct = "111";
        }
        String registerd = parameter.get(0);
        String register1 = parameter.get(1);
        arg[3] = zeros(parameter.get(2),32);
        String imm = zeros(parameter.get(2), 12);
        arg[1] = reg.get(registerd);
        arg[2] = reg.get(register1);
        if (isInteger(imm, 2) && check_register(registerd) && check_register(register1)) {
            return imm + reg.get(register1) + funct + reg.get(registerd) + opcode;
        } else {
            throw new MyException("Wrong number of args");
        }

    }

    public static String fence_parse(String command) throws MyException {
        String opcode = "0001111";
        String funct;
        if (command.equals("fence.tso")) {
            funct = "100000110011";
        } else if (command.equals("pause")) {
            funct = "000000010000";
        } else if (command.equals("ecall")) {
            funct = "000000000000";
            opcode = "1110011";
        } else {
            funct = "000000000001";
            opcode = "1110011";
        }
        return funct + "0000000000000" + opcode;

    }

    public static String operate_parse(String command, ArrayList<String> parameter) throws MyException {
        String opcode = "0110011";
        String funct;
        String imm = "0000000";
        if (command.equals("add")) {
            funct = "000";
        } else if (command.equals("sub")) {
            imm = "0100000";
            funct = "000";
        } else if (command.equals("sll")) {
            funct = "001";
        } else if (command.equals("slt")) {
            funct = "010";
        } else if (command.equals("sltu")) {
            funct = "011";
        } else if (command.equals("xor")) {
            funct = "100";
        } else if (command.equals("srl")) {
            funct = "101";
        } else if (command.equals("sra")) {
            imm = "0100000";
            funct = "101";
        } else if (command.equals("or")) {
            funct = "110";
        } else {
            funct = "111";
        }
        String registerd = parameter.get(0);
        String register1 = parameter.get(1);
        String register2 = parameter.get(2);
        arg[1] = reg.get(registerd);
        arg[2] = reg.get(register1);
        arg[3] = reg.get(register2);
        if (check_register(registerd) && check_register(register1) && check_register(registerd)) {
            return imm + reg.get(register2) + reg.get(register1) + funct + reg.get(registerd) + opcode;
        } else {
            throw new MyException("Wrong number of args");
        }

    }

    public static String lui_parse(boolean command, ArrayList<String> parameter) throws MyException {
        String opcode;
        if (command) {
            opcode = "0110111";
        } else {
            opcode = "0010111";
        }
        String register = parameter.get(0);
        arg[1] = reg.get(register);
        arg[2] = zeros(parameter.get(1),32);
        String inn = zeros(parameter.get(1)+"000000000000",32);
        if (isInteger(inn, 2) && check_register(register)) {
            return (inn).substring(0, 20) + reg.get(register) + opcode;
        } else {
            throw new MyException("Wrong number of args");
        }

    }

    public static String zeros(String str, int strLen) {
        while (str.length() < strLen) {
            str = "0" + str;
        }
        if (str.length() > strLen) {
            str = str.substring(str.length() - strLen);
        }
        return str;
    }

    public static boolean check_register(String register) {
        String result = reg.getOrDefault(register, null);
        if (result == null) {
            return false;
        }
        return true;
    }
}
