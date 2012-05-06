/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.hardware;

import uk101.hardware.bus.DataBus;
import uk101.machine.Computer;
import uk101.machine.Data;
import uk101.machine.Trace;

/**
 * Simulation of the MOS Technology 6502 microprocessor.
 *
 * Instruction decoding and cycle times are from my old copy of Rodney Zaks
 * "Programming the 6502" (3rd edition).
 *
 * @author Baldwin
 */
public class CPU6502 {

    // Status register flag bits
    static final byte FLAG_N = (byte)0x80;
    static final byte FLAG_V = (byte)0x40;
    static final byte FLAG_x = (byte)0x20;
    static final byte FLAG_B = (byte)0x10;
    static final byte FLAG_D = (byte)0x08;
    static final byte FLAG_I = (byte)0x04;
    static final byte FLAG_Z = (byte)0x02;
    static final byte FLAG_C = (byte)0x01;

    // Addressing modes
    static final int MODE_IMPLICIT = 0;
    static final int MODE_IMMEDIATE = 1;
    static final int MODE_ABSOLUTE = 2;
    static final int MODE_INDIRECT = 3;
    static final int MODE_ZEROPAGE = 4;
    static final int MODE_RELATIVE = 5;
    static final int MODE_ABS_X = 6;
    static final int MODE_ABS_Y = 7;
    static final int MODE_PRE_X = 8;
    static final int MODE_POST_Y = 9;
    static final int MODE_0PAGE_X = 10;
    static final int MODE_0PAGE_Y = 11;

    // Important address pages
    static final int STACK_BASE = 0x100;
    static final byte STACK_TOP = (byte)0xFF;

    // Address vectors
    static final int NMI_VECTOR = 0xFFFA;
    static final int RESET_VECTOR = 0xFFFC;
    static final int IRQ_VECTOR = 0xFFFE;

    // Processor registers
    byte A;              // Accumulator
    byte X, Y;           // X and Y Index registers
    byte S;              // Stack pointer
    byte P;              // Status register
    short PC;            // 16-bit program counter

    // Arithmetic and Logic Unit
    ALU6502 alu;

    // Data Bus gives access to the RAM and ROM
    DataBus bus;

    // Execution control
    boolean running;
    boolean sigRESET, sigNMI, sigIRQ;

    // Timing control
    int speed, cpuCycles;
    long cpuStart, nanoInterval;

    // Debugging
    Trace trace;
    Trace.Entry traceEntry;

    public CPU6502(int mhz, DataBus bus) {
        this.alu = new ALU6502();
        this.bus = bus;
        reset();
        calibrate();
        setMHz(mhz);
    }

    /*
     * Set/get the required processor clock speed.
     */
    public void setMHz(int mhz) {
        // speed = cycle time in nanoseconds (or zero)
        speed = (mhz == 0) ? 0 : 1000/mhz;

        // cycle time ought to be 1000/mhz, but due to the way the timing
        // loops work this causes the CPU to run very slightly slow.  So
        // the cycle time is reduced by 0.5% to try to compensate.
        if (speed != 0) {
            speed -= speed/200;
        }
    }

    public int getMHz() {
        // Convert cycle time back to clock speed
        return (speed == 0) ? 0 : 1000/speed;
    }

    /*
     * Return the actual simulated clock speed in MHz since this
     * method was last called.  This method will probably be called
     * on a regular basis to be used to display the actual processor
     * speed.
     */
    public float getSpeed() {
        // Calculate speed
        long cpuTime = System.currentTimeMillis() - cpuStart;
        float mhz = (float)cpuCycles/(float)(cpuTime*1000);

        // Reset counters ready for next call
        cpuStart = System.currentTimeMillis();
        cpuCycles = 0;

        return mhz;
    }

