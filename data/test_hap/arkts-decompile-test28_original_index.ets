// === Round 28: Generic class with type-specific behavior,
//   decorator-like pattern via wrapper, array destructuring manual,
//   object rest/spread manual, generator-like coroutine (manual),
//   curried function pattern, partial application, memoization,
//   deep clone manual, property descriptor emulation ===

// --- Generic class (manual, ArkTS has limited generics) ---

class NumStack<T> {
  private items: T[];

  constructor() {
    this.items = [];
  }

  public push(item: T): void {
    this.items.push(item);
  }

  public pop(): T {
    if (this.items.length === 0) {
      return undefined as T;
    }
    return this.items.pop() as T;
  }

  public peek(): T {
    if (this.items.length === 0) {
      return undefined as T;
    }
    return this.items[this.items.length - 1] as T;
  }

  public size(): number {
    return this.items.length;
  }
}

function testGenericStack(): string {
  let s: NumStack<string> = new NumStack<string>();
  s.push('a');
  s.push('b');
  s.push('c');
  let size: number = s.size();
  let top: string = s.peek();
  let popped: string = s.pop();
  let size2: number = s.size();
  return String(size) + ',' + top + ',' + popped + ',' + String(size2);
}

// --- Curried function pattern ---

function makeAdder(x: number): (n: number) => number {
  let addX: (n: number) => number = (n: number): number => {
    return x + n;
  };
  return addX;
}

function testCurrying(): string {
  let add5: (n: number) => number = makeAdder(5);
  let add10: (n: number) => number = makeAdder(10);
  let r1: number = add5(3);
  let r2: number = add10(7);
  return String(r1) + ',' + String(r2);
}

// --- Memoization (manual cache) ---

function testMemoization(): string {
  let cache: Map<number, number> = new Map<number, number>();
  // Compute square with cache
  let key: number = 5;
  let cached: number | undefined = cache.get(key);
  if (cached === undefined) {
    cached = key * key;
    cache.set(key, cached);
  }
  let r1: number = cached;
  // Second lookup (cached)
  cached = cache.get(key);
  if (cached === undefined) {
    cached = key * key;
    cache.set(key, cached);
  }
  let r2: number = cached;
  return String(r1) + ',' + String(r2);
}

// --- Deep clone manual ---

function cloneRecord(src: Record<string, number>): Record<string, number> {
  let dst: Record<string, number> = {};
  let keys: string[] = Object.keys(src) as string[];
  for (let i: number = 0; i < keys.length; i++) {
    let k: string = keys[i];
    dst[k] = src[k];
  }
  return dst;
}

function testDeepClone(): string {
  let original: Record<string, number> = {};
  original['x'] = 10;
  original['y'] = 20;
  let cloned: Record<string, number> = cloneRecord(original);
  cloned['x'] = 99;
  return String(original['x']) + ',' + String(cloned['x']);
}

// --- Array destructuring manual ---

function testArrayDestructure(): string {
  let arr: number[] = [100, 200, 300];
  let a: number = arr[0];
  let b: number = arr[1];
  let c: number = arr[2];
  return String(a + b + c);
}

// --- Object rest manual (pick/omit) ---

function testObjectRest(): string {
  let src: Record<string, number> = {};
  src['a'] = 1;
  src['b'] = 2;
  src['c'] = 3;
  // Omit 'b' (manual)
  let dst: Record<string, number> = {};
  let keys: string[] = Object.keys(src) as string[];
  for (let i: number = 0; i < keys.length; i++) {
    let k: string = keys[i];
    if (k !== 'b') {
      dst[k] = src[k];
    }
  }
  return String(dst['a']) + ',' + String(dst['c']);
}

// --- Partial application ---

function multiply(a: number, b: number): number {
  return a * b;
}

function testPartialApplication(): string {
  let double: (n: number) => number = (n: number): number => {
    return multiply(2, n);
  };
  let triple: (n: number) => number = (n: number): number => {
    return multiply(3, n);
  };
  let r1: number = double(5);
  let r2: number = triple(5);
  return String(r1) + ',' + String(r2);
}

// --- Property descriptor emulation (get/set pair) ---

class ObservableValue {
  private _val: number;
  private _listeners: number;

  constructor(v: number) {
    this._val = v;
    this._listeners = 0;
  }

  public get value(): number {
    return this._val;
  }

  public set value(v: number) {
    if (v !== this._val) {
      this._val = v;
      this._listeners = this._listeners + 1;
    }
  }

  public changeCount(): number {
    return this._listeners;
  }
}

function testObservableValue(): string {
  let ov: ObservableValue = new ObservableValue(10);
  ov.value = 10;
  ov.value = 20;
  ov.value = 20;
  ov.value = 30;
  return String(ov.value) + ',' + String(ov.changeCount());
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest28';

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
