package org.example.javafx;

import java.util.Arrays;

public class CHIP8 {
    private int opcode;
    private final int[] MEMORY = new int[4096];

    private final int[] V = new int[16];

    private int indexRegister;
    private int pc;

//    0x000-0x1FF - Chip 8 interpreter (contains font set in emu)
//    0x050-0x0A0 - Used for the built in 4x5 pixel font set (0-F)
//    0x200-0xFFF - Program ROM and work RAM

    private int delay_timer;
    private int sound_timer;

    private final int[] STACK = new int[16];
    private int sp;
    private boolean drawFlag = false;

    private final Keyboard keyboard;
    private final Screen screen;
    public CHIP8(Keyboard keyboard, Screen screen){
        initialize();
        this.keyboard = keyboard;
        this.screen = screen;
    }
    public void initialize() {
        opcode = 0x000000000;
        Arrays.fill(V, 0x00);
        delay_timer = 0x00;
        sound_timer = 0x00;
        Arrays.fill(STACK, 0x00);
        Arrays.fill(MEMORY, 0);
        sp = 0x00;
        indexRegister = 0x00;
        pc = 0x200;
        drawFlag = true;
        //Load font into memory
        System.arraycopy(Keyboard.FONT, 0, MEMORY, 0, 80);
    }
    public void emulateCycle() {
        // Fetch Opcode
        opcode = ((MEMORY[pc] << 8) | (MEMORY[pc + 1]));
        // Decode Opcode
        // Execute Opcode
        printDebug();
        execute();

        // Update timers

    }
    public void printDebug(){
        System.out.print("Opcode=" + String.format("%04x", opcode) +
                " pc=" + pc +
                " sp=" + sp +
                " I=" + indexRegister);
        // Register
        System.out.print(" V[");
        for (int i = 0; i < V.length; i++) {
            System.out.print(i+"="+V[i]+",");
        }
        System.out.print("]");
        // key pressed
        System.out.print(" KEY[");
        for (int i = 0; i < keyboard.getKeys().length; i++) {
            System.out.print(i+"="+(keyboard.getKeys()[i]?"1":"0")+",");
        }
        System.out.print("]");
        System.out.println();
    }

