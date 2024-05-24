Intermediate Representation (IR)
---

## Source

```
const int max = 10;
const int min = -1 * max;

fn test(int n): int {
    if (n < 0)
        return -1 * (n - 1);
    return n + 1;
}

fn main() {
    for (int i = min; i <= max; i = test(i)) {
        println(i);
    }
}

fn fib(int n): int {
    if (n == 0 || n == 1)
        return n;
    return fib(n - 1) + fib(n - 2);
}

fn arraySum(Array<int> nums): int {
    if (length(nums) == 0)
        return 0;
    int total = 0;
    for (int num : nums) {
        total += num;
    }
    return total;
}
```

## VM Bytecode

```
test:
    LOAD_CONST 0
    LOAD 0
    LT
    JMPT .true
    LOAD_CONST 1
    LOAD 0
    ADD
    RET
    .true:
        LOAD_CONST 1
        LOAD 0
        SUB
        LOAD_CONST -1
        MUL
        RET
    
_entry:
    LOAD_CONST 10
    GSTORE
    GLOAD 0
    LOAD_CONST -1
    MUL
    GSTORE
    GLOAD 0
    STORE
    JMP .loop
    .loop:
        GLOAD 1
        LOAD 0
        LE
        JMPF .end
        LOAD 0
        CALL test 1
        DUP
        STORE 0
        CALL println 1
        JMP .loop
    .end:
        LOAD_CONST 0
        RET
    HALT

fib:
    LOAD_CONST 0
    LOAD 0
    EQ
    SJMPT .true
    LOAD_CONST 1
    LOAD 0
    EQ
    OR
    JMPT .if-0
    LOAD_CONST 2
    LOAD 0
    SUB
    CALL fib 1
    LOAD_CONST 1
    LOAD 0
    SUB
    CALL fib 1
    ADD
    RET
    .if-0:
        LOAD 0
        RET

arraySum:
    LOAD_CONST 0
    LOAD 0
    CALL _length_a 1
    EQ
    JMPT .empty
    LOAD_CONST 0
    STORE
    LOAD_CONST 0
    LOAD 0
    AGET
    STORE
    LOAD 0
    STORE
    .loop:
        LOAD 3
        LOAD 0
        CALL _length_a 1
        EQ
        JMPT .end
        LOAD 2
        LOAD 1
        ADD
        STORE 1
        LOAD_CONST 1
        LOAD 3
        ADD
        STORE 3
        LOAD 3
        LOAD 0
        AGET
        STORE 2
        JMP .loop
    .end:
        LOAD 1
        RET
    .empty:
        LOAD_CONST 0
        RET

```

---

> **Goal:** Create an IR that is somewhere between the two forms

## IR proposals:

#### \#0

Skip IR and go directly to optimization then code generation

#### \#1

```
int test(local int n-0) {
    0 n-0 GT JMPT .true
    .true: return 1 n-0 SUB -1 MUL
    return 1 n-0 ADD
}

int _entry() {
    // global variables
    global int max-0 = 10
    global int min-0 = max-0 -1 MUL
    // main body
    local int i-0 = min-0
    loop: {
        max-0 i-0 LE JMPF .end
        i-0 = test(i-0)
        println(i-0)
    }
    .end: return 0
}

int fib(local int n-0) {
    0 n-0 EQ SJMPT .if-0 1 n-0 EQ OR JMPT .if-0
    return fib(2 n-0 SUB) fib(1 n-0 SUB) ADD
    .if-0: return n-0
}

int arraySum(local Array<int> nums-0) {
    0 length(nums-0) EQ JMPT .empty
    local int total-0 = 0
    local int num-0 = 0 nums-0 AGET
    local int i-0 = 0
    loop: {
        length(nums-0) i-0 EQ JMPT .end
        total-0 = num-0 total-0 ADD
        i-0 = 1 i-0 ADD
        num-0 = i-0 nums-0 AGET
    }
    .end: return total-0
    .empty: return 0
}
```

*Approach*
 - reorder operands to simulate stack operations
 - replace operators with their bytecode equivalents
 - replace conditionals with jumps
 - replace for and while loops with `loop {}`
 - move global variable declarations and main body to `_entry` function
 - if `main` function is void, return 0 in `_entry` function
 - add number to the end variable/function names to disambiguate them
 - label variables with `local` and `global` keywords during declaration for clarity
 - anything to the right of a `return` is considered part of the same statement

