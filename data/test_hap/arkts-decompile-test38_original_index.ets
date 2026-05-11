// === Round 38: Map forEach iteration, switch with break,
//   object property enumeration, Promise chain (.then/.catch),
//   string encoding patterns, nested function references ===

// --- Map forEach iteration ---

function testMapIteration(): string {
  let m: Map<string, number> = new Map();
  m.set('x', 10);
  m.set('y', 20);
  m.set('z', 30);
  let total: number = 0;
  let keys: string[] = ['x', 'y', 'z'];
  for (let i: number = 0; i < keys.length; i++) {
    let val: number | undefined = m.get(keys[i]);
    if (val !== undefined) {
      total = total + val;
    }
  }
  return String(total) + ',' + String(m.size);
}

// --- Object property enumeration with computed keys ---

function testComputedKeys(): string {
  let fields: string[] = ['name', 'age', 'city'];
  let data: Record<string, string> = {};
  for (let i: number = 0; i < fields.length; i++) {
    let key: string = fields[i];
    if (key === 'name') {
      data[key] = 'Alice';
    } else if (key === 'age') {
      data[key] = '30';
    } else {
      data[key] = 'Beijing';
    }
  }
  return data['name'] + ',' + data['age'] + ',' + data['city'];
}

// --- Promise chain (.then/.catch) ---

function testPromiseChain(): string {
  let p: Promise<number> = new Promise<number>((resolve: Function, reject: Function): void => {
    resolve(42);
  });
  let transformed: Promise<string> = p.then((val: number): string => {
    return String(val * 2);
  });
  let caught: Promise<string> = transformed.catch((err: Error): string => {
    return 'error';
  });
  return 'created';
}

// --- String encoding patterns (charCodeAt range) ---

function testCharCodeRange(): string {
  let result: string = '';
  for (let i: number = 65; i <= 70; i++) {
    result = result + String.fromCharCode(i);
  }
  let code0: number = 'A'.charCodeAt(0);
  let code5: number = 'F'.charCodeAt(0);
  return result + ',' + String(code0) + ',' + String(code5);
}

// --- Nested function references via closures ---

function makeAdder(x: number): (n: number) => number {
  return (n: number): number => {
    return n + x;
  };
}

function testClosureChain(): string {
  let add5: (n: number) => number = makeAdder(5);
  let add10: (n: number) => number = makeAdder(10);
  let r1: number = add5(3);
  let r2: number = add10(7);
  return String(r1) + ',' + String(r2);
}

// --- Leap year check (multi-condition) ---

function isLeapYear(year: number): boolean {
  if (year % 4 !== 0) {
    return false;
  }
  if (year % 100 !== 0) {
    return true;
  }
  if (year % 400 === 0) {
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

// --- FizzBuzz with string accumulation ---

function fizzBuzz(n: number): string {
  let result: string = '';
  for (let i: number = 1; i <= n; i++) {
    if (i % 15 === 0) {
      result = result + 'FB,';
    } else if (i % 3 === 0) {
      result = result + 'F,';
    } else if (i % 5 === 0) {
      result = result + 'B,';
    } else {
      result = result + String(i) + ',';
    }
  }
  return result;
}

function testFizzBuzz(): string {
  return fizzBuzz(15);
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest38';

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
