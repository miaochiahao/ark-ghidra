// === Round 8: Try-catch-finally, Throw, instanceof, Symbol, Proxy,
//   Generator-like patterns (iterator), Namespace-like patterns ===

// --- Try-catch-finally ---

function testTryCatchFinally(): string {
  let result: string = '';
  try {
    result = 'try';
    let x: number = 1 / 0;
    if (x > 100) {
      throw new Error('too big');
    }
  } catch (e) {
    result = 'catch:' + String(e);
  } finally {
    result = result + '|finally';
  }
  return result;
}

// --- Nested try-catch ---

function testNestedTryCatch(): string {
  let result: string = '';
  try {
    try {
      result = 'inner';
      throw new Error('inner error');
    } catch (e) {
      result = result + ':' + 'inner_catch';
    }
  } catch (e) {
    result = result + ':' + 'outer_catch';
  }
  return result;
}

// --- instanceof check ---

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

  constructor(name: string, breed: string) {
    super(name);
    this.breed = breed;
  }

  public getBreed(): string {
    return this.breed;
  }
}

function testInstanceof(obj: Animal): string {
  if (obj instanceof Dog) {
    let d: Dog = obj as Dog;
    return d.getBreed();
  }
  return obj.getName();
}

// --- Record access pattern ---

function testRecordAccess(): string {
  let rec: Record<string, string> = {};
  rec['regular'] = 'normal';
  rec['extra'] = 'value';
  return rec['regular'] + '|' + rec['extra'];
}

// --- While loop with break/continue ---

function testWhileBreak(): number {
  let sum: number = 0;
  let i: number = 0;
  while (i < 100) {
    if (i % 2 === 0) {
      i = i + 1;
      continue;
    }
    if (i > 10) {
      break;
    }
    sum = sum + i;
    i = i + 1;
  }
  return sum;
}

// --- Switch with default and fallthrough ---

function testSwitchDefault(x: number): string {
  let result: string = '';
  switch (x) {
    case 1:
      result = 'one';
      break;
    case 2:
      result = 'two';
      break;
    default:
      result = 'other:' + String(x);
      break;
  }
  return result;
}

// --- Array findIndex / filter ---

function testArrayFindFilter(): string {
  let nums: number[] = [10, 20, 30, 40, 50];
  let idx: number = nums.findIndex((n: number): boolean => {
    return n > 25;
  });
  let filtered: number[] = nums.filter((n: number): boolean => {
    return n > 25;
  });
  return String(idx) + ':' + String(filtered.length);
}

// --- Object.keys / values ---

function testObjectKeys(): string {
  let obj: Record<string, number> = {};
  obj['x'] = 1;
  obj['y'] = 2;
  obj['z'] = 3;
  let keys: string[] = Object.keys(obj);
  return keys.join(',');
}

// --- Static class members ---

class Tally {
  private static tally: number = 0;

  public static increment(): number {
    Tally.tally = Tally.tally + 1;
    return Tally.tally;
  }

  public static reset(): void {
    Tally.tally = 0;
  }

  public static getCount(): number {
    return Tally.tally;
  }
}

function testStaticMembers(): string {
  Tally.reset();
  let a: number = Tally.increment();
  let b: number = Tally.increment();
  let c: number = Tally.getCount();
  return String(a) + ',' + String(b) + ',' + String(c);
}

// --- String methods: indexOf, includes, slice, repeat ---

function testStringMethods(): string {
  let s: string = 'hello world';
  let idx: number = s.indexOf('world');
  let has: boolean = s.includes('hello');
  let sub: string = s.slice(0, 5);
  let rep: string = 'ab'.repeat(3);
  return String(idx) + '|' + String(has) + '|' + sub + '|' + rep;
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest8';

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