#### \#2

```
int test(int n) {
    JMPT .if-0 0 n LT
    return 1 n ADD
    .if-0: return 1 n SUB -1 MUL
}

int _entry() {
    // global variables
    global int max = 10
    global int min = max -1 MUL
    // main body
    int i = min
    loop {
        JMPF .loop-cond-0 max i LE
        i = CALL test 1 (i)
        CALL println 1 (i)
    }
    .loop-cond-0: return 0
}

int fib(int n) {
    JMPT .if-0 1 n EQ 0 n EQ SJMPT .if-0 OR
    return CALL fib 1 (2 n SUB) CALL fib 1 (1 n SUB) ADD
    .if-0: return n
}

int arraySum(int[] nums) {
    JMPT .if-0 0 CALL _length_a 1 (nums)
    int total = 0
    int num = 0 nums AGET
    int i = 0
    loop {
        JMPT .end CALL _length_a 1 (nums)
        total = num total ADD
        i = 1 i ADD
        num = i num AGET
    }
    .loop-cond-0: return total
    .if-0: return 0
}
```
*Approach*
- use simple names unless there are multiple of the same name
- if / loop condition labels are incrementally set
- jumps come at the beginning of conditionals with the condition following (with short circuit jumps in between)
- function calls are formatted as `CALL <function-name> <number of parameters> (param_1,param_2,...,param_N)`
- builtin functions use their bytecode equivalent names
- variables are assumed to be local unless global specifier is present
- Arrays types are reformatted with bracket notation
- In all other ways, the same as approach #1

#### \#3

```
int test(int n) {
    JMPT .if-0 0 n LT
    return 1 n ADD
    .if-0: return 1 n SUB -1 MUL
}

int _entry() {
    // global variables
    global int max = 10
    global int min = max -1 MUL
    // main body
    int i = min
    JMP .loop-0
    .loop-0 {
        JMPF .loop-cond-0 max i LE
        i = CALL test 1 (i)
        CALL println 1 (i)
        JMP .loop-0
    }
    .loop-cond-0: return 0
}

int fib(int n) {
    JMPT .if-0 1 n EQ 0 n EQ SJMPT .if-0 OR
    return CALL fib 1 (2 n SUB) CALL fib 1 (1 n SUB) ADD
    .if-0: return n
}

int arraySum(int[] nums) {
    JMPT .if-0 0 CALL _length_a 1 (nums)
    int total = 0
    int num = 0 nums AGET
    int i = 0
    JMP .loop-0
    .loop-0: {
        JMPT .end CALL _length_a 1 (nums)
        total = num total ADD
        i = 1 i ADD
        num = i num AGET
        JMP .loop-0
    }
    .loop-cond-0: return total
    .if-0: return 0
}
```
*Approach*
- Same as approach #2 but need to explicitly use jumps for loops (closer to bytecode)

#### \#4

```
int test(int n) {
    JMPT .if-0 0 LOAD n LT
    return 1 LOAD n ADD
    .if-0: return 1 LOAD n SUB -1 MUL
}

int _entry() {
    // global variables
    GSTORE int max 10
    GSTORE int min GLOAD max -1 MUL
    // main body
    STORE int i GLOAD min
    JMP .loop-0
    .loop-0 {
        JMPF .loop-cond-0 GLOAD max LOAD i LE
        STORE i CALL test 1 (LOAD i)
        CALL println 1 (LOAD i)
        JMP .loop-0
    }
    .loop-cond-0: return 0
}

int fib(int n) {
    JMPT .if-0 1 LOAD n EQ 0 LOAD n EQ SJMPT .if-0 OR
    return CALL fib 1 (2 LOAD n SUB) CALL fib 1 (1 LOAD n SUB) ADD
    .if-0: return LOAD n
}

int arraySum(int[] nums) {
    JMPT .if-0 0 CALL _length_a 1 (LOAD nums)
    STORE int total 0
    STORE num  0 LOAD nums AGET
    STORE int i 0
    JMP .loop-0
    .loop-0: {
        JMPT .end CALL _length_a 1 (LOAD nums)
        STORE total LOAD num LOAD total ADD
        STORE i 1 LOAD i ADD
        STORE num LOAD i LOAD num AGET
        JMP .loop-0
    }
    .loop-cond-0: return LOAD total
    .if-0: return 0
}
```
*Approach*
- Same as approach #3 but explicitly load store values

