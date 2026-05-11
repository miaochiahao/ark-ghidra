// === Round 12: Private field access patterns, Object.keys/values iteration,
//   Array.flat/flatMap (manual), String template with computed values,
//   Multiple class interaction, Enum-like const object pattern,
//   Bitwise shift patterns, Type guard with Array.isArray ===

// --- Private field access (this._field pattern) ---

class BankAccount {
  private _balance: number;
  private _owner: string;

  constructor(owner: string, balance: number) {
    this._owner = owner;
    this._balance = balance;
  }

  public deposit(amount: number): void {
    this._balance = this._balance + amount;
  }

  public withdraw(amount: number): boolean {
    if (amount > this._balance) {
      return false;
    }
    this._balance = this._balance - amount;
    return true;
  }

  public getBalance(): number {
    return this._balance;
  }

  public getOwner(): string {
    return this._owner;
  }
}

function testBankAccount(): string {
  let acct: BankAccount = new BankAccount('Alice', 1000);
  acct.deposit(500);
  let ok: boolean = acct.withdraw(200);
  return acct.getOwner() + ':' + String(acct.getBalance()) + ':' + String(ok);
}

// --- Object.keys/values iteration pattern ---

function testObjectIteration(): string {
  let scores: Record<string, number> = {};
  scores['math'] = 95;
  scores['english'] = 87;
  scores['science'] = 92;
  let keys: string[] = Object.keys(scores);
  let total: number = 0;
  for (let i: number = 0; i < keys.length; i++) {
    let key: string = keys[i];
    total = total + scores[key];
  }
  let avg: number = total / keys.length;
  return keys.join(',') + '=' + String(Math.round(avg));
}

// --- Const object enum-like pattern ---

const COLOR_RED: number = 0;
const COLOR_GREEN: number = 1;
const COLOR_BLUE: number = 2;

function colorName(c: number): string {
  if (c === COLOR_RED) {
    return 'red';
  }
  if (c === COLOR_GREEN) {
    return 'green';
  }
  if (c === COLOR_BLUE) {
    return 'blue';
  }
  return 'unknown';
}

function testColorEnum(): string {
  return colorName(COLOR_RED) + ',' + colorName(COLOR_GREEN) + ',' + colorName(3);
}

// --- Bitwise shift patterns ---

function testBitwiseShifts(): string {
  let a: number = 1 << 4;
  let b: number = 64 >> 2;
  let c: number = -1 >>> 0;
  let d: number = a | b;
  let e: number = a & 0x0F;
  let f: number = a ^ b;
  return String(a) + ',' + String(b) + ',' + String(c) + ',' +
    String(d) + ',' + String(e) + ',' + String(f);
}

// --- Array.isArray type guard ---

function testArrayGuard(val: Object): string {
  if (val instanceof Array) {
    let arr: Object[] = val as Object[];
    return 'array:' + String(arr.length);
  }
  return 'not-array';
}

// --- Nested template literal with computed values ---

function testComplexTemplate(): string {
  let x: number = 42;
  let y: number = 3;
  let result: string = `x=${x},y=${y},sum=${String(x + y)}`;
  return result;
}

// --- Multiple class interaction ---

class Pair {
  private first: number;
  private second: number;

  constructor(first: number, second: number) {
    this.first = first;
    this.second = second;
  }

  public swap(): Pair {
    return new Pair(this.second, this.first);
  }

  public sum(): number {
    return this.first + this.second;
  }

  public toString(): string {
    return '(' + String(this.first) + ',' + String(this.second) + ')';
  }
}

function testPairSwap(): string {
  let p1: Pair = new Pair(10, 20);
  let p2: Pair = p1.swap();
  return p1.toString() + '->' + p2.toString() + '=' + String(p2.sum());
}

// --- Compound assignment operators ---

function testCompoundAssign(): number {
  let x: number = 10;
  x = x + 5;
  x = x - 3;
  x = x * 2;
  x = x / 4;
  x = x % 7;
  return x;
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest12';

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
