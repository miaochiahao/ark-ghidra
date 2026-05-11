// === Round 29: Array.find with predicate, Array.every/some callbacks,
//   String.fromCharCode range, Number.toString radix, charCodeAt chains,
//   class-based enum with reverse lookup, switch expression emulation,
//   String.concat method, Array.join with object conversion,
//   compound logical expressions, bitwise flag patterns ===

// --- Array.find with predicate (manual loop) ---

function findEven(arr: number[]): number {
  for (let i: number = 0; i < arr.length; i++) {
    let mod: number = arr[i] % 2;
    if (mod === 0) {
      return arr[i];
    }
  }
  return -1;
}

function testFind(): string {
  let nums: number[] = [11, 23, 36, 47, 58];
  let even: number = findEven(nums);
  return String(even);
}

// --- Array.every/some manual ---

function allPositive(arr: number[]): boolean {
  for (let i: number = 0; i < arr.length; i++) {
    if (arr[i] <= 0) {
      return false;
    }
  }
  return true;
}

function hasLarge(arr: number[], threshold: number): boolean {
  for (let i: number = 0; i < arr.length; i++) {
    if (arr[i] > threshold) {
      return true;
    }
  }
  return false;
}

function testEverySome(): string {
  let a: number[] = [1, 2, 3, 4];
  let b: number[] = [1, -2, 3];
  let allPos: boolean = allPositive(a);
  let notAllPos: boolean = allPositive(b);
  let hasBig: boolean = hasLarge(a, 3);
  return String(allPos) + ',' + String(notAllPos) + ',' + String(hasBig);
}

// --- String.fromCharCode range (build alphabet) ---

function testFromCharCodeRange(): string {
  let result: string = '';
  for (let i: number = 65; i <= 70; i++) {
    result = result + String.fromCharCode(i);
  }
  return result;
}

// --- Number.toString radix ---

function testNumberRadix(): string {
  let n: number = 255;
  let hex: string = n.toString(16);
  let bin: string = n.toString(2);
  let oct: string = n.toString(8);
  return hex + ',' + bin + ',' + oct;
}

// --- charCodeAt chain (string hash) ---

function simpleHash(s: string): number {
  let h: number = 0;
  for (let i: number = 0; i < s.length; i++) {
    let code: number = s.charCodeAt(i);
    h = h + code;
  }
  return h;
}

function testCharCodeAtChain(): string {
  let h1: number = simpleHash('abc');
  let h2: number = simpleHash('xyz');
  return String(h1) + ',' + String(h2);
}

// --- Class-based enum with reverse lookup ---

class HttpStatus {
  public code: number;
  public text: string;

  constructor(code: number, text: string) {
    this.code = code;
    this.text = text;
  }

  public static OK: HttpStatus = new HttpStatus(200, 'OK');
  public static NOT_FOUND: HttpStatus = new HttpStatus(404, 'Not Found');
  public static ERROR: HttpStatus = new HttpStatus(500, 'Internal Error');

  public isOk(): boolean {
    return this.code >= 200 && this.code < 300;
  }

  public toString(): string {
    return String(this.code) + ' ' + this.text;
  }
}

function testHttpStatus(): string {
  let ok: boolean = HttpStatus.OK.isOk();
  let nf: boolean = HttpStatus.NOT_FOUND.isOk();
  let s1: string = HttpStatus.OK.toString();
  let s2: string = HttpStatus.ERROR.toString();
  return String(ok) + ',' + String(nf) + ',' + s1 + ',' + s2;
}

// --- Bitwise flag patterns ---

function testBitwiseFlags(): string {
  let READ: number = 1;
  let WRITE: number = 2;
  let EXEC: number = 4;
  let perms: number = READ | WRITE;
  let canRead: boolean = (perms & READ) !== 0;
  let canWrite: boolean = (perms & WRITE) !== 0;
  let canExec: boolean = (perms & EXEC) !== 0;
  return String(canRead) + ',' + String(canWrite) + ',' + String(canExec);
}

// --- Compound logical expressions ---

function testCompoundLogic(): string {
  let a: boolean = true;
  let b: boolean = false;
  let c: boolean = true;
  let r1: boolean = a && b;
  let r2: boolean = a || b;
  let r3: boolean = a && c;
  let r4: boolean = !a;
  let r5: boolean = !(a && b);
  return String(r1) + ',' + String(r2) + ',' + String(r3) + ',' + String(r4) + ',' + String(r5);
}

// --- Switch expression emulation (lookup pattern) ---

function httpMethodLabel(method: string): string {
  if (method === 'GET') {
    return 'Read';
  }
  if (method === 'POST') {
    return 'Create';
  }
  if (method === 'PUT') {
    return 'Update';
  }
  if (method === 'DELETE') {
    return 'Remove';
  }
  return 'Unknown';
}

function testSwitchExpr(): string {
  return httpMethodLabel('GET') + ',' + httpMethodLabel('POST') + ',' + httpMethodLabel('DELETE');
}

// --- String.concat method ---

function testStringConcat(): string {
  let s: string = 'Hello';
  s = s.concat(', ');
  s = s.concat('World');
  s = s.concat('!');
  return s;
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest29';

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
