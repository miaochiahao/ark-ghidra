// === Round 33: Record traversal, string methods combination,
//   array slice/splice, class with static factory + instance check,
//   error subclassing with re-throw, nested conditionals,
//   multiple assignment patterns, type coercion ===

// --- Record traversal with Object.keys ---

function testRecordTraversal(): string {
  let data: Record<string, string> = {};
  data['name'] = 'Alice';
  data['city'] = 'Shanghai';
  data['lang'] = 'ArkTS';
  let keys: string[] = Object.keys(data);
  let result: string = '';
  for (let i: number = 0; i < keys.length; i++) {
    let key: string = keys[i];
    let val: string = data[key];
    result = result + key + '=' + val + ';';
  }
  return result;
}

// --- String method combination ---

function testStringCombo(): string {
  let s: string = 'Hello, World!';
  let upper: string = s.toUpperCase();
  let lower: string = s.toLowerCase();
  let sub: string = s.substring(7, 12);
  let idx: number = s.indexOf('World');
  let replaced: string = s.replace('World', 'ArkTS');
  let sliced: string = s.slice(0, 5);
  return upper + '|' + lower + '|' + sub + '|' + String(idx) + '|' + replaced + '|' + sliced;
}

// --- Array slice and splice (manual) ---

function testArraySlice(): string {
  let arr: number[] = [10, 20, 30, 40, 50];
  let s1: number[] = arr.slice(1, 4);
  let s2: number[] = arr.slice(2);
  return String(s1.length) + ',' + String(s1[0]) + ',' + String(s2.length);
}

// --- Class with static factory + instance method ---

class Vec2 {
  public x: number;
  public y: number;

  constructor(x: number, y: number) {
    this.x = x;
    this.y = y;
  }

  public static zero(): Vec2 {
    return new Vec2(0, 0);
  }

  public static fromAngle(angle: number): Vec2 {
    return new Vec2(Math.cos(angle), Math.sin(angle));
  }

  public length(): number {
    return Math.sqrt(this.x * this.x + this.y * this.y);
  }

  public add(other: Vec2): Vec2 {
    return new Vec2(this.x + other.x, this.y + other.y);
  }

  public scale(factor: number): Vec2 {
    return new Vec2(this.x * factor, this.y * factor);
  }
}

function testVec2(): string {
  let v1: Vec2 = new Vec2(3, 4);
  let len: number = v1.length();
  let v2: Vec2 = v1.scale(2);
  let len2: number = v2.length();
  let v3: Vec2 = v1.add(v2);
  let v0: Vec2 = Vec2.zero();
  return String(len) + ',' + String(len2) + ',' + String(v3.x) + ',' + String(v0.x);
}

// --- Error subclassing with re-throw ---

class AppError extends Error {
  public code: number;

  constructor(message: string, code: number) {
    super(message);
    this.code = code;
  }
}

function testErrorSubclass(): string {
  try {
    let err: AppError = new AppError('test error', 42);
    throw err;
  } catch (e) {
    let msg: string = 'caught';
    return msg;
  }
}

// --- Nested conditionals ---

function classifyAge(age: number): string {
  if (age < 13) {
    return 'child';
  }
  if (age < 20) {
    return 'teen';
  }
  if (age < 65) {
    return 'adult';
  }
  return 'senior';
}

function testClassify(): string {
  return classifyAge(8) + ',' + classifyAge(16) + ',' + classifyAge(40) + ',' + classifyAge(70);
}

// --- Multiple assignment patterns ---

function testAssignPatterns(): string {
  let a: number = 1;
  a = a + 1;
  a = a * 3;
  a = a - 2;
  a = a / 2;
  let b: string = 'x';
  b = b + 'y';
  b = b + 'z';
  return String(a) + ',' + b;
}

// --- Type coercion patterns ---

function testTypeCoercion(): string {
  let n: number = 42;
  let s: string = String(n);
  let back: number = Number(s);
  let bool: boolean = Boolean(n);
  return s + ',' + String(back) + ',' + String(bool);
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest33';

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
