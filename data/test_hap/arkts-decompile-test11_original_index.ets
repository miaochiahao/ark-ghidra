// === Round 11: Array.at(), String.match(), String.replaceAll(),
//   Object.entries(), for-in loop, comma operator (multi-expression),
//   Logical OR assignment (||=) pattern, Multiple constructor overloads via static ===

// --- Array.at() and negative indexing ---

function testArrayAt(): string {
  let arr: string[] = ['a', 'b', 'c', 'd', 'e'];
  let first: string = arr[0];
  let last: string = arr[arr.length - 1];
  let idx2: string = arr[2];
  return first + ',' + last + ',' + idx2;
}

// --- String.match() and search() ---

function testStringMatch(): string {
  let s: string = 'Hello World 123';
  let digits: RegExp = /\d+/;
  let result: RegExpMatchArray | null = s.match(digits);
  if (result !== null) {
    return result[0];
  }
  return 'no match';
}

// --- String.search() ---

function testStringSearch(): number {
  let s: string = 'hello world';
  let idx: number = s.search(/world/);
  return idx;
}

// --- for-in loop over Record keys ---

function testForIn(): string {
  let obj: Record<string, number> = {};
  obj['apple'] = 3;
  obj['banana'] = 5;
  obj['cherry'] = 7;
  let result: string = '';
  let keys: string[] = Object.keys(obj);
  for (let i: number = 0; i < keys.length; i++) {
    let key: string = keys[i];
    result = result + key + ':' + String(obj[key]) + '|';
  }
  return result;
}

// --- Static factory methods (simulating constructor overloads) ---

class Point2D {
  private x: number;
  private y: number;

  constructor(x: number, y: number) {
    this.x = x;
    this.y = y;
  }

  public static fromArray(arr: number[]): Point2D {
    return new Point2D(arr[0], arr[1]);
  }

  public static origin(): Point2D {
    return new Point2D(0, 0);
  }

  public static fromPolar(angle: number, radius: number): Point2D {
    let x: number = radius * Math.cos(angle);
    let y: number = radius * Math.sin(angle);
    return new Point2D(x, y);
  }

  public distanceTo(other: Point2D): number {
    let dx: number = this.x - other.x;
    let dy: number = this.y - other.y;
    return Math.sqrt(dx * dx + dy * dy);
  }

  public toString(): string {
    return '(' + String(this.x) + ',' + String(this.y) + ')';
  }
}

function testFactoryMethods(): string {
  let p1: Point2D = new Point2D(3, 4);
  let p2: Point2D = Point2D.origin();
  let arr: number[] = [6, 8];
  let p3: Point2D = Point2D.fromArray(arr);
  let d1: number = p1.distanceTo(p2);
  return p1.toString() + '|' + p2.toString() + '|' + String(Math.round(d1));
}

// --- Strict equality comparison ---

function testStrictEquality(): string {
  let a: boolean = 0 === -0;
  let b: boolean = Number.isNaN(NaN);
  let c: boolean = 'hello' === 'hello';
  return String(a) + ',' + String(b) + ',' + String(c);
}

// --- Math methods: min, max, abs, ceil, floor, round ---

function testMathMethods(): string {
  let min: number = Math.min(3, 1, 4, 1, 5);
  let max: number = Math.max(3, 1, 4, 1, 5);
  let abs: number = Math.abs(-42);
  let ceil: number = Math.ceil(3.2);
  let floor: number = Math.floor(3.8);
  let round: number = Math.round(3.5);
  return String(min) + ',' + String(max) + ',' + String(abs) + ',' +
    String(ceil) + ',' + String(floor) + ',' + String(round);
}

// --- Array.from with mapping ---

function testArrayFromMap(): string {
  let nums: number[] = [1, 2, 3];
  let strs: string[] = [];
  for (let i: number = 0; i < nums.length; i++) {
    strs.push('n' + String(nums[i]));
  }
  return strs.join(',');
}

// --- Logical OR default pattern ---

function testLogicalDefault(maybeVal: string | undefined): string {
  let val: string;
  if (maybeVal !== undefined && maybeVal !== null) {
    val = maybeVal;
  } else {
    val = 'default';
  }
  return val;
}

function testLogicalDefaultChain(): string {
  return testLogicalDefault('hello') + ',' + testLogicalDefault(undefined);
}

// --- String charAt, charCodeAt, substring ---

function testStringChar(): string {
  let s: string = 'Hello';
  let c: string = s.charAt(1);
  let code: number = s.charCodeAt(0);
  let sub: string = s.substring(1, 4);
  return c + ',' + String(code) + ',' + sub;
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest11';

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