    // This method makes an attempt to determine the time taken to read
    // and the granularity of the results of the System.nanoTime() timer.
    void calibrate() {
        long l1 = System.nanoTime();
        long l2 = System.nanoTime();
        l2 = System.nanoTime();
        l2 = System.nanoTime();
        l2 = System.nanoTime();
        nanoInterval = (l2-l1)/4;
    }

    /*
     * Normal processor execution
     */
    public void run() {
        running = true;
        cpuStart = System.currentTimeMillis();
        cpuCycles = 0;

        long now = System.nanoTime();
        long end = now;
        long sync = now;
        while (running) {
            int cycles = execute();
            cpuCycles += cycles;

            // It is difficult to get timings exactly right in Java.  This logic
            // assumes we are running too fast (which should be true most of the
            // time on anything except a very slow machine) and adds some delays
            // when we have accumulated enough excess time for a delay to be
            // worthwhile - this means individual instructions won't be at the
            // exact correct speed, but on average the CPU should be close.
            // However just in case we start to run too slow the clock is
            // re-synchronised every 1/5th of a second or so.
            if (speed > 0) {
                end += cycles*speed;
                if (end-sync > 200000000) {
                    sync = now = System.nanoTime();
                    if (now > end) {
                        end = now;
                    }
                }
                while (end-now > nanoInterval) {
                    try {
                        Thread.sleep(0, Math.min((int)(end-now), 999999));
                    } catch (InterruptedException e) {
                    }
                    now = System.nanoTime();
                }
            }
        }
    }

    public void stop() {
        running = false;
    }

    /*
     * External signals
     */
    public synchronized void signalReset() {
        sigRESET = true;
        notify();
    }

    public synchronized void signalNMI() {
        sigNMI = true;
        notify();
    }

    public synchronized void signalIRQ() {
        if (!testFlag(FLAG_I)) {
            sigIRQ = true;
            notify();
        }
    }

    // Processor reset state
    void reset() {
        A = X = Y = 0;
        S = STACK_TOP;
        P = FLAG_x;
        PC = 0;
    }

