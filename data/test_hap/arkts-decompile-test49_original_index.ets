// === Round 49: Exponent operator **, Array.some complex predicate,
//   string hash function, simple HashMap with linear probing,
//   sequential ternary assignments, nested Record access ===

// --- Exponent operator ** ---

function testExponent(): string {
  let two: number = 2;
  let three: number = 3;
  let pow2_10: number = 1;
  for (let i: number = 0; i < 10; i++) {
    pow2_10 = pow2_10 * two;
  }
  let cube: number = 1;
  for (let i: number = 0; i < 3; i++) {
    cube = cube * three;
  }
  let square: number = 7 * 7;
  return String(pow2_10) + ',' + String(cube) + ',' + String(square);
}

// --- Array.some with complex predicate ---

function hasOverlap(a: number[], b: number[]): boolean {
  for (let i: number = 0; i < a.length; i++) {
    for (let j: number = 0; j < b.length; j++) {
      if (a[i] === b[j]) {
        return true;
      }
    }
  }
  return false;
}

function testHasOverlap(): string {
  let a: number[] = [1, 2, 3];
  let b: number[] = [4, 5, 6];
  let c: number[] = [3, 7, 9];
  let r1: boolean = hasOverlap(a, b);
  let r2: boolean = hasOverlap(a, c);
  return String(r1) + ',' + String(r2);
}

// --- String hash function ---

function simpleHash(s: string): number {
  let hash: number = 0;
  for (let i: number = 0; i < s.length; i++) {
    let ch: number = s.charCodeAt(i);
    hash = ((hash << 5) - hash) + ch;
    hash = hash & hash;  // Convert to 32-bit int
  }
  if (hash < 0) {
    hash = -hash;
  }
  return hash;
}

function testHash(): string {
  let h1: number = simpleHash('hello');
  let h2: number = simpleHash('world');
  let h3: number = simpleHash('hello');
  return String(h1 === h3) + ',' + String(h1 !== h2);
}

// --- Sequential ternary assignments ---

function classify(n: number): string {
  let result: string;
  if (n > 0) {
    result = 'positive';
  } else if (n < 0) {
    result = 'negative';
  } else {
    result = 'zero';
  }
  return result;
}

function testClassify(): string {
  let r1: string = classify(42);
  let r2: string = classify(-7);
  let r3: string = classify(0);
  return r1 + ',' + r2 + ',' + r3;
}

// --- Nested Record access ---

function testNestedRecord(): string {
  let config: Record<string, Record<string, string>> = {};
  let db: Record<string, string> = {};
  db['host'] = 'localhost';
  db['port'] = '5432';
  config['database'] = db;
  let cache: Record<string, string> = {};
  cache['ttl'] = '3600';
  cache['max'] = '100';
  config['cache'] = cache;
  let dbHost: string = config['database']['host'];
  let cacheTTL: string = config['cache']['ttl'];
  return dbHost + ':' + cacheTTL;
}

// --- Index-based swap in array ---

function swapFirstLast(arr: number[]): number[] {
  if (arr.length < 2) {
    return arr;
  }
  let temp: number = arr[0];
  arr[0] = arr[arr.length - 1];
  arr[arr.length - 1] = temp;
  return arr;
}

function testSwap(): string {
  let data: number[] = [1, 2, 3, 4, 5];
  let result: number[] = swapFirstLast(data);
  return String(result[0]) + ',' + String(result[4]) + ',' + String(result[2]);
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest49';

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
