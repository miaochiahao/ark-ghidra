// === Math Operations ===

function testMathOps(): number {
  let a: number = Math.abs(-5);
  let b: number = Math.max(1, 2, 3);
  let c: number = Math.min(1, 2, 3);
  let d: number = Math.ceil(3.2);
  let e: number = Math.floor(3.8);
  let f: number = Math.round(3.5);
  let g: number = Math.sqrt(16);
  let h: number = Math.pow(2, 10);
  return a + b + c + d + e + f + g + h;
}

// === Bitwise Operations ===

function testBitwiseOps(a: number, b: number): number {
  let and: number = a & b;
  let or: number = a | b;
  let xor: number = a ^ b;
  let not: number = ~a;
  let left: number = a << 2;
  let right: number = a >> 2;
  let unsignedRight: number = a >>> 2;
  return and + or + xor + not + left + right + unsignedRight;
}

// === Number Conversions ===

function testNumberConversions(): string {
  let intVal: number = parseInt('42', 10);
  let floatVal: number = parseFloat('3.14');
  let nanCheck: boolean = isNaN(floatVal);
  let finiteCheck: boolean = isFinite(intVal);
  return String(intVal) + '|' + String(floatVal) + '|' + String(nanCheck) + '|' + String(finiteCheck);
}

function testNumberFormat(): string {
  let n: number = 3.14159;
  let fixed: string = n.toFixed(2);
  let str: string = n.toString();
  return fixed + '|' + str;
}

// === Array Method Chain ===

function testArrayChain(): number {
  let nums: number[] = [5, 3, 8, 1, 9, 2, 7, 4, 6];
  let sorted: number[] = nums.sort((a: number, b: number): number => a - b);
  let top5: number[] = sorted.slice(0, 5);
  let sum: number = top5.reduce((acc: number, n: number): number => acc + n, 0);
  return sum;
}

function testArraySearch(): number {
  let nums: number[] = [10, 20, 30, 40, 50];
  let idx: number = nums.indexOf(30);
  let has20: boolean = nums.includes(20);
  let firstBig: number | undefined = nums.find((n: number): boolean => n > 25);
  let allPositive: boolean = nums.every((n: number): boolean => n > 0);
  let someBig: boolean = nums.some((n: number): boolean => n > 45);
  return idx;
}

function testArrayTransform(): string {
  let words: string[] = ['hello', 'world', 'test'];
  let joined: string = words.join('-');
  let upper: string[] = words.map((w: string): string => w.toUpperCase());
  let hasLong: boolean = words.some((w: string): boolean => w.length > 4);
  return joined;
}

// === String Methods ===

function testStringMethods2(): string {
  let s: string = '  Hello, World!  ';
  let trimmed: string = s.trim();
  let lower: string = trimmed.toLowerCase();
  let upper: string = trimmed.toUpperCase();
  let hasHello: boolean = trimmed.includes('Hello');
  let startsWith: boolean = trimmed.startsWith('Hello');
  let endsWith: boolean = trimmed.endsWith('!');
  let idx: number = trimmed.indexOf(',');
  let replaced: string = trimmed.replace('World', 'ArkTS');
  let repeated: string = 'ab'.repeat(3);
  let padded: string = '5'.padStart(3, '0');
  return lower + '|' + String(hasHello) + '|' + String(startsWith) + '|' + replaced;
}

// === JSON Operations ===

function testJsonOps(): string {
  let obj: Record<string, number> = {};
  obj['x'] = 10;
  obj['y'] = 20;
  let jsonStr: string = JSON.stringify(obj);
  return jsonStr;
}

// === Conditional (ternary) chaining ===

function testTernaryChain(x: number): string {
  return x > 0 ? 'positive' : (x < 0 ? 'negative' : 'zero');
}

// === Increment/Decrement operators ===

function testIncDecOps(): number {
  let a: number = 5;
  a++;
  a++;
  a--;
  let b: number = 10;
  b += 3;
  b -= 2;
  b *= 4;
  b /= 2;
  b %= 3;
  return a + b;
}

// === Comparison operators ===

function testComparisons(a: number, b: number): string {
  let eq: boolean = a === b;
  let neq: boolean = a !== b;
  let lt: boolean = a < b;
  let gt: boolean = a > b;
  let lte: boolean = a <= b;
  let gte: boolean = a >= b;
  return String(eq) + String(neq) + String(lt) + String(gt) + String(lte) + String(gte);
}

// === Logical NOT ===

function testLogicalNot(flag: boolean): boolean {
  return !flag;
}

// === Void expression / undefined check ===

function testUndefinedCheck(): string {
  let val: string | undefined = undefined;
  if (val === undefined) {
    return 'undefined';
  }
  return val;
}

// === Chained method calls (builder pattern) ===

class StringBuilder {
  private parts: string[];

  constructor() {
    this.parts = [];
  }

  public add(s: string): StringBuilder {
    this.parts.push(s);
    return this;
  }

  public build(): string {
    return this.parts.join(' ');
  }
}

function testBuilderPattern(): string {
  let sb: StringBuilder = new StringBuilder();
  let result: string = sb.add('Hello').add('Beautiful').add('World').build();
  return result;
}

// === Nested object access via methods ===

class Pair {
  private first: number;
  private second: number;

  constructor(first: number, second: number) {
    this.first = first;
    this.second = second;
  }

  public getFirst(): number {
    return this.first;
  }

  public getSecond(): number {
    return this.second;
  }

  public swap(): Pair {
    return new Pair(this.second, this.first);
  }
}

function testNestedMethodCalls(): number {
  let p: Pair = new Pair(1, 2);
  let swapped: Pair = p.swap();
  let swappedAgain: Pair = swapped.swap();
  return swappedAgain.getFirst() + swappedAgain.getSecond();
}

// === Main entry ===

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest4';

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
