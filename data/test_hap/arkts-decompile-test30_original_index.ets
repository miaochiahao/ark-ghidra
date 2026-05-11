// === Round 30: Default parameters, rest parameters,
//   nested try-catch with throw, Object.assign/Object.keys/values,
//   Map advanced, Set operations, instanceof with inheritance,
//   typeof type guard, Object.keys iteration ===

// --- Default parameters ---

function greet(name: string, greeting: string = 'Hello'): string {
  return greeting + ', ' + name + '!';
}

function testDefaultParams(): string {
  return greet('World') + '|' + greet('ArkTS', 'Hi');
}

// --- Rest parameters ---

function sumAll(...nums: number[]): number {
  let total: number = 0;
  for (let i: number = 0; i < nums.length; i++) {
    total = total + nums[i];
  }
  return total;
}

function testRestParams(): string {
  return String(sumAll(1, 2, 3)) + ',' + String(sumAll(10, 20));
}

// --- Nested try-catch with throw ---

function safeDivide(a: number, b: number): number {
  try {
    if (b === 0) {
      throw new Error('Division by zero');
    }
    return a / b;
  } catch (e) {
    return -1;
  }
}

function testNestedTryCatch(): string {
  let result: string = 'A';
  try {
    result = result + 'B';
    try {
      result = result + 'C';
      let v: number = safeDivide(10, 0);
      result = result + String(v);
    } catch (e) {
      result = result + 'X';
    }
    result = result + 'D';
  } catch (e) {
    result = result + 'Y';
  }
  return result;
}

// --- Manual object merge (Object.assign restricted) ---

function testObjectMerge(): string {
  let a: Record<string, number> = {};
  a['x'] = 1;
  a['y'] = 2;
  let b: Record<string, number> = {};
  b['y'] = 3;
  b['z'] = 4;
  let c: Record<string, number> = {};
  let aKeys: string[] = Object.keys(a);
  for (let i: number = 0; i < aKeys.length; i++) {
    c[aKeys[i]] = a[aKeys[i]];
  }
  let bKeys: string[] = Object.keys(b);
  for (let i: number = 0; i < bKeys.length; i++) {
    c[bKeys[i]] = b[bKeys[i]];
  }
  return String(c['x']) + ',' + String(c['y']) + ',' + String(c['z']);
}

// --- Object.keys / Object.values ---

function testObjectKeysValues(): string {
  let obj: Record<string, number> = {};
  obj['p'] = 10;
  obj['q'] = 20;
  obj['r'] = 30;
  let keys: string[] = Object.keys(obj);
  let vals: number[] = Object.values(obj);
  let sum: number = 0;
  for (let i: number = 0; i < vals.length; i++) {
    sum = sum + vals[i];
  }
  return keys.join(',') + '|' + String(sum);
}

// --- Map advanced operations ---

function testMapAdvanced(): string {
  let m: Map<string, number> = new Map();
  m.set('a', 1);
  m.set('b', 2);
  m.set('c', 3);
  let hasB: boolean = m.has('b');
  let del: boolean = m.delete('b');
  let size: number = m.size;
  let gotA: number | undefined = m.get('a');
  return String(hasB) + ',' + String(del) + ',' + String(size) + ',' + String(gotA);
}

// --- Set operations ---

function testSetOps(): string {
  let s: Set<number> = new Set();
  s.add(1);
  s.add(2);
  s.add(3);
  s.add(2);
  let hasTwo: boolean = s.has(2);
  let del: boolean = s.delete(3);
  let size: number = s.size;
  return String(hasTwo) + ',' + String(del) + ',' + String(size);
}

// --- instanceof with custom class hierarchy ---

class Shape2D {
  public kind: string;

  constructor(kind: string) {
    this.kind = kind;
  }

  public describe(): string {
    return 'Shape:' + this.kind;
  }
}

class Circle2D extends Shape2D {
  public radius: number;

  constructor(radius: number) {
    super('circle');
    this.radius = radius;
  }

  public area(): number {
    return 3.14159 * this.radius * this.radius;
  }
}

function testInstanceOfCustom(): string {
  let s: Shape2D = new Shape2D('square');
  let c: Shape2D = new Circle2D(5);
  let r1: boolean = s instanceof Circle2D;
  let r2: boolean = c instanceof Circle2D;
  let r3: boolean = c instanceof Shape2D;
  return String(r1) + ',' + String(r2) + ',' + String(r3);
}

// --- typeof type guard with narrowing ---

function describeValue(value: number | string): string {
  if (typeof value === 'number') {
    return 'num:' + String(value * 2);
  }
  return 'str:' + value;
}

function testTypeGuard(): string {
  return describeValue(21) + ',' + describeValue('hello');
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest30';

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
