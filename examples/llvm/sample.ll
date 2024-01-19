; global vars
@pi = constant float 3.14
@e = constant float 2.718
@g = constant float -9.8
@unused = global i1 true

; non-main functions
define float @weight(float noundef %0) {
    %2 = alloca float
    store float %0, ptr %2
    %3 = load float, ptr %2
    %4 = fmul %3, 0xC0239999A0000000
    ret float %4
}

define float @potential_energy(float noundef %0, float noundef %1) {
    %3 = alloca float
    %4 = alloca float
    store float %0, ptr %3
    store float %1, ptr %4
    %5 = load float, ptr %3
    %6 = call float @weight(float noundef %5)
    %7 = load float, ptr %4
    %8 = fmul float %6, %7
    ret float %8
}

define float @circumference() {

}

define float @area(float noundef %2) {
    %1 = alloca float
    store float %1, ptr %2
    %3 = load double, ptr %2
    %4 = load float, @pi
    %5 = fmul float %3, %4
    %6 = load float ptr @pi
    %7 = fmul float %5, %6
    ret float %7

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
        %mass = alloca i32
        store i32 2, i32* %mass

        ret i32 0
}