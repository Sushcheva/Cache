import java.util.Arrays;

public class ExecuteCommands {
    private static final int NUM_REGISTERS = 32;
    private static final int MEM_SIZE = 1024; // Размер памяти (можно изменить)

    private int[] registers = new int[32];
    private String[][] instructions;
    private int[] mem1;
    private int[] mem2;
    private int[][][] cacheLRU = new int[16][4][8];
    private int[][][] cachepLRU = new int[16][4][8];
    private int[][] tagLRU = new int[16][4];
    private int[][] tagpLRU = new int[16][4];
    private int[][] usageLRU = new int[16][4];
    private int[][] usagepLRU = new int[16][4];
    private int[][] bitpLRU = new int[16][4];
    private int[][] priorityLRU = new int[16][4];
    public int triesCommand = 0;
    public int triesData = 0;
    public int successData1 = 0;
    public int successData2 = 0;
    public int successCommand1 = 0;
    public int successCommand2 = 0;
    public int pc;
    private boolean success1 = false;
    private boolean success2 = false;

    public final int CONST_OFF = 65536;

    public ExecuteCommands(String[][] instructions, int[] mem1, int[] mem2) {
        this.instructions = instructions;
        this.mem1 = mem1;
        this.mem2 = mem2;
        this.pc = 0;
    }

