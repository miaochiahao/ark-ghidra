// === Round 44: Chained comparisons, multi-dim array, function composition,
//   Map forEach, string building, leap year check, power of two check ===

// --- Chained range check (AND composition) ---

function inRange(x: number, lo: number, hi: number): boolean {
  return x > lo && x < hi;
}

function testInRange(): string {
  let r1: boolean = inRange(5, 1, 10);
  let r2: boolean = inRange(0, 1, 10);
  let r3: boolean = inRange(15, 1, 10);
  let r4: boolean = inRange(1, 1, 10);
  return String(r1) + ',' + String(r2) + ',' + String(r3) + ',' + String(r4);
}

// --- Multi-dimensional array creation ---

function createMatrix(rows: number, cols: number, fill: number): number[][] {
  let matrix: number[][] = [];
  for (let i: number = 0; i < rows; i++) {
    let row: number[] = [];
    for (let j: number = 0; j < cols; j++) {
      row.push(fill);
    }
    matrix.push(row);
  }
  return matrix;
}

function testMatrix(): string {
  let m: number[][] = createMatrix(3, 4, 7);
  return String(m.length) + 'x' + String(m[0].length) + ':' + String(m[1][2]);
}

// --- Function composition (curried) ---

function compose<A, B, C>(f: (x: A) => B, g: (x: B) => C): (x: A) => C {
  let composed = (x: A): C => {
    let intermediate: B = f(x);
    return g(intermediate);
  };
  return composed;
}

function double(n: number): number {
  return n * 2;
}

function addOne(n: number): number {
  return n + 1;
}

function testCompose(): string {
  let doubleThenAdd: (n: number) => number = compose(double, addOne);
  let addThenDouble: (n: number) => number = compose(addOne, double);
  let r1: number = doubleThenAdd(5);
  let r2: number = addThenDouble(5);
  return String(r1) + ',' + String(r2);
}

// --- Map forEach ---

function testMapForEach(): string {
  let scores: Map<string, number> = new Map();
  scores.set('Alice', 85);
  scores.set('Bob', 92);
  scores.set('Charlie', 78);
  let total: number = 0;
  let count: number = 0;
  let keys: string[] = ['Alice', 'Bob', 'Charlie'];
  for (let i: number = 0; i < keys.length; i++) {
    let val: number | undefined = scores.get(keys[i]);
    if (val !== undefined) {
      total = total + val;
      count = count + 1;
    }
  }
  return String(count) + ',' + String(total);
}

// --- String building with reverse ---

function reverseWords(s: string): string {
  let words: string[] = s.split(' ');
  let result: string[] = [];
  for (let i: number = words.length - 1; i >= 0; i--) {
    result.push(words[i]);
  }
  return result.join(' ');
}

function testReverseWords(): string {
  let r: string = reverseWords('hello world foo bar');
  return r;
}

// --- Leap year check ---

function isLeapYear(year: number): boolean {
  let div4: boolean = year % 4 === 0;
  let div100: boolean = year % 100 === 0;
  let div400: boolean = year % 400 === 0;
  if (div4 && !div100) {
    return true;
  }
  if (div400) {
    return true;
  }
  return false;
}

function testLeapYear(): string {
  let r1: boolean = isLeapYear(2000);
  let r2: boolean = isLeapYear(1900);
  let r3: boolean = isLeapYear(2024);
  let r4: boolean = isLeapYear(2023);
  return String(r1) + ',' + String(r2) + ',' + String(r3) + ',' + String(r4);
}

// --- Power of two check ---

function isPowerOfTwo(n: number): boolean {
  if (n <= 0) {
    return false;
  }
  return (n & (n - 1)) === 0;
}

function testPowerOfTwo(): string {
  let r1: boolean = isPowerOfTwo(1);
  let r2: boolean = isPowerOfTwo(8);
  let r3: boolean = isPowerOfTwo(12);
  let r4: boolean = isPowerOfTwo(64);
  return String(r1) + ',' + String(r2) + ',' + String(r3) + ',' + String(r4);
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest44';

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
