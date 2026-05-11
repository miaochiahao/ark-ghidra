// === Round 26: ArrayBuffer TypedArrays (Int32Array, Float64Array),
//   Uint8Array buffer manipulation, TypedArray.slice/set/subarray,
//   Array.from with mapping, Map constructor from entries,
//   Set constructor from array, Object.freeze emulation,
//   String.fromCharCode chain, Number.EPSILON comparison ===

// --- Int32Array and Float64Array ---

function testTypedArrays(): string {
  let i32: Int32Array = new Int32Array(4);
  i32[0] = 10;
  i32[1] = 20;
  i32[2] = 30;
  i32[3] = 40;
  let sum: number = i32[0] + i32[1] + i32[2] + i32[3];
  return String(sum);
}

// --- Float64Array with computation ---

function testFloat64Array(): string {
  let f64: Float64Array = new Float64Array(3);
  f64[0] = 1.5;
  f64[1] = 2.5;
  f64[2] = 3.5;
  let avg: number = (f64[0] + f64[1] + f64[2]) / 3;
  return avg.toFixed(2);
}

// --- Uint8Array byte manipulation ---

function testUint8Array(): string {
  let bytes: Uint8Array = new Uint8Array(3);
  bytes[0] = 65;
  bytes[1] = 66;
  bytes[2] = 67;
  let b0: number = bytes[0];
  let b1: number = bytes[1];
  let b2: number = bytes[2];
  return String.fromCharCode(b0) + String.fromCharCode(b1) + String.fromCharCode(b2);
}

// --- Map constructor from entries (manual) ---

function testMapFromEntries(): string {
  let entries: string[][] = [['name', 'Alice'], ['age', '30']];
  let m: Map<string, string> = new Map<string, string>();
  for (let i: number = 0; i < entries.length; i++) {
    let pair: string[] = entries[i];
    m.set(pair[0], pair[1]);
  }
  let name: string | undefined = m.get('name');
  let age: string | undefined = m.get('age');
  if (name !== undefined && age !== undefined) {
    return name + ':' + age;
  }
  return 'none';
}

// --- Set constructor from array ---

function testSetFromArray(): string {
  let nums: number[] = [1, 2, 2, 3, 3, 3, 4];
  let s: Set<number> = new Set<number>();
  for (let i: number = 0; i < nums.length; i++) {
    s.add(nums[i]);
  }
  return String(s.size);
}

// --- Object.freeze emulation (manual copy with seal) ---

function testImmutableCopy(): string {
  let original: Record<string, number> = {};
  original['a'] = 1;
  original['b'] = 2;
  // Manual copy (no Object.freeze in strict ArkTS)
  let copy: Record<string, number> = {};
  copy['a'] = original['a'];
  copy['b'] = original['b'];
  return String(copy['a']) + ',' + String(copy['b']);
}

// --- String.fromCharCode chain ---

function testFromCharCode(): string {
  let s: string = String.fromCharCode(72) + String.fromCharCode(105) + String.fromCharCode(33);
  return s;
}

// --- Number.EPSILON comparison ---

function testEpsilonCompare(): string {
  let a: number = 0.1 + 0.2;
  let b: number = 0.3;
  let diff: number = a - b;
  if (diff < 0) {
    diff = -diff;
  }
  let close: boolean = diff < 0.0001;
  return String(close);
}

// --- Array reverse in-place ---

function testArrayReverse(): string {
  let arr: number[] = [1, 2, 3, 4, 5];
  let reversed: number[] = arr.reverse();
  return String(reversed[0]) + ',' + String(reversed[4]);
}

// --- String.endsWith and startsWith combined ---

function testStartsEndsWith(): string {
  let s: string = 'Hello, World!';
  let starts: boolean = s.startsWith('Hello');
  let ends: boolean = s.endsWith('!');
  return String(starts) + ',' + String(ends);
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest26';

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