    public void execute() {
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 4; j++) {
                tagLRU[i][j] = -1;
                tagpLRU[i][j] = -1;
            }
        }
        while (pc < instructions.length && instructions[pc] != null) {
            System.out.println(pc);
            String[] parts = instructions[pc];
            triesCommand += 1;
            getInstructionsCache1(pc * 4 + CONST_OFF);
            getInstructionsCache2(pc * 4 + CONST_OFF);
            String opcode = parts[0].toLowerCase();
            try {
                switch (opcode) {
                    case "lui":
                        executeLUI(parts);
                        break;
                    case "auipc":
                        executeAUIPC(parts);
                        break;
                    case "jal":
                        executeJAL(parts);
                        break;
                    case "jalr":
                        executeJALR(parts);
                        break;
                    case "beq":
                        executeBEQ(parts);
                        break;
                    case "bne":
                        executeBNE(parts);
                        break;
                    case "blt":
                        executeBLT(parts);
                        break;
                    case "bge":
                        executeBGE(parts);
                        break;
                    case "mul":
                        executeMUL(parts);
                        break;
                    case "mulh":
                        executeMULH(parts);
                        break;
                    case "mulhu":
                        executeMULHU(parts);
                        break;
                    case "mulhsu":
                        executeMULHSU(parts);
                        break;
                    case "divu":
                        executeDIVU(parts);
                        break;
                    case "div":
                        executeDIV(parts);
                        break;
                    case "rem":
                        executeREM(parts);
                        break;
                    case "remu":
                        executeREMU(parts);
                        break;
                    case "bltu":
                        executeBLTU(parts);
                        break;
                    case "bgeu":
                        executeBGEU(parts);
                        break;
                    case "lb":
                        executeLB(parts);
                        break;
                    case "lh":
                        executeLH(parts);
                        break;
                    case "lw":
                        executeLW(parts);
                        break;
                    case "lbu":
                        executeLBU(parts);
                        break;
                    case "lhu":
                        executeLHU(parts);
                        break;
                    case "sb":
                        executeSB(parts);
                        break;
                    case "sh":
                        executeSH(parts);
                        break;
                    case "sw":
                        executeSW(parts);
                        break;
                    case "addi":
                        executeADDI(parts);
                        break;
                    case "slti":
                        executeSLTI(parts);
                        break;
                    case "sltiu":
                        executeSLTIU(parts);
                        break;
                    case "xori":
                        executeXORI(parts);
                        break;
                    case "ori":
                        executeORI(parts);
                        break;
                    case "andi":
                        executeANDI(parts);
                        break;
                    case "slli":
                        executeSLLI(parts);
                        break;
                    case "srli":
                        executeSRLI(parts);
                        break;
                    case "srai":
                        executeSRAI(parts);
                        break;
                    case "add":
                        executeADD(parts);
                        break;
                    case "sub":
                        executeSUB(parts);
                        break;
                    case "sll":
                        executeSLL(parts);
                        break;
                    case "slt":
                        executeSLT(parts);
                        break;
                    case "sltu":
                        executeSLTU(parts);
                        break;
                    case "xor":
                        executeXOR(parts);
                        break;
                    case "srl":
                        executeSRL(parts);
                        break;
                    case "sra":
                        executeSRA(parts);
                        break;
                    case "or":
                        executeOR(parts);
                        break;
                    case "ecall":
                        break;
                    case "ebreak":
                        break;
                    case "pause":
                        break;
                    case "fence":
                        break;
                    case "fence.tso":
                        break;
                    case "and":
                        executeAND(parts);
                        break;
                    default:
                        System.err.println("Unknown instruction at pc=" + pc + ": " + opcode);
                }
            } catch (Exception e) {
                System.err.println("Error executing instruction at pc=" + pc + ": " + opcode);
                e.printStackTrace();
                break;
            }

            if (!isJumpOrBranch(opcode)) {
                pc++;
            }
        }
    }

    private int getDataByte(int address) {
        triesData += 1;
        int first = getDataCache1(address);
        int first1 = getDataCache2(address);
        return first;
    }

    private int getDataHalf(int address) {
        triesData += 1;
        int first = getDataCache1((address / 2) * 2);
        int second = getDataCache1((address / 2) * 2 + 1);
        int first1 = getDataCache2((address / 2) * 2);
        int second1 = getDataCache2((address / 2) * 2 + 1);
        successData1 -= 1;
        successData2 -= 1;
        return parseBin(Integer.toBinaryString(first) + Integer.toBinaryString(second));


    }

    private int getDataWord(int address) {
        triesData += 1;
        int first = getDataCache1((address / 4) * 4);
        int second = getDataCache1((address / 4) * 4 + 1);
        int third = getDataCache1((address / 4) * 4 + 2);
        int fourth = getDataCache1((address / 4) * 4 + 3);
        int first1 = getDataCache2((address / 4) * 4);
        int second1 = getDataCache2((address / 4) * 4 + 1);
        int third1 = getDataCache2((address / 4) * 4 + 2);
        int fourth1 = getDataCache2((address / 4) * 4 + 3);
        successData1 -= 3;
        successData2 -= 3;
        return parseBin(Integer.toBinaryString(first) + Integer.toBinaryString(second) + Integer.toBinaryString(third) + Integer.toBinaryString(fourth));

    }

    private void loadByte(int significant, int address) {
        triesData += 1;
        loadByteCache1(significant, address);
        loadByteCache2(significant, address);
    }

    private void loadHalf(int significant, int address) {
        triesData += 1;
        loadByteCache1(significant, (address / 2) * 2);
        loadByteCache2(significant, (address / 2) * 2);
        loadByteCache1(significant, (address / 2) * 2 + 1);
        loadByteCache2(significant, (address / 2) * 2 + 1);
        successData1 -= 1;
        successData2 -= 1;

    }

    private void loadWord(int significant, int address) {
        triesData += 1;
        loadByteCache1(significant, (address / 4) * 4);
        loadByteCache2(significant, (address / 4) * 4);
        loadByteCache1(significant, (address / 4) * 4 + 1);
        loadByteCache2(significant, (address / 4) * 4 + 1);
        loadByteCache1(significant, (address / 4) * 4 + 2);
        loadByteCache2(significant, (address / 4) * 4 + 2);
        loadByteCache1(significant, (address / 4) * 4 + 3);
        loadByteCache2(significant, (address / 4) * 4 + 3);
        successData1 -= 3;
        successData2 -= 3;

    }

    private void loadByteCache1(int significant, int address) {
        int set = (address / 32) % 16;
        int tag = (address / 512);
        int offset = (address % 32);
        int dataSet = 0;
        boolean result = false;
        for (int i = 0; i < 4; i++) {
            if (tag == tagLRU[set][i]) {
                result = true;
                successData1 += 1;
                usageLRU[set][i] = 1;
                if(offset%4 != 3){
                    cacheLRU[set][i][(offset / 4)] = Integer.parseInt(Machine.zeros(Integer.toBinaryString(cacheLRU[set][i][(offset / 4)]), 32).substring(0, (offset % 4) * 8) + Machine.zeros(Integer.toBinaryString(significant), 8) + Machine.zeros(Integer.toBinaryString(cacheLRU[set][i][(offset / 4)]), 32).substring((offset % 4 + 1) * 8));
                }
                else{
                    cacheLRU[set][i][(offset / 4)] = Integer.parseInt(Machine.zeros(Integer.toBinaryString(cacheLRU[set][i][(offset / 4)]), 32).substring(0, (offset % 4) * 8) + Machine.zeros(Integer.toBinaryString(significant), 8));
                }
                priorityLRU[set][i] += 1;
                break;
            }
        }
        if (!result) {
            result = false;
            for (int j = 0; j < 4; j++) {
                if (tagLRU[set][j] == -1) {
                    result = true;
                    tagLRU[set][j] = tag;
                    priorityLRU[set][j] += 1;
                    usageLRU[set][j] = 1;
                    for (int k = 0; k < 8; k++) {
                        cacheLRU[set][j][k] = mem1[(address / 32 * 32) / 4 + k];
                    }
                    cacheLRU[set][j][(offset / 4) % 8] = Integer.parseInt(Machine.zeros(Integer.toBinaryString(cacheLRU[set][j][(offset / 4) % 8]), 32).substring(0, (offset % 4) * 8) + Machine.zeros(Integer.toBinaryString(significant), 8) + Machine.zeros(Integer.toBinaryString(cacheLRU[set][j][(offset / 4) % 8]), 32).substring((offset % 4 + 1) * 8));

                    break;
                }
            }
            if (result == false) {
                int minprior = Integer.MAX_VALUE;
                int j1 = 0;
                for (int j = 0; j < 4; j++) {
                    if (priorityLRU[set][j] < minprior) {
                        j1 = j;
                        minprior = priorityLRU[set][j];
                    }
                }

                tagLRU[set][j1] = tag;
                priorityLRU[set][j1] += 1;
                for (int k = 0; k < 8; k++) {
                    mem1[(tag * 32 * 16 + set * 32) / 4 + k] = cacheLRU[set][j1][k];
                }
                usageLRU[set][j1] = 1;
                for (int k = 0; k < 8; k++) {
                    cacheLRU[set][j1][k] = mem1[(address / 32 * 32) / 4 + k];
                }
                cacheLRU[set][j1][(offset / 4) % 8] = Integer.parseInt(Machine.zeros(Integer.toBinaryString(cacheLRU[set][j1][(offset / 4) % 8]), 32).substring(0, (offset % 4) * 8) + Machine.zeros(Integer.toBinaryString(significant), 8) + Machine.zeros(Integer.toBinaryString(cacheLRU[set][j1][(offset / 4) % 8]), 32).substring((offset % 4 + 1) * 8));

            }
        }

    }

    private void loadByteCache2(int significant, int address) {
        int set = (address / 32) % 16;
        int tag = (address / 512);
        int offset = (address % 32);
        boolean result = false;
        for (int i = 0; i < 4; i++) {
            if (tag == tagpLRU[set][i]) {
                result = true;
                successData2 += 1;
                bitpLRU[set][i] = 1;
                usageLRU[set][i] = 1;
                int c = 0;
                for (int k = 0; k < 4; k++) {
                    c += bitpLRU[set][k];
                }
                if (c == 4) {
                    for (int k = 0; k < 4; k++) {
                        bitpLRU[set][k] = 0;
                    }
                    bitpLRU[set][i] = 1;
                }
                if(offset%4 != 3){
                    cachepLRU[set][i][(offset / 4)] = Integer.parseInt(Machine.zeros(Integer.toBinaryString(cachepLRU[set][i][(offset % 4)]), 32).substring(0, (offset % 4) * 8) + Machine.zeros(Integer.toBinaryString(significant), 8) + Machine.zeros(Integer.toBinaryString(cachepLRU[set][i][(offset / 4)]), 32).substring((offset % 4 + 1) * 8));
                }
                else{
                    cachepLRU[set][i][(offset / 4)] = Integer.parseInt(Machine.zeros(Integer.toBinaryString(cachepLRU[set][i][(offset % 4)]), 32).substring(0, (offset % 4) * 8) + Machine.zeros(Integer.toBinaryString(significant), 8));
                }
            }
        }
        if (!result) {
            result = false;
            for (int j = 0; j < 4; j++) {
                if (tagpLRU[set][j] == -1) {
                    result = true;
                    usageLRU[set][j] = 1;
                    tagpLRU[set][j] = tag;
                    bitpLRU[set][j] = 1;
                    int c = 0;
                    for (int k = 0; k < 4; k++) {
                        c += bitpLRU[set][k];
                    }
                    if (c == 4) {
                        for (int k = 0; k < 4; k++) {
                            bitpLRU[set][k] = 0;
                        }
                        bitpLRU[set][j] = 1;
                    }
                    for (int k = 0; k < 8; k++) {
                        cachepLRU[set][j][k] = mem2[(address / 32 * 32) / 4 + k];
                    }
                    cachepLRU[set][j][(offset / 4) % 8] = Integer.parseInt(Machine.zeros(Integer.toBinaryString(cachepLRU[set][j][(offset / 4) % 8]), 32).substring(0, (offset % 4) * 8) + Machine.zeros(Integer.toBinaryString(significant), 8) + Machine.zeros(Integer.toBinaryString(cachepLRU[set][j][(offset / 4) % 8]), 32).substring((offset % 4 + 1) * 8));
                    break;
                }
            }
            if (result == false) {
                for (int j = 0; j < 4; j++) {
                    if (bitpLRU[set][j] == 0) {
                        for (int k = 0; k < 8; k++) {
                            mem2[(tag * 32 * 16 + set * 32) / 4 + k] = cachepLRU[set][j][k];
                        }
                        tagpLRU[set][j] = tag;
                        bitpLRU[set][j] = 1;
                        int c = 0;
                        for (int k = 0; k < 4; k++) {
                            c += bitpLRU[set][k];
                        }
                        if (c == 4) {
                            for (int k = 0; k < 4; k++) {
                                bitpLRU[set][k] = 0;
                            }
                            bitpLRU[set][j] = 1;
                        }
                        for (int k = 0; k < 8; k++) {
                            cachepLRU[set][j][k] = mem2[(address / 32 * 32) / 4 + k];
                        }
                        usageLRU[set][j] = 1;
                        cachepLRU[set][j][(offset / 4) % 8] = Integer.parseInt(Machine.zeros(Integer.toBinaryString(cachepLRU[set][j][(offset / 4) % 8]), 32).substring(0, (offset % 4) * 8) + Machine.zeros(Integer.toBinaryString(significant), 8) + Machine.zeros(Integer.toBinaryString(cachepLRU[set][j][(offset / 4) % 8]), 32).substring((offset % 4 + 1) * 8));

                        break;
                    }
                }
            }
        }


    }

    private int getDataCache1(int address) {

        int set = (address / 32) % 16;
        int tag = (address / 512);
        int offset = (address % 32);
        int dataSet = 0;
        boolean result = false;
        for (int i = 0; i < 4; i++) {
            if (tag == tagLRU[set][i]) {
                result = true;
                successData1 += 1;
                priorityLRU[set][i] += 1;
                dataSet = Integer.parseInt(Machine.zeros(Integer.toBinaryString(cacheLRU[set][i][(offset / 4)]), 32).substring((offset % 4) * 8, (offset % 4 + 1) * 8));
                break;
            }
        }
        if (!result) {
            result = false;
            dataSet = Integer.parseInt(Machine.zeros(Integer.toBinaryString(mem1[address / 4]), 32).substring((offset % 4) * 8, (offset % 4 + 1) * 8));
            for (int j = 0; j < 4; j++) {
                if (tagLRU[set][j] == -1) {
                    result = true;
                    tagLRU[set][j] = tag;
                    priorityLRU[set][j] += 1;
                    usageLRU[set][j] = 0;
                    for (int k = 0; k < 8; k++) {
                        cacheLRU[set][j][k] = mem1[(address/32*32) / 4 + k];
                    }
                    break;
                }
            }
            if (result == false) {
                int minprior = Integer.MAX_VALUE;
                int j1 = 0;
                for (int j = 0; j < 4; j++) {
                    if (priorityLRU[set][j] < minprior) {
                        j1 = j;
                        minprior = priorityLRU[set][j];
                    }
                }
                tagLRU[set][j1] = tag;
                priorityLRU[set][j1] += 1;
                for (int k = 0; k < 8; k++) {
                    mem1[(tag * 32 * 16 + set * 32) / 4 + k] = cacheLRU[set][j1][k];
                }
                usageLRU[set][j1] = 0;
                for (int k = 0; k < 8; k++) {
                    cacheLRU[set][j1][k] = mem1[(address / 32 * 32) / 4 + k];
                }
            }
        }
        return dataSet;
    }

    private void getInstructionsCache1(int address) {
        int set = (address / 32) % 16;
        int tag = (address / 512);
        int offset = (address % 32);
        boolean result = false;
        for (int i = 0; i < 4; i++) {
            if (tag == tagLRU[set][i]) {
                result = true;
                successCommand1 += 1;
                priorityLRU[set][i] += 1;
                break;
            }
        }
        if (!result) {
            for (int j = 0; j < 4; j++) {
                if (tagLRU[set][j] == -1) {
                    result = true;
                    tagLRU[set][j] = tag;
                    priorityLRU[set][j] += 1;
                    usageLRU[set][j] = 0;
                    for (int k = 0; k < 8; k++) {
                        cacheLRU[set][j][k] = mem1[(address / 32 * 32) / 4 + k];
                    }
                    break;
                }
            }
            if (result == false) {
                int minprior = Integer.MAX_VALUE;
                int j1 = 0;
                for (int j = 0; j < 4; j++) {
                    if (priorityLRU[set][j] < minprior) {
                        j1 = j;
                        minprior = priorityLRU[set][j];

                    }
                }
                tagLRU[set][j1] = tag;
                priorityLRU[set][j1] += 1;
                for (int k = 0; k < 8; k++) {
                    mem1[(tag * 32 * 16 + set * 32) / 4 + k] = cacheLRU[set][j1][k];
                }
                usageLRU[set][j1] = 0;

                for (int k = 0; k < 8; k++) {
                    cacheLRU[set][j1][k] = mem1[(address / 32 * 32) / 4 + k];
                }
            }

        }

    }

    private void getInstructionsCache2(int address) {
        int set = (address / 32) % 16;
        int tag = (address / 512);
        int offset = (address % 32);
        int dataSet = 0;
        boolean result = false;
        for (int i = 0; i < 4; i++) {
            if (tag == tagpLRU[set][i]) {
                result = true;
                successCommand2 += 1;
                bitpLRU[set][i] = 1;
                int c = 0;
                for (int k = 0; k < 4; k++) {
                    c += bitpLRU[set][k];
                }
                if (c == 4) {
                    for (int k = 0; k < 4; k++) {
                        bitpLRU[set][k] = 0;
                    }
                    bitpLRU[set][i] = 1;
                }
            }
        }
        if (!result) {
            result = false;
            for (int j = 0; j < 4; j++) {
                if (tagpLRU[set][j] == -1) {
                    result = true;
                    tagpLRU[set][j] = tag;
                    bitpLRU[set][j] = 1;
                    int c = 0;
                    for (int k = 0; k < 4; k++) {
                        c += bitpLRU[set][k];
                    }
                    if (c == 4) {
                        for (int k = 0; k < 4; k++) {
                            bitpLRU[set][k] = 0;
                        }
                        bitpLRU[set][j] = 1;
                    }
                    usageLRU[set][j] = 0;
                    for (int k = 0; k < 8; k++) {
                        cachepLRU[set][j][k] = mem2[(address / 32 * 32) / 4 + k];
                    }
                    break;
                }
            }
            if (result == false) {
                for (int j = 0; j < 4; j++) {
                    if (bitpLRU[set][j] == 0) {
                        for (int k = 0; k < 8; k++) {
                            mem2[(tag * 32 * 16 + set * 32) / 4 + k] = cachepLRU[set][j][k];
                        }
                        tagpLRU[set][j] = tag;
                        bitpLRU[set][j] = 1;
                        int c = 0;
                        for (int k = 0; k < 4; k++) {
                            c += bitpLRU[set][k];
                        }
                        if (c == 4) {
                            for (int k = 0; k < 4; k++) {
                                bitpLRU[set][k] = 0;
                            }
                            bitpLRU[set][j] = 1;
                        }
                        usageLRU[set][j] = 0;
                        for (int k = 0; k < 8; k++) {
                            cachepLRU[set][j][k] = mem2[(address / 32 * 32) / 4 + k];
                        }
                        break;
                    }
                }
            }
        }
    }

    private int getDataCache2(int address) {
        int set = (address / 32) % 16;
        int tag = (address / 512);
        int offset = (address % 32);
        int dataSet = 0;
        boolean result = false;
        for (int i = 0; i < 4; i++) {
            if (tag == tagpLRU[set][i]) {
                result = true;
                successData2 += 1;
                bitpLRU[set][i] = 1;
                int c = 0;
                for (int k = 0; k < 4; k++) {
                    c += bitpLRU[set][k];
                }
                if (c == 4) {
                    for (int k = 0; k < 4; k++) {
                        bitpLRU[set][k] = 0;
                    }
                    bitpLRU[set][i] = 1;
                }
                dataSet = Integer.parseInt(Machine.zeros(Integer.toBinaryString(cachepLRU[set][i][(offset / 4)]), 32).substring((offset % 4) * 8, (offset %4 + 1) * 8));
            }
        }
        if (!result) {
            result = false;
            dataSet = Integer.parseInt(Machine.zeros(Integer.toBinaryString(mem2[address / 4]), 32).substring((offset % 4) * 8, (offset % 4 + 1) * 8));
            for (int j = 0; j < 4; j++) {
                if (tagpLRU[set][j] == -1) {
                    result = true;
                    tagpLRU[set][j] = tag;
                    usageLRU[set][j] = 0;
                    for (int k = 0; k < 8; k++) {
                        cachepLRU[set][j][k] = mem2[(address / 32 * 32) / 4 + k];
                    }
                    break;
                }
            }
            if (result == false) {
                for (int j = 0; j < 4; j++) {
                    if (bitpLRU[set][j] == 0) {
                        for (int k = 0; k < 8; k++) {
                            mem2[(tag * 32 * 16 + set * 32) / 4 + k] = cachepLRU[set][j][k];
                        }
                        tagpLRU[set][j] = tag;
                        bitpLRU[set][j] = 1;
                        usageLRU[set][j] = 0;
                        for (int k = 0; k < 8; k++) {
                            cachepLRU[set][j][k] = mem2[(address / 32 * 32) / 4 + k];
                        }
                        break;
                    }
                }
            }
        }
        return dataSet;
    }

    // Проверка, является ли инструкция jump или branch
    private boolean isJumpOrBranch(String opcode) {
        return opcode.startsWith("jal") || opcode.startsWith("beq") || opcode.startsWith("bne") ||
                opcode.startsWith("blt") || opcode.startsWith("bge") || opcode.startsWith("bltu") ||
                opcode.startsWith("bgeu") || opcode.startsWith("jalr");
    }


    // Инструкции
    // Нужно взять первые 20 битов иммедиаты и загрузить в регистр данных
    private void executeLUI(String[] parts) throws Exception {
        // LUI rd, imm
        int rd = parseBin(parts[1]);
        int imm = parseBin(Machine.zeros(parts[2] + "000000000000", 32));
        registers[rd] = imm;
        setZero();

    }


    private void setZero() {
        registers[0] = 0;
    }


    private void executeAUIPC(String[] parts) throws Exception {
        int rd = parseBin(parts[1]);
        int offset = parseBin(Machine.zeros(parts[2] + "000000000000", 32));
        registers[rd] = offset + pc * 4 + CONST_OFF;
        setZero();

    }

    private void executeJAL(String[] parts) throws Exception {
        int rd = parseBin(parts[1]);
        int offset = parseBin(parts[2]);
        registers[rd] = pc * 4 + 4 + CONST_OFF;
        setZero();
        pc += offset / 4;
    }

    private void executeJALR(String[] parts) throws Exception {
        int rd = parseBin(parts[1]);
        int rs1 = parseBin(parts[2]);
        int offset = parseBin(parts[3]);
        registers[rd] = pc * 4 + 4 + CONST_OFF;
        setZero();
        pc = (rs1 + offset - CONST_OFF) / 4; // Clear least significant bit
    }

    // offset/4
    private void executeBEQ(String[] parts) throws Exception {
        // BEQ rs1, rs2, offset
        if (parts.length != 4) throw new Exception("Invalid BEQ syntax");
        int rs1 = parseBin(parts[1]);
        int rs2 = parseBin(parts[2]);
        int offset = parseBin(parts[3]);
        if (registers[rs1] == registers[(rs2)]) {
            pc += offset / 4;
        } else {
            pc++;
        }
    }

    // offset/4
    private void executeBNE(String[] parts) throws Exception {
        // BNE rs1, rs2, offset
        int rs1 = parseBin(parts[1]);
        int rs2 = parseBin(parts[2]);
        int offset = parseBin(parts[3]);
        if (registers[rs1] != registers[rs2]) {
            pc += offset / 4;
        } else {
            pc++;
        }
    }

    // offset/4
    private void executeBLT(String[] parts) throws Exception {
        // BLT rs1, rs2, offset
        int rs1 = parseBin(parts[1]);
        int rs2 = parseBin(parts[2]);
        int offset = parseBin(parts[3]);
        if (registers[rs1] < registers[rs2]) {
            pc += offset / 4;
        } else {
            pc++;
        }
    }

    // offset/4
    private void executeBGE(String[] parts) throws Exception {
        // BGE rs1, rs2, offset
        int rs1 = parseBin(parts[1]);
        int rs2 = parseBin(parts[2]);
        int offset = parseBin(parts[3]);
        if (registers[rs1] >= registers[rs2]) {
            pc += offset / 4;
        } else {
            pc++;
        }
    }

    // offset/4
    private void executeBLTU(String[] parts) throws Exception {
        // BLTU rs1, rs2, offset (Unsigned)
        if (parts.length != 4) throw new Exception("Invalid BLTU syntax");
        int rs1 = parseBin(parts[1]);
        int rs2 = parseBin(parts[2]);
        int offset = parseBin(parts[3]);
        int val1 = registers[rs1];
        int val2 = registers[rs2];
        long unsigned1 = Integer.toUnsignedLong(val1);
        long unsigned2 = Integer.toUnsignedLong(val2);
        if (unsigned1 < unsigned2) {
            pc += offset / 4;
        } else {
            pc++;
        }
    }

    public static int parseBin(String bin) {
        if (bin.length() < 32) {
            return Integer.parseInt(bin, 2);
        } else {
            if (bin.length()>32){
                bin = bin.substring(bin.length()-32);
            }
            return (int) (Integer.parseInt(bin.substring(1), 2) - Character.getNumericValue(bin.charAt(0)) * Math.pow(2, 31));
        }
    }

    public static int parseBinUpdate(String bin, int length) {
        if (bin.length() < length) {
            return Integer.parseInt(bin, 2);
        } else {
            bin = bin.substring(bin.length() - length);

            return (int) (Integer.parseInt(bin.substring(1), 2) - Character.getNumericValue(bin.charAt(0)) * Math.pow(2, length - 1));
        }
    }

    // offset/4
    private void executeBGEU(String[] parts) throws Exception {
        // BGEU rs1, rs2, offset (Unsigned)
        int rs1 = parseBin(parts[1]);
        int rs2 = parseBin(parts[2]);
        int offset = parseBin(parts[3]);
        int val1 = registers[rs1];
        int val2 = registers[rs2];
        long unsigned1 = Integer.toUnsignedLong(val1);
        long unsigned2 = Integer.toUnsignedLong(val2);
        if (unsigned1 >= unsigned2) {
            pc += offset / 4;
        } else {
            pc++;
        }
    }

    private void executeLB(String[] parts) throws Exception {
        // LB rd, offset(rs1)
        int rd = Integer.parseInt(parts[1], 2);

        int offset = parseBin(parts[2]);
        int rs1 = Integer.parseInt(parts[3], 2);
        int address = registers[rs1] + offset;

        // Симулируем загрузку байта как знакованного значения

        int value = parseBinUpdate(Integer.toBinaryString(getDataByte(address)), 8); // Приведение к int сохраняет знак
        registers[rd] = value;
        setZero();


    }

    private void executeLH(String[] parts) throws Exception {
        // LB rd, offset(rs1)
        int rd = Integer.parseInt(parts[1], 2);

        int offset = parseBin(parts[2]);
        int rs1 = Integer.parseInt(parts[3], 2);
        int address = registers[rs1] + offset;

        // Симулируем загрузку байта как знакованного значения

        int value = parseBinUpdate(Integer.toBinaryString(getDataHalf(address)), 16); // Приведение к int сохраняет знак
        registers[rd] = value;
        setZero();


    }

    private void executeLW(String[] parts) throws Exception {
        // LB rd, offset(rs1)
        int rd = Integer.parseInt(parts[1], 2);

        int offset = parseBin(parts[2]);
        int rs1 = Integer.parseInt(parts[3], 2);
        int address = registers[rs1] + offset;

        // Симулируем загрузку байта как знакованного значения

        int value = getDataWord(address); // Приведение к int сохраняет знак
        registers[rd] = value;
        setZero();


    }

    private void executeLBU(String[] parts) throws Exception {
        // LB rd, offset(rs1)
        int rd = Integer.parseInt(parts[1], 2);

        int offset = parseBin(parts[2]);
        int rs1 = Integer.parseInt(parts[3], 2);
        int address = registers[rs1] + offset;

        // Симулируем загрузку байта как знакового значения

        int value = getDataByte(address); // Приведение к int сохраняет знак
        registers[rd] = value;
        setZero();


    }

    private void executeLHU(String[] parts) throws Exception {
        // LB rd, offset(rs1)
        int rd = Integer.parseInt(parts[1], 2);

        int offset = parseBin(parts[2]);
        int rs1 = Integer.parseInt(parts[3], 2);
        int address = registers[rs1] + offset;

        // Симулируем загрузку байта как знакового значения

        int value = getDataHalf(address); // Приведение к int сохраняет знак
        registers[rd] = value;
        setZero();


    }

    private void executeSB(String[] parts) throws Exception {
        // SB rs2, offset(rs1)
        System.err.println(parts[1]);
        System.err.println(parts[2]);
        System.err.println(parts[3]);
        int rs2 = Integer.parseInt(parts[1], 2);

        int offset = parseBin(parts[2]);
        int rs1 = Integer.parseInt(parts[3], 2);

        int address = registers[rs1] + offset;
        // Сохраняем младший байт из rs2
        int value = Integer.parseInt(Machine.zeros(Integer.toBinaryString(registers[rs2]),32).substring(24));
        loadByte(value, address);
    }

    private void executeSH(String[] parts) throws Exception {
        int rs2 = Integer.parseInt(parts[1], 2);

        int offset = parseBin(parts[2]);
        int rs1 = Integer.parseInt(parts[3], 2);
        int address = registers[rs1] + offset;

        // Сохраняем младший байт из rs2
        int value = Integer.parseInt(Machine.zeros(Integer.toBinaryString(registers[rs2]), 16));
        loadHalf(value, address);
    }

    private void executeSW(String[] parts) throws Exception {
        int rs2 = Integer.parseInt(parts[1], 2);
        int offset = parseBin(parts[2]);
        int rs1 = Integer.parseInt(parts[3], 2);
        int address = registers[rs1] + offset;
        // Сохраняем младший байт из rs2
        int value = registers[rs2];
        loadWord(value, address);
    }

    private void executeADDI(String[] parts) throws Exception {
        // ADDI rd, rs1, imm
        int rd = Integer.parseInt(parts[1], 2);
        int rs1 = Integer.parseInt(parts[2], 2);
        int imm = parseBinUpdate(parts[3], 12);
        int result = registers[rs1] + imm;
        registers[rd] = result;
        setZero();
    }

    private void executeSLTI(String[] parts) throws Exception {
        // SLTI rd, rs1, imm
        int rd = Integer.parseInt(parts[1], 2);
        int rs1 = Integer.parseInt(parts[2], 2);
        int imm = parseBinUpdate(parts[3], 12);
        int result;
        if (registers[rs1] < imm) {
            result = 1;
        } else {
            result = 0;
        }
        registers[rd] = result;
        setZero();
    }

    private void executeSLTIU(String[] parts) throws Exception {
        // SLTI rd, rs1, imm
        int rd = Integer.parseInt(parts[1], 2);
        int rs1 = Integer.parseInt(parts[2], 2);
        int imm = parseBinUpdate(parts[3], 12);
        long unsigned_rs1 = Integer.toUnsignedLong(registers[rs1]);
        long unsigned_imm = Integer.toUnsignedLong(imm);
        int result;
        if (unsigned_rs1 < unsigned_imm) {
            result = 1;
        } else {
            result = 0;
        }
        registers[rd] = result;
        setZero();
    }

    private void executeXORI(String[] parts) throws Exception {
        // XORI rd, rs1, imm
        int rd = Integer.parseInt(parts[1], 2);
        int rs1 = Integer.parseInt(parts[2], 2);
        int imm = Integer.parseInt((parts[3]).substring((parts[3].length() - 12)));
        int result = registers[(rs1)] ^ imm;
        registers[rd] = result;
        setZero();
    }

    private void executeORI(String[] parts) throws Exception {
        // XORI rd, rs1, imm
        int rd = Integer.parseInt(parts[1], 2);
        int rs1 = Integer.parseInt(parts[2], 2);
        int imm = Integer.parseInt((parts[3]).substring((parts[3].length() - 12)));
        int result = registers[(rs1)] | imm;
        registers[rd] = result;
        setZero();
    }

    private void executeANDI(String[] parts) throws Exception {
        // XORI rd, rs1, imm
        int rd = Integer.parseInt(parts[1], 2);
        int rs1 = Integer.parseInt(parts[2], 2);
        int imm = Integer.parseInt((parts[3]).substring((parts[3].length() - 12)));
        int result = registers[(rs1)] & imm;
        registers[rd] = result;
        setZero();
    }

    private void executeSLLI(String[] parts) throws Exception {
        // XORI rd, rs1, imm
        int rd = Integer.parseInt(parts[1], 2);
        int rs1 = Integer.parseInt(parts[2], 2);
        int imm = Integer.parseInt((parts[3]).substring((parts[3].length() - 5)));
        int result = registers[(rs1)] << imm;
        registers[rd] = result;
        setZero();
    }

    private void executeSRLI(String[] parts) throws Exception {
        // XORI rd, rs1, imm
        int rd = Integer.parseInt(parts[1], 2);
        int rs1 = Integer.parseInt(parts[2], 2);
        int imm = Integer.parseInt((parts[3]).substring((parts[3].length() - 5)));
        int result = registers[(rs1)] >>> imm;
        registers[rd] = result;
        setZero();
    }

    private void executeSRAI(String[] parts) throws Exception {
        // XORI rd, rs1, imm
        int rd = Integer.parseInt(parts[1], 2);
        int rs1 = Integer.parseInt(parts[2], 2);
        int imm = Integer.parseInt((parts[3]).substring((parts[3].length() - 5)));
        int result = registers[(rs1)] >> imm;
        registers[rd] = result;
        setZero();
    }

    private void executeADD(String[] parts) throws Exception {
        // ADD rd, rs1, rs2
        int rd = parseBin(parts[1]);
        int rs1 = parseBin(parts[2]);
        int rs2 = parseBin(parts[3]);
        registers[rd] = registers[rs1] + registers[rs2];
        setZero();
    }

    private void executeSUB(String[] parts) throws Exception {
        int rd = parseBin(parts[1]);
        int rs1 = parseBin(parts[2]);
        int rs2 = parseBin(parts[3]);
        registers[rd] = registers[rs1] - registers[rs2];
        setZero();
    }

    private void executeSLL(String[] parts) throws Exception {
        // XORI rd, rs1, imm
        int rd = Integer.parseInt(parts[1], 2);
        int rs1 = Integer.parseInt(parts[2], 2);
        int rs2 = Integer.parseInt(parts[3], 2);
        int result = registers[(rs1)] << registers[rs2];
        registers[rd] = result;
        setZero();
    }

    private void executeSLT(String[] parts) throws Exception {
        int rd = Integer.parseInt(parts[1], 2);
        int result = 0;
        int rs1 = Integer.parseInt(parts[2], 2);
        int rs2 = Integer.parseInt(parts[3], 2);
        if (registers[(rs1)] < registers[rs2]) {
            result = 1;
        } else {
            result = 0;
        }
        registers[rd] = result;
        setZero();
    }

    private void executeSLTU(String[] parts) throws Exception {
        int rd = Integer.parseInt(parts[1], 2);
        int result = 0;
        int rs1 = Integer.parseInt(parts[2], 2);
        int rs2 = Integer.parseInt(parts[3], 2);
        int val1 = registers[rs1];
        int val2 = registers[rs2];
        long unsigned1 = Integer.toUnsignedLong(val1);
        long unsigned2 = Integer.toUnsignedLong(val2);
        if (unsigned1 < unsigned2) {
            result = 1;
        } else {
            result = 0;
        }
        registers[rd] = result;
        setZero();
    }

    private void executeXOR(String[] parts) throws Exception {
        // OR rd, rs1, rs2
        int rd = parseBin(parts[1]);
        int rs1 = parseBin(parts[2]);
        int rs2 = parseBin(parts[3]);
        registers[rd] = registers[rs1] ^ registers[rs2];
        setZero();
    }

    private void executeSRA(String[] parts) throws Exception {
        // XORI rd, rs1, imm
        int rd = Integer.parseInt(parts[1], 2);
        int rs1 = Integer.parseInt(parts[2], 2);
        int rs2 = Integer.parseInt(parts[3], 2);
        int result = registers[(rs1)] >> rs2;
        registers[rd] = result;
        setZero();
    }

    private void executeSRL(String[] parts) throws Exception {
        // XORI rd, rs1, imm
        int rd = Integer.parseInt(parts[1], 2);
        int rs1 = Integer.parseInt(parts[2], 2);
        int rs2 = Integer.parseInt(parts[3], 2);
        int result = registers[(rs1)] >>> rs2;
        registers[rd] = result;
        setZero();
    }

    private void executeOR(String[] parts) throws Exception {
        // OR rd, rs1, rs2
        int rd = parseBin(parts[1]);
        int rs1 = parseBin(parts[2]);
        int rs2 = parseBin(parts[3]);
        registers[rd] = registers[rs1] | registers[rs2];
        setZero();
    }

    private void executeAND(String[] parts) throws Exception {
        // OR rd, rs1, rs2
        int rd = parseBin(parts[1]);
        int rs1 = parseBin(parts[2]);
        int rs2 = parseBin(parts[3]);
        registers[rd] = registers[rs1] & registers[rs2];
        setZero();
    }

    private void executeMUL(String[] parts) throws Exception {
        // MUL rd, rs1, rs2
        int rd = parseBin(parts[1]);
        int rs1 = parseBin(parts[2]);
        int rs2 = parseBin(parts[3]);
        long product = Integer.toUnsignedLong(registers[rs1]) * Integer.toUnsignedLong(registers[rs2]);
        registers[rd] = (int) product;
        setZero();
    }

    private void executeMULH(String[] parts) throws Exception {
        // MULH rd, rs1, rs2
        if (parts.length != 4) throw new Exception("Invalid MULH syntax");
        int rd = parseBin(parts[1]);
        int rs1 = parseBin(parts[2]);
        int rs2 = parseBin(parts[3]);
        long product = (long) registers[rs1] * (long) registers[rs2];
        registers[rd] = (int) (product >> 32);
        setZero();
    }

    private void executeMULHSU(String[] parts) throws Exception {
        // MULHSU rd, rs1, rs2
        if (parts.length != 4) throw new Exception("Invalid MULHSU syntax");
        int rd = parseBin(parts[1]);
        int rs1 = parseBin(parts[2]);
        int rs2 = parseBin(parts[3]);
        long signedRs1 = registers[rs1];
        long unsignedRs2 = Integer.toUnsignedLong(registers[rs2]);
        long product = signedRs1 * unsignedRs2;
        registers[rd] = (int) (product >> 32);
        setZero();
    }

    private void executeMULHU(String[] parts) throws Exception {
        // MULHU rd, rs1, rs2
        if (parts.length != 4) throw new Exception("Invalid MULHU syntax");
        int rd = parseBin(parts[1]);
        int rs1 = parseBin(parts[2]);
        int rs2 = parseBin(parts[3]);
        long unsignedRs1 = Integer.toUnsignedLong(registers[rs1]);
        long unsignedRs2 = Integer.toUnsignedLong(registers[rs2]);
        long product = unsignedRs1 * unsignedRs2;
        registers[rd] = (int) (product >> 32);
        setZero();
    }

    private void executeDIV(String[] parts) throws Exception {
        // DIV rd, rs1, rs2
        if (parts.length != 4) throw new Exception("Invalid DIV syntax");
        int rd = parseBin(parts[1]);
        int rs1 = parseBin(parts[2]);
        int rs2 = parseBin(parts[3]);
        if (registers[rs2] == 0) {
            registers[rd] = -1;
        } else {
            registers[rd] = registers[rs1] / registers[rs2];
        }
        setZero();
        pc++;
    }

    private void executeDIVU(String[] parts) throws Exception {
        // DIVU rd, rs1, rs2
        if (parts.length != 4) throw new Exception("Invalid DIVU syntax");
        int rd = parseBin(parts[1]);
        int rs1 = parseBin(parts[2]);
        int rs2 = parseBin(parts[3]);
        if (registers[rs2] == 0) {
            registers[rd] = 0xFFFFFFFF;
        } else {
            long unsignedRs1 = Integer.toUnsignedLong(registers[rs1]);
            long unsignedRs2 = Integer.toUnsignedLong(registers[rs2]);
            registers[rd] = (int) (unsignedRs1 / unsignedRs2);
        }
        setZero();
        pc++;
    }

    private void executeREM(String[] parts) throws Exception {
        // REM rd, rs1, rs2
        if (parts.length != 4) throw new Exception("Invalid REM syntax");
        int rd = parseBin(parts[1]);
        int rs1 = parseBin(parts[2]);
        int rs2 = parseBin(parts[3]);
        if (registers[rs2] == 0) {
            registers[rd] = registers[rs1];
        } else {
            registers[rd] = registers[rs1] % registers[rs2];
        }
        setZero();
        pc++;
    }

    private void executeREMU(String[] parts) throws Exception {
        // REMU rd, rs1, rs2
        if (parts.length != 4) throw new Exception("Invalid REMU syntax");
        int rd = parseBin(parts[1]);
        int rs1 = parseBin(parts[2]);
        int rs2 = parseBin(parts[3]);
        if (registers[rs2] == 0) {
            registers[rd] = registers[rs1];
        } else {
            long unsignedRs1 = Integer.toUnsignedLong(registers[rs1]);
            long unsignedRs2 = Integer.toUnsignedLong(registers[rs2]);
            registers[rd] = (int) (unsignedRs1 % unsignedRs2);
        }
        setZero();
        pc++;
    }


    // Метод для отображения содержимого памяти (для отладки)
    public void printMemory() {
        for (int i = 0; i < mem1.length; i++) {
            if (mem1[i] != 0) {
                System.out.println("mem[" + i + "] = " + mem1[i]);
            }

        }
    }

    public void printcache() {
        for (int i = 0; i < cacheLRU.length; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 8; k++) {
                    System.out.println("cache[" + i + "][" + j + "][" + k + "]=" + cacheLRU[i][j][k]);
                }
            }

        }
    }

    public void printcache2() {
        for (int i = 0; i < cachepLRU.length; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 8; k++) {
                    System.out.println("cache[" + i + "][" + j + "][" + k + "]=" + cachepLRU[i][j][k]);
                }
            }

        }
    }

    public void printRegisters() {
//        for (int i = 0; i < registers.length; i++) {
//            System.out.println("registers[" + i + "] = " + registers[i]);
//        }
        System.err.println(Arrays.toString(registers));
    }
}