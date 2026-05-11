// === Round 57: Nested closures, function composition,
//   higher-order map/filter chain, memoization, currying ===

// --- Nested closures (counter factory) ---

function createCounter(initialValue: number): () => number {
  let count: number = initialValue;
  return (): number => {
    count = count + 1;
    return count;
  };
}

function testCounter(): string {
  let counter1 = createCounter(0);
  let counter2 = createCounter(10);
  let a: number = counter1();
  let b: number = counter1();
  let c: number = counter2();
  let d: number = counter2();
  return String(a) + ',' + String(b) + ',' + String(c) + ',' + String(d);
}

// --- Function composition ---

function compose(f: (x: number) => number, g: (x: number) => number): (x: number) => number {
  return (x: number): number => {
    return f(g(x));
  };
}

function double(x: number): number {
  return x * 2;
}

function addOne(x: number): number {
  return x + 1;
}

function testCompose(): string {
  let doubleThenAdd = compose(addOne, double);
  let addThenDouble = compose(double, addOne);
  let r1: number = doubleThenAdd(3);
  let r2: number = addThenDouble(3);
  return String(r1) + ',' + String(r2);
}

// --- Higher-order map/filter chain ---

function processNumbers(nums: number[]): string {
  let filtered: number[] = nums.filter((n: number): boolean => {
    return n > 3;
  });
  let mapped: number[] = filtered.map((n: number): number => {
    return n * n;
  });
  let total: number = mapped.reduce((acc: number, n: number): number => {
    return acc + n;
  }, 0);
  return String(total);
}

function testProcessNumbers(): string {
  return processNumbers([1, 2, 3, 4, 5]);
}

// --- Memoization ---

function memoize(fn: (n: number) => number): (n: number) => number {
  let cache: Map<number, number> = new Map();
  return (n: number): number => {
    let cached: number | undefined = cache.get(n);
    if (cached !== undefined) {
      return cached;
    }
    let result: number = fn(n);
    cache.set(n, result);
    return result;
  };
}

function slowSquare(n: number): number {
  return n * n;
}

function testMemoize(): string {
  let memoSquare = memoize(slowSquare);
  let a: number = memoSquare(4);
  let b: number = memoSquare(4);
  let c: number = memoSquare(5);
  return String(a) + ',' + String(b) + ',' + String(c);
}

// --- Currying ---

function curryAdd(a: number): (b: number) => number {
  return (b: number): number => {
    return a + b;
  };
}

function testCurry(): string {
  let add5 = curryAdd(5);
  let add10 = curryAdd(10);
  let r1: number = add5(3);
  let r2: number = add10(7);
  return String(r1) + ',' + String(r2);
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest57';

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
