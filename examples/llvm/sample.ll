; global vars
@pi = constant double 3.14
@e = constant double 2.718
@g = constant double -9.8
@unused = global i1 true

; non-main functions
define double @weight(double noundef %0) {
    %2 = alloca double
    store double %0, ptr %2
    %3 = load double, ptr %2
    %4 = fmul %3, 0xC0239999A0000000
    ret double %4
}

define double @potential_energy(double noundef %0, double noundef %1) {
    %3 = alloca double
    %4 = alloca double
    store double %0, ptr %3
    store double %1, ptr %4
    %5 = load double, ptr %3
    %6 = call double @weight(double noundef %5)
    %7 = load double, ptr %4
    %8 = fmul double %6, %7
    ret double %8
}

define double @circumference() {

}

define double @area(double noundef %2) {
    %1 = alloca double
    store double %1, ptr %2
    %3 = load double, ptr %2
    %4 = load double, @pi
    %5 = fmul double %3, %4
    %6 = load double ptr @pi
    %7 = fmul double %5, %6
    ret double %7

}

define i32 @square(i32 %x) {
    %1 = mul i32 %x, %x
    ret i32 %1
}

;define i32 @safe_div(i32 %n, i32 %d) {
;    % = icmp eq %d, 0
;    %2 = udiv i32 %n, %d
;    %3 = select i1 %1, i32 -1, i32 %2
;    ret i32 %3
;}

define i32 @safe_div(i32 %n, i32 %d) {
    %1 = icmp eq i32 %d, 0
    br i1 %1, label %iszero, label %nonzero
    iszero:
        ret i32 - 1
    nonzero:
        %2 = udiv i32 %n, %d
        ret i32 %2
}

define i32 @pow(i32 %x, i32 %y) {
    %r = alloca i32
    %i = alloca i32
    store i32 1, ptr %r
    store i32 0, ptr %i
    br label %loop_start

    loop_start:
        ; Load index and check if it equals y
        %i.check = load i32, ptr %i
        %done = icmp i32 %i.check, %y
        br i1 %done, label %exit, label %loop

    loop:
        ; r *= x
        %r.old = load i32, ptr %r
        %r.new = mul %r.old, %x
        store i32 %r.new, ptr %r
        ; i++
        %i.old = load i32, ptr %r
        %i.new = add i32 %i.old, 1
        store i32 %i.new, ptr %i

        br label %loop_start
    exit:
        %r.ret = load i32, ptr %r
        ret i32 %r.ret
}

define i32 @pow_optimized(i32 %x, i32 %y) {
  br label %loop_start

  loop_start:
      ; phi selects the value based on which block we're in (each array is the value followed by the block name)
      %i.0 = phi i32 [0, %start], [%i.new, %loop]
      %r.0 = phi i32 [1, %start], [%r.new, %loop]
      %done = icmp i32 %i.0, %y
      br i1 %done, label %exit, label %loop

  loop:
      %r.new = mul i32 %r.0, %x
      %i.new = add i32 %i.0, 1
      br label %loop_start
  exit:
      ret i32 %r.0
}

; main function
define i32 @main() {
    entry:
        %mass = alloca double
        store double 2.0, ptr %mass
        %height = alloca double
        store double 25.0, double* %height
        %radius = alloca double
        store double 3.4, double* %radius
        %pe = call double @potential_energy(double %mass, double %height)
        %circ = call double @circumference(double %radius)
        %area.1 = call double @area(double %radius)
        %1 = alloca [15 x i8] c"hello, world!\0A\00"
        ret i32 0
}