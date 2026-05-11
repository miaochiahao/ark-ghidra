// === Round 20: Remainder/modulo patterns, bitwise shift assignment,
//   compound assignment chains, nested ternary, string template
//   in function args, default parameters, rest parameters (manual),
//   Array.reduce, Array.sort with comparator, deep equality === ===

// --- Remainder and modulo edge cases ---

function testModuloPatterns(): string {
  let a: number = 17 % 5;
  let b: number = -17 % 5;
  let c: number = 17 % -5;
  let d: number = -17 % -5;
  return String(a) + ',' + String(b) + ',' + String(c) + ',' + String(d);
}

// --- Bitwise shift assignment ---

function testShiftAssignment(): string {
  let v: number = 100;
  v = v << 2;
  let a: number = v;
  v = v >> 1;
  let b: number = v;
  v = v >>> 1;
  let c: number = v;
  return String(a) + ',' + String(b) + ',' + String(c);
}

// --- Compound assignment chains ---

function testCompoundAssignment(): string {
  let x: number = 10;
  x = x + 5;
  let a: number = x;
  x = x - 3;
  let b: number = x;
  x = x * 2;
  let c: number = x;
  x = x / 3;
  let d: number = x;
  x = x % 4;
  let e: number = x;
  return String(a) + ',' + String(b) + ',' + String(c) + ',' + String(d) + ',' + String(e);
}

// --- Nested ternary ---

function testNestedTernary(): string {
  let x: number = 15;
  let result: string = x > 20 ? 'big' : x > 10 ? 'medium' : x > 5 ? 'small' : 'tiny';
  return result;
}

// --- String template in function args ---

function greet(name: string, age: number): string {
  return 'Hello ' + name + ', age ' + String(age);
}

function testTemplateInArgs(): string {
  let name: string = 'World';
  let age: number = 25;
  let msg: string = greet(name, age);
  return msg;
}

// --- Default parameters ---

function addWithDefault(a: number, b: number): number {
  if (b === 0) {
    return a + 10;
  }
  return a + b;
}

function testDefaultParams(): string {
  let r1: number = addWithDefault(5, 0);
  let r2: number = addWithDefault(5, 3);
  return String(r1) + ',' + String(r2);
}

// --- Manual rest parameters via array ---

function sumArray(nums: number[]): number {
  let total: number = 0;
  for (let i: number = 0; i < nums.length; i++) {
    total = total + nums[i];
  }
  return total;
}

function testManualRest(): string {
  let nums: number[] = [1, 2, 3, 4, 5];
  let total: number = sumArray(nums);
  return String(total);
}

// --- Array.reduce ---

function testArrayReduce(): string {
  let nums: number[] = [1, 2, 3, 4, 5];
  let sum: number = 0;
  for (let i: number = 0; i < nums.length; i++) {
    sum = sum + nums[i];
  }
  let product: number = 1;
  for (let i: number = 0; i < nums.length; i++) {
    product = product * nums[i];
  }
  return String(sum) + ',' + String(product);
}

// --- Array.sort with comparator ---

function testSortComparator(): string {
  let strs: string[] = ['banana', 'apple', 'cherry'];
  let sorted: string[] = strs.sort();
  return sorted[0] + ',' + sorted[1] + ',' + sorted[2];
}

// --- Deep equality === chains ---

function testDeepEquality(): string {
  let a: number = 42;
  let b: number = 42;
  let c: number = 43;
  let eq1: boolean = a === b;
  let eq2: boolean = a === c;
  let neq: boolean = a !== c;
  return String(eq1) + ',' + String(eq2) + ',' + String(neq);
}

// --- Logical AND / OR in return ---

function testLogicalInReturn(): string {
  let x: number = 5;
  let y: number = 10;
  if (x > 0 && y > 0) {
    return 'both positive';
  }
  return 'not both positive';
}

// --- String comparison ---

function testStringComparison(): string {
  let a: string = 'apple';
  let b: string = 'banana';
  let lt: boolean = a < b;
  let gt: boolean = a > b;
  let eq: boolean = a === b;
  return String(lt) + ',' + String(gt) + ',' + String(eq);
}

// --- Increment / decrement patterns ---

function testIncrementDecrement(): string {
  let i: number = 5;
  i = i + 1;
  let a: number = i;
  i = i + 1;
  let b: number = i;
  i = i - 1;
  let c: number = i;
  i = i - 1;
  let d: number = i;
  return String(a) + ',' + String(b) + ',' + String(c) + ',' + String(d);
}

// --- Nested if-else with multiple returns ---

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

function testClassifyAge(): string {
  return classifyAge(8) + ',' + classifyAge(16) + ',' + classifyAge(40) + ',' + classifyAge(70);
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest20';

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
