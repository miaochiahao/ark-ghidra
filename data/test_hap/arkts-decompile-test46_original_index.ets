// === Round 46: TypedArrays (Int32Array, Float32Array),
//   compound shift assignment (>>=, <<=), Boolean constructor,
//   Object.entries, Set from array, array equality check,
//   multi-key Map lookup, nested optional chaining ===

// --- Int32Array and Float32Array ---

function testTypedArrays(): string {
  let i32: Int32Array = new Int32Array(4);
  i32[0] = 10;
  i32[1] = 20;
  i32[2] = 30;
  i32[3] = 40;
  let sum: number = 0;
  for (let i: number = 0; i < i32.length; i++) {
    sum = sum + i32[i];
  }
  let f32: Float32Array = new Float32Array(3);
  f32[0] = 1.5;
  f32[1] = 2.5;
  f32[2] = 3.5;
  let fsum: number = 0;
  for (let i: number = 0; i < f32.length; i++) {
    fsum = fsum + f32[i];
  }
  return String(sum) + ',' + String(fsum);
}

// --- Compound shift assignment (>>=, <<=) ---

function testCompoundShift(): string {
  let flags: number = 0b11110000;
  flags = flags >> 2;
  let shifted: number = 0b00001111;
  shifted = shifted << 4;
  let combined: number = flags | shifted;
  return String(flags) + ',' + String(shifted) + ',' + String(combined);
}

// --- Boolean constructor ---

function testBooleanOps(): string {
  let b1: boolean = Boolean(1);
  let b2: boolean = Boolean(0);
  let b3: boolean = Boolean('');
  let b4: boolean = Boolean('hello');
  let b5: boolean = Boolean(null);
  let b6: boolean = Boolean(undefined);
  return String(b1) + ',' + String(b2) + ',' + String(b3) + ',' + String(b4) + ',' + String(b5) + ',' + String(b6);
}

// --- Object.entries ---

function testObjectEntries(): string {
  let data: Record<string, number> = {};
  data['alpha'] = 1;
  data['beta'] = 2;
  data['gamma'] = 3;
  let keys: string[] = Object.keys(data);
  let result: string = '';
  for (let i: number = 0; i < keys.length; i++) {
    let key: string = keys[i];
    let val: number = data[key];
    if (result.length > 0) {
      result = result + ';';
    }
    result = result + key + '=' + String(val);
  }
  return String(keys.length) + ':' + result;
}

// --- Set from array ---

function testSetFromArray(): string {
  let nums: number[] = [1, 2, 3, 2, 1, 4, 3, 5];
  let unique: Set<number> = new Set(nums);
  let result: number[] = [];
  unique.forEach((v: number): void => {
    result.push(v);
  });
  return String(unique.size) + ',' + String(result.length);
}

// --- Array equality check (deep compare) ---

function arraysEqual(a: number[], b: number[]): boolean {
  if (a.length !== b.length) {
    return false;
  }
  for (let i: number = 0; i < a.length; i++) {
    if (a[i] !== b[i]) {
      return false;
    }
  }
  return true;
}

function testArraysEqual(): string {
  let a: number[] = [1, 2, 3];
  let b: number[] = [1, 2, 3];
  let c: number[] = [1, 2, 4];
  let d: number[] = [1, 2];
  let r1: boolean = arraysEqual(a, b);
  let r2: boolean = arraysEqual(a, c);
  let r3: boolean = arraysEqual(a, d);
  return String(r1) + ',' + String(r2) + ',' + String(r3);
}

// --- Multi-key Map lookup ---

function testMultiKeyMap(): string {
  let edges: Map<string, number> = new Map();
  let pairs: string[][] = [['A', 'B'], ['A', 'C'], ['B', 'C'], ['B', 'D'], ['C', 'D']];
  for (let i: number = 0; i < pairs.length; i++) {
    let key: string = pairs[i][0] + '-' + pairs[i][1];
    edges.set(key, 1);
  }
  let ab: number | undefined = edges.get('A-B');
  let cd: number | undefined = edges.get('C-D');
  let missing: number | undefined = edges.get('X-Y');
  let result: string = String(ab !== undefined) + ',' + String(cd !== undefined) + ',' + String(missing === undefined);
  return result;
}

// --- Nested optional chaining ---

interface Config {
  db?: DatabaseConfig;
}

interface DatabaseConfig {
  host?: string;
  port?: number;
}

function getPort(config: Config | undefined): number {
  if (config !== undefined && config.db !== undefined && config.db.port !== undefined) {
    return config.db.port;
  }
  return 5432;
}

function testNestedOptional(): string {
  let c1: Config = { db: { host: 'localhost', port: 3306 } };
  let c2: Config = { db: { host: 'localhost' } };
  let c3: Config = {};
  let r1: number = getPort(c1);
  let r2: number = getPort(c2);
  let r3: number = getPort(c3);
  let r4: number = getPort(undefined);
  return String(r1) + ',' + String(r2) + ',' + String(r3) + ',' + String(r4);
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest46';

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
