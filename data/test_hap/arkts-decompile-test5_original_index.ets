// === Date Operations ===

function testDateOps(): string {
  let now: Date = new Date();
  let year: number = now.getFullYear();
  let month: number = now.getMonth();
  let day: number = now.getDate();
  let hours: number = now.getHours();
  let minutes: number = now.getMinutes();
  let timestamp: number = now.getTime();
  return String(year) + '-' + String(month) + '-' + String(day) + ' ' + String(hours) + ':' + String(minutes);
}

function testDateConstructor(): number {
  let d: Date = new Date(2024, 0, 1);
  return d.getFullYear();
}

// === Timer Patterns ===

function testSetTimeout(): number {
  let counter: number = 0;
  counter = 42;
  return counter;
}

// === Custom Error Class ===

class AppError extends Error {
  private code: number;

  constructor(message: string, code: number) {
    super(message);
    this.code = code;
  }

  public getCode(): number {
    return this.code;
  }

  public toString(): string {
    return 'Error[' + String(this.code) + ']: ' + this.message;
  }
}

function testCustomError(): string {
  let err: AppError = new AppError('not found', 404);
  return err.toString();
}

// === Number Constants ===

function testNumberConstants(): string {
  let inf: number = Infinity;
  let nan: number = NaN;
  let maxInt: number = Number.MAX_SAFE_INTEGER;
  let minInt: number = Number.MIN_SAFE_INTEGER;
  let isSafe: boolean = Number.isSafeInteger(42);
  let isNa: boolean = Number.isNaN(NaN);
  let isFin: boolean = Number.isFinite(Infinity);
  return String(isSafe) + '|' + String(isNa) + '|' + String(isFin);
}

// === String.fromCharCode ===

function testFromCharCode(): string {
  let chars: number[] = [72, 101, 108, 108, 111];
  let result: string = '';
  for (const code of chars) {
    result = result + String.fromCharCode(code);
  }
  return result;
}

// === Object.assign pattern ===

function testObjectMerge(): string {
  let base: Record<string, string> = {};
  base['name'] = 'test';
  base['version'] = '1.0';
  let override: Record<string, string> = {};
  override['version'] = '2.0';
  override['author'] = 'dev';
  // Manual merge since Object.assign may not work
  let merged: Record<string, string> = {};
  merged['name'] = base['name'];
  merged['version'] = override['version'];
  merged['author'] = override['author'];
  return merged['name'] + '|' + merged['version'] + '|' + merged['author'];
}

// === Array.from like pattern ===

function testArrayCreation(): string {
  let source: number[] = [10, 20, 30];
  let arr: number[] = [];
  for (const n of source) {
    arr.push(n * 2);
  }
  let result: string = '';
  for (const item of arr) {
    if (result.length > 0) {
      result = result + ',';
    }
    result = result + String(item);
  }
  return result;
}

// === String search and manipulation ===

function testStringSearch(): number {
  let text: string = 'The quick brown fox jumps over the lazy dog';
  let foxIdx: number = text.indexOf('fox');
  let dogIdx: number = text.lastIndexOf('dog');
  let substr: string = text.substring(foxIdx, foxIdx + 3);
  let upper: string = substr.toUpperCase();
  return foxIdx + dogIdx;
}

// === Nested conditionals ===

function testNestedConditions(x: number, y: number): string {
  if (x > 0) {
    if (y > 0) {
      return 'both positive';
    } else {
      return 'x positive, y not';
    }
  } else {
    if (y > 0) {
      return 'y positive, x not';
    } else {
      return 'neither positive';
    }
  }
}

// === Switch with break pattern (explicit fallthrough prevention) ===

function testSwitchBreak(val: number): number {
  let result: number = 0;
  switch (val) {
    case 1:
      result = 10;
      break;
    case 2:
      result = 20;
      break;
    case 3:
      result = 30;
      break;
    default:
      result = 99;
      break;
  }
  return result;
}

// === Error catching with type check ===

function testErrorCatch(): string {
  try {
    let err: AppError = new AppError('test', 500);
    throw err;
  } catch (e) {
    if (e instanceof AppError) {
      return String(e.getCode());
    }
    return 'unknown error';
  }
}

// === String comparison ===

function testStringCompare(a: string, b: string): number {
  if (a < b) {
    return -1;
  }
  if (a > b) {
    return 1;
  }
  return 0;
}

// === Main entry with @Watch decorator pattern ===

@Entry
@Component
struct Index {
  @State count: number = 0;
  @State message: string = 'DecompileTest5';

  onCountChange(oldValue: number, newValue: number): void {
    this.message = 'Count changed from ' + String(oldValue) + ' to ' + String(newValue);
  }

  build() {
    Column() {
      if (this.count > 0) {
        Text('Count: ' + String(this.count))
          .fontSize(20)
      }
      Text(this.message)
        .fontSize(30)
        .fontWeight(FontWeight.Bold)
      Button('Increment')
        .onClick(() => {
          this.count++;
        })
    }
    .width('100%')
    .height('100%')
  }
}