    /*
     * Execute a single instruction
     */
    int execute() {
        // Check for external signals before executing the next instruction
        checkSignals();

        // Add trace record if tracing
        if (trace != null) { 
            traceEntry = trace.trace(PC, A, X, Y, S, P);
        }    

        // Read the next opcode
        int op = Data.asBits(fetchByte());

        // The original instruction opcodes had a regular pattern and can be decoded by
        // breaking down into various bit groups for the operation, addressing mode etc.
        // But a big switch statement works just as well and copes better if we want to
        // add support for undocumented instructions or additional instructions added
        // to later versions of the processor (which didn't always match the original
        // patterns).
        // Note: Java will compile this into an indexed jump table, so it will be very
        //       efficient and the order we write the 'case's doesn't matter.
        int cycles = 0, bc = 0;
        switch (op) {
        default:                                            break;
        case 0x6D: adc(MODE_ABSOLUTE);         cycles = 4;  break;
        case 0x65: adc(MODE_ZEROPAGE);         cycles = 3;  break;
        case 0x69: adc(MODE_IMMEDIATE);        cycles = 2;  break;
        case 0x7D: adc(MODE_ABS_X);            cycles = 4;  break;
        case 0x79: adc(MODE_ABS_Y);            cycles = 4;  break;
        case 0x61: adc(MODE_PRE_X);            cycles = 6;  break;
        case 0x71: adc(MODE_POST_Y);           cycles = 5;  break;
        case 0x75: adc(MODE_0PAGE_X);          cycles = 4;  break;
        case 0x2D: and(MODE_ABSOLUTE);         cycles = 4;  break;
        case 0x25: and(MODE_ZEROPAGE);         cycles = 3;  break;
        case 0x29: and(MODE_IMMEDIATE);        cycles = 2;  break;
        case 0x3D: and(MODE_ABS_X);            cycles = 4;  break;
        case 0x39: and(MODE_ABS_Y);            cycles = 4;  break;
        case 0x21: and(MODE_PRE_X);            cycles = 6;  break;
        case 0x31: and(MODE_POST_Y);           cycles = 5;  break;
        case 0x35: and(MODE_0PAGE_X);          cycles = 4;  break;
        case 0xCD: cmp(MODE_ABSOLUTE);         cycles = 4;  break;
        case 0xC5: cmp(MODE_ZEROPAGE);         cycles = 3;  break;
        case 0xC9: cmp(MODE_IMMEDIATE);        cycles = 2;  break;
        case 0xDD: cmp(MODE_ABS_X);            cycles = 4;  break;
        case 0xD9: cmp(MODE_ABS_Y);            cycles = 4;  break;
        case 0xC1: cmp(MODE_PRE_X);            cycles = 6;  break;
        case 0xD1: cmp(MODE_POST_Y);           cycles = 5;  break;
        case 0xD5: cmp(MODE_0PAGE_X);          cycles = 4;  break;
        case 0x4D: eor(MODE_ABSOLUTE);         cycles = 4;  break;
        case 0x45: eor(MODE_ZEROPAGE);         cycles = 3;  break;
        case 0x49: eor(MODE_IMMEDIATE);        cycles = 2;  break;
        case 0x5D: eor(MODE_ABS_X);            cycles = 4;  break;
        case 0x59: eor(MODE_ABS_Y);            cycles = 4;  break;
        case 0x41: eor(MODE_PRE_X);            cycles = 6;  break;
        case 0x51: eor(MODE_POST_Y);           cycles = 5;  break;
        case 0x55: eor(MODE_0PAGE_X);          cycles = 4;  break;
        case 0xAD: lda(MODE_ABSOLUTE);         cycles = 4;  break;
        case 0xA5: lda(MODE_ZEROPAGE);         cycles = 3;  break;
        case 0xA9: lda(MODE_IMMEDIATE);        cycles = 2;  break;
        case 0xBD: lda(MODE_ABS_X);            cycles = 4;  break;
        case 0xB9: lda(MODE_ABS_Y);            cycles = 4;  break;
        case 0xA1: lda(MODE_PRE_X);            cycles = 6;  break;
        case 0xB1: lda(MODE_POST_Y);           cycles = 5;  break;
        case 0xB5: lda(MODE_0PAGE_X);          cycles = 4;  break;
        case 0x0D: ora(MODE_ABSOLUTE);         cycles = 4;  break;
        case 0x05: ora(MODE_ZEROPAGE);         cycles = 3;  break;
        case 0x09: ora(MODE_IMMEDIATE);        cycles = 2;  break;
        case 0x1D: ora(MODE_ABS_X);            cycles = 4;  break;
        case 0x19: ora(MODE_ABS_Y);            cycles = 4;  break;
        case 0x01: ora(MODE_PRE_X);            cycles = 6;  break;
        case 0x11: ora(MODE_POST_Y);           cycles = 5;  break;
        case 0x15: ora(MODE_0PAGE_X);          cycles = 4;  break;
        case 0xED: sbc(MODE_ABSOLUTE);         cycles = 4;  break;
        case 0xE5: sbc(MODE_ZEROPAGE);         cycles = 3;  break;
        case 0xE9: sbc(MODE_IMMEDIATE);        cycles = 2;  break;
        case 0xFD: sbc(MODE_ABS_X);            cycles = 4;  break;
        case 0xF9: sbc(MODE_ABS_Y);            cycles = 4;  break;
        case 0xE1: sbc(MODE_PRE_X);            cycles = 6;  break;
        case 0xF1: sbc(MODE_POST_Y);           cycles = 5;  break;
        case 0xF5: sbc(MODE_0PAGE_X);          cycles = 4;  break;
        case 0x8D: sta(MODE_ABSOLUTE);         cycles = 4;  break;
        case 0x85: sta(MODE_ZEROPAGE);         cycles = 3;  break;
        case 0x9D: sta(MODE_ABS_X);            cycles = 5;  break;
        case 0x99: sta(MODE_ABS_Y);            cycles = 5;  break;
        case 0x81: sta(MODE_PRE_X);            cycles = 6;  break;
        case 0x91: sta(MODE_POST_Y);           cycles = 6;  break;
        case 0x95: sta(MODE_0PAGE_X);          cycles = 4;  break;
        case 0xEC: cpx(MODE_ABSOLUTE);         cycles = 4;  break;
        case 0xE4: cpx(MODE_ZEROPAGE);         cycles = 3;  break;
        case 0xE0: cpx(MODE_IMMEDIATE);        cycles = 2;  break;
        case 0xCC: cpy(MODE_ABSOLUTE);         cycles = 4;  break;
        case 0xC4: cpy(MODE_ZEROPAGE);         cycles = 3;  break;
        case 0xC0: cpy(MODE_IMMEDIATE);        cycles = 2;  break;
        case 0xAE: ldx(MODE_ABSOLUTE);         cycles = 4;  break;
        case 0xA6: ldx(MODE_ZEROPAGE);         cycles = 3;  break;
        case 0xA2: ldx(MODE_IMMEDIATE);        cycles = 2;  break;
        case 0xBE: ldx(MODE_ABS_Y);            cycles = 4;  break;
        case 0xB6: ldx(MODE_0PAGE_Y);          cycles = 4;  break;
        case 0xAC: ldy(MODE_ABSOLUTE);         cycles = 4;  break;
        case 0xA4: ldy(MODE_ZEROPAGE);         cycles = 3;  break;
        case 0xA0: ldy(MODE_IMMEDIATE);        cycles = 2;  break;
        case 0xBC: ldy(MODE_ABS_X);            cycles = 4;  break;
        case 0xB4: ldy(MODE_0PAGE_X);          cycles = 4;  break;
        case 0x8E: stx(MODE_ABSOLUTE);         cycles = 4;  break;
        case 0x86: stx(MODE_ZEROPAGE);         cycles = 3;  break;
        case 0x96: stx(MODE_0PAGE_Y);          cycles = 4;  break;
        case 0x8C: sty(MODE_ABSOLUTE);         cycles = 4;  break;
        case 0x84: sty(MODE_ZEROPAGE);         cycles = 3;  break;
        case 0x94: sty(MODE_0PAGE_X);          cycles = 4;  break;
        case 0x0A: asl(MODE_IMPLICIT);         cycles = 2;  break;
        case 0x0E: asl(MODE_ABSOLUTE);         cycles = 6;  break;
        case 0x06: asl(MODE_ZEROPAGE);         cycles = 5;  break;
        case 0x1E: asl(MODE_ABS_X);            cycles = 7;  break;
        case 0x16: asl(MODE_0PAGE_X);          cycles = 6;  break;
        case 0x4A: lsr(MODE_IMPLICIT);         cycles = 2;  break;
        case 0x4E: lsr(MODE_ABSOLUTE);         cycles = 6;  break;
        case 0x46: lsr(MODE_ZEROPAGE);         cycles = 5;  break;
        case 0x5E: lsr(MODE_ABS_X);            cycles = 7;  break;
        case 0x56: lsr(MODE_0PAGE_X);          cycles = 6;  break;
        case 0x2A: rol(MODE_IMPLICIT);         cycles = 2;  break;
        case 0x2E: rol(MODE_ABSOLUTE);         cycles = 6;  break;
        case 0x26: rol(MODE_ZEROPAGE);         cycles = 5;  break;
        case 0x3E: rol(MODE_ABS_X);            cycles = 7;  break;
        case 0x36: rol(MODE_0PAGE_X);          cycles = 6;  break;
        case 0x6A: ror(MODE_IMPLICIT);         cycles = 2;  break;
        case 0x6E: ror(MODE_ABSOLUTE);         cycles = 6;  break;
        case 0x66: ror(MODE_ZEROPAGE);         cycles = 5;  break;
        case 0x7E: ror(MODE_ABS_X);            cycles = 7;  break;
        case 0x76: ror(MODE_0PAGE_X);          cycles = 6;  break;
        case 0x2C: bit(MODE_ABSOLUTE);         cycles = 4;  break;
        case 0x24: bit(MODE_ZEROPAGE);         cycles = 3;  break;
        case 0xCE: dec(MODE_ABSOLUTE);         cycles = 6;  break;
        case 0xC6: dec(MODE_ZEROPAGE);         cycles = 5;  break;
        case 0xDE: dec(MODE_ABS_X);            cycles = 7;  break;
        case 0xD6: dec(MODE_0PAGE_X);          cycles = 6;  break;
        case 0xEE: inc(MODE_ABSOLUTE);         cycles = 6;  break;
        case 0xE6: inc(MODE_ZEROPAGE);         cycles = 5;  break;
        case 0xFE: inc(MODE_ABS_X);            cycles = 7;  break;
        case 0xF6: inc(MODE_0PAGE_X);          cycles = 6;  break;
        case 0xCA: dex();                      cycles = 2;  break;
        case 0x88: dey();                      cycles = 2;  break;
        case 0xE8: inx();                      cycles = 2;  break;
        case 0xC8: iny();                      cycles = 2;  break;
        case 0xAA: tax();                      cycles = 2;  break;
        case 0xA8: tay();                      cycles = 2;  break;
        case 0xBA: tsx();                      cycles = 2;  break;
        case 0x8A: txa();                      cycles = 2;  break;
        case 0x9A: txs();                      cycles = 2;  break;
        case 0x98: tya();                      cycles = 2;  break;
        case 0x90: bc = branch(FLAG_C, false); cycles = 2;  break;
        case 0xB0: bc = branch(FLAG_C, true);  cycles = 2;  break;
        case 0xD0: bc = branch(FLAG_Z, false); cycles = 2;  break;
        case 0xF0: bc = branch(FLAG_Z, true);  cycles = 2;  break;
        case 0x10: bc = branch(FLAG_N, false); cycles = 2;  break;
        case 0x30: bc = branch(FLAG_N, true);  cycles = 2;  break;
        case 0x50: bc = branch(FLAG_V, false); cycles = 2;  break;
        case 0x70: bc = branch(FLAG_V, true);  cycles = 2;  break;
        case 0x18: flag(FLAG_C, false);        cycles = 2;  break;
        case 0xD8: flag(FLAG_D, false);        cycles = 2;  break;
        case 0x58: flag(FLAG_I, false);        cycles = 2;  break;
        case 0xB8: flag(FLAG_V, false);        cycles = 2;  break;
        case 0x38: flag(FLAG_C, true);         cycles = 2;  break;
        case 0xF8: flag(FLAG_D, true);         cycles = 2;  break;
        case 0x78: flag(FLAG_I, true);         cycles = 2;  break;
        case 0xEA: nop();                      cycles = 2;  break;
        case 0x48: pha();                      cycles = 3;  break;
        case 0x08: php();                      cycles = 3;  break;
        case 0x68: pla();                      cycles = 4;  break;
        case 0x28: plp();                      cycles = 4;  break;
        case 0x00: brk();                      cycles = 7;  break;
        case 0x4C: jmp(MODE_ABSOLUTE);         cycles = 3;  break;
        case 0x6C: jmp(MODE_INDIRECT);         cycles = 5;  break;
        case 0x20: jsr(MODE_ABSOLUTE);         cycles = 6;  break;
        case 0x40: rti();                      cycles = 6;  break;
        case 0x60: rts();                      cycles = 6;  break;

        // Some extra simulator opcodes.
        case 0x02: halt();   break;
        case 0x22: debug();  break;
        }

        if (trace != null) {
            traceEntry = null;
        }    

        return cycles + bc;
    }

