// === Round 35: Array.reduce, Array.findIndex, nested ternary,
//   Number constants (isInteger/isSafeInteger), Array.forEach,
//   String split+join, chained range comparisons ===

// --- Array.reduce (sum and max) ---

function testReduce(): string {
  let arr: number[] = [1, 2, 3, 4, 5];
  let sum: number = arr.reduce((acc: number, val: number): number => {
    return acc + val;
  }, 0);
  let max: number = arr.reduce((acc: number, val: number): number => {
    if (val > acc) {
      return val;
    }
    return acc;
  }, arr[0]);
  return String(sum) + ',' + String(max);
}

// --- Array.findIndex ---

function testFindIndex(): string {
  let arr: number[] = [10, 20, 30, 40, 50];
  let idx: number = arr.findIndex((val: number): boolean => {
    return val > 25;
  });
  return String(idx);
}

// --- Nested ternary chains ---

function gradeLabel(score: number): string {
  if (score >= 90) {
    return 'A';
  }
  if (score >= 80) {
    return 'B';
  }
  if (score >= 70) {
    return 'C';
  }
  if (score >= 60) {
    return 'D';
  }
  return 'F';
}

function testGradeLabel(): string {
  return gradeLabel(95) + ',' + gradeLabel(85) + ',' + gradeLabel(75) + ',' + gradeLabel(65) + ',' + gradeLabel(50);
}

// --- Number.isInteger / isSafeInteger ---

function testNumberChecks(): string {
  let isInt1: boolean = Number.isInteger(42);
  let isInt2: boolean = Number.isInteger(3.14);
  let isSafe: boolean = Number.isSafeInteger(42);
  return String(isInt1) + ',' + String(isInt2) + ',' + String(isSafe);
}

// --- Array.forEach with side effects ---

function testForEach(): string {
  let arr: string[] = ['a', 'b', 'c', 'd'];
  let result: string = '';
  arr.forEach((val: string): void => {
    result = result + val.toUpperCase();
  });
  return result;
}

// --- String split + join ---

function testSplitJoin(): string {
  let csv: string = 'one,two,three,four';
  let parts: string[] = csv.split(',');
  let joined: string = parts.join('-');
  return String(parts.length) + ',' + joined;
}

// --- Chained range comparisons ---

function inRange(x: number, low: number, high: number): boolean {
  return x >= low && x <= high;
}

function testRangeCheck(): string {
  let r1: boolean = inRange(5, 1, 10);
  let r2: boolean = inRange(15, 1, 10);
  let r3: boolean = inRange(1, 1, 10);
  let r4: boolean = inRange(10, 1, 10);
  return String(r1) + ',' + String(r2) + ',' + String(r3) + ',' + String(r4);
}

// --- String reverse (manual) ---

function reverseString(s: string): string {
  let result: string = '';
  for (let i: number = s.length - 1; i >= 0; i--) {
    result = result + s.charAt(i);
  }
  return result;
}

function testStringReverse(): string {
  return reverseString('hello') + ',' + reverseString('ArkTS');
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest35';

  build() {
    Column() {
      Text(this.message)
        .fontSize(30)
        .fontWeight(FontWeight.Bold)
    }
    .width('100%')
    .height('100%')
  }
}
