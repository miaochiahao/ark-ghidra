// === Round 50: Extended GCD, array rotation, run-length encoding,
//   Range class, multi-level data structure, digit sum ===

// --- Extended GCD (iterative) ---

function extendedGCD(a: number, b: number): number[] {
  let oldR: number = a;
  let r: number = b;
  let oldS: number = 1;
  let s: number = 0;
  while (r !== 0) {
    let q: number = Math.floor(oldR / r);
    let temp: number = r;
    r = oldR - q * r;
    oldR = temp;
    temp = s;
    s = oldS - q * s;
    oldS = temp;
  }
  return [oldR, oldS];
}

function testExtendedGCD(): string {
  let r1: number[] = extendedGCD(30, 12);
  let r2: number[] = extendedGCD(35, 15);
  let r3: number[] = extendedGCD(101, 13);
  return String(r1[0]) + ',' + String(r2[0]) + ',' + String(r3[0]);
}

// --- Array rotation ---

function rotateLeft(arr: number[], k: number): number[] {
  let n: number = arr.length;
  let steps: number = k % n;
  let result: number[] = [];
  for (let i: number = 0; i < n; i++) {
    let src: number = (i + steps) % n;
    result.push(arr[src]);
  }
  return result;
}

function testRotate(): string {
  let data: number[] = [1, 2, 3, 4, 5];
  let rotated: number[] = rotateLeft(data, 2);
  return String(rotated[0]) + ',' + String(rotated[1]) + ',' + String(rotated[2]);
}

// --- Run-length encoding ---

function runLengthEncode(s: string): string {
  if (s.length === 0) {
    return '';
  }
  let result: string = '';
  let count: number = 1;
  let current: string = s.charAt(0);
  for (let i: number = 1; i < s.length; i++) {
    let ch: string = s.charAt(i);
    if (ch === current) {
      count = count + 1;
    } else {
      result = result + current + String(count);
      current = ch;
      count = 1;
    }
  }
  result = result + current + String(count);
  return result;
}

function testRLE(): string {
  let r1: string = runLengthEncode('AAABBBCC');
  let r2: string = runLengthEncode('ABCDE');
  let r3: string = runLengthEncode('AABBCCDD');
  return r1 + '|' + r2 + '|' + r3;
}

// --- Range class ---

class Range {
  private start: number;
  private end: number;

  constructor(start: number, end: number) {
    this.start = start;
    this.end = end;
  }

  public contains(value: number): boolean {
    return value >= this.start && value <= this.end;
  }

  public overlaps(other: Range): boolean {
    return this.start <= other.end && other.start <= this.end;
  }

  public toArray(): number[] {
    let result: number[] = [];
    for (let i: number = this.start; i <= this.end; i++) {
      result.push(i);
    }
    return result;
  }
}

function testRange(): string {
  let r1: Range = new Range(1, 5);
  let r2: Range = new Range(3, 8);
  let contains3: boolean = r1.contains(3);
  let contains7: boolean = r1.contains(7);
  let overlaps: boolean = r1.overlaps(r2);
  let arr: number[] = r1.toArray();
  return String(contains3) + ',' + String(contains7) + ',' + String(overlaps) + ',' + String(arr.length);
}

// --- Digit sum with loop ---

function digitSum(n: number): number {
  let sum: number = 0;
  let num: number = n;
  while (num > 0) {
    sum = sum + (num % 10);
    num = Math.floor(num / 10);
  }
  return sum;
}

function testDigitSum(): string {
  let r1: number = digitSum(12345);
  let r2: number = digitSum(999);
  let r3: number = digitSum(1001);
  return String(r1) + ',' + String(r2) + ',' + String(r3);
}

// --- Multi-level data structure ---

function testMultiLevel(): string {
  let departments: Map<string, Map<string, number>> = new Map();
  let eng: Map<string, number> = new Map();
  eng.set('senior', 5);
  eng.set('junior', 10);
  let sales: Map<string, number> = new Map();
  sales.set('senior', 3);
  sales.set('junior', 8);
  departments.set('engineering', eng);
  departments.set('sales', sales);
  let total: number = 0;
  let deptKeys: string[] = ['engineering', 'sales'];
  for (let i: number = 0; i < deptKeys.length; i++) {
    let dept: Map<string, number> | undefined = departments.get(deptKeys[i]);
    if (dept !== undefined) {
      let senior: number | undefined = dept.get('senior');
      if (senior !== undefined) {
        total = total + senior;
      }
    }
  }
  return String(total);
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest50';

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