    /*
     * Signal processing
     */
    void checkSignals() {
        if (sigRESET) {
            sigRESET = sigNMI = sigIRQ = false;
            reset();
            PC = bus.readWord(RESET_VECTOR);
        } else if (sigNMI) {
            sigNMI = false;
            pushWord(PC);
            pushByte(P);
            setFlag(FLAG_I, true);
            PC = bus.readWord(NMI_VECTOR);
        } else if (sigIRQ) {
            sigIRQ = false;
            pushWord(PC);
            pushByte((byte)(P & ~FLAG_B));
            setFlag(FLAG_I, true);
            PC = bus.readWord(IRQ_VECTOR);
        }
    }

    /*
     * Standard instruction set
     */
    void nop() {
        return;
    }

    void brk() {
        pushWord((short)(PC + 1));
        pushByte((byte)(P | FLAG_B));
        PC = bus.readWord(IRQ_VECTOR);
    }

    void sta(int mode) {
        setResult(A, mode);
    }

    void stx(int mode) {
        setResult(X, mode);
    }

    void sty(int mode) {
        setResult(Y, mode);
    }

    void lda(int mode) {
        A = getOperand(mode);
        setNZ(A);
    }

    void ldx(int mode) {
        X = getOperand(mode);
        setNZ(X);
    }

