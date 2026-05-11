// === Round 13: Closure factories, Higher-order functions, Custom Error subclass,
//   Array.find, String.startsWith/endsWith, Map.has/get, Stack impl, JSON.parse ===

// --- Closure factory (function returning function) ---

function makeAdder(base: number): (n: number) => number {
  let captured: number = base;
  let adder: (n: number) => number = (n: number): number => {
    return n + captured;
  };
  return adder;
}

function testClosureFactory(): string {
  let add5: (n: number) => number = makeAdder(5);
  let add10: (n: number) => number = makeAdder(10);
  let a: number = add5(3);
  let b: number = add10(3);
  return String(a) + ',' + String(b);
}

// --- Higher-order function (takes function arg) ---

function applyTwice(x: number, fn: (n: number) => number): number {
  let once: number = fn(x);
  let twice: number = fn(once);
  return twice;
}

function testHigherOrder(): string {
  let double: (n: number) => number = (n: number): number => {
    return n * 2;
  };
  let result: number = applyTwice(3, double);
  return String(result);
}

// --- Custom Error subclass ---

class AppError {
  private code: number;
  private msg: string;

  constructor(code: number, msg: string) {
    this.code = code;
    this.msg = msg;
  }

  public getCode(): number {
    return this.code;
  }

  public getMessage(): string {
    return this.msg;
  }
}

function testCustomError(): string {
  let err: AppError = new AppError(404, 'not found');
  return String(err.getCode()) + ':' + err.getMessage();
}

// --- Array.find ---

function testArrayFind(): string {
  let items: string[] = ['apple', 'banana', 'cherry', 'date'];
  let found: string | undefined;
  let i: number = 0;
  while (i < items.length) {
    if (items[i].startsWith('ch')) {
      found = items[i];
      break;
    }
    i = i + 1;
  }
  if (found !== undefined) {
    return found;
  }
  return 'not found';
}

// --- String.startsWith / endsWith ---

function testStringStartsEnds(): string {
  let s: string = 'Hello, World!';
  let starts: boolean = s.startsWith('Hello');
  let ends: boolean = s.endsWith('!');
  let starts2: boolean = s.startsWith('World', 7);
  return String(starts) + ',' + String(ends) + ',' + String(starts2);
}

// --- Map.has / Map.get pattern ---

function testMapGetDefault(): string {
  let cache: Map<string, number> = new Map();
  cache.set('a', 1);
  cache.set('b', 2);
  let key: string = 'c';
  let val: number;
  if (cache.has(key)) {
    let existing: number | undefined = cache.get(key);
    if (existing !== undefined) {
      val = existing;
    } else {
      val = 0;
    }
  } else {
    val = 99;
    cache.set(key, val);
  }
  return String(val);
}

// --- Stack implementation (push/pop) ---

class NumStack {
  private items: number[];

  constructor() {
    this.items = [];
  }

  public push(val: number): void {
    this.items.push(val);
  }

  public pop(): number {
    let len: number = this.items.length;
    if (len === 0) {
      return -1;
    }
    let last: number = this.items[len - 1];
    this.items.splice(len - 1, 1);
    return last;
  }

  public peek(): number {
    let len: number = this.items.length;
    if (len === 0) {
      return -1;
    }
    return this.items[len - 1];
  }

  public size(): number {
    return this.items.length;
  }
}

function testStack(): string {
  let s: NumStack = new NumStack();
  s.push(10);
  s.push(20);
  s.push(30);
  let peeked: number = s.peek();
  let popped: number = s.pop();
  let size: number = s.size();
  return String(peeked) + ',' + String(popped) + ',' + String(size);
}

// --- JSON.parse ---

function testJsonParse(): string {
  let json: string = '{"name":"test","value":42}';
  let obj: Record<string, Object> = JSON.parse(json) as Record<string, Object>;
  let name: string = obj['name'] as string;
  let val: number = obj['value'] as number;
  return name + '=' + String(val);
}

// --- Object property access with default ---

function testPropDefault(): string {
  let obj: Record<string, number> = {};
  obj['x'] = 10;
  let valX: number = obj['x'];
  let valY: number = obj['y'];
  return String(valX) + ',' + String(valY);
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest13';

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
