// === Round 7: TypedArrays, Optional Chaining, Nullish Coalescing,
//   Interface impl, Map/Set iteration, Type assertion (as) ===

// --- TypedArray patterns ---

function testInt32Array(): number {
  let arr: Int32Array = new Int32Array(4);
  arr[0] = 10;
  arr[1] = 20;
  arr[2] = 30;
  arr[3] = 40;
  let sum: number = 0;
  for (let i: number = 0; i < arr.length; i++) {
    sum = sum + arr[i];
  }
  return sum;
}

function testFloat64ArrayFrom(): number {
  let src: number[] = [1.1, 2.2, 3.3];
  let view: Float64Array = Float64Array.from(src);
  return view[0] + view[1] + view[2];
}

// --- Optional chaining ---

interface UserProfile {
  name: string;
  address?: AddressInfo;
}

interface AddressInfo {
  city: string;
  zip: string;
}

function testOptionalChain(profile: UserProfile): string {
  let city: string | undefined = profile.address?.city;
  if (city !== undefined) {
    return city;
  }
  return 'unknown';
}

// --- Nullish coalescing ---

function testNullish(a: number | undefined, b: string | undefined): string {
  let x: number = a ?? 42;
  let y: string = b ?? 'default';
  return String(x) + ':' + y;
}

// --- Interface implementation ---

interface Printable {
  format(): string;
}

class NamedItem implements Printable {
  private label: string;
  private value: number;

  constructor(label: string, value: number) {
    this.label = label;
    this.value = value;
  }

  public format(): string {
    return this.label + '=' + String(this.value);
  }

  public getValue(): number {
    return this.value;
  }
}

function testInterfaceImpl(): string {
  let item: NamedItem = new NamedItem('score', 100);
  let p: Printable = item;
  return p.format();
}

// --- Map iteration ---

function testMapIteration(): string {
  let m: Map<string, number> = new Map();
  m.set('a', 1);
  m.set('b', 2);
  m.set('c', 3);
  let keys: string[] = [];
  m.forEach((v: number, k: string) => {
    keys.push(k);
  });
  return keys.join(',');
}

// --- Set operations ---

function testSetOps(): number {
  let s: Set<number> = new Set();
  s.add(1);
  s.add(2);
  s.add(2);
  s.add(3);
  return s.size;
}

// --- Type assertion (as) ---

function testTypeAssertion(val: Object): string {
  let str: string = val as string;
  return str.toUpperCase();
}

// --- Array reduce ---

function testReduce(): number {
  let nums: number[] = [1, 2, 3, 4, 5];
  let total: number = nums.reduce((acc: number, cur: number): number => {
    return acc + cur;
  }, 0);
  return total;
}

// --- String split/join ---

function testSplitJoin(): string {
  let csv: string = 'apple,banana,cherry';
  let parts: string[] = csv.split(',');
  return parts.join('|');
}

// --- Conditional expression (ternary) ---

function testTernary(x: number): string {
  return x > 0 ? 'positive' : x < 0 ? 'negative' : 'zero';
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest7';

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