    void ldy(int mode) {
        Y = getOperand(mode);
        setNZ(Y);
    }

    void tax() {
        X = A;
        setNZ(X);
    }

    void txa() {
        A = X;
        setNZ(A);
    }

    void tay() {
        Y = A;
        setNZ(Y);
    }

    void tya() {
        A = Y;
        setNZ(A);
    }

     void tsx() {
        X = S;
        setNZ(X);
    }

    void txs() {
        S = X;
    }

    void ora(int mode) {
        A = alu.or(A, getOperand(mode));
        setNZ(A);
    }

    void and(int mode) {
        A = alu.and(A, getOperand(mode));
        setNZ(A);
    }

    void eor(int mode) {
        A = alu.xor(A, getOperand(mode));
        setNZ(A);
    }

    void adc(int mode) {
        A = alu.add(A, getOperand(mode), testFlag(FLAG_C));
        setNZCV(A);
    }

    void sbc(int mode) {
        A = alu.sub(A, getOperand(mode), testFlag(FLAG_C));
        setNZCV(A);
    }


    void cmp(int mode) {
        byte b = alu.cmp(A, getOperand(mode));
        setNZC(b);
    }

    void cpx(int mode) {
        byte b = alu.cmp(X, getOperand(mode));
        setNZC(b);
    }

