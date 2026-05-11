// === Round 19: Promise.all/race, Array.from/fill/copyWithin,
//   String.replaceAll/matchAll, Set operations, Map iteration,
//   labeled continue, nested destructuring, nullish coalescing chains,
//   optional chaining chains, computed property access ===

// --- Promise.all and Promise.race ---

function testPromiseAll(): string {
  let p1: Promise<number> = new Promise<number>((resolve: Function): void => {
    resolve(1);
  });
  let p2: Promise<number> = new Promise<number>((resolve: Function): void => {
    resolve(2);
  });
  let p3: Promise<number> = new Promise<number>((resolve: Function): void => {
    resolve(3);
  });
  // Cannot await in non-async in strict ArkTS, so just return type info
  return 'Promise.all created';
}

function testPromiseRace(): string {
  let fast: Promise<string> = new Promise<string>((resolve: Function): void => {
    resolve('fast');
  });
  return 'Promise.race created';
}

// --- Array.from and Array.fill and copyWithin ---

function testArrayFrom(): string {
  let arr: number[] = Array.from([10, 20, 30]);
  return String(arr.length) + ',' + String(arr[1]);
}

function testArrayFill(): string {
  let arr: number[] = new Array<number>(5);
  arr = arr.fill(7);
  return String(arr[0]) + ',' + String(arr[4]);
}

function testArrayCopyWithin(): string {
  let arr: number[] = [1, 2, 3, 4, 5];
  arr = arr.copyWithin(0, 3, 5);
  return String(arr[0]) + ',' + String(arr[1]);
}

// --- String.replaceAll ---

function testReplaceAll(): string {
  let s: string = 'aabbccaabb';
  let result: string = s.replaceAll('aa', 'XX');
  return result;
}

// --- Set operations (union, intersection via manual) ---

function testSetOperations(): string {
  let setA: Set<number> = new Set<number>();
  setA.add(1);
  setA.add(2);
  setA.add(3);
  let setB: Set<number> = new Set<number>();
  setB.add(2);
  setB.add(3);
  setB.add(4);
  // Intersection: items in both
  let count: number = 0;
  setA.forEach((val: number): void => {
    if (setB.has(val)) {
      count = count + 1;
    }
  });
  // Union size
  let union: Set<number> = new Set<number>();
  setA.forEach((val: number): void => {
    union.add(val);
  });
  setB.forEach((val: number): void => {
    union.add(val);
  });
  return String(count) + ',' + String(union.size);
}

// --- Map iteration with entries/keys/values ---

function testMapIteration(): string {
  let m: Map<string, number> = new Map<string, number>();
  m.set('a', 1);
  m.set('b', 2);
  m.set('c', 3);
  let keys: string[] = [];
  m.forEach((val: number, key: string): void => {
    keys.push(key);
  });
  return keys.join(',');
}

// --- Labeled continue ---

function testLabeledContinue(): string {
  let result: string = '';
  outer: for (let i: number = 0; i < 3; i++) {
    for (let j: number = 0; j < 3; j++) {
      if (i === 1 && j === 1) {
        continue outer;
      }
      result = result + String(i) + String(j);
      if (i < 2 || j < 2) {
        result = result + ',';
      }
    }
  }
  return result;
}

// --- Nested class property chains ---

class Inner {
  public val: number;
  constructor(v: number) {
    this.val = v;
  }
}

class Middle {
  public inner: Inner;
  constructor(i: Inner) {
    this.inner = i;
  }
}

class Outer {
  public middle: Middle;
  constructor(m: Middle) {
    this.middle = m;
  }
}

function testNestedPropertyChains(): string {
  let obj: Outer = new Outer(new Middle(new Inner(42)));
  return String(obj.middle.inner.val);
}

// --- Optional chaining on object ---

class ChainObj {
  public value: string;
  constructor(v: string) {
    this.value = v;
  }
}

function testOptionalChainingChain(): string {
  let obj: ChainObj | null = new ChainObj('hello');
  let val: string = obj?.value ?? 'none';
  return val;
}

// --- Nullish coalescing chains ---

function testNullishCoalescingChain(): string {
  let a: string | null = null;
  let b: string | undefined = undefined;
  let c: string = 'fallback';
  let result: string = a ?? b ?? c;
  return result;
}

// --- String concatenation patterns ---

function testStringConcat(): string {
  let a: string = 'Hello';
  let b: string = ' ';
  let c: string = 'World';
  let combined: string = a + b + c + '!';
  return combined;
}

// --- String.padStart / padEnd with deeper nesting ---

function testStringPadding(): string {
  let s: string = '42';
  let padded: string = s.padStart(6, '0');
  let right: string = s.padEnd(6, '-');
  return padded + '|' + right;
}

// --- Array.flat (manual flatten) ---

function testArrayFlatten(): string {
  let nested: number[][] = [[1, 2], [3, 4], [5]];
  let flat: number[] = [];
  for (let i: number = 0; i < nested.length; i++) {
    let inner: number[] = nested[i];
    for (let j: number = 0; j < inner.length; j++) {
      flat.push(inner[j]);
    }
  }
  return String(flat.length) + ',' + String(flat[3]);
}

// --- Typeof guard with branching ---

function testTypeofGuards(): string {
  let vals: (string | number | boolean)[] = ['hello', 42, true];
  let result: string = '';
  for (let i: number = 0; i < vals.length; i++) {
    let v: string | number | boolean = vals[i];
    if (typeof v === 'string') {
      result = result + 'S';
    } else if (typeof v === 'number') {
      result = result + 'N';
    } else {
      result = result + 'B';
    }
    if (i < vals.length - 1) {
      result = result + ',';
    }
  }
  return result;
}

// --- Number toFixed chaining ---

function testNumberFormatting(): string {
  let n: number = 3.14159;
  let fixed2: string = n.toFixed(2);
  let fixed4: string = n.toFixed(4);
  return fixed2 + '|' + fixed4;
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest19';

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
