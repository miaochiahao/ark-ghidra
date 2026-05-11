// === Round 22: String case conversion, Number static methods,
//   JSON round-trip, Object.keys, Math.min/max, early return,
//   switch-like if-else, string compare, function composition,
//   2D array flatten, boolean negation chains ===

// --- String case conversion ---

function testStringCase(): string {
  let s: string = 'Hello World';
  let upper: string = s.toUpperCase();
  let lower: string = s.toLowerCase();
  return upper + '|' + lower;
}

// --- Number static methods ---

function testNumberStatics(): string {
  let a: boolean = Number.isInteger(42);
  let b: boolean = Number.isInteger(3.14);
  let c: boolean = Number.isFinite(100);
  let d: boolean = Number.isNaN(NaN);
  return String(a) + ',' + String(b) + ',' + String(c) + ',' + String(d);
}

// --- JSON round-trip ---

function testJsonRoundTrip(): string {
  let obj: Record<string, number> = {};
  obj['x'] = 10;
  obj['y'] = 20;
  let json: string = JSON.stringify(obj);
  let parsed: Record<string, number> = JSON.parse(json) as Record<string, number>;
  return String(parsed['x']) + ',' + String(parsed['y']);
}

// --- Object.keys on Record ---

function testObjectKeys(): string {
  let obj: Record<string, number> = {};
  obj['a'] = 1;
  obj['b'] = 2;
  obj['c'] = 3;
  let keys: string[] = Object.keys(obj) as string[];
  return keys.join(',');
}

// --- Math.min/max with multiple args ---

function testMathMinMax(): string {
  let min: number = Math.min(5, 2, 8, 1, 9);
  let max: number = Math.max(5, 2, 8, 1, 9);
  return String(min) + ',' + String(max);
}

// --- Early return patterns ---

function findFirst(arr: number[], target: number): number {
  for (let i: number = 0; i < arr.length; i++) {
    if (arr[i] === target) {
      return i;
    }
  }
  return -1;
}

function testEarlyReturn(): string {
  let nums: number[] = [10, 20, 30, 40, 50];
  let idx1: number = findFirst(nums, 30);
  let idx2: number = findFirst(nums, 99);
  return String(idx1) + ',' + String(idx2);
}

// --- Switch-like if-else chain ---

function testSwitchLike(): string {
  let grade: string = 'B';
  let desc: string = '';
  if (grade === 'A') {
    desc = 'Excellent';
  } else if (grade === 'B') {
    desc = 'Good';
  } else if (grade === 'C') {
    desc = 'Average';
  } else {
    desc = 'Below Average';
  }
  return desc;
}

// --- String.localeCompare ---

function testStringCompare(): string {
  let a: string = 'apple';
  let b: string = 'banana';
  let cmp: number = a.localeCompare(b);
  if (cmp < 0) {
    return 'a<b';
  }
  if (cmp > 0) {
    return 'a>b';
  }
  return 'equal';
}

// --- Nested function calls (composition) ---

function double(x: number): number {
  return x * 2;
}

function addOne(x: number): number {
  return x + 1;
}

function testComposition(): string {
  let v: number = 5;
  v = double(v);
  v = addOne(v);
  v = double(v);
  return String(v);
}

// --- 2D array flatten (manual) ---

function testFlatten2D(): string {
  let matrix: number[][] = [[1, 2, 3], [4, 5, 6], [7, 8, 9]];
  let flat: number[] = [];
  for (let i: number = 0; i < matrix.length; i++) {
    let row: number[] = matrix[i];
    for (let j: number = 0; j < row.length; j++) {
      flat.push(row[j]);
    }
  }
  return String(flat[4]) + ',' + String(flat[8]);
}

// --- Boolean negation chains ---

function testBoolNegation(): string {
  let a: boolean = true;
  let b: boolean = !a;
  let c: boolean = !b;
  let d: boolean = !!a;
  return String(a) + ',' + String(b) + ',' + String(c) + ',' + String(d);
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest22';

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