    void cpy(int mode) {
        byte b = alu.cmp(Y, getOperand(mode));
        setNZC(b);
    }

    void asl(int mode) {
        byte b;
        if (mode == MODE_IMPLICIT) {
            b = A = alu.shl(A);
        } else {
            int addr = getAddress(mode);
            b = alu.shl(getOperand(addr, mode));
            setResult(addr, b);
        }
        setNZC(b);
    }

    void rol(int mode) {
        byte b;
        if (mode == MODE_IMPLICIT) {
            b = A = alu.rol(A, testFlag(FLAG_C));
        } else {
            int addr = getAddress(mode);
            b = alu.rol(getOperand(addr, mode), testFlag(FLAG_C));
            setResult(addr, b);
        }
        setNZC(b);
    }

    void ror(int mode) {
        byte b;
        if (mode == MODE_IMPLICIT) {
            b = A = alu.ror(A, testFlag(FLAG_C));
        } else {
            int addr = getAddress(mode);
            b = alu.ror(getOperand(addr, mode), testFlag(FLAG_C));
            setResult(addr, b);
        }
        setNZC(b);
    }

    void lsr(int mode) {
        byte b;
        if (mode == MODE_IMPLICIT) {
            b = A = alu.shr(A);
        } else {
            int addr = getAddress(mode);
            b = alu.shr(getOperand(addr, mode));
            setResult(addr, b);
        }
        setNZC(b);
    }

