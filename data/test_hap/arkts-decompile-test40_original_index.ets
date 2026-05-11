// === Round 40: Array.flat/flatMap, Number constants, localeCompare,
//   nested Map, chained pipeline, higher-order function returns,
//   readonly class, word frequency counting ===

// --- Array.flat and flatMap ---

function testFlat(): string {
  let nested: number[][] = [[1, 2], [3, 4], [5]];
  let flat: number[] = [];
  for (let i: number = 0; i < nested.length; i++) {
    let inner: number[] = nested[i];
    for (let j: number = 0; j < inner.length; j++) {
      flat.push(inner[j]);
    }
  }
  return String(flat.length) + ',' + String(flat[0]) + ',' + String(flat[4]);
}

function testFlattenManual(): string {
  let data: number[][] = [[10, 20], [30], [40, 50, 60]];
  let flat: number[] = data.reduce((acc: number[], cur: number[]): number[] => {
    return acc.concat(cur);
  }, [] as number[]);
  return String(flat.length) + ',' + String(flat[2]);
}

// --- Number constants and methods ---

function testNumberConstants(): string {
  let isInt: boolean = Number.isInteger(42);
  let isNan: boolean = Number.isNaN(NaN);
  let isFin: boolean = Number.isFinite(Infinity);
  let isIntF: boolean = Number.isInteger(3.14);
  return String(isInt) + ',' + String(isNan) + ',' + String(isFin) + ',' + String(isIntF);
}

// --- String.localeCompare ---

function testLocaleCompare(): string {
  let a: string = 'apple';
  let b: string = 'banana';
  let c: string = 'apple';
  let cmp1: number = a.localeCompare(b);
  let cmp2: number = a.localeCompare(c);
  let cmp3: number = b.localeCompare(a);
  return String(cmp1 < 0) + ',' + String(cmp2 === 0) + ',' + String(cmp3 > 0);
}

// --- Nested Map (Map of Maps) ---

function testNestedMap(): string {
  let outer: Map<string, Map<string, number>> = new Map();
  let inner1: Map<string, number> = new Map();
  inner1.set('x', 10);
  inner1.set('y', 20);
  let inner2: Map<string, number> = new Map();
  inner2.set('x', 30);
  inner2.set('y', 40);
  outer.set('first', inner1);
  outer.set('second', inner2);
  let second: Map<string, number> | undefined = outer.get('second');
  let val: number | undefined = -1;
  if (second !== undefined) {
    let y: number | undefined = second.get('y');
    if (y !== undefined) {
      val = y;
    }
  }
  return String(val);
}

// --- Chained pipeline ---

function testPipeline(): string {
  let nums: number[] = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
  let result: number = nums
    .filter((n: number): boolean => { return n % 2 === 0; })
    .map((n: number): number => { return n * n; })
    .reduce((sum: number, n: number): number => { return sum + n; }, 0);
  return String(result);
}

// --- Higher-order function returns function ---

function createMultiplier(factor: number): (n: number) => number {
  let fn = (n: number): number => {
    return n * factor;
  };
  return fn;
}

function testHigherOrder(): string {
  let triple: (n: number) => number = createMultiplier(3);
  let quintuple: (n: number) => number = createMultiplier(5);
  return String(triple(4)) + ',' + String(quintuple(3));
}

// --- Word frequency counting with Map ---

function testWordFrequency(): string {
  let items: string[] = ['apple', 'banana', 'apple', 'cherry', 'banana', 'apple'];
  let counts: Map<string, number> = new Map();
  for (let i: number = 0; i < items.length; i++) {
    let item: string = items[i];
    let current: number | undefined = counts.get(item);
    if (current === undefined) {
      counts.set(item, 1);
    } else {
      counts.set(item, current + 1);
    }
  }
  let appleCount: number | undefined = counts.get('apple');
  let bananaCount: number | undefined = counts.get('banana');
  return String(appleCount) + ',' + String(bananaCount);
}

// --- Readonly-style class with immutable operations ---

class Point {
  private x: number;
  private y: number;

  constructor(x: number, y: number) {
    this.x = x;
    this.y = y;
  }

  public getX(): number {
    return this.x;
  }

  public getY(): number {
    return this.y;
  }

  public distanceTo(other: Point): number {
    let dx: number = this.x - other.x;
    let dy: number = this.y - other.y;
    return Math.sqrt(dx * dx + dy * dy);
  }

  public translate(dx: number, dy: number): Point {
    return new Point(this.x + dx, this.y + dy);
  }

  public toString(): string {
    return '(' + String(this.x) + ',' + String(this.y) + ')';
  }
}

function testPoint(): string {
  let p1: Point = new Point(3, 4);
  let p2: Point = new Point(6, 8);
  let dist: number = p1.distanceTo(p2);
  let p3: Point = p1.translate(10, 20);
  return String(dist) + ',' + p3.toString();
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest40';

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