    public void execute(){
        int caseSwitch = opcode & 0xF000;
        int x;
        switch (caseSwitch){
            case 0x0000:
                if(opcode == 0x00E0){
                    screen.clear();
                    drawFlag = true;
                    pc += 2;
                }
                if(opcode == 0x00EE){
                    pc = STACK[sp-1];
                    --sp;
                    drawFlag = true;
                    pc += 2;
                }
                break;
            case 0x1000:
                // JP addr
                pc = opcode & 0x0FFF;
                break;
            case 0x2000:
                // 2nnn - CALL addr
                STACK[sp] = pc;
                ++sp;
                pc = opcode & 0x0FFF;
                break;
            case 0x3000:
                // 3xkk - SE Vx, byte
                if (V[(opcode & 0x0F00) >>> 8] == (opcode & 0x00FF)){
                    pc += 4;
                } else {
                    pc += 2;
                }
                break;
            case 0x4000:
                // 4xkk - SNE Vx, byte
                if (V[(opcode & 0x0F00) >>> 8] != (opcode & 0x00FF)){
                    pc += 4;
                } else {
                    pc += 2;
                }
                break;
            case 0x5000:
                // 5xy0 - SE Vx, Vy
                if (V[(opcode & 0x0F00) >>> 8] != V[(opcode & 0x00F0) >>> 4]){
                    pc += 4;
                } else {
                    pc += 2;
                }
                break;
            case 0x6000:
                // 6xkk - LD Vx, byte
                V[(opcode & 0x0F00) >>> 8] = (opcode & 0x00FF);
                pc += 2;
                break;
            case 0x7000:
                // 7xkk - ADD Vx, byte
                x = (opcode & 0x0F00) >>> 8;
                // V[(opcode & 0x0F00) >>> 8] += (opcode & 0x00FF);
                int nn = opcode & 0x00FF;
                int result = V[x] + nn;
                if (result >= 256){
                    V[x] = result - 256;
                } else {
                    V[x] = result;
                }
                pc += 2;
                break;
            case 0x8000:
                int subOp = opcode & 0x000F;
                switch (subOp) {
                    case 0x0000:
                        // 8xy0 - LD Vx, Vy
                        V[(opcode & 0x0F00) >>> 8] = V[(opcode & 0x00F0) >>> 4];
                        pc += 2;
                        break;
                    case 0x0001:
                        // 8xy1 - OR Vx, Vy
                        V[(opcode & 0x0F00) >>> 8] = V[(opcode & 0x0F00) >>> 8] | V[(opcode & 0x00F0) >>> 4];
                        pc += 2;
                        break;
                    case 0x0002:
                        // 8xy2 - AND Vx, Vy
                        V[(opcode & 0x0F00) >>> 8] = V[(opcode & 0x0F00) >>> 8] & V[(opcode & 0x00F0) >>> 4];
                        pc += 2;
                        break;
                    case 0x0003:
                        // 8xy3 - XOR Vx, Vy
                        V[(opcode & 0x0F00) >>> 8] = V[(opcode & 0x0F00) >>> 8] ^ V[(opcode & 0x00F0) >>> 4];
                        pc += 2;
                        break;
                    case 0x0004:
                        // 8xy4 - ADD Vx, Vy
                        V[(opcode & 0x0F00) >>> 8] = V[(opcode & 0x0F00) >>> 8] + V[(opcode & 0x00F0) >>> 4];
                        if (V[(opcode & 0x0F00) >>> 8] > 0xFF){
                            V[0xF] = 1;
                            V[(opcode & 0x0F00) >>> 8] = V[(opcode & 0x0F00) >>> 8] & 0xFF;
                        } else {
                            V[0xF] = 0;
                        }
                        pc += 2;
                        break;
                    case 0x0005:
                        // 8xy5 - SUB Vx, Vy
                        if (V[(opcode & 0x0F00) >>> 8] > V[(opcode & 0x00F0) >>> 4]) {
                            V[0xF] = 1;
                        } else {
                            V[0xF] = 0;
                        }
                        V[(opcode & 0x0F00) >>> 8] = (V[(opcode & 0x0F00) >>> 8] - V[(opcode & 0x00F0) >>> 4]) & 0xFF;
                        pc += 2;
                        break;
                    case 0x0006:
                        // 8xy6 - SHR Vx {, Vy}
                        if ((V[(opcode & 0x0F00) >>> 8] & 0x00008000) > 0){
                            V[0xF] = 1;
                        } else {
                            V[0xF] = 0;
                        }
                        V[(opcode & 0x0F00) >>> 8] = V[(opcode & 0x0F00) >>> 8] >>> 1;
                        pc += 2;
                        break;
                    case 0x0007:
                        // 8xy7 - SUBN Vx, Vy
                        if (V[(opcode & 0x00F0) >>> 4] > V[(opcode & 0x0F00) >>> 8]) {
                            V[0xF] = 1;
                        } else {
                            V[0xF] = 0;
                        }
                        V[(opcode & 0x0F00) >>> 8] = V[(opcode & 0x00F0) >>> 4] - V[(opcode & 0x0F00) >>> 8];
                        pc += 2;
                        break;
                    case 0x000E:
                        // 8xyE - SHL Vx {, Vy}
                        if ((V[(opcode & 0x0F00) >>> 8] & 0x00008000) > 0){
                            V[0xF] = 1;
                        } else {
                            V[0xF] = 0;
                        }
                        V[(opcode & 0x0F00) >>> 8] = V[(opcode & 0x0F00) >>> 8] << 1;
                        pc += 2;
                        break;
                }
                break;
            case 0x9000:
                if (V[(opcode & 0x0F00) >>> 8] != V[(opcode & 0x00F0) >>> 4]) {
                    pc += 4;
                } else {
                    pc += 2;
                }
            break;
            case 0xA000:
                indexRegister = opcode & 0x0FFF;
                pc += 2;
                break;
            case 0xB000:
                pc = V[0x0] + (opcode & 0x0FFF);
                break;
            case 0xC000:
                int random = (int)(Math.random() * (255 - 10 + 1)) + 10;
                V[(opcode & 0x0F00) >>> 8] = random & (opcode & 0x00FF);
                pc += 2;
                break;
            case 0xD000:
                // DXYN - Display n-byte sprite starting at memory location I at (Vx, Vy), set VF = collision.
                x = V[(opcode & 0x0F00) >> 8];
                int y = V[(opcode & 0x00F0) >> 4];
                int height = opcode & 0x000F;
                V[0xF] = 0;
                for (int yLine = 0; yLine < height; yLine++) {
                    int pixel = MEMORY[indexRegister + yLine];

                    for (int xLine = 0; xLine < 8; xLine++) {
                        // check each bit (pixel) in the 8 bit row
                        if ((pixel & (0x80 >> xLine)) != 0) {

                            // wrap pixels if they're drawn off screen
                            int xCoord = x+xLine;
                            int yCoord = y+yLine;

                            if (xCoord < 64 && yCoord < 32) {
                                // if pixel already exists, set carry (collision)
                                if (screen.getPixel(xCoord, yCoord) == 1) {
                                    V[0xF] = 1;
                                }
                                // draw via xor
                                screen.setPixel(xCoord,yCoord);
                            }
                        }
                    }
                }
                drawFlag = true;
                pc += 2;
                break;
            case 0xE000:
                int subOpE = opcode & 0x00FF;
                switch (subOpE) {
                    case 0x009E:
                        // Ex9E - SKP Vx
                        if(keyboard.isPressed(V[(opcode & 0x0F00) >>> 8])) {
                            pc += 4;
                        } else {
                            pc += 2;
                        }
                        break;
                    case 0x00A1:
                        // ExA1 - SKNP Vx
                        if(keyboard.isPressed(V[(opcode & 0x0F00) >>> 8])) {
                            pc += 4;
                        } else {
                            pc += 2;
                        }
                        break;
                }
                break;
            case 0xF000:
                int subOpF = opcode & 0x00FF;
                switch (subOpF) {
                    case 0x0007:
                        // Fx07 - LD Vx, DT
                        V[(opcode & 0x0F00) >>> 8] = delay_timer;
                        pc += 2;
                        break;
                    case 0x000A:
                        // Fx0A - LD Vx, K
                        for (int i = 0; i < 0xF; i++) {
                            if(keyboard.isPressed(i)){
                                V[(opcode & 0x0F00) >>> 8] = i;
                                pc += 2;
                                break;
                            }
                        }
                        break;
                    case 0x0015:
                        // Fx15 - LD DT, Vx
                        delay_timer = V[(opcode & 0x0F00) >>> 8];
                        pc += 2;
                        break;
                    case 0x0018:
                        // Fx18 - LD ST, Vx
                        sound_timer = V[(opcode & 0x0F00) >>> 8];
                        pc += 2;
                        break;
                    case 0x001E:
                        // Fx1E - ADD I, Vx
                        indexRegister += V[(opcode & 0x0F00) >>> 8];
                        pc += 2;
                        break;
                    case 0x0029:
                        // Fx29 - LD F, Vx
                        indexRegister = V[(opcode & 0x0F00) >>> 8] * 5;
                        pc += 2;
                        drawFlag = true;
                        break;
                    case 0x0033:
                        // Fx33 - LD B, Vx
                        MEMORY[indexRegister] = V[(opcode & 0x0F00) >>> 8] / 100;
                        MEMORY[indexRegister + 1] = (V[(opcode & 0x0F00) >>> 8] / 10) % 10;
                        MEMORY[indexRegister + 2] = (V[(opcode & 0x0F00) >>> 8] % 100) % 10;
                        pc += 2;
                        break;
                    case 0x0055:
                        // Fx55 - LD [I], Vx
                        x = opcode & 0x0F00 >>> 8;
                        System.arraycopy(V, 0, MEMORY, indexRegister, x + 1);
                        pc += 2;

                        break;
                    case 0x0065:
                        // Fx65 - LD Vx, [I]
                        x = opcode & 0x0F00 >>> 8;
                        for (int i = 0; i <= x; i++) {
                            V[i] = MEMORY[indexRegister + i] & 0xFF;
                        }
                        pc += 2;
                        break;
                }
                break;
            default:
                break;
        }
    }

    public void loadRom(byte[] rom){
        for (int i = 0; i < rom.length; i++) {
            MEMORY[i + 512] =  (rom[i] & 0xFF);
//            System.out.println("Load rom: " + (i + 512) + String.format("Memory: %02x rom: %02x",MEMORY[i + 512], rom[i]));
        }
    }

    public int getDelay_timer() {
        return delay_timer;
    }

    public void setDelay_timer(int delay_timer) {
        this.delay_timer = delay_timer;
    }

    public int getSound_timer() {
        return sound_timer;
    }

    public void setSound_timer(int sound_timer) {
        this.sound_timer = sound_timer;
    }

    public boolean isDrawFlag() {
        return drawFlag;
    }

    public void setDrawFlag(boolean drawFlag) {
        this.drawFlag = drawFlag;
    }

}