    void dec(int mode) {
        int addr = getAddress(mode);
        byte b = (byte)(getOperand(addr, mode) - 1);
        setResult(addr, b);
        setNZ(b);
    }

    void inc(int mode) {
        int addr = getAddress(mode);
        byte b = (byte)(getOperand(addr, mode) + 1);
        setResult(addr, b);
        setNZ(b);
    }

    void dex() {
        X -= 1;
        setNZ(X);
    }

    void inx() {
        X += 1;
        setNZ(X);
    }

    void dey() {
        Y -= 1;
        setNZ(Y);
    }

    void iny() {
        Y += 1;
        setNZ(Y);
    }

    void bit(int mode) {
        byte b = getOperand(mode);
        setFlag(FLAG_Z, (b & A) == 0);
        setFlag(FLAG_N, (b & 0x80) != 0);
        setFlag(FLAG_V, (b & 0x40) != 0);
    }

    void jmp(int mode) {
        PC = Data.asWord(getAddress(mode));
    }

    void jsr(int mode) {
        pushWord(Data.asWord(PC + 1));
        PC = Data.asWord(getAddress(mode));
    }

    void rts() {
        PC = pullWord();
        PC += 1;
    }

    void rti() {
        P = pullByte();
        PC = pullWord();
    }

    void php() {
        pushByte(P);
    }

    void pha() {
        pushByte(A);
    }

    void plp() {
        P = pullByte();
    }

    void pla() {
        A = pullByte();
        setNZ(A);
    }

    // Combined instructions for simple operations
    void pushByte(byte b) {
        bus.writeByte(STACK_BASE + Data.asAddr(S), b);
        S -= 1;
    }

    void pushWord(short w) {
        bus.writeWord(STACK_BASE + Data.asAddr(S)-1, w);
        S -= 2;
    }

    byte pullByte() {
        S += 1;
        return bus.readByte(STACK_BASE + Data.asAddr(S));
    }

    short pullWord() {
        S += 2;
        return bus.readWord(STACK_BASE + Data.asAddr(S)-1);
    }

    void flag(byte flag, boolean value) {
        setFlag(flag, value);
        if (flag == FLAG_D) {
            alu.setDecimal(value);
        }
    }

    int branch(byte flag, boolean value) {
        int extraCycles = 0;
        int offset = getAddress(MODE_RELATIVE);
        if (testFlag(flag) == value) {
            PC += offset;
            extraCycles = 1;
        }
        return extraCycles;
    }

    /*
     * Additional simulator instructions
     */
    synchronized void halt() {
        try {
            wait();
        } catch (InterruptedException e) {
        }
    }

    void debug() {
        int action = getOperand(MODE_IMMEDIATE);
        switch (action) {
        case 0xFF:
            if (bus instanceof Computer)
                ((Computer)bus).dump();
            break;
        case 0x01: case 0x02:
            if (bus instanceof Computer)
                ((Computer)bus).trace(action == 0x01);
            break;
        default:
            PC -= 1;
            break;
        }
    }

    /*
     * Set and test processor status flags
     */
    void setFlag(byte flag, boolean set) {
        if (set)
            P |= flag;
        else
            P &= ~flag;
    }

    boolean testFlag(byte flag) {
        return (P & flag) != 0;
    }

    void setNZ(byte value) {
        setFlag(FLAG_N, value < 0);
        setFlag(FLAG_Z, value == 0);
    }

    void setNZC(byte value) {
        setFlag(FLAG_N, value < 0);
        setFlag(FLAG_Z, value == 0);
        setFlag(FLAG_C, alu.isCarry);
    }

