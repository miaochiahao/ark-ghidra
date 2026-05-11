// === Round 25: Async/await with try-catch, for-of loop, type predicate
//   guards, instanceof narrowing, as type assertion, Tuple-like array,
//   RegExp test/exec, String.match with groups, ArrayBuffer byte ops ===

// --- Async/await with try-catch ---

async function asyncFetch(id: number): Promise<string> {
  if (id > 0) {
    return 'item:' + String(id);
  }
  throw new Error('invalid id');
}

async function testAsyncAwait(): Promise<string> {
  try {
    let r1: string = await asyncFetch(5);
    let r2: string = await asyncFetch(-1);
    return r1 + ',' + r2;
  } catch (e) {
    let err: Error = e as Error;
    return 'caught:' + err.message;
  }
}

// --- for-of loop ---

function testForOf(): string {
  let items: string[] = ['a', 'b', 'c', 'd'];
  let result: string = '';
  for (let item of items) {
    result = result + item;
  }
  return result;
}

// --- Type predicate guard ---

function isString(val: Object): boolean {
  return typeof val === 'string';
}

function testTypePredicate(): string {
  let v1: Object = 'hello' as Object;
  let v2: Object = 42 as Object;
  let r1: boolean = isString(v1);
  let r2: boolean = isString(v2);
  return String(r1) + ',' + String(r2);
}

// --- instanceof narrowing ---

function testInstanceOf(): string {
  let err: Object = new Error('test') as Object;
  if (err instanceof Error) {
    let e: Error = err as Error;
    return e.message;
  }
  return 'not error';
}

// --- String pattern matching (manual, no RegExpMatchArray) ---

function testPatternMatch(): string {
  let s: string = 'Hello, World!';
  let idx: number = s.indexOf('World');
  let found: boolean = idx >= 0;
  let before: string = s.substring(0, 5);
  let after: string = s.substring(7, 12);
  return String(found) + ',' + before + ',' + after;
}

// --- ArrayBuffer byte operations ---

function testArrayBuffer(): string {
  let buf: ArrayBuffer = new ArrayBuffer(4);
  let view: DataView = new DataView(buf);
  view.setUint8(0, 72);
  view.setUint8(1, 101);
  view.setUint8(2, 108);
  view.setUint8(3, 111);
  let b0: number = view.getUint8(0);
  let b1: number = view.getUint8(1);
  let b2: number = view.getUint8(2);
  let b3: number = view.getUint8(3);
  return String(b0) + ',' + String(b1) + ',' + String(b2) + ',' + String(b3);
}

// --- Chained optional method calls (manual) ---

class Container {
  public label: string;
  constructor(label: string) {
    this.label = label;
  }
  public getLabel(): string {
    return this.label;
  }
}

function testChainedMethodCall(): string {
  let c: Container | null = new Container('box');
  if (c !== null) {
    let label: string = c.getLabel();
    return label;
  }
  return 'empty';
}

// --- String.split with limit ---

function testSplitLimit(): string {
  let csv: string = 'a,b,c,d,e';
  let parts: string[] = csv.split(',');
  let first: string = parts[0];
  let last: string = parts[parts.length - 1];
  return first + ',' + last;
}

// --- Nested Map with string values ---

function testNestedMap(): string {
  let m: Map<string, Map<string, number>> = new Map<string, Map<string, number>>();
  let inner: Map<string, number> = new Map<string, number>();
  inner.set('x', 100);
  inner.set('y', 200);
  m.set('point', inner);
  let retrieved: Map<string, number> | undefined = m.get('point');
  if (retrieved !== undefined) {
    let x: number | undefined = retrieved.get('x');
    if (x !== undefined) {
      return String(x);
    }
  }
  return 'none';
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest25';

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
