// === Exception Handling ===

function testTryCatchFinally(): string {
  let result: string = '';
  try {
    let arr: number[] = [1, 2, 3];
    let val: number = arr[10];
    result = 'no error';
  } catch (err) {
    result = 'caught: ' + String(err);
  } finally {
    result = result + ' done';
  }
  return result;
}

function testTryCatchWithThrow(): number {
  try {
    throw new Error('test error');
  } catch (err) {
    return 42;
  }
  return 0;
}

// === Promise / Async ===

async function testAsyncAwait(): Promise<string> {
  let result: string = await Promise.resolve('hello');
  return result + ' world';
}

function testPromiseChain(): Promise<number> {
  return new Promise<number>((resolve: Function, reject: Function): void => {
    resolve(100);
  });
}

// === Map / Set Collections ===

function testMapOperations(): number {
  let m: Map<string, number> = new Map();
  m.set('a', 1);
  m.set('b', 2);
  m.set('c', 3);
  let total: number = 0;
  m.forEach((value: number, key: string): void => {
    total += value;
  });
  return total;
}

function testSetOperations(): number {
  let s: Set<number> = new Set();
  s.add(1);
  s.add(2);
  s.add(2);
  s.add(3);
  return s.size;
}

// === Destructuring ===

function testArrayAccess(): string {
  let arr: number[] = [10, 20, 30];
  let a: number = arr[0];
  let b: number = arr[1];
  let c: number = arr[2];
  return String(a + b + c);
}

function testObjectAccess(): string {
  let obj: Record<string, number> = { 'x': 1, 'y': 2, 'z': 3 };
  let x: number = obj['x'];
  let y: number = obj['y'];
  return String(x + y);
}

// === Spread Operator ===

function testSpreadArray(): number {
  let a: number[] = [1, 2, 3];
  let b: number[] = [...a, 4, 5];
  let total: number = 0;
  for (const item of b) {
    total += item;
  }
  return total;
}

// === Optional Chaining / Nullish Coalescing ===

function testNullCheck(): string {
  let obj: string | null = null;
  if (obj !== null) {
    return obj;
  }
  return 'was null';
}

function testDefaultValue(): number {
  let val: number | null = null;
  let result: number = val !== null ? val : 99;
  return result;
}

// === Type Guards (instanceof) ===

function testInstanceOf(): string {
  let obj: Animal = new Dog('Rex', 5, 'Labrador');
  if (obj instanceof Dog) {
    return 'is Dog';
  }
  return 'not Dog';
}

// === Getter / Setter ===

class Temperature {
  private _celsius: number;

  constructor(celsius: number) {
    this._celsius = celsius;
  }

  get fahrenheit(): number {
    return this._celsius * 9 / 5 + 32;
  }

  set fahrenheit(f: number) {
    this._celsius = (f - 32) * 5 / 9;
  }

  public getCelsius(): number {
    return this._celsius;
  }
}

// === Classes (reused by testInstanceOf) ===

class Animal {
  private name: string;

  constructor(name: string) {
    this.name = name;
  }

  public getName(): string {
    return this.name;
  }
}

class Dog extends Animal {
  private breed: string;

  constructor(name: string, age: number, breed: string) {
    super(name);
    this.breed = breed;
  }

  public getBreed(): string {
    return this.breed;
  }
}

// === Template Literal with expressions ===

function testTemplateLiteral(name: string, age: number): string {
  return `Name: ${name}, Age: ${String(age)}`;
}

// === Record type ===

function testRecordType(): number {
  let scores: Record<string, number> = {};
  scores['alice'] = 90;
  scores['bob'] = 85;
  scores['charlie'] = 95;
  return scores['alice'] + scores['bob'] + scores['charlie'];
}

// === Method overloading pattern (multiple calls with different args) ===

function testStringMethods(): string {
  let s: string = 'Hello, World!';
  let lower: string = s.toLowerCase();
  let upper: string = s.toUpperCase();
  let sub: string = s.substring(0, 5);
  let len: number = s.length;
  return lower + '|' + upper + '|' + sub + '|' + String(len);
}

// === Main entry ===

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest2';

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