    void setNZCV(byte value) {
        setFlag(FLAG_N, value < 0);
        setFlag(FLAG_Z, value == 0);
        setFlag(FLAG_C, alu.isCarry);
        setFlag(FLAG_V, alu.isOverflow);
    }

    /*
     * Memory access through the program counter
     */
    byte fetchByte() {
        byte b = bus.readByte(Data.asAddr(PC));
        PC += 1;
        if (traceEntry != null) {
            traceEntry.addByte(b);
        }
        return b;
    }

    short fetchWord() {
        short w = bus.readWord(Data.asAddr(PC));
        PC += 2;
        if (traceEntry != null) {
            traceEntry.addWord(w);
        }
        return w;
    }

    // Decode the instruction operand address
    int getAddress(int mode) {
        int addr = 0;
        switch (mode) {
        case MODE_IMPLICIT:  break;
        case MODE_IMMEDIATE: addr = Data.asBits(fetchByte());                             break;
        case MODE_ABSOLUTE:  addr = Data.asAddr(fetchWord());                             break;
        case MODE_INDIRECT:  addr = Data.asAddr(bus.readWord(Data.asAddr(fetchWord())));  break;
        case MODE_ZEROPAGE:  addr = Data.asAddr(fetchByte());                             break;
        case MODE_RELATIVE:  addr = Data.asInt(fetchByte());                              break;
        case MODE_ABS_X:     addr = Data.asAddr(fetchWord()) + Data.asAddr(X);            break;
        case MODE_ABS_Y:     addr = Data.asAddr(fetchWord()) + Data.asAddr(Y);            break;
        case MODE_PRE_X:     addr = Data.asAddr(bus.readWord(Data.asAddr(fetchByte()) + Data.asAddr(X))); break;
        case MODE_POST_Y:    addr = Data.asAddr(bus.readWord(Data.asAddr(fetchByte()))) + Data.asAddr(Y); break;
        case MODE_0PAGE_X:   addr = (Data.asAddr(fetchByte()) + Data.asAddr(X)) & 0xFF;   break;
        case MODE_0PAGE_Y:   addr = (Data.asAddr(fetchByte()) + Data.asAddr(Y)) & 0xFF;   break;
        }
        if (traceEntry != null) {
            traceEntry.addAddr(addr);
        }
        return addr;
    }

    // Return the instruction operand
    byte getOperand(int mode) {
        return getOperand(getAddress(mode), mode);
    }

    byte getOperand(int addr, int mode) {
        return (mode == MODE_IMMEDIATE) ? Data.asByte(addr) : bus.readByte(addr);
    }

    // Write instruction result
    void setResult(byte b, int mode) {
        setResult(getAddress(mode), b);
    }

    void setResult(int addr, byte b) {
        bus.writeByte(addr, b);
    }

    // Enable/disable tracing
    public void trace(Trace trace) {
        this.trace = trace;
    }

    /*
     * Mainly for debugging
     */
    public String toString() {
        StringBuilder s = new StringBuilder("CPU: ");
        s.append("PC=").append(Data.toHexString(PC));
        s.append(" A=").append(Data.toHexString(A));
        s.append(" X=").append(Data.toHexString(X));
        s.append(" Y=").append(Data.toHexString(Y));
        s.append(" S=").append(Data.toHexString(S));
        s.append(" P=").append(toFlagString(P));
        return s.toString();
    }

    public static String toFlagString(byte b) {
        StringBuilder s = new StringBuilder();
        s.append((b & FLAG_N) != 0 ? "N" : "n");
        s.append((b & FLAG_V) != 0 ? "V" : "v");
        s.append("-");
        s.append((b & FLAG_B) != 0 ? "B" : "b");
        s.append((b & FLAG_D) != 0 ? "D" : "d");
        s.append((b & FLAG_I) != 0 ? "I" : "i");
        s.append((b & FLAG_Z) != 0 ? "Z" : "z");
        s.append((b & FLAG_C) != 0 ? "C" : "c");
        return s.toString();
    }
}
