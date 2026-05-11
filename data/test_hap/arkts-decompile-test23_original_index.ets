// === Round 23: Custom Error subclasses, try-catch-finally nesting,
//   enum with string values, WeakMap/WeakSet, re-throw patterns,
//   Error.message/stack access, exhaustive switch with enum,
//   for-of with Map entries, Promise.resolve/reject statics ===

// --- Custom Error subclass ---

class AppError extends Error {
  public code: number;

  constructor(message: string, code: number) {
    super(message);
    this.code = code;
  }

  public toString(): string {
    return 'Error(' + String(this.code) + '): ' + this.message;
  }
}

function testCustomError(): string {
  try {
    let err: AppError = new AppError('not found', 404);
    throw err;
  } catch (e) {
    let msg: string = (e as AppError).toString();
    return msg;
  }
}

// --- Nested try-catch-finally ---

function testNestedTryCatch(): string {
  let result: string = '';
  try {
    result = result + 'A';
    try {
      result = result + 'B';
      throw new Error('inner');
    } catch (e) {
      result = result + 'C';
    } finally {
      result = result + 'D';
    }
    result = result + 'E';
  } catch (e) {
    result = result + 'X';
  } finally {
    result = result + 'F';
  }
  return result;
}

// --- Re-throw pattern ---

function testRethrow(): string {
  try {
    try {
      throw new AppError('fail', 500);
    } catch (e) {
      let appErr: AppError = e as AppError;
      if (appErr.code >= 500) {
        throw appErr;
      }
      return 'handled';
    }
  } catch (e) {
    let appErr: AppError = e as AppError;
    return appErr.message;
  }
}

// --- Const enum-like pattern (ArkTS strict: no enum merging) ---

type DirType = 'UP' | 'DOWN' | 'LEFT' | 'RIGHT';

function testStringEnum(): string {
  let d: DirType = 'UP';
  let result: string = d;
  if (d === 'UP') {
    result = result + ',matched';
  }
  return result;
}

// --- Exhaustive switch with union type ---

function directionLabel(d: DirType): string {
  if (d === 'UP') {
    return 'up';
  }
  if (d === 'DOWN') {
    return 'down';
  }
  if (d === 'LEFT') {
    return 'left';
  }
  return 'right';
}

function testExhaustiveEnum(): string {
  return directionLabel('UP') + ',' + directionLabel('DOWN') + ',' + directionLabel('RIGHT');
}

// --- WeakMap / WeakSet (manual test) ---

class WmKey {
  public id: number;
  constructor(id: number) {
    this.id = id;
  }
}

function testWeakMap(): string {
  let wm: WeakMap<object, string> = new WeakMap<object, string>();
  let key: WmKey = new WmKey(1);
  wm.set(key, 'value1');
  let val: string | undefined = wm.get(key);
  if (val !== undefined) {
    return val;
  }
  return 'none';
}

function testWeakSet(): string {
  let ws: WeakSet<object> = new WeakSet<object>();
  let key: WmKey = new WmKey(2);
  ws.add(key);
  let has: boolean = ws.has(key);
  return String(has);
}

// --- for-of with Map entries ---

function testMapForOf(): string {
  let m: Map<string, number> = new Map<string, number>();
  m.set('x', 10);
  m.set('y', 20);
  let result: string = '';
  let entries: IterableIterator<[string, number]> = m.entries();
  let entry: IteratorResult<[string, number]> = entries.next();
  while (!entry.done) {
    let pair: [string, number] = entry.value;
    result = result + pair[0] + ':' + String(pair[1]);
    entry = entries.next();
    if (!entry.done) {
      result = result + ',';
    }
  }
  return result;
}

// --- Error.message and Error.stack access ---

function testErrorProperties(): string {
  try {
    throw new Error('test error');
  } catch (e) {
    let err: Error = e as Error;
    let msg: string = err.message;
    if (msg.length > 0) {
      return 'caught:' + msg;
    }
    return 'empty';
  }
}

// --- Promise.resolve / Promise.reject statics ---

function testPromiseStatics(): string {
  let resolved: Promise<number> = Promise.resolve(42);
  let rejected: Promise<number> = Promise.reject(-1);
  // Can't await in strict mode non-async, just verify creation
  return 'promises created';
}

// --- Try-finally without catch ---

function testTryFinallyNoCatch(): string {
  let x: number = 0;
  try {
    x = 10;
  } finally {
    x = x + 5;
  }
  return String(x);
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest23';

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
