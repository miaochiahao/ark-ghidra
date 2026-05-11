// === Round 31: Destructuring (manual), finally block,
//   void operator, multi-level super calls, string padding,
//   array from + fill, number parsing edge cases,
//   conditional chaining with ternary, localeCompare ===

// --- Manual array destructuring ---

function testArrayDestructure(): string {
  let arr: number[] = [10, 20, 30, 40, 50];
  let first: number = arr[0];
  let second: number = arr[1];
  let third: number = arr[2];
  return String(first + second + third);
}

// --- Manual object destructuring ---

class Point {
  public x: number;
  public y: number;

  constructor(x: number, y: number) {
    this.x = x;
    this.y = y;
  }
}

function testObjectDestructure(): string {
  let p: Point = new Point(3, 7);
  let x: number = p.x;
  let y: number = p.y;
  return String(x) + ',' + String(y);
}

// --- Finally block behavior ---

function testFinallyBasic(): string {
  let result: string = 'A';
  try {
    result = result + 'B';
  } finally {
    result = result + 'C';
  }
  return result;
}

function testFinallyWithCatch(): string {
  let result: string = 'A';
  try {
    result = result + 'B';
    throw new Error('test');
  } catch (e) {
    result = result + 'E';
  } finally {
    result = result + 'F';
  }
  return result;
}

// --- void operator (returns undefined) ---

function testVoidExpr(): string {
  let a: number = 42;
  let b: number | undefined = void a;
  return String(b);
}

// --- Multi-level super calls ---

class Base {
  public value: number;

  constructor(value: number) {
    this.value = value;
  }

  public double(): number {
    return this.value * 2;
  }
}

class Middle extends Base {
  public label: string;

  constructor(value: number, label: string) {
    super(value);
    this.label = label;
  }

  public double(): number {
    return super.double() + 1;
  }
}

class Leaf extends Middle {
  public extra: number;

  constructor(value: number, label: string, extra: number) {
    super(value, label);
    this.extra = extra;
  }

  public double(): number {
    return super.double() + this.extra;
  }
}

function testMultiLevelSuper(): string {
  let l: Leaf = new Leaf(10, 'test', 5);
  let r: number = l.double();
  return String(r);
}

// --- String padStart / padEnd ---

function testStringPad(): string {
  let s: string = '42';
  let padded: string = s.padStart(6, '0');
  let right: string = s.padEnd(6, '-');
  return padded + '|' + right;
}

// --- Array.from + fill ---

function testArrayFromFill(): string {
  let arr: number[] = new Array<number>(5);
  arr.fill(7);
  let arr2: number[] = [1, 2, 3, 4, 5];
  arr2.fill(0, 2, 4);
  return String(arr[0]) + ',' + String(arr[4]) + ',' + String(arr2[2]) + ',' + String(arr2[3]);
}

// --- Number parsing edge cases ---

function testNumberParsing(): string {
  let a: number = parseInt('ff', 16);
  let b: number = parseInt('1010', 2);
  let c: number = parseFloat('3.14');
  let d: number = Number('0');
  return String(a) + ',' + String(b) + ',' + String(c) + ',' + String(d);
}

// --- Conditional chaining with ternary ---

function testConditionalChain(): string {
  let x: number = 15;
  let label: string = '';
  if (x > 100) {
    label = 'huge';
  } else if (x > 50) {
    label = 'big';
  } else if (x > 10) {
    label = 'medium';
  } else if (x > 0) {
    label = 'small';
  } else {
    label = 'none';
  }
  return label;
}

// --- localeCompare ---

function testLocaleCompare(): string {
  let a: string = 'apple';
  let b: string = 'banana';
  let c: string = 'apple';
  let r1: number = a.localeCompare(b);
  let r2: number = a.localeCompare(c);
  let r3: number = b.localeCompare(a);
  return String(r1 < 0) + ',' + String(r2 === 0) + ',' + String(r3 > 0);
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest31';

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
